package com.nmartinez.mealplanrandomizer.http.routes

import io.circe.generic.auto.*
import org.http4s.circe.*
import org.http4s.circe.CirceEntityCodec.*
import com.nmartinez.mealplanrandomizer.domain.*
import com.nmartinez.mealplanrandomizer.RecipeFixture
import com.nmartinez.mealplanrandomizer.dao.Recipes
import monocle.syntax.all.*
import org.scalatest.matchers.*
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger
import cats.effect.*
import cats.implicits.*
import org.scalatest.matchers.should.Matchers
import org.scalatest.freespec.AsyncFreeSpec
import cats.effect.testing.scalatest.AsyncIOSpec
import org.http4s.dsl.{io, *}
import org.http4s.*
import org.http4s.implicits.*
import java.util.UUID

class RecipesRoutesSpec
    extends AsyncFreeSpec
    with AsyncIOSpec
    with Matchers
    with Http4sDsl[IO]
    with RecipeFixture {

  val mockRecipes: Recipes[IO] = new Recipes[IO] {
    override def create(ownerEmail: String, recipeInfo: RecipeInfo): IO[UUID] =
      IO.pure(newRecipeUUID)

    override def all(): IO[List[Recipe]] =
      IO.pure(List(testRecipe))

    override def find(id: UUID): IO[Option[Recipe]] =
      if (id == testRecipeUUID)
        IO.pure(Option(testRecipe))
      else
        IO.pure(None)

    override def update(id: UUID, recipeInfo: RecipeInfo): IO[Option[Recipe]] =
      if (id == testRecipeUUID)
        IO.pure(Option(updatedTestRecipe))
      else
        IO.pure(None)

    override def delete(id: UUID): IO[Int] =
      if (id == testRecipeUUID)
        IO.pure(1)
      else
        IO.pure(0)
  }
  given logger: Logger[IO] = Slf4jLogger.getLogger[IO]
  val recipeRoutes: HttpRoutes[IO] = RecipeRoutes[IO](mockRecipes).routes

  "RecipeRoutes" - {
    "should only return a recipe with a given id when it exists" in {
      for {
        responseOk <- recipeRoutes.orNotFound.run(
          Request(Method.GET, uri"/recipes/11111111-2222-3333-4444-000000000001")
        )
        responseInvalid <- recipeRoutes.orNotFound.run(
          Request(Method.GET, uri"/recipes/11111111-2222-3333-4444-00000000000x")
        )
        retrieved <- responseOk.as[Recipe]
      } yield {
        responseOk.status shouldBe Status.Ok
        retrieved shouldBe testRecipe
        responseInvalid.status shouldBe Status.NotFound
      }
    }

    "should return all recipes" in {
      for {
        response <- recipeRoutes.orNotFound.run(
          Request(Method.POST, uri"/recipes")
        )
        retrieved <- response.as[List[Recipe]]
      } yield {
        response.status shouldBe Status.Ok
        retrieved shouldBe List(testRecipe)
      }
    }

    "should create a new recipe" in {
      for {
        response <- recipeRoutes.orNotFound.run(
          Request(Method.POST, uri"/recipes/create")
            .withEntity(testRecipe.recipeInfo)
        )
        retrieved <- response.as[UUID]
      } yield {
        response.status shouldBe Status.Created
        retrieved shouldBe newRecipeUUID
      }
    }

    "should only update a recipe that exists" in {
      for {
        responseOk <- recipeRoutes.orNotFound.run(
          Request(Method.PUT, uri"/recipes/11111111-2222-3333-4444-000000000001")
            .withEntity(updatedTestRecipe.recipeInfo)
        )
        responseInvalid <- recipeRoutes.orNotFound.run(
          Request(Method.PUT, uri"/recipes/11111111-2222-3333-4444-00000000000x")
            .withEntity(updatedTestRecipe.recipeInfo)
        )
      } yield {
        responseOk.status shouldBe Status.Ok
        responseInvalid.status shouldBe Status.NotFound
      }
    }

    "should only delete a recipe that exists" in {
      for {
        responseOk <- recipeRoutes.orNotFound.run(
          Request(Method.DELETE, uri"/recipes/11111111-2222-3333-4444-000000000001")
        )
        responseInvalid <- recipeRoutes.orNotFound.run(
          Request(Method.DELETE, uri"/recipes/11111111-2222-3333-4444-00000000000x")
        )
      } yield {
        responseOk.status shouldBe Status.Ok
        responseInvalid.status shouldBe Status.NotFound
      }
    }
  }
}
