package com.nmartinez.meals.http.validation

import io.circe.generic.auto.*
import org.http4s.circe.*
import org.http4s.circe.CirceEntityCodec.*
import org.http4s.*
import org.http4s.implicits.*
import org.http4s.dsl.*
import cats.*
import cats.implicits.*
import cats.data.*
import cats.data.Validated.*
import org.typelevel.log4cats.Logger
import com.nmartinez.meals.http.validation.Validators.*
import com.nmartinez.meals.http.responses.*
import com.nmartinez.meals.domain.*
import com.nmartinez.meals.logging.Syntax.*

object Syntax {

  def validateEntity[A](entity: A)(using validator: Validator[A]): ValidationResult[A] =
    validator.validate(entity)

  trait HttpValidationDsl[F[_]: MonadThrow: Logger] extends Http4sDsl[F] {

    extension (req: Request[F])
      def validate[A: Validator](
                                  serverLogicIfValid: A => F[Response[F]]
                                )(using EntityDecoder[F, A]): F[Response[F]] =
        req.as[A]
          .logError(e => s"Parsing payload failed: $e")
          .map(validateEntity) // F[ValidationResult[A]]
          .flatMap {
            case Valid(entity) =>
              serverLogicIfValid(entity) // F[Response[F]]
            case Invalid(errors) =>
              BadRequest(FailureResponse(errors.toList.map(_.errorMessage).mkString(", ")))
          }
  }
}
