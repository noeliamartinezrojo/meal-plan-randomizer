package com.nmartinez.mealplanrandomizer.domain

import com.nmartinez.mealplanrandomizer.domain.*
import io.circe.*, io.circe.generic.semiauto.*
import cats.implicits.*
import cats.*

case class Ingredient(
                       `type`: IngredientType,
                       name: String,
                       unit: IngredientUnit,
                       qty: Double
                     )
object Ingredient {

  implicit val ingredientDecoder: Decoder[Ingredient] = deriveDecoder[Ingredient]
  implicit val ingredientEncoder: Encoder[Ingredient] = deriveEncoder[Ingredient]

  implicit val ingredientListMonoid: Monoid[List[Ingredient]] = new Monoid[List[Ingredient]] {
    override def combine(first: List[Ingredient], second: List[Ingredient]): List[Ingredient] = {
      combineTailrec(first ++ second, Map.empty)
    }

    override def empty: List[Ingredient] = Nil

    private def combineTailrec(remaining: List[Ingredient], acc: Map[(IngredientType, String, IngredientUnit), Double]): List[Ingredient] = {
      if (remaining.isEmpty)
        acc.map(kv => {
          val ((ingrType, name, unit), qty) = kv
          Ingredient(ingrType, name.capitalize, unit, qty)
        }).toList
      else {
        val key = (remaining.head.`type`, remaining.head.name.toLowerCase, remaining.head.unit)
        combineTailrec(remaining.tail, acc + (key -> (acc.getOrElse(key, 0.0) + remaining.head.qty)))
      }
    }
  }
}
