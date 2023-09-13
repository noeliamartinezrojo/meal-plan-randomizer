package com.nmartinez.meals.http.routes

import io.circe.generic.auto.*
import org.http4s.circe.*
import org.http4s.circe.CirceEntityCodec.*
import java.util.UUID
import scala.collection.{Iterable, mutable}
import cats.*
import cats.data.Validated
import cats.implicits.*
import cats.effect.Concurrent
import com.nmartinez.meals.core.Recipes
import org.http4s.{HttpRoutes, ParseFailure, QueryParamDecoder}
import org.http4s.dsl.{&, Http4sDsl}
import org.http4s.server.Router
import org.typelevel.log4cats.Logger
import com.nmartinez.meals.domain.*
import com.nmartinez.meals.domain.Ingredient.*
import com.nmartinez.meals.http.responses.*
import com.nmartinez.meals.logging.Syntax.*
import com.nmartinez.meals.http.validation.Syntax.*

class RecipeRoutes[F[_]: Concurrent: Logger] private (recipes: Recipes[F]) extends HttpValidationDsl[F] {

  // POST /recipes?offset=x&limit=y { filters }
  // TODO add query params and filters
  private val allRecipesRoute: HttpRoutes[F] = HttpRoutes.of[F] {
    case POST -> Root =>
      for {
        recipes <- recipes.all()
        resp <- Ok(recipes)
      } yield resp
  }

  // GET /recipes/uuid
  private val findRecipeRoute: HttpRoutes[F] = HttpRoutes.of[F] {
    case GET -> Root / UUIDVar(id) =>
      recipes.find(id).flatMap {
        case Some(recipe) => Ok(recipe)
        case None => NotFound(FailureResponse("recipe not found"))
      }
  }

  // POST /recipes { recipeInfo }
  private val createRecipeRoute: HttpRoutes[F] = HttpRoutes.of[F] {
    case req@POST -> Root / "create" =>
      req.validate[RecipeInfo] { recipeInfo =>
        for {
          _ <- Logger[F].info(s"Parsed recipe info: $recipeInfo")
          id <- recipes.create("TODO@gmail.com", recipeInfo)
          resp <- Created(id)
        } yield resp
      }
  }

  // PUT /recipes/uuid { recipeInfo }
  private val updateRecipeRoute: HttpRoutes[F] = HttpRoutes.of[F] {
    case req @ PUT -> Root / UUIDVar(id) =>
      req.validate[RecipeInfo] { recipeInfo =>
        for {
          _ <- Logger[F].info(s"Parsed recipe info: $recipeInfo")
          maybeUpdatedRecipe <- recipes.update(id, recipeInfo)
          resp <- maybeUpdatedRecipe match {
            case Some(recipe) => Ok(s"updated ${recipe.id}")
            case None => NotFound(FailureResponse(s"cannot update recipe $id: not found"))
          }
        } yield resp
      }
  }

  // DELETE /recipes/uuid
  private val deleteRecipeRoute: HttpRoutes[F] = HttpRoutes.of[F] {
    case req @ DELETE -> Root / UUIDVar(id) =>
      recipes.find(id).flatMap {
        case Some(recipe) =>
          for {
            _ <- recipes.delete(recipe.id)
            resp <- Ok(s"deleted ${recipe.id}")
          } yield resp
        case None => NotFound(FailureResponse(s"cannot delete recipe $id: not found"))
      }
  }

  val routes = Router(
    "/recipes" -> (
      allRecipesRoute <+>
      findRecipeRoute <+>
      createRecipeRoute <+>
      updateRecipeRoute <+>
      deleteRecipeRoute
    )
  )
}

object RecipeRoutes {
  def apply[F[_]: Concurrent: Logger](recipes: Recipes[F]) = new RecipeRoutes[F](recipes)
}
