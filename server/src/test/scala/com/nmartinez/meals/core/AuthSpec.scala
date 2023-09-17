package com.nmartinez.meals.core

import cats.effect.*
import cats.effect.implicits.*
import cats.effect.testing.scalatest.AsyncIOSpec
import com.nmartinez.meals.UserFixture
import com.nmartinez.meals.domain.auth.User.*
import com.nmartinez.meals.domain.auth.Security.*
import doobie.implicits.*
import doobie.postgres.implicits.*
import monocle.syntax.all.*
import org.postgresql.util.PSQLException
import org.scalatest.Inside
import org.scalatest.freespec.AsyncFreeSpec
import org.scalatest.matchers.should.Matchers
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger
import tsec.authentication.{Authenticator, JWTAuthenticator, IdentityStore}
import tsec.mac.jca.HMACSHA256
import cats.data.*
import scala.concurrent.duration.*

class AuthSpec extends AsyncFreeSpec with AsyncIOSpec with Matchers with Inside with UserFixture {
  given logger: Logger[IO] = Slf4jLogger.getLogger[IO]

  private val mockUsers = new Users[IO] {
    override def find(email: String): IO[Option[User]] =
      if (email == existingUser.email) IO(Option(existingUser)) else IO(None)

    override def create(user: User): IO[Boolean] = IO(true)

    override def update(user: User): IO[Boolean] = IO(true)

    override def delete(email: String): IO[Boolean] = IO(true)
  }

  private val mockAuthenticator: JwtAuthenticator[IO] = {
    val key = HMACSHA256.unsafeGenerateKey

    val idStore: IdentityStore[IO, String, User] = (email: String) =>
      if (email == existingUser.email) OptionT.pure(existingUser)
      else OptionT.none[IO, User]

    JWTAuthenticator.unbacked.inBearerToken(
      1.day,    // expiration of tokens
      None,     // max idle time
      idStore,  // identity store (gets users by email)
      key       // key to hash password
    )
  }

  "Auth 'algebra'" - {
    "login should return None if the user doesn't exist" in {
      val program = for {
        auth <- LiveAuth[IO](mockUsers, mockAuthenticator)
        maybeToken <- auth.login(notFoundUser.email, notFoundUser.hashedPassword)
      } yield maybeToken
      program.asserting(_ shouldBe None)
    }

    "login should return None if the user exists but the password is wrong" in {
      val program = for {
        auth <- LiveAuth[IO](mockUsers, mockAuthenticator)
        maybeToken <- auth.login(existingUser.email, "wrongpassword")
      } yield maybeToken
      program.asserting(_ shouldBe None)
    }

    "login should return a token if the user exists and the password is correct" in {
      val program = for {
        auth <- LiveAuth[IO](mockUsers, mockAuthenticator)
        maybeToken <- auth.login(existingUser.email, "password")
      } yield maybeToken
      program.asserting(_ shouldBe defined)
    }
  }
}
