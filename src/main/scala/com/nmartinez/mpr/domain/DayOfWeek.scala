package com.nmartinez.mpr.domain

import io.circe._, io.circe.syntax._
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
  given Decoder[DayOfWeek] =
    Decoder[String].emap(fromString)

  implicit val dayOfWeekKeyEncoder: KeyEncoder[DayOfWeek] = new KeyEncoder[DayOfWeek] {
    override def apply(dayOfWeek: DayOfWeek): String = dayOfWeek.toString
  }

  implicit val dayOfWeekKeyDecoder: KeyDecoder[DayOfWeek] = new KeyDecoder[DayOfWeek] {
    override def apply(key: String): Option[DayOfWeek] = DayOfWeek.fromString(key) match
      case Left(dayOfWeek) => None
      case Right(dayOfWeek) => Some(dayOfWeek)
  }
}