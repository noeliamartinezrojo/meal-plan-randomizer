package com.nmartinez.mpr.domain

import io.circe.{Encoder, Decoder}

enum MealType {
  case Breakfast, Lunch, Dinner
}
object MealType {
  private def fromString(str: String): Either[String, MealType] =
    values
      .find(_.toString.toUpperCase == str.toUpperCase)
      .toRight(s"$str is not a valid MealType")
  given Encoder[MealType] =
    Encoder[String].contramap(_.toString)
  given Decoder[MealType] =
    Decoder[String].emap(fromString)
}

// Serialising scala 3 enums with circe: https://scalajobs.com/blog/enum-serialization-in-scala/