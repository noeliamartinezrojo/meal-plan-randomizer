package com.nmartinez.mpr.http.routes

import java.util.UUID
import scala.collection.mutable
import scala.util.Try
import cats.*
import cats.data.Validated
import cats.implicits.*
import cats.effect.Concurrent
import org.http4s.{HttpRoutes, ParseFailure, QueryParamDecoder}
import org.http4s.dsl.{&, Http4sDsl}
import org.http4s.server.Router
import io.circe.generic.auto.*
import org.http4s.circe.CirceEntityCodec.*
import org.typelevel.log4cats.Logger
import com.nmartinez.mpr.domain.Recipe.*
import com.nmartinez.mpr.domain.MealType
import com.nmartinez.mpr.domain.DayOfWeek
import com.nmartinez.mpr.http.responses.*
import com.nmartinez.mpr.logging.Syntax.*

class RecipeRoutes[F[_]: Concurrent: Logger] private extends Http4sDsl[F] {

  // "database"
  private val database = mutable.Map[UUID, Recipe]()

  object OptionalDaysOfWeek extends OptionalMultiQueryParamDecoderMatcher[DayOfWeek]("dayOfWeek")
  object OptionalMealTypes extends OptionalMultiQueryParamDecoderMatcher[MealType]("mealType")

  // POST /meal-plan/randomise?dayOfWeek=Monday&dayOfWeek=Tuesday&mealType=Lunch&mealType=LunchDinner
  private val randomMealPlanRoute: HttpRoutes[F] = HttpRoutes.of[F] {
    case POST -> Root / "randomize" :? OptionalDaysOfWeek(validatedDaysOfWeek) +& OptionalMealTypes(validatedMealTypes) => {
      val allRecipes = database.values
      val mealPlan: Map[DayOfWeek, Map[MealType, Recipe]] = Map.empty
      (validatedDaysOfWeek, validatedMealTypes).mapN{ (daysOfWeek, mealTypes) =>
        for {
          _ <- Logger[F].info(s"Correctly parsed days of week multi query param: $daysOfWeek")
          _ <- Logger[F].info(s"Correctly parsed meal types multi query param: $mealTypes")
          resp <- Ok()
        } yield resp
      }.getOrElse(BadRequest())
    }
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

  private def createRecipe(recipeInfo: RecipeInfo): F[Recipe] =
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
    "/recipes" -> (randomMealPlanRoute <+> allRecipesRoute <+> findRecipeRoute <+> createRecipeRoute <+> updateRecipeRoute <+> deleteRecipeRoute)
  )
}

object RecipeRoutes {
  def apply[F[_]: Concurrent: Logger] = new RecipeRoutes[F]
}
