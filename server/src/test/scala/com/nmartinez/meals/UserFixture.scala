package com.nmartinez.meals

import com.nmartinez.meals.domain.User.*

trait UserFixture {

  val adminUser = User(
    "noeliamtzrojo@gmail.com",
    "password",
    Option("Noelia"),
    Option("Martinez Rojo"),
    Role.ADMIN
  )

  val clientUser = User(
    "janesmith@fakeuser.com",
    "password",
    Option("Jane"),
    Option("Smith"),
    Role.CLIENT
  )

  val updatedClientUser = clientUser.copy(
    hashedPassword = "newpassword",
    firstName = Option("New Jane"),
    lastName = Option("New Smith"),
    role = Role.ADMIN
  )

  val newUser = User(
    "newuser@email.com",
    "password",
    None,
    None,
    Role.CLIENT
  )

  val notFoundEmail = "notfound@email.com"

  val notFoundUser = User(
    notFoundEmail,
    "password",
    None,
    None,
    Role.CLIENT
  )
}