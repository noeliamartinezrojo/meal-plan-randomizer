package com.nmartinez.mealplanrandomizer.playground

import cats.effect.*
import com.nmartinez.mealplanrandomizer.domain.RecipeInfo
import com.nmartinez.mealplanrandomizer.core.*
import doobie.*
import doobie.implicits.*
import doobie.util.*
import doobie.hikari.HikariTransactor
import scala.io.StdIn

object RecipesPlayground extends IOApp.Simple {
  val postgresResource: Resource[IO, HikariTransactor[IO]] = for {
    ec <- ExecutionContexts.fixedThreadPool(32)
    xa <- HikariTransactor.newHikariTransactor[IO](
      "org.postgresql.Driver",
      "jdbc:postgresql:mealplanrandomizer",
      "docker",
      "docker",
      ec
    )
  } yield xa

  val recipeInfo = RecipeInfo(name = "paella", servingsPerBatch = 4)

  override def run: IO[Unit] = postgresResource.use { xa =>
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
