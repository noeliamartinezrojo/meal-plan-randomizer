package com.nmartinez.mpr.http.routes

import java.util.UUID
import scala.collection.{Iterable, mutable}
import scala.util.Random
import cats.*
import cats.data.Validated
import cats.implicits.*
import cats.effect.Concurrent
import com.nmartinez.mpr.domain.DayOfWeek.*
import com.nmartinez.mpr.domain.MealPlan.{Meal, MealPlan}
import com.nmartinez.mpr.domain.MealType.*
import com.nmartinez.mpr.domain.Ingredient.*
import com.nmartinez.mpr.domain.Recipe.*
import org.http4s.{HttpRoutes, ParseFailure, QueryParamDecoder}
import org.http4s.dsl.{&, Http4sDsl}
import org.http4s.server.Router
import io.circe.generic.auto.*
import org.http4s.circe.*
import org.http4s.circe.CirceEntityCodec.*
import org.typelevel.log4cats.Logger
import com.nmartinez.mpr.domain.Recipe.*
import com.nmartinez.mpr.domain.{DayOfWeek, IngredientType, IngredientUnit, MealPlan, MealType}
import com.nmartinez.mpr.http.responses.*
import com.nmartinez.mpr.logging.Syntax.*

import scala.annotation.tailrec

class RecipeRoutes[F[_]: Concurrent: Logger] private extends Http4sDsl[F] {

  // "database"
  private[routes] val database = mutable.Map[UUID, Recipe]()

  private def getRandomRecipes(n: Int): List[Recipe] = {
    if (database.isEmpty) Nil
    else {
      val timesBigger = Math.ceil(n.toFloat / database.size).toInt
      (1 to timesBigger).flatMap(_ => database.values.take(n)).toList
    }
  }

  private def getValidRandomRecipe(day: DayOfWeek,
                                   meal: MealType,
                                   acc: List[Recipe]): Option[Recipe] = {
    database.values
      .filterNot(_.recipeInfo.excludeFrom.contains_(Meal(day, meal)))
      .filterNot(r => acc.count(_ === r) >= r.recipeInfo.maxBatchesPerWeek)
    match {
      case Nil => None
      case validRecipes => Option(validRecipes.maxBy(_ => Random.nextInt()))
    }
  }


  @tailrec
  final def generateRandomMealPlanByDay(
                                         day: DayOfWeek,
                                         meals: List[MealType],
                                         acc: Map[MealType, Recipe],
                                         used: List[Recipe]
                                       ): Map[MealType, Recipe] = {
    if (meals.isEmpty) acc
    else getValidRandomRecipe(day, meals.head, used) match {
      case None => generateRandomMealPlanByDay(day, meals.tail, acc, used)
      case Some(recipe) => generateRandomMealPlanByDay(day, meals.tail, acc + (meals.head -> recipe), used)
    }
  }

  @tailrec
  final def generateRandomMealPlan(
                                      days: List[DayOfWeek],
                                      meals: List[MealType],
                                      acc: Map[DayOfWeek, Map[MealType, Recipe]] = Map.empty
                                    ): Map[DayOfWeek, Map[MealType, Recipe]] = {
    if (days.isEmpty) acc.filter(_._2.nonEmpty)
    else generateRandomMealPlan(days.tail, meals, acc +
      (days.head -> generateRandomMealPlanByDay(days.head, meals, Map.empty, acc.values.flatMap(_.values).toList))
    )
  }

  object OptionalDays extends OptionalMultiQueryParamDecoderMatcher[DayOfWeek]("day")
  object OptionalMeals extends OptionalMultiQueryParamDecoderMatcher[MealType]("meal")

  // POST /meal-plan/randomise?day=Monday&day=Tuesday&meal=Lunch&meal=LunchDinner
  private val randomMealPlanRoute: HttpRoutes[F] = HttpRoutes.of[F] {
    case POST -> Root / "random" :? OptionalDays(validatedDays) +& OptionalMeals(validatedMeals) =>
      (validatedDays, validatedMeals).mapN{ (days, meals) =>
        for {
          _ <- Logger[F].info(s"Correctly parsed days multi query param: $days")
          _ <- Logger[F].info(s"Correctly parsed meals multi query param: $meals")
          resp <- Ok(generateRandomMealPlan(days, meals))
        } yield resp
      }.getOrElse(BadRequest())
  }

