package com.nmartinez.mealplanrandomizer.logic

import com.nmartinez.mealplanrandomizer.domain.*
import scala.annotation.tailrec
import cats.implicits.*
import scala.util.Random
object MealPlans {

  def generateShoppingList(mealPlan: MealPlan): List[Ingredient] = {
    import com.nmartinez.mealplanrandomizer.domain.Ingredient.ingredientListMonoid
    val recipes = mealPlan.values.flatMap(_.values)
    val ingredientLists = recipes.map(_.recipeInfo.ingredients)
    ingredientLists.fold(Nil)((l1, l2) => l1 |+| l2)
  }

  @tailrec
  def generateRandomMealPlan(
                              recipes: List[Recipe],
                              days: List[DayOfWeek],
                              meals: List[MealType],
                              acc: Map[DayOfWeek, Map[MealType, Recipe]] = Map.empty
                            ): Map[DayOfWeek, Map[MealType, Recipe]] = {
    if (days.isEmpty) acc.filter(_._2.nonEmpty)
    else generateRandomMealPlan(recipes, days.tail, meals, acc +
      (days.head -> generateRandomMealPlanByDay(recipes, days.head, meals, Map.empty, acc.values.flatMap(_.values).toList))
    )
  }

  @tailrec
  private def generateRandomMealPlanByDay(
                                         recipes: List[Recipe],
                                         day: DayOfWeek,
                                         meals: List[MealType],
                                         acc: Map[MealType, Recipe],
                                         used: List[Recipe]
                                       ): Map[MealType, Recipe] = {
    if (meals.isEmpty) acc
    else getValidRandomRecipe(recipes, day, meals.head, used) match {
      case None => generateRandomMealPlanByDay(recipes, day, meals.tail, acc, used)
      case Some(recipe) => generateRandomMealPlanByDay(recipes, day, meals.tail, acc + (meals.head -> recipe), used)
    }
  }
  private def getValidRandomRecipe(recipes: List[Recipe],
                                   day: DayOfWeek,
                                   meal: MealType,
                                   acc: List[Recipe]): Option[Recipe] = {
    recipes
      .filterNot(_.recipeInfo.excludeFrom.contains_(Meal(day, meal)))
      .filterNot(r => acc.count(_ === r) >= r.recipeInfo.maxBatchesPerWeek)
    match {
      case Nil => None
      case validRecipes => Option(validRecipes.maxBy(_ => Random.nextInt()))
    }
  }
}
