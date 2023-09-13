package com.nmartinez.meals.components

import tyrian.*
import tyrian.Html.*
import com.nmartinez.meals.core.*
import com.nmartinez.meals.pages.*
import scala.scalajs.js
import scala.scalajs.js.annotation.*

object Header {

  // public API
  def view() =
    div(`class` := "header-container")(
      renderLogo(),
      div(`class` := "header-nav")(
        ul(`class` := "header-links")(
          renderNavLink("Recipes", Page.URLs.RECIPES),
          renderNavLink("Login", Page.URLs.LOGIN),
          renderNavLink("Sign Up", Page.URLs.SIGNUP)
        )
      )
    )

  // private API
  @js.native
  @JSImport("/static/img/logo.png", JSImport.Default)
  private val logoImage: String = js.native
  private def renderLogo() =
    a(
      href := "/",
      onEvent("click", e => {
        e.preventDefault() // to prevent reloading the page
        Router.ChangeLocation("/")
      })
    )(
      img(
        `class` := "home-logo",
        src := logoImage,
        alt := "home-logo"
      )
    )

  private def renderNavLink(text: String, location: String) =
    li(`class` := "nav-item")(
      a(
        href := location,
        `class` := "nav-link",
        onEvent("click", e => {
          e.preventDefault() // to prevent reloading the page
          Router.ChangeLocation(location)
        })
      )(text)
    )
}
