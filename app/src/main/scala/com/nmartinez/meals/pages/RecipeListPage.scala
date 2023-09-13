package com.nmartinez.meals.pages

import tyrian.*
import tyrian.Html.*
import cats.effect.IO

final case class RecipeListPage() extends Page {
  override def initCmd: Cmd[IO, Page.Msg] =
    Cmd.None

  override def update(msg: Page.Msg): (Page, Cmd[IO, Page.Msg]) =
    (this, Cmd.None)

  override def view(): Html[Page.Msg] =
    div("Recipe list page - TODO")
}
