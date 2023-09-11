package com.nmartinez.mealplanrandomizer.domain

import com.nmartinez.mealplanrandomizer.domain.*
import io.circe.*
import io.circe.generic.auto.*
import io.circe.syntax.*

type MealPlan = Map[DayOfWeek, Map[MealType, Recipe]]
