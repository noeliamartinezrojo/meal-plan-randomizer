package com.nmartinez.meals.http.validation

import cats.*
import cats.implicits.*
import cats.data.*
import cats.data.Validated.*
import com.nmartinez.meals.domain.*

object Validators {
  sealed trait ValidationFailure(val errorMessage: String)
  case class EmptyField(fieldName: String) extends ValidationFailure(s"$fieldName is empty")
  case class InvalidField(errorMsg: String) extends ValidationFailure(errorMsg)


  type ValidationResult[A] = ValidatedNel[ValidationFailure, A]

  trait Validator[A] {
    def validate(value: A): ValidationResult[A]
  }

  def validateRequired[A](field: A, fieldName: String)(required: A => Boolean): ValidationResult[A] =
    if (required(field)) field.validNel
    else EmptyField(fieldName).invalidNel

  def validateValid[A](field: A, errorMsg: String)(valid: A => Boolean): ValidationResult[A] =
    if (valid(field)) field.validNel
    else InvalidField(errorMsg).invalidNel


  given recipeInfoValidator: Validator[RecipeInfo] = (recipeInfo: RecipeInfo) => {
    val RecipeInfo(
      name,
      image,
      servingsPerBatch,
      minBatchesPerWeek,
      maxBatchesPerWeek,
      excludeFrom, // TODO NMR: validate meals separately
      ingredients // TODO NMR: validate ingredients separately
    ) = recipeInfo

    val validName = validateRequired(name, "name")(_.trim.nonEmpty)
    val validServingsPerBatch =
      validateValid(servingsPerBatch, "servingsPerBatch must be greater than 0")(_ > 0)
    val validMinBatchesPerWeek =
      validateValid(minBatchesPerWeek, "minBatchesPerWeek must be greater than or equal to 0")(_ >= 0)
    val validMaxBatchesPerWeek =
      validateValid(maxBatchesPerWeek, "maxBatchesPerWeek must be greater than 0")(_ > 0)
    // TODO NMR: consider making validMaxBatchesPerWeek an option (no max)

    (
      validName,
      image.validNel,
      validServingsPerBatch,
      validMinBatchesPerWeek,
      validMaxBatchesPerWeek,
      excludeFrom.validNel,
      ingredients.validNel
    ).mapN(RecipeInfo.apply)
  }
}
