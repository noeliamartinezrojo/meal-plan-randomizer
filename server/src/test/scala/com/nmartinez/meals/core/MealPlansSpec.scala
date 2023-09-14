package com.nmartinez.meals.core

import com.nmartinez.meals.RecipeFixture
import com.nmartinez.meals.core.MealPlans
import com.nmartinez.meals.domain.*
import com.nmartinez.meals.domain.DayOfWeek.*
import com.nmartinez.meals.domain.MealType.*
import monocle.syntax.all.*
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.*
import org.scalatest.matchers.must.Matchers.contain
import org.scalatest.matchers.should.Matchers.*
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger
import java.util.UUID
import scala.collection.MapView
import scala.collection.immutable.Map

class MealPlansSpec extends AnyFlatSpec with RecipeFixture {

  "generateRandomMealPlan" should "return an empty plan if recipes, days or meals are empty" in {
    MealPlans.generateRandomMealPlan(Nil, List(Monday), List(Lunch)) shouldBe Map.empty
    MealPlans.generateRandomMealPlan(List(recipe1), Nil, List(Lunch)) shouldBe Map.empty
    MealPlans.generateRandomMealPlan(List(recipe1), List(Monday), Nil) shouldBe Map.empty
  }

  it should "exclude recipes from day and meal combinations in their excludeFrom" in {
    val recipeExcludedFromMondayLunch =
      recipe1
        .focus(_.recipeInfo.excludeFrom)
        .replace(List(Meal(Monday, Lunch)))

    MealPlans.generateRandomMealPlan(
      List(recipeExcludedFromMondayLunch),
      List(Monday, Tuesday),
      List(Lunch)
    ) shouldBe Map(
      Tuesday -> Map(
        Lunch -> recipeExcludedFromMondayLunch
      )
    )
  }

  it should "duplicate recipes up to their maxBatchesPerWeek if there are fewer recipes than meals in the meal plan" in {
    val recipeUpToTwicePerWeek =
      recipe1
        .focus(_.recipeInfo.maxBatchesPerWeek)
        .replace(2)

    MealPlans.generateRandomMealPlan(
      List(recipeUpToTwicePerWeek),
      List(Monday, Tuesday),
      List(Lunch, Dinner)
    ) shouldBe Map(
      Monday -> Map(
        Lunch -> recipeUpToTwicePerWeek,
        Dinner -> recipeUpToTwicePerWeek
      )
    )
  }

  it should "not duplicate recipes if there as many recipes or more than meals in the meal plan" in {
    val mealPlan = MealPlans.generateRandomMealPlan(
      List(recipe1, recipe2, recipe3),
      List(Monday, Tuesday),
      List(Lunch, Dinner)
    )

    mealPlan.values.count(_ === recipe1) should be < 2
    mealPlan.values.count(_ === recipe2) should be < 2
    mealPlan.values.count(_ === recipe3) should be < 2
  }
}
