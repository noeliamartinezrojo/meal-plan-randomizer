package com.nmartinez.mealplanrandomizer.domain

import cats.kernel.Eq

import java.time.Duration
import java.util.{Date, UUID}
import com.nmartinez.mealplanrandomizer.domain.*
import com.nmartinez.mealplanrandomizer.domain.Ingredient.*
import io.circe.*
import io.circe.generic.auto.*

case class Recipe(
                   id: UUID,
                   ownerEmail: String,
                   recipeInfo: RecipeInfo
                 )
case class RecipeInfo(
                       name: String,
                       image: Option[String] = None,
                       servingsPerBatch: Int,
                       minBatchesPerWeek: Int = 0,
                       maxBatchesPerWeek: Int = 1,
                       excludeFrom: List[Meal] = Nil,
                       ingredients: List[Ingredient] = Nil
                     )

object Recipe {

  implicit val eqRecipe: Eq[Recipe] = Eq.fromUniversalEquals
}
object RecipeInfo {
  val empty = RecipeInfo(name = "", servingsPerBatch = 1)
}
