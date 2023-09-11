package com.nmartinez.mealplanrandomizer.domain.model

import cats.Monoid
import cats.implicits.catsSyntaxSemigroup
import com.nmartinez.mealplanrandomizer.domain.Ingredient.*
import com.nmartinez.mealplanrandomizer.domain.*
import io.circe.*
import io.circe.parser.*
import io.circe.syntax.*
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.*
import org.scalatest.matchers.should.Matchers.*
class IngredientSpec extends AnyFlatSpec {

  behavior of "Ingredient"

  it should "be decodable from JSON string" in {
    decode(ingredientAsJsonString) shouldBe Right(ingredient)
  }

  it should "be encodable as JSON string" in {
    ingredient.asJson.noSpaces shouldBe ingredientAsJsonString.filterNot(_.isWhitespace)
  }

  it should "combine lists of ingredients" in {
    val ingredientList1 = List(
      Ingredient(IngredientType.Vegetables, "garlic", IngredientUnit.Units, 1.0),
      Ingredient(IngredientType.Meat, "whole chicken", IngredientUnit.Units, 1.0),
      Ingredient(IngredientType.Vegetables, "garlic", IngredientUnit.Grams, 20.0),
    )
    val ingredientList2 = List(
      Ingredient(IngredientType.Vegetables, "garlic", IngredientUnit.Units, 2.0),
      Ingredient(IngredientType.Vegetables, "red pepper", IngredientUnit.Units, 2.0)
    )
    (ingredientList1 |+| ingredientList2).toSet shouldBe Set(
      Ingredient(IngredientType.Vegetables, "Garlic", IngredientUnit.Units, 3.0),
      Ingredient(IngredientType.Vegetables, "Garlic", IngredientUnit.Grams, 20.0),
      Ingredient(IngredientType.Vegetables, "Red pepper", IngredientUnit.Units, 2.0),
      Ingredient(IngredientType.Meat, "Whole chicken", IngredientUnit.Units, 1.0)
    )
  }

  val ingredient = Ingredient(IngredientType.Vegetables, "Onion", IngredientUnit.Units, 1.0)
  val ingredientAsJsonString =
    """
    {
      "type": "Vegetables",
      "name": "Onion",
      "unit": "Units",
      "qty": 1.0
    }
  """


}
