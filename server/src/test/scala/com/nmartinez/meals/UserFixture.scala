package com.nmartinez.meals

import com.nmartinez.meals.domain.auth.User.*

trait UserFixture {

  val existingUser = User(
    "noeliamtzrojo@gmail.com",
    "$2a$10$/yKAcFyVz8njqqoZE4u4Z.55I01DZXfWTymJrtZwkwJo28x89jRUy", // "password"
    Option("Noelia"),
    Option("Martinez Rojo"),
    Role.ADMIN
  )

  val updatedUser = existingUser.copy(
    hashedPassword = "updatedpassword",
    firstName = Option("NOELIA"),
    lastName = Option("MARTINEZ ROJO"),
    role = Role.CLIENT
  )

  val notFoundUser = User(
    "notfound@email.com",
    "notfoundpassword",
    None,
    None,
    Role.CLIENT
  )

  val newUser = User(
    "newuser@email.com",
    "newpassword",
    Option("Jane"),
    Option("Smith"),
    Role.CLIENT
  )
}