package com.nmartinez.meals.http.routes

import cats.*
import cats.effect.Concurrent
import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl
import org.http4s.server.Router
import org.typelevel.log4cats.Logger
class HealthRoutes[F[_]: Concurrent: Logger] private extends Http4sDsl[F] {
  private val healthRoute: HttpRoutes[F] = HttpRoutes.of[F] { case GET -> Root =>
    Ok("All going great!")
  }
  val routes = Router(
    "/health" -> healthRoute
  )
}

object HealthRoutes {
  def apply[F[_]: Concurrent: Logger] = new HealthRoutes[F]
}
