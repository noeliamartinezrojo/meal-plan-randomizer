package com.nmartinez.meals.core

import com.nmartinez.meals.domain.auth.Security.*
import com.nmartinez.meals.domain.auth.User.*
import com.nmartinez.meals.core.Users
import cats.effect.*
import cats.implicits.*
import com.nmartinez.meals.domain.auth.*
import doobie.*
import tsec.passwordhashers.PasswordHash
import tsec.passwordhashers.jca.BCrypt

trait Auth[F[_]: MonadCancelThrow] {
  def login(email: String, password: String): F[Option[JwtToken]]
  def signUp(newUserInfo: NewUserInfo): F[Option[User]]
  def changePassword(email: String, newPasswordInfo: NewPasswordInfo): F[Either[String, Option[User]]]
}

class LiveAuth[F[_]: Async] private (users: Users[F], authenticator: JwtAuthenticator[F]) extends Auth[F] {
  override def login(email: String, password: String): F[Option[JwtToken]] = {
    for {
      // check if email exists
      maybeUser <- users.find(email)
      // check if password is correct
      maybeValidatedUser <- maybeUser.filterA(user =>
        BCrypt.checkpwBool[F](
          password, PasswordHash[BCrypt](user.hashedPassword)
        )
      )
      // return a json web token if password is correct
      maybeToken <- maybeValidatedUser.traverse(user =>
        authenticator.create(user.email)
      )
    } yield maybeToken
  }

  override def signUp(newUserInfo: NewUserInfo): F[Option[User]] = {
    None.pure[F]
  }

  override def changePassword(email: String, newPasswordInfo: NewPasswordInfo): F[Either[String, Option[User]]] = {
    Right(None).pure[F]
  }
}
object LiveAuth {
  def apply[F[_]: Async](users: Users[F], authenticator: JwtAuthenticator[F]): F[LiveAuth[F]] =
    new LiveAuth[F](users, authenticator).pure[F]
}