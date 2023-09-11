package com.nmartinez.mealplanrandomizer

import cats.*
import cats.implicits.*
import cats.effect.{Concurrent, IO, IOApp}
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl
import org.http4s.server.middleware.ErrorAction
import org.http4s.server.middleware.ErrorHandling
import pureconfig.ConfigSource
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger
import com.nmartinez.mealplanrandomizer.config.EmberConfig
import com.nmartinez.mealplanrandomizer.config.Syntax.loadF
import com.nmartinez.mealplanrandomizer.modules.*

object Application extends IOApp.Simple {
  given logger: Logger[IO] = Slf4jLogger.getLogger[IO]

  def withErrorLogging(httpApi: HttpApi[IO]) = ErrorHandling.Recover.total(
    ErrorAction.log(
      httpApi.routes.orNotFound,
      messageFailureLogAction = (t, msg) =>
        IO.println(msg) >>
          IO.println(t),
      serviceErrorLogAction = (t, msg) =>
        IO.println(msg) >>
          IO.println(t)
    )
  )

  override def run = ConfigSource.default.loadF[IO, EmberConfig].flatMap { config =>
    // postgres -> recipes -> core -> httpAppi -> app
    val appResource = for {
      core <- Core[IO]
      httpApi <- HttpApi[IO](core)
      server <- EmberServerBuilder
        .default[IO]
        .withHost(config.host)
        .withPort(config.port)
        .withHttpApp(withErrorLogging(httpApi))
        .build
    } yield server

    appResource.use(_ => IO.println("Server ready!") *> IO.never)
  }
}
