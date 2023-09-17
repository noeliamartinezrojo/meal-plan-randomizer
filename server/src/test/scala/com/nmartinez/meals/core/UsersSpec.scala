package com.nmartinez.meals.core

import cats.effect.*
import cats.effect.implicits.*
import cats.effect.testing.scalatest.AsyncIOSpec
import com.nmartinez.meals.UserFixture
import com.nmartinez.meals.domain.auth.User.User
import doobie.implicits.*
import doobie.postgres.implicits.*
import monocle.syntax.all.*
import org.postgresql.util.PSQLException
import org.scalatest.freespec.AsyncFreeSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.Inside
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger

class UsersSpec extends AsyncFreeSpec with AsyncIOSpec with Matchers with Inside with DoobieSpec with UserFixture {

  override val initScript: String = "sql/users.sql"
  given logger: Logger[IO] = Slf4jLogger.getLogger[IO]

  "Users 'algebra'" - {
    "should retrieve a user by email if it exists" in {
      transactor.use { xa =>
        val program = for {
          users <- LiveUsers[IO](xa)
          result <- users.find(existingUser.email)
        } yield result

        program.asserting(_ shouldBe Option(existingUser))
      }
    }

    "should return none when trying to find a user by an email that doesn't exist" in {
      transactor.use { xa =>
        val program = for {
          users <- LiveUsers[IO](xa)
          result <- users.find(notFoundUser.email)
        } yield result

        program.asserting(_ shouldBe None)
      }
    }

    "should create a new user if the email doesn't exist" in {
      transactor.use { xa =>
        val program = for {
          users <- LiveUsers[IO](xa)
          beforeUser <- sql"SELECT * FROM users WHERE email = ${newUser.email}"
            .query[User].option.transact(xa)
          result <- users.create(newUser)
          afterUser <- sql"SELECT * FROM users WHERE email = ${newUser.email}"
            .query[User].option.transact(xa)
        } yield (beforeUser, result, afterUser)

        program.asserting { (beforeUser, result, afterUser) =>
          beforeUser shouldBe None
          result shouldBe true
          afterUser shouldBe Option(newUser)
        }
      }
    }

    "should fail creating a new user if the email already exists" in {
      transactor.use { xa =>
        val program = for {
          users <- LiveUsers[IO](xa)
          result <- users.create(existingUser).attempt // IO[Either[Throwable, Boolean]]
        } yield result

        program.asserting { outcome =>
          inside(outcome) {
            case Left(e) => e shouldBe a[PSQLException]
            case _ => fail()
          }
        }
      }
    }

    "should update any field of a user (except email) if it exists" in {
      transactor.use { xa =>
        val program = for {
          users <- LiveUsers[IO](xa)
          beforeUser <- sql"SELECT * FROM users WHERE email = ${existingUser.email}"
            .query[User].option.transact(xa)
          result <- users.update(updatedUser)
          afterUser <- sql"SELECT * FROM users WHERE email = ${existingUser.email}"
            .query[User].option.transact(xa)
        } yield (beforeUser, result, afterUser)

        program.asserting { (beforeUser, result, afterUser) =>
          beforeUser shouldBe Option(existingUser)
          result shouldBe true
          afterUser shouldBe Option(updatedUser)
        }
      }
    }

    "should not update a user that doesn't exist" in {
      transactor.use { xa =>
        val program = for {
          users <- LiveUsers[IO](xa)
          beforeUser <- sql"SELECT * FROM users WHERE email = ${notFoundUser.email}"
            .query[User].option.transact(xa)
          result <- users.update(notFoundUser)
          afterUser <- sql"SELECT * FROM users WHERE email = ${notFoundUser.email}"
            .query[User].option.transact(xa)
        } yield (beforeUser, result, afterUser)

        program.asserting { (beforeUser, result, afterUser) =>
          beforeUser shouldBe None
          result shouldBe false
          afterUser shouldBe None
        }
      }
    }

    "should delete a user by email if it exists" in {
      transactor.use { xa =>
        val program = for {
          users <- LiveUsers[IO](xa)
          beforeUser <- sql"SELECT * FROM users WHERE email = ${existingUser.email}"
            .query[User].option.transact(xa)
          result <- users.delete(existingUser.email)
          afterUser <- sql"SELECT * FROM users WHERE email = ${existingUser.email}"
            .query[User].option.transact(xa)
        } yield (beforeUser, result, afterUser)

        program.asserting { (beforeUser, result, afterUser) =>
          beforeUser shouldBe Option(existingUser)
          result shouldBe true
          afterUser shouldBe None
        }
      }
    }

    "should not delete any user if the email doesn't exist" in {
      transactor.use { xa =>
        val program = for {
          users <- LiveUsers[IO](xa)
          beforeUser <- sql"SELECT * FROM users WHERE email = ${notFoundUser.email}"
            .query[User].option.transact(xa)
          result <- users.delete(notFoundUser.email)
          afterUser <- sql"SELECT * FROM users WHERE email = ${notFoundUser.email}"
            .query[User].option.transact(xa)
        } yield (beforeUser, result, afterUser)

        program.asserting { (beforeUser, result, afterUser) =>
          beforeUser shouldBe None
          result shouldBe false
          afterUser shouldBe None
        }
      }
    }
  }
}
