package com.nmartinez.meals.core

import cats.*
import cats.data.NonEmptyList
import cats.effect.*
import cats.implicits.*
import com.nmartinez.meals.domain.*
import doobie.*
import doobie.implicits.*
import doobie.postgres.*
import doobie.postgres.implicits.*
import doobie.util.*
import org.postgresql.util.PGobject

import java.util.UUID
trait Recipes[F[_]] {
  def create(ownerEmail: String, recipeInfo: RecipeInfo): F[UUID]
  def all(): F[List[Recipe]]
  def find(id: UUID): F[Option[Recipe]]
  def update(id: UUID, recipeInfo: RecipeInfo): F[Option[Recipe]]
  def delete(id: UUID): F[Int]
}

class LiveRecipes[F[_]: MonadCancelThrow] private (xa: Transactor[F]) extends Recipes[F] {
  override def create(ownerEmail: String, recipeInfo: RecipeInfo): F[UUID] =
    sql"""
      INSERT INTO recipes(
        ownerEmail,
        name,
        image,
        servingsPerBatch,
        minBatchesPerWeek,
        maxBatchesPerWeek
      ) VALUES(
        $ownerEmail,
        ${recipeInfo.name},
        ${recipeInfo.image},
        ${recipeInfo.servingsPerBatch},
        ${recipeInfo.minBatchesPerWeek},
        ${recipeInfo.maxBatchesPerWeek}
      )
   """
    .update
    .withUniqueGeneratedKeys[UUID]("id")
    .transact(xa)

  override def all(): F[List[Recipe]] =
    sql"""SELECT
            id,
            ownerEmail,
            name,
            image,
            servingsPerBatch,
            minBatchesPerWeek,
            maxBatchesPerWeek
          FROM recipes
    """
      .query[(UUID, String, String, Option[String], Int, Int, Int)]
      .to[List]
      .map(recipes => recipes.map(cols => {
        val (id, ownerEmail, name, image, servingsPerBatch, minBatchesPerWeek, maxBatchesPerWeek) = cols
        Recipe(id, ownerEmail, RecipeInfo(
          name, image, servingsPerBatch, minBatchesPerWeek, maxBatchesPerWeek, Nil, Nil
        ))
      }))
      .transact(xa)

  override def find(id: UUID): F[Option[Recipe]] =
    sql"""SELECT
        id,
        ownerEmail,
        name,
        image,
        servingsPerBatch,
        minBatchesPerWeek,
        maxBatchesPerWeek
      FROM recipes
      WHERE id = $id
    """
      .query[(UUID, String, String, Option[String], Int, Int, Int)]
      .option
      .map(maybeCols => maybeCols.map(cols => {
          val (id, ownerEmail, name, image, servingsPerBatch, minBatchesPerWeek, maxBatchesPerWeek) = cols
          Recipe(id, ownerEmail, RecipeInfo(
            name, image, servingsPerBatch, minBatchesPerWeek, maxBatchesPerWeek, Nil, Nil
          ))
      })) // TODO NMR: find a better solution
      .transact(xa)

  override def update(id: UUID, recipeInfo: RecipeInfo): F[Option[Recipe]] =
    sql"""
      UPDATE recipes
      SET
        name = ${recipeInfo.name},
        image = ${recipeInfo.image},
        servingsPerBatch = ${recipeInfo.servingsPerBatch},
        minBatchesPerWeek = ${recipeInfo.minBatchesPerWeek},
        maxBatchesPerWeek = ${recipeInfo.maxBatchesPerWeek}
      WHERE id = $id
    """
      .update
      .run
      .transact(xa)
      .flatMap(_ => find(id))

  override def delete(id: UUID): F[Int] =
    sql"""
        DELETE FROM recipes
        WHERE id = $id
    """
      .update
      .run
      .transact(xa)
}

object LiveRecipes {
  given recipeRead: Read[Recipe] = Read[(
    UUID,                     // id
    String,                   // ownerEmail
    String,                   // name
    Option[String],           // image
    Int,                      // servingsPerBatch
    Int,                      // minBatchesPerWeek
    Int,                      // maxBatchesPerWeek
    List[Meal],               // excludeFrom
    List[Ingredient]          // ingredients
  )].map{
    case (
      id: UUID,
      ownerEmail: String,
      name: String,
      image: Option[String],
      servingsPerBatch: Int,
      minBatchesPerWeek: Int,
      maxBatchesPerWeek: Int,
      excludeFrom: List[Meal],
      ingredients: List[Ingredient]
    ) => Recipe(
      id,
      ownerEmail,
      RecipeInfo(
        name,
        image,
        servingsPerBatch,
        minBatchesPerWeek,
        maxBatchesPerWeek,
        Nil,
        Nil
      )
    )
  }

  def apply[F[_]: MonadCancelThrow](xa: Transactor[F]): F[LiveRecipes[F]] =
    new LiveRecipes[F](xa).pure[F]
}