package com.nmartinez.meals.domain.auth

import doobie.Meta

object User {
  final case class User(
                         email: String,
                         hashedPassword: String,
                         firstName: Option[String],
                         lastName: Option[String],
                         role: Role
                       )

  final case class NewUserInfo(
                                email: String,
                                password: String,
                                firstName: Option[String],
                                lastName: Option[String]
                              )

  enum Role {
    case ADMIN, CLIENT
  }

  object Role {
    given metaRole: Meta[Role] =
      Meta[String].timap[Role](Role.valueOf)(_.toString)
  }
}
