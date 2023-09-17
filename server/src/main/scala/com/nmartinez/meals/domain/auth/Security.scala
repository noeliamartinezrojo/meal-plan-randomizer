package com.nmartinez.meals.domain.auth

import com.nmartinez.meals.domain.auth.User.User
import tsec.authentication.{AugmentedJWT, JWTAuthenticator}
import tsec.mac.jca.HMACSHA256
import cats.effect.*

object Security {
  type EncryptionAlgorithm = HMACSHA256
  type JwtToken = AugmentedJWT[EncryptionAlgorithm, String]
  type JwtAuthenticator[F[_]] = JWTAuthenticator[F, String, User, EncryptionAlgorithm]
}
