package com.nmartinez.mealplanrandomizer.domain

import cats.implicits.*
import doobie.Put
import io.circe.*
import io.circe.syntax.*
import jdk.internal.net.http.common.Log.errors
import org.http4s.CharsetRange.Atom
import org.http4s.{EntityDecoder, ParseFailure, QueryParamDecoder}

enum DayOfWeek {
  case Monday, Tuesday, Wednesday, Thrusday, Friday, Saturday, Sunday
}
object DayOfWeek {
  private def fromString(str: String): Either[String, DayOfWeek] =
    values
      .find(_.toString.toUpperCase == str.toUpperCase)
      .toRight(s"$str is not a valid DayOfWeek")

  given Encoder[DayOfWeek] =
    Encoder[String].contramap(_.toString)
  Encoder
  given Decoder[DayOfWeek] =
    Decoder[String].emap(fromString)

  implicit val dayOfWeekKeyEncoder: KeyEncoder[DayOfWeek] =
    (day: DayOfWeek) => day.toString

  implicit val dayOfWeekKeyDecoder: KeyDecoder[DayOfWeek] =
    (key: String) => DayOfWeek.fromString(key) match
      case Left(_) => None
      case Right(day) => Some(day)

  implicit val dayOfWeekQueryParamDecoder: QueryParamDecoder[DayOfWeek] =
    QueryParamDecoder[String].emap { str =>
      DayOfWeek.fromString(str)
        .leftMap(err => ParseFailure(err, err))
    }
}