package com.nmartinez.mealplanrandomizer.playground

import cats.effect.*
import com.nmartinez.mealplanrandomizer.config.AppConfig
import com.nmartinez.mealplanrandomizer.db.LiveRecipes
import com.nmartinez.mealplanrandomizer.domain.RecipeInfo
import com.nmartinez.mealplanrandomizer.modules.Core.*
import com.nmartinez.mealplanrandomizer.modules.{Core, Database}
import doobie.*
import doobie.implicits.*
import doobie.util.*
import doobie.hikari.HikariTransactor
import pureconfig.ConfigSource
import scala.io.StdIn
import com.nmartinez.mealplanrandomizer.config.Syntax.loadF

object RecipesPlayground extends IOApp.Simple {

  val recipeInfo = RecipeInfo(name = "paella", servingsPerBatch = 4)

  override def run: IO[Unit] = ConfigSource.default.loadF[IO, AppConfig].flatMap {
    case AppConfig(postgresConfig, _) =>
      Database.makePostgresResource[IO](postgresConfig).use { xa =>
        for {
          recipes <- LiveRecipes[IO](xa)
          _ <- IO(println("Press to create a recipe...")) *> IO(StdIn.readLine)
          id <- recipes.create("noeliamtzrojo@gmail.com", recipeInfo)
          _ <- IO(println("Press to view recipes...")) *> IO(StdIn.readLine)
          list <- recipes.all()
          _ <- IO(println(s"${list.length} recipe(s): $list\nPress to update the recipe...")) *> IO(StdIn.readLine)
          _ <- recipes.update(id, recipeInfo.copy(name = "carbonara"))
          newRecipe <- recipes.find(id)
          _ <- IO(println(s"Updated and found $newRecipe\nPress to delete the recipe...")) *> IO(StdIn.readLine)
          _ <- recipes.delete(id)
          listAfter <- recipes.all()
          _ <- IO(println(s"Deleted recipe. Now ${listAfter.length} recipe(s): $listAfter")) *> IO(StdIn.readLine)
        } yield ()
      }
  }
}
