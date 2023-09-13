package com.nmartinez.meals.pages

import tyrian.*
import cats.effect.IO
import com.nmartinez.meals.pages.auth.*
import com.nmartinez.meals.pages.error.*
import com.nmartinez.meals.pages.*
import com.nmartinez.meals.pages.Page.URLs.RECIPES
import scala.util.matching.Regex

object Page {
  trait Msg

  object URLs {
    val EMPTY = ""
    val HOME = "/"
    val LOGIN = "/login"
    val SIGNUP = "/signup"
    val RESET_PASSWORD = "/resetpassword"
    val FORGOT_PASSWORD = "/forgotpassword"
    val RECIPES = "/recipes"
    val MEAL_PLAN = "/mealplan"
    val PREFERENCES = "/preferences"
    val SHOPPING_LIST = "/shoppinglist"
    val NOT_FOUND = "/notfound"

    val recipePattern: Regex = """/recipes/(\w+)""".r
  }

  import URLs.*
  def get(location: String) = location match {
    case LOGIN => LoginPage()
    case SIGNUP => SignUpPage()
    case RESET_PASSWORD => ResetPasswordPage()
    case FORGOT_PASSWORD => ForgotPasswordPage()
    case EMPTY | HOME | MEAL_PLAN=> MealPlanPage ()
    case RECIPES => RecipeListPage()
    case recipePattern(id) => RecipePage(id)
    case PREFERENCES => PreferencesPage()
    case SHOPPING_LIST => ShoppingListPage()
    case _ => NotFoundPage()
  }
}
abstract class Page {
  // API

  // send a command upon instantiating
  def initCmd: Cmd[IO, Page.Msg]
  def update(msg: Page.Msg): (Page, Cmd[IO, Page.Msg])
  def view(): Html[Page.Msg]
}
