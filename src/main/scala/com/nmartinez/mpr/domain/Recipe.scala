package com.nmartinez.mpr.domain

import java.time.Duration
import java.util.{Date, UUID}
import com.nmartinez.mpr.domain._
import io.circe._
import io.circe.generic.auto._

object Recipe {
  case class Recipe(
      id: UUID,
      created: Long,
      ownerEmail: String,
      recipeInfo: RecipeInfo
  )
  case class RecipeInfo(
      name: String,
      image: Option[String],
      people: Int,
      servingsPerPerson: Int,
      activeTime: Option[Long],
      passiveTime: Option[Long],
      totalTime: Option[Long],
      tags: Option[List[String]],
      excludedMealSlots: Map[DayOfWeek, List[MealType]]
  )
  
  object RecipeInfo {
    val empty = RecipeInfo(
      "",
      None,
      0,
      0,
      None,
      None,
      None,
      None,
      Map.empty
    )
  }

  case class RecipeView(
                         name: String,
                         image: Option[String]
                       )

  object RecipeView {
    def apply(recipe: Recipe): RecipeView =
      RecipeView(recipe.recipeInfo.name, recipe.recipeInfo.image)
  }
}
