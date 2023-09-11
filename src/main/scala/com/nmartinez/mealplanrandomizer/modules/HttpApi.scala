package com.nmartinez.mealplanrandomizer.modules

import cats.effect.Concurrent
import cats.implicits.*
import cats.effect.*
import com.nmartinez.mealplanrandomizer.dao.Recipes
import com.nmartinez.mealplanrandomizer.http.routes.*
import org.http4s.server.Router
import org.typelevel.log4cats.Logger
class HttpApi[F[_]: Concurrent: Logger] private (core: Core[F]) {
  private val healthRoutes = HealthRoutes[F].routes
  private val recipeRoutes = RecipeRoutes[F](core.recipes).routes
  private val mealPlanRoutes = MealPlanRoutes[F](core.recipes).routes

  val routes = Router(
    "/api" -> (healthRoutes <+> recipeRoutes <+> mealPlanRoutes)
  )
}

object HttpApi {
  def apply[F[_]: Concurrent: Logger](core: Core[F]): Resource[F, HttpApi[F]] =
    Resource.pure(new HttpApi[F](core))
}
