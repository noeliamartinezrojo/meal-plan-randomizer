package com.nmartinez.meals.domain

import io.circe.{Decoder, Encoder, KeyDecoder, KeyEncoder}
import org.http4s.{ParseFailure, QueryParamDecoder}

enum IngredientType {
  case Unspecified, Fruits, Vegetables, Meat, Fish, DryGoods, CannedGoods, ChilledItems, FrozenItems, OilsVinegarsAndSauces, HerbsAndSpices
}

object IngredientType {
  private def fromString(str: String): Either[String, IngredientType] =
    values
      .find(_.toString.toUpperCase == str.toUpperCase)
      .toRight(s"$str is not a valid IngredientType")

  given Encoder[IngredientType] =
    Encoder[String].contramap(_.toString)
  Encoder
  given Decoder[IngredientType] =
    Decoder[String].emap(fromString)
}