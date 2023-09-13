package com.nmartinez.meals.pages

import cats.effect.IO
import tyrian.*
import tyrian.Html.*

final case class RecipePage(id: String) extends Page {
  override def initCmd: Cmd[IO, Page.Msg] =
    Cmd.None

  override def update(msg: Page.Msg): (Page, Cmd[IO, Page.Msg]) =
    (this, Cmd.None)

  override def view(): Html[Page.Msg] =
    div(s"Individual recipe page for id $id - TODO")
}