package com.nmartinez.meals

import cats.effect.*
import com.nmartinez.meals.core.*
import com.nmartinez.meals.components.*
import com.nmartinez.meals.pages.*
import org.scalajs.dom.window
import tyrian.*
import tyrian.Html.*
import tyrian.cmds.Logger
import scala.concurrent.duration.*
import scala.scalajs.js.annotation.*
import snabbdom.init

object App {
  type Msg = Router.Msg | Page.Msg
  case class Model(router: Router, page: Page)
}

@JSExportTopLevel("MealPlanRandomizerApp")
class App extends TyrianApp[App.Msg, App.Model] {
  import App.*

  override def init(flags: Map[String, String]): (Model, Cmd[IO, Msg]) = {
    val location = window.location.pathname
    val page = Page.get(location)
    val pageCmd = page.initCmd
    val (router, routerCmd) = Router.startAt(location)
    (Model(router, page), routerCmd |+| pageCmd)
  }

  override def subscriptions(model: Model): Sub[IO, Msg] =
    Sub.make( // listener for browser history changes
      "urlChange",
      model.router.history.state.discrete // stream of locations
        .map(_.get)
        .map(newLocation => Router.ChangeLocation(newLocation, true)
      )
    )

  override def update(model: Model): Msg => (Model, Cmd[IO, Msg]) = {
    case msg: Router.Msg =>
      val (newRouter, routerCmd) = model.router.update(msg)
      if (model.router == newRouter)
        (model, Cmd.None) // no page changes
      else {
        val newPage = Page.get(newRouter.location)
        val newPageCmd = newPage.initCmd
        (model.copy(router = newRouter, page = newPage), routerCmd |+| newPageCmd)
      }
    case msg: Page.Msg =>
      val (newPage, cmd) = model.page.update(msg)
      (model.copy(page = newPage), cmd)
  }

  override def view(model: Model): Html[Msg] =
    div(
      Header.view(),
      model.page.view()
    )
}