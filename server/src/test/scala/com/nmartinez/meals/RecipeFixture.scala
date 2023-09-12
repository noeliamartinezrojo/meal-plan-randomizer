package com.nmartinez.meals

import cats.effect.IO
import com.nmartinez.meals.domain.*
import com.nmartinez.meals.domain.DayOfWeek.*
import com.nmartinez.meals.domain.MealType.*
import com.nmartinez.meals.http.routes.RecipeRoutes
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger
import java.util.UUID
import monocle.syntax.all.*

trait RecipeFixture {

  val notFoundUUID = UUID.fromString("11111111-2222-3333-4444-000000000000")
  val testRecipeUUID = UUID.fromString("11111111-2222-3333-4444-000000000001")
  val newRecipeUUID = UUID.fromString("11111111-2222-3333-4444-000000000002")

  val testRecipe = Recipe(
    id = testRecipeUUID,
    ownerEmail = "TODO@nmartinez.com",
    recipeInfo = RecipeInfo(
      name = "someRecipeName",
      image = Option("someRecipeImage"),
      servingsPerBatch = 4,
      minBatchesPerWeek = 1,
      maxBatchesPerWeek = 2,
      excludeFrom = List(Meal(Monday, Lunch)),
      ingredients = List(
        Ingredient(IngredientType.Vegetables, "Garlic", IngredientUnit.Grams, 20.0),
        Ingredient(IngredientType.Vegetables, "Red pepper", IngredientUnit.Units, 2.0),
        Ingredient(IngredientType.Meat, "Whole chicken", IngredientUnit.Units, 1.0)
      )
    )
  )

  val testRecipeDbVersion: Recipe =
    testRecipe
      .focus(_.recipeInfo.excludeFrom)
      .replace(Nil)
      .focus(_.recipeInfo.ingredients)
      .replace(Nil)

  val newRecipeInfo = RecipeInfo(
    name = "newRecipeName",
    image = Option("newRecipeImage"),
    servingsPerBatch = 5,
    minBatchesPerWeek = 2,
    maxBatchesPerWeek = 3,
    excludeFrom = Nil,
    ingredients = Nil
  )

  val updatedTestRecipe =
    testRecipe
      .focus(_.recipeInfo.image)
      .replace(Option("updatedRecipeImage"))
      .focus(_.recipeInfo.ingredients.index(1).as[Ingredient].qty)
      .replace(1.0) // reduce quantity of red pepper from 2 to 1

  val updatedTestRecipeDbVersion =
    updatedTestRecipe
      .focus(_.recipeInfo.excludeFrom)
      .replace(Nil)
      .focus(_.recipeInfo.ingredients)
      .replace(Nil)

  def anyRecipe(name: String) = Recipe(
    id = UUID.randomUUID(),
    ownerEmail = "TODO@nmartinez.com",
    recipeInfo = RecipeInfo.empty.copy(name = name)
  )

  val recipe1 = anyRecipe("recipe 1")
  val recipe2 = anyRecipe("recipe 2")
  val recipe3 = anyRecipe("recipe 3")
}