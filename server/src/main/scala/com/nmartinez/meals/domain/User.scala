package com.nmartinez.meals.domain

import doobie.Meta

object User {
  final case class User(
                         email: String,
                         hashedPassword: String,
                         firstName: Option[String],
                         lastName: Option[String],
                         role: Role
                       )

  enum Role {
    case ADMIN, CLIENT
  }

  object Role {
    given metaRole: Meta[Role] =
      Meta[String].timap[Role](Role.valueOf)(_.toString)
  }
}
