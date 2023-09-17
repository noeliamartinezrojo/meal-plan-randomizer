package com.nmartinez.meals.playground

import cats.effect.*
import pureconfig.ConfigSource
import tsec.passwordhashers.PasswordHash
import tsec.passwordhashers.jca.BCrypt

object PasswordHashingPlayground extends IOApp.Simple {
  override def run: IO[Unit] =
    BCrypt.hashpw[IO]("password").flatMap(IO.println)
}
