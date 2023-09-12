package com.nmartinez.meals.db

import cats.effect.*
import cats.effect.implicits.*
import cats.effect.testing.scalatest.AsyncIOSpec
import com.nmartinez.meals.RecipeFixture
import org.scalatest.freespec.AsyncFreeSpec
import org.scalatest.matchers.should.Matchers
import monocle.syntax.all.*
import doobie.implicits.*
import doobie.postgres.implicits.*

class RecipesSpec
  extends AsyncFreeSpec
  with AsyncIOSpec
  with Matchers
  with DoobieSpec
  with RecipeFixture {

  override val initScript: String = "sql/recipes.sql"

  "Recipes 'algebra'" - {
    "should return no recipe if the given UUID does not exist" in {
      transactor.use { xa =>
        val program = for {
          recipes <- LiveRecipes[IO](xa)
          retrieved <- recipes.find(notFoundUUID)
        } yield retrieved

        program.asserting(_ shouldBe None)
      }
    }

    "should retrieve a recipe by id" in {
      transactor.use { xa =>
        val program = for {
          recipes <- LiveRecipes[IO](xa)
          retrieved <- recipes.find(testRecipeUUID)
        } yield retrieved

        program.asserting(_ shouldBe Option(testRecipeDbVersion))
      }
    }

    "should retrieve all recipes" in {
      transactor.use { xa =>
        val program = for {
          recipes <- LiveRecipes[IO](xa)
          retrieved <- recipes.all()
        } yield retrieved

        program.asserting(_ shouldBe List(testRecipeDbVersion))
      }
    }

    "should create a recipe" in {
      transactor.use { xa =>
        val program = for {
          recipes <- LiveRecipes[IO](xa)
          newRecipeId <- recipes.create("example@gmail.com", newRecipeInfo)
          maybeRecipe <- recipes.find(newRecipeId)
        } yield maybeRecipe

        program.asserting(_.map(_.recipeInfo) shouldBe Option(newRecipeInfo))
      }
    }

    "should return an updated recipe if it exists" in {
      transactor.use { xa =>
        val program = for {
          recipes <- LiveRecipes[IO](xa)
          retrieved <- recipes.update(testRecipeUUID, updatedTestRecipeDbVersion.recipeInfo)
        } yield retrieved

        program.asserting(_ shouldBe Option(updatedTestRecipeDbVersion))
      }
    }

    "should return none when trying to update a recipe that doesn't exist" in {
      transactor.use { xa =>
        val program = for {
          recipes <- LiveRecipes[IO](xa)
          retrieved <- recipes.update(notFoundUUID, updatedTestRecipeDbVersion.recipeInfo)
        } yield retrieved

        program.asserting(_ shouldBe None)
      }
    }

    "should delete a recipe if it exists" in {
      transactor.use { xa =>
        val program = for {
          recipes <- LiveRecipes[IO](xa)
          numDeletedRecipes <- recipes.delete(testRecipeUUID)
          numMatchedRecipes <- sql"""SELECT COUNT(*) FROM recipes WHERE id = $testRecipeUUID"""
            .query[Int]
            .unique
            .transact(xa)
        } yield (numDeletedRecipes, numMatchedRecipes)

        program.asserting {
          case (numDeletedRecipes, numMatchedRecipes) =>
            numDeletedRecipes shouldBe 1
            numMatchedRecipes shouldBe 0
        }
      }
    }

    "should delete no rows when trying to delete a recipe that doesn't exist" in {
      transactor.use { xa =>
        val program = for {
          recipes <- LiveRecipes[IO](xa)
          numDeletedRecipes <- recipes.delete(notFoundUUID)
          numMatchedRecipes <- sql"""SELECT COUNT(*) FROM recipes WHERE id = $notFoundUUID"""
            .query[Int]
            .unique
            .transact(xa)
        } yield (numDeletedRecipes, numMatchedRecipes)

        program.asserting {
          case (numDeletedRecipes, numMatchedRecipes) =>
            numDeletedRecipes shouldBe 0
            numMatchedRecipes shouldBe 0
        }
      }
    }
  }
}
