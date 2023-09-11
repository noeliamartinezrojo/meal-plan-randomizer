package com.nmartinez.mpr

import cats.effect.IO
import com.nmartinez.mpr.domain.Recipe.*
import com.nmartinez.mpr.http.routes.RecipeRoutes
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger
import java.util.UUID

trait RecipeFixture {
  def anyRecipe(name: String) = Recipe(
    id = UUID.randomUUID(),
    created = System.currentTimeMillis(),
    ownerEmail = "TODO@nmartinez.com",
    recipeInfo = RecipeInfo.empty.copy(name = name)
  )

  val recipe1 = anyRecipe("recipe 1")
  val recipe2 = anyRecipe("recipe 2")
  val recipe3 = anyRecipe("recipe 3")
  given logger: Logger[IO] = Slf4jLogger.getLogger[IO]

  val uut = RecipeRoutes[IO]
}