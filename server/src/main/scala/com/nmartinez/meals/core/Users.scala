package com.nmartinez.meals.core

import com.nmartinez.meals.domain.User.*
import cats.*
import cats.data.NonEmptyList
import cats.effect.*
import cats.effect.implicits.*
import cats.implicits.*
import doobie.*
import doobie.implicits.*
import doobie.postgres.*
import doobie.postgres.implicits.*
import doobie.util.*

trait Users[F[_]: MonadCancelThrow] {
  // CRUD
  def find(email: String): F[Option[User]]
  def create(user: User): F[Boolean]
  def update(user: User): F[Boolean]
  def delete(email: String): F[Boolean]
}

final class LiveUsers[F[_]: MonadCancelThrow] private (xa: Transactor[F]) extends Users[F] {
  override def find(email: String): F[Option[User]] =
    sql"""SELECT email, hashedPassword, firstName, lastName, role
          FROM users
          WHERE email = $email"""
      .query[User]
      .option
      .transact(xa)

  override def create(user: User): F[Boolean] =
    sql"""INSERT INTO users(email, hashedPassword, firstName, lastName, role)
          VALUES(${user.email}, ${user.hashedPassword}, ${user.firstName}, ${user.lastName}, ${user.role})"""
      .update.run.transact(xa)
      .map(_ > 0)

  override def update(user: User): F[Boolean] =
    sql"""
      UPDATE users
      SET
        email = ${user.email},
        hashedPassword = ${user.hashedPassword},
        firstName = ${user.firstName},
        lastName = ${user.lastName},
        role = ${user.role}
      WHERE email = ${user.email}
    """
      .update.run.transact(xa)
      .map(_ > 0)

  override def delete(email: String): F[Boolean] =
    sql"""DELETE FROM users WHERE email = $email"""
      .update.run.transact(xa)
      .map(_ > 0)
}

object LiveUsers {
  def apply[F[_]: MonadCancelThrow](xa: Transactor[F]): F[LiveUsers[F]] =
    new LiveUsers[F](xa).pure[F]

  given userRead: Read[User] =
    Read[(String, String, Option[String], Option[String], Role)]
      .map { (email, hashedPassword, firstName, lastName, role) =>
        User(email, hashedPassword, firstName, lastName, role)
      }
}