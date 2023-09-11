package com.nmartinez.mealplanrandomizer.modules

import com.nmartinez.mealplanrandomizer.dao.*
import cats.*
import cats.effect.*
import doobie.*
import doobie.implicits.*
import doobie.util.*
import doobie.hikari.HikariTransactor

final class Core[F[_]] private (val recipes: Recipes[F])
object Core {
  def postgresResource[F[_]: Async]: Resource[F, HikariTransactor[F]] = for {
    ec <- ExecutionContexts.fixedThreadPool(32)
    xa <- HikariTransactor.newHikariTransactor[F](
      "org.postgresql.Driver",
      "jdbc:postgresql:mealplanrandomizer", // TODO NMR: move to config
      "docker",
      "docker",
      ec
    )
  } yield xa
  def apply[F[_]: Async]: Resource[F, Core[F]] =
    postgresResource[F]
      .evalMap(postgres => LiveRecipes[F](postgres))
      .map(recipes => new Core(recipes))
}
