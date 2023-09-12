package com.nmartinez.mealplanrandomizer.http.routes

import cats.*
import cats.data.Validated
import cats.effect.Concurrent
import cats.implicits.*
import com.nmartinez.mealplanrandomizer.db.Recipes
import com.nmartinez.mealplanrandomizer.domain.*
import com.nmartinez.mealplanrandomizer.domain.Ingredient.*
import com.nmartinez.mealplanrandomizer.http.responses.*
import com.nmartinez.mealplanrandomizer.logging.Syntax.*
import com.nmartinez.mealplanrandomizer.logic.MealPlans
import io.circe.generic.auto.*
import org.http4s.circe.*
import org.http4s.circe.CirceEntityCodec.*
import org.http4s.dsl.{&, Http4sDsl}
import org.http4s.server.Router
import org.http4s.{HttpRoutes, ParseFailure, QueryParamDecoder}
import org.typelevel.log4cats.Logger

import java.util.UUID
import scala.annotation.tailrec
import scala.collection.{Iterable, mutable}

class MealPlanRoutes[F[_]: Concurrent: Logger] private (recipes: Recipes[F]) extends Http4sDsl[F] {
  
  object OptionalDays extends OptionalMultiQueryParamDecoderMatcher[DayOfWeek]("day")

  object OptionalMeals extends OptionalMultiQueryParamDecoderMatcher[MealType]("meal")

  // POST /meal-plan/randomise?day=Monday&day=Tuesday&meal=Lunch&meal=LunchDinner
  private val randomMealPlanRoute: HttpRoutes[F] = HttpRoutes.of[F] {
    case POST -> Root / "meal-plan" :? OptionalDays(validatedDays) +& OptionalMeals(validatedMeals) =>
      (validatedDays, validatedMeals).mapN { (days, meals) =>
        for {
          _ <- Logger[F].info(s"Correctly parsed days multi query param: $days")
          _ <- Logger[F].info(s"Correctly parsed meals multi query param: $meals")
          recipes <- recipes.all()
          resp <- Ok(MealPlans.generateRandomMealPlan(recipes, days, meals))
        } yield resp
      }.getOrElse(BadRequest())
  }

  private val shoppingListRoute: HttpRoutes[F] = HttpRoutes.of[F] {
    case req@POST -> Root / "shopping-list" =>
      for {
        mealPlan <- req.as[MealPlan].logError(e => s"Parsing payload failed: $e")
        _ <- Logger[F].info(s"Parsed meal plan: $mealPlan")
        resp <- Ok(MealPlans.generateShoppingList(mealPlan))
      } yield resp
  }

  val routes = Router(
    "/" -> (randomMealPlanRoute <+> shoppingListRoute)
  )
}

object MealPlanRoutes {
  def apply[F[_]: Concurrent: Logger](recipes: Recipes[F]) = new MealPlanRoutes[F](recipes)
}
