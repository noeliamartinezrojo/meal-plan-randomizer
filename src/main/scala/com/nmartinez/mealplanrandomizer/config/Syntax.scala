package com.nmartinez.mealplanrandomizer.config

import pureconfig.{ConfigReader, ConfigSource}
import pureconfig.error.ConfigReaderException
import cats.MonadThrow
import cats.implicits.*
import scala.reflect.ClassTag
object Syntax {
  extension (source: ConfigSource)
    def loadF[F[_], A: ClassTag](using reader: ConfigReader[A], F: MonadThrow[F]): F[A] =
      F.pure(source.load[A]).flatMap {
        case Left(errors) => F.raiseError[A](ConfigReaderException(errors))
        case Right(value) => F.pure(value)
      }
}
