package com.nmartinez.mpr.domain

import java.time.Duration
import java.util.{Date, UUID}

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
      None
    )
  }
}
