package com.nmartinez.mealplanrandomizer.domain

import cats.kernel.Eq
import com.nmartinez.mealplanrandomizer.domain.*
case class Meal(
                 day: DayOfWeek,
                 meal: MealType
               )

object Meal {
  implicit val eqMeal: Eq[Meal] = Eq.fromUniversalEquals
}