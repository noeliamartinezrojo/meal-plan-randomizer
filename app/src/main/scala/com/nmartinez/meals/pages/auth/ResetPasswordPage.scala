package com.nmartinez.meals.pages.auth

import cats.effect.IO
import com.nmartinez.meals.pages.Page
import tyrian.*
import tyrian.Html.*

final case class ResetPasswordPage() extends Page {
  override def initCmd: Cmd[IO, Page.Msg] =
    Cmd.None

  override def update(msg: Page.Msg): (Page, Cmd[IO, Page.Msg]) =
    (this, Cmd.None)

  override def view(): Html[Page.Msg] =
    div("Reset password page - TODO")
}