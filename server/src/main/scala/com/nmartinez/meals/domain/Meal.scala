package com.nmartinez.meals.domain

import cats.kernel.Eq
import com.nmartinez.meals.domain.*
case class Meal(
                 day: DayOfWeek,
                 meal: MealType
               )

object Meal {
  implicit val eqMeal: Eq[Meal] = Eq.fromUniversalEquals
}