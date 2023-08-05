package com.nmartinez.mpr

import cats._
import cats.implicits._
import cats.effect.{IO, IOApp}
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl
import pureconfig.ConfigSource
import com.nmartinez.mpr.http.routes.HealthRoutes
import com.nmartinez.mpr.config.EmberConfig
import com.nmartinez.mpr.config.Syntax.loadF

object Application extends IOApp.Simple {

  override def run = ConfigSource.default.loadF[IO, EmberConfig].flatMap { config =>
    EmberServerBuilder
      .default[IO]
      .withHost(config.host)
      .withPort(config.port)
      .withHttpApp(HealthRoutes[IO].routes.orNotFound)
      .build
      .use(_ => IO.println("Server ready!") *> IO.never)
  }
}
