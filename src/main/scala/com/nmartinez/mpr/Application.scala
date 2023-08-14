package com.nmartinez.mpr

import cats.*
import cats.implicits.*
import cats.effect.{IO, IOApp}
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl
import org.http4s.server.middleware.ErrorAction
import org.http4s.server.middleware.ErrorHandling
import pureconfig.ConfigSource
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger
import com.nmartinez.mpr.http.HttpApi
import com.nmartinez.mpr.config.EmberConfig
import com.nmartinez.mpr.config.Syntax.loadF
object Application extends IOApp.Simple {

  given logger: Logger[IO] = Slf4jLogger.getLogger[IO]

  val withErrorLogging = ErrorHandling.Recover.total(
    ErrorAction.log(
      HttpApi[IO].routes.orNotFound,
      messageFailureLogAction = (t, msg) =>
        IO.println(msg) >>
          IO.println(t),
      serviceErrorLogAction = (t, msg) =>
        IO.println(msg) >>
          IO.println(t)
    )
  )

  override def run = ConfigSource.default.loadF[IO, EmberConfig].flatMap { config =>
    EmberServerBuilder
      .default[IO]
      .withHost(config.host)
      .withPort(config.port)
      .withHttpApp(withErrorLogging)
      .build
      .use(_ => IO.println("Server ready!") *> IO.never)
  }
}
