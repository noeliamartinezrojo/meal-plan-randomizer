package com.nmartinez.mpr.http.routes

import java.util.UUID
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.*
import org.scalatest.matchers.should.Matchers.*
import org.scalatest.matchers.must.Matchers.contain
import cats.effect.IO
import com.nmartinez.mpr.RecipeFixture
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger
import com.nmartinez.mpr.domain.MealType.*
import com.nmartinez.mpr.domain.DayOfWeek.*
import com.nmartinez.mpr.domain.MealPlan.*
import com.nmartinez.mpr.domain.Recipe.*
import monocle.syntax.all.*
import scala.collection.MapView
import scala.collection.immutable.Map

class RecipeRoutesSpec extends AnyFlatSpec with RecipeFixture {

  behavior of "generateRandomMealPlanTailrec"

  it should "return an empty plan if there are no recipes" in {
    // setup
    uut.database.size shouldBe 0
    // test
    val mealPlan = uut.generateRandomMealPlan(List(Monday), List(Lunch))
    // assert
    mealPlan shouldBe Map.empty
  }

  it should "return an empty plan if no meals are specified" in {
    // setup
    uut.database.put(recipe1.id, recipe1)
    // test
    val mealPlan = uut.generateRandomMealPlan(Nil, Nil)
    // assert
    mealPlan shouldBe Map.empty
  }

  it should "exclude recipes from day and meal combinations in their excludeFrom" in {
    // setup
    uut.database.put(recipe1.id, recipe1.focus(_.recipeInfo.excludeFrom).replace(List(Meal(Monday, Lunch))))
    // test
    val mealPlan = uut.generateRandomMealPlan(List(Monday, Tuesday), List(Lunch))
    // assert
    mealPlan.view.mapValues(_.view.mapValues(_.recipeInfo.name).toMap).toMap shouldBe Map(
      Tuesday -> Map(
        Lunch -> "recipe 1"
      )
    )
  }

  it should "duplicate recipes up to their maxBatchesPerWeek if there are fewer recipes than meals in the meal plan" in {
    // setup
    uut.database.put(recipe1.id, recipe1.focus(_.recipeInfo.maxBatchesPerWeek).replace(2))
    // test
    val mealPlan = uut.generateRandomMealPlan(List(Monday, Tuesday), List(Lunch, Dinner))
    // assert
    mealPlan.view.mapValues(_.view.mapValues(_.recipeInfo.name).toMap).toMap shouldBe Map(
      Monday -> Map(
        Lunch -> "recipe 1",
        Dinner -> "recipe 1",
      )
    )
  }

  it should "not duplicate recipes if there as many recipes or more than meals in the meal plan" in {
    // setup
    uut.database.put(recipe1.id, recipe1)
    uut.database.put(recipe2.id, recipe2)
    uut.database.put(recipe3.id, recipe3)
    // test
    val mealPlan = uut.generateRandomMealPlan(List(Monday), List(Lunch, Dinner))
    // assert
    mealPlan.values.count(_ === recipe1) should be < 2
    mealPlan.values.count(_ === recipe2) should be < 2
    mealPlan.values.count(_ === recipe3) should be < 2
  }
}
