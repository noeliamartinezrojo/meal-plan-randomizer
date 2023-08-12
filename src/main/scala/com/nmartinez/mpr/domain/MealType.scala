package com.nmartinez.mpr.domain

import cats.implicits.*
import io.circe.{Decoder, Encoder}
import org.http4s.{ParseFailure, QueryParamDecoder}

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

  implicit val mealTypeQueryParamDecoder: QueryParamDecoder[MealType] =
    QueryParamDecoder[String].emap { str =>
      MealType.fromString(str)
        .leftMap(err => ParseFailure(err, err))
    }
}
// Serialising scala 3 enums with circe: https://scalajobs.com/blog/enum-serialization-in-scala/