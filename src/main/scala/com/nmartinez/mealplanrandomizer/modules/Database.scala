package com.nmartinez.mealplanrandomizer.modules

import cats.effect.*
import com.nmartinez.mealplanrandomizer.config.*
import doobie.hikari.HikariTransactor
import doobie.util.ExecutionContexts
object Database {
  def makePostgresResource[F[_] : Async](config: PostgresConfig): Resource[F, HikariTransactor[F]] =
    for {
      ec <- ExecutionContexts.fixedThreadPool(config.numThreads)
      xa <- HikariTransactor.newHikariTransactor[F](
        "org.postgresql.Driver",
        config.url,
        config.username,
        config.password,
        ec
      )
    } yield xa
}
