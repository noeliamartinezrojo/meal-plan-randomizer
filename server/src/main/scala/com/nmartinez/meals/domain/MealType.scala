package com.nmartinez.meals.domain

import cats.implicits.*
import io.circe.{Decoder, Encoder, KeyDecoder, KeyEncoder}
import org.http4s.{ParseFailure, QueryParamDecoder}

enum MealType {
  case Breakfast, Lunch, Dinner
}
object MealType {
  private def fromString(str: String): Either[String, MealType] =
    values
      .find(_.toString.toUpperCase == str.toUpperCase)
      .toRight(s"$str is not a valid MealOfDay")
  given Encoder[MealType] =
    Encoder[String].contramap(_.toString)
  given Decoder[MealType] =
    Decoder[String].emap(fromString)

  implicit val mealOfDayKeyEncoder: KeyEncoder[MealType] =
    (meal: MealType) => meal.toString

  implicit val mealOfDayKeyDecoder: KeyDecoder[MealType] =
    (key: String) => MealType.fromString(key) match
      case Left(_) => None
      case Right(meal) => Some(meal)

  implicit val mealOfDayQueryParamDecoder: QueryParamDecoder[MealType] =
    QueryParamDecoder[String].emap { str =>
      MealType.fromString(str)
        .leftMap(err => ParseFailure(err, err))
    }
}
// Serialising scala 3 enums with circe: https://scalajobs.com/blog/enum-serialization-in-scala/