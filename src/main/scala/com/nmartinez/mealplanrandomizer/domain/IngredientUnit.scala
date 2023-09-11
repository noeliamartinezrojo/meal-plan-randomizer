package com.nmartinez.mealplanrandomizer.domain

import io.circe.{Decoder, Encoder}

enum IngredientUnit {
  case Unspecified, Grams, Milliliters, Teaspoons, Units
}

object IngredientUnit {
  private def fromString(str: String): Either[String, IngredientUnit] =
    values
      .find(_.toString.toUpperCase == str.toUpperCase)
      .toRight(s"$str is not a valid IngredientUnit")

  given Encoder[IngredientUnit] =
    Encoder[String].contramap(_.toString)
  Encoder
  given Decoder[IngredientUnit] =
    Decoder[String].emap(fromString)
}