package com.nmartinez.mpr.domain

import cats.kernel.Eq
import com.nmartinez.mpr.domain.*
import io.circe.*
import io.circe.generic.auto.*
import io.circe.syntax.*
object MealPlan {

  case class Meal(
                   day: DayOfWeek,
                   meal: MealType
                 )

  implicit val eqMeal: Eq[Meal] = Eq.fromUniversalEquals
}
