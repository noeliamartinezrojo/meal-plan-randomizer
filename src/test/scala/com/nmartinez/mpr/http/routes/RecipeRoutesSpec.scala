package com.nmartinez.mpr.http.routes

import java.util.UUID
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.*
import org.scalatest.matchers.should.Matchers.*
import cats.effect.IO
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger
import com.nmartinez.mpr.domain.MealType.*
import com.nmartinez.mpr.domain.DayOfWeek.*
import com.nmartinez.mpr.domain.Recipe.*
import org.scalatest.matchers.must.Matchers.contain

class RecipeRoutesSpec extends AnyFlatSpec {

  "generateRandomMealPlan" should "return an empty plan if the recipe db is empty" in new RecipesFixture {
    uut.database.size shouldBe 0
    uut.generateRandomMealPlan(List(Monday), List(Lunch)) shouldBe Map.empty
  }

  it should "return an empty plan if days of the week is an empty list" in new RecipesFixture {
    uut.database.put(recipe1.id, recipe1)
    uut.generateRandomMealPlan(Nil, List(Lunch)) shouldBe Map.empty
  }

  it should "return an empty plan if meal types is an empty list" in new RecipesFixture {
    uut.database.put(recipe1.id, recipe1)
    uut.generateRandomMealPlan(List(Monday), Nil) shouldBe Map.empty
  }

  "getRandomRecipes" should "return no recipes if recipe db is empty" in new RecipesFixture {
    uut.getRandomRecipes(0) shouldBe Iterable()
    uut.getRandomRecipes(1) shouldBe Iterable()
  }

  it should "duplicate recipes if N is greater than the number of recipes in the db" in new RecipesFixture {
    uut.database.put(recipe1.id, recipe1)
    uut.getRandomRecipes(2) shouldBe Iterable(RecipeView(recipe1), RecipeView(recipe1))
  }

  it should "not duplicate recipes if N is equal or less than the number of recipes in the db" in new RecipesFixture {
    uut.database.put(recipe1.id, recipe1)
    uut.database.put(recipe2.id, recipe2)
    uut.getRandomRecipes(1) should contain oneOf(RecipeView(recipe1), RecipeView(recipe2))
    uut.getRandomRecipes(2) should contain allOf(RecipeView(recipe1), RecipeView(recipe2))
  }

  trait RecipesFixture {
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
}
