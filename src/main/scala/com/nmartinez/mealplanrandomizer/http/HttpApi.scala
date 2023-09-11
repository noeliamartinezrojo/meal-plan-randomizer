package com.nmartinez.mealplanrandomizer.http

import cats.implicits.*
import cats.effect.Concurrent
import org.http4s.server.Router
import org.typelevel.log4cats.Logger
import com.nmartinez.mealplanrandomizer.http.routes.*
class HttpApi[F[_]: Concurrent: Logger] private {
  private val healthRoutes = HealthRoutes[F].routes
  private val recipeRoutes = RecipeRoutes[F].routes

  val routes = Router(
    "/api" -> (healthRoutes <+> recipeRoutes)
  )
}

object HttpApi {
  def apply[F[_]: Concurrent: Logger] = new HttpApi[F]
}