  final def generateShoppingList(mealPlan: MealPlan): List[Ingredient] = {
    val recipes = mealPlan.values.flatMap(_.values)
    val ingredientLists = recipes.map(_.recipeInfo.ingredients)
    ingredientLists.fold(Nil)((l1, l2) => l1 |+| l2)
  }

  private val shoppingListRoute: HttpRoutes[F] = HttpRoutes.of[F] {
    case req @ POST -> Root / "shopping-list" =>
      for {
        mealPlan <- req.as[MealPlan].logError(e => s"Parsing payload failed: $e")
        _ <- Logger[F].info(s"Parsed meal plan: $mealPlan")
        resp <- Ok(generateShoppingList(mealPlan))
      } yield resp
  }

    // POST /recipes?offset=x&limit=y { filters }
  // TODO add query params and filters
  private val allRecipesRoute: HttpRoutes[F] = HttpRoutes.of[F] {
    case POST -> Root =>
      Ok(database.values)
  }

  // GET /recipes/uuid
  private val findRecipeRoute: HttpRoutes[F] = HttpRoutes.of[F] {
    case GET -> Root / UUIDVar(id) =>
      database.get(id) match {
        case Some(recipe) => Ok(recipe)
        case None => NotFound(FailureResponse("recipe not found"))
      }
  }

  private[routes] def createRecipe(recipeInfo: RecipeInfo): F[Recipe] =
    Recipe(
      id = UUID.randomUUID(),
      created = System.currentTimeMillis(),
      ownerEmail = "TODO@nmartinez.com",
      recipeInfo = recipeInfo
    ).pure[F]

  // POST /recipes { recipeInfo }
  private val createRecipeRoute: HttpRoutes[F] = HttpRoutes.of[F] {
    case req @ POST -> Root / "create" =>
      for {
        _ <- Logger[F].info("Trying to create recipe")
        recipeInfo <- req.as[RecipeInfo].logError(e => s"Parsing payload failed: $e")
        _ <- Logger[F].info(s"Parsed recipe info: $recipeInfo")
        recipe <- createRecipe(recipeInfo)
        _ <- database.put(recipe.id, recipe).pure[F]
        _ <- Logger[F].info(s"Created recipe: $recipe")
        resp <- Created(recipe.id)
      } yield resp
  }

  // PUT /recipes/uuid { recipeInfo }
  private val updateRecipeRoute: HttpRoutes[F] = HttpRoutes.of[F] {
    case req @ PUT -> Root / UUIDVar(id) =>
      database.get(id) match {
        case Some(recipe) =>
          for {
            recipeInfo <- req.as[RecipeInfo]
            _ <- database.put(id, recipe.copy(recipeInfo = recipeInfo)).pure[F]
            resp <- Ok(s"updated ${recipe.id}")
          } yield resp
        case None => NotFound(FailureResponse(s"cannot update recipe $id: not found"))
      }
  }

  // DELETE /recipes/uuid
  private val deleteRecipeRoute: HttpRoutes[F] = HttpRoutes.of[F] {
    case req @ DELETE -> Root / UUIDVar(id) =>
      database.get(id) match {
        case Some(recipe) =>
          for {
            _ <- database.remove(recipe.id).pure[F]
            resp <- Ok(s"deleted ${recipe.id}")
          } yield resp
        case None => NotFound(FailureResponse(s"cannot delete recipe $id: not found"))
      }
  }

  val routes = Router(
    "/meal-plan" -> (randomMealPlanRoute <+> shoppingListRoute),
    "/recipes" -> (allRecipesRoute <+> findRecipeRoute <+> createRecipeRoute <+> updateRecipeRoute <+> deleteRecipeRoute)
  )
}

object RecipeRoutes {
  def apply[F[_]: Concurrent: Logger] = new RecipeRoutes[F]
}
