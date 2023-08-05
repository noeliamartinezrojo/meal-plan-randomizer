package com.nmartinez.mpr

import cats.*
import cats.effect.{IO, IOApp}
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl
import com.nmartinez.mpr.http.routes.HealthRoutes

object Application extends IOApp.Simple {
  override def run = EmberServerBuilder
    .default[IO]
    .withHttpApp(HealthRoutes[IO].routes.orNotFound)
    .build
    .use(_ => IO.println("Server ready!") *> IO.never)
}
