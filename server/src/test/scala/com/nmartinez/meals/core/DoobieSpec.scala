package com.nmartinez.meals.core

import cats.effect.*
import doobie.*
import doobie.implicits.*
import doobie.util.*
import org.testcontainers.containers.PostgreSQLContainer
import doobie.hikari.HikariTransactor
import doobie.util.ExecutionContexts

trait DoobieSpec {

  // each test class should create their own database on the test container with a custom sql script
  val initScript: String

  // simulated database on a test docker container
  val postgres: Resource[IO, PostgreSQLContainer[Nothing]] = {
    val acquire = IO {
      val container: PostgreSQLContainer[Nothing] =
        new PostgreSQLContainer("postgres")
          .withInitScript(initScript)
      container.start()
      container
    }
    val release = (container: PostgreSQLContainer[Nothing]) => IO(container.stop())
    Resource.make(acquire)(release)
  }

  val transactor: Resource[IO, Transactor[IO]] = for {
    db <- postgres
    ec <- ExecutionContexts.fixedThreadPool[IO](1)
    xa <- HikariTransactor.newHikariTransactor[IO](
      "org.postgresql.Driver",
      db.getJdbcUrl(),
      db.getUsername(),
      db.getPassword(),
      ec
    )
  } yield xa
}
