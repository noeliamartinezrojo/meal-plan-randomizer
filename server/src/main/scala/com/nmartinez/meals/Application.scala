package com.nmartinez.meals

import cats.*
import cats.implicits.*
import cats.effect.*
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl
import org.http4s.server.middleware.ErrorAction
import org.http4s.server.middleware.ErrorHandling
import pureconfig.ConfigSource
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger
import com.nmartinez.meals.config.*
import com.nmartinez.meals.modules.*
import com.nmartinez.meals.config.Syntax.loadF

object Application extends IOApp.Simple {

  override def run = ConfigSource.default.loadF[IO, AppConfig].flatMap {
    case AppConfig(postgresConfig, emberConfig) =>
      // postgres -> recipes -> core -> httpAppi -> app
      val appResource = for {
        xa <- Database.makePostgresResource[IO](postgresConfig)
        core <- Core[IO](xa)
        httpApi <- HttpApi[IO](core)
        server <- EmberServerBuilder
          .default[IO]
          .withHost(emberConfig.host)
          .withPort(emberConfig.port)
          .withHttpApp(withErrorLogging(httpApi))
          .build
      } yield server

      appResource.use(_ => IO.println("Server ready!") *> IO.never)
  }

  given logger: Logger[IO] = Slf4jLogger.getLogger[IO]

  private def withErrorLogging(httpApi: HttpApi[IO]) = ErrorHandling.Recover.total(
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
}

object ThingPlayground {
  // to prove both backend and frontend projects can access definitions in the common project
  val myThing = MyThing("Noelia")
}