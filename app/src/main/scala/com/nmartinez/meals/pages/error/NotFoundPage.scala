package com.nmartinez.meals.pages.error

import cats.effect.IO
import com.nmartinez.meals.pages.Page
import tyrian.*
import tyrian.Html.*

final case class NotFoundPage() extends Page {
  override def initCmd: Cmd[IO, Page.Msg] =
    Cmd.None

  override def update(msg: Page.Msg): (Page, Cmd[IO, Page.Msg]) =
    (this, Cmd.None)

  override def view(): Html[Page.Msg] =
    div("Oopsie! This page doesn't exist.")
}