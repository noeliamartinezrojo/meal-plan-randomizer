package com.nmartinez.mealplanrandomizer.modules

import com.nmartinez.mealplanrandomizer.dao.*
import cats.effect.*
import doobie.util.*
import doobie.util.transactor.Transactor

final class Core[F[_]] private (val recipes: Recipes[F])
object Core {
  def apply[F[_]: Async](xa: Transactor[F]): Resource[F, Core[F]] =
    Resource
      .eval(LiveRecipes[F](xa))
      .map(recipes => new Core(recipes))
}
