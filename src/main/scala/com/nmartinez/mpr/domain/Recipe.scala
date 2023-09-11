package com.nmartinez.mpr.domain

import cats.kernel.Eq

import java.time.Duration
import java.util.{Date, UUID}
import com.nmartinez.mpr.domain.*
import com.nmartinez.mpr.domain.Ingredient.*
import com.nmartinez.mpr.domain.MealPlan.*
import io.circe.*
import io.circe.generic.auto.*

object Recipe {
  case class Recipe(
                     id: UUID,
                     created: Long,
                     ownerEmail: String,
                     recipeInfo: RecipeInfo
                   )

  implicit val eqRecipe: Eq[Recipe] = Eq.fromUniversalEquals

  case class RecipeInfo(
                         name: String,
                         image: Option[String],
                         servingsPerBatch: Int,
                         maxBatchesPerWeek: Int, // TODO: make optional? by default 1? Add minBatchesPerWeek
                         excludeFrom: List[Meal], // TODO: make optional?
                         ingredients: List[Ingredient],
                         tags: Option[List[String]]
                       )

  object RecipeInfo {
    val empty = RecipeInfo(
      "",
      None,
      1,
      1,
      Nil,
      Nil,
      None
    )
  }
}
