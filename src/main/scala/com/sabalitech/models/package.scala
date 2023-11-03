package com.sabalitech

import cats.Order
import eu.timepit.refined.W
import eu.timepit.refined.api.{Refined, RefinedTypeOps}
import eu.timepit.refined.cats.CatsRefinedTypeOpsSyntax
import eu.timepit.refined.collection.NonEmpty
import eu.timepit.refined.string.MatchesRegex

import java.util.UUID
/**
  * Created by Bomen Derick.
  */
package object models {
  // A language code format according to ISO 639-1. Please note that this only verifies the format and not the veracity of the language code!
  type LanguageCode = String Refined MatchesRegex[W.`"^[a-z]{2}$"`.T]
  // A product id which must be a valid UUID in version 4.
  type ProductId = UUID
  // A product name must be a non-empty string.
  type ProductName = String Refined NonEmpty

  // Provides functions to create values of the refined types above from values of their base type
  object LanguageCode extends RefinedTypeOps[LanguageCode, String] with CatsRefinedTypeOpsSyntax

  object ProductName extends RefinedTypeOps[ProductName, String] with CatsRefinedTypeOpsSyntax

  // implicit ordering instances for comparison
  implicit val orderLanguageCode: Order[LanguageCode] = (x: LanguageCode, y: LanguageCode) => x.value.compare(y.value)
  implicit val orderProductName: Order[ProductName] = (x: ProductName, y: ProductName) => x.value.compare(y.value)
}
