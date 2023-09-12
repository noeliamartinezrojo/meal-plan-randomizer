package com.nmartinez.meals.domain

import com.nmartinez.meals.domain.*
import io.circe.*
import io.circe.generic.auto.*
import io.circe.syntax.*

type MealPlan = Map[DayOfWeek, Map[MealType, Recipe]]
