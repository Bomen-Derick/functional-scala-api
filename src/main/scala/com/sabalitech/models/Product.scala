package com.sabalitech.models

import cats.Order
import cats.data.NonEmptySet
import cats.implicits._
//import cats.derived
import io.circe._
import io.circe.generic.semiauto._
import eu.timepit.refined.auto._

/**
  * Created by Bomen Derick.
  */
final case class Product (id: ProductId, names: NonEmptySet[Translation])

object Product {
  implicit val decode: Decoder[Product] = deriveDecoder
  implicit val encode: Encoder[Product] = deriveEncoder
//  implicit val order: Order[Product] = derived.semi.order[Product]

    implicit val order: Order[Product] = (x: Product, y: Product) => x.id.compare(y.id)

  /**
   * Try to create a Product from the given list of database rows.
   *
   * @param rows The database rows describing a product and its translations.
   * @return An option to the successfully created Product.
   */
  def fromDatabase(rows: Seq[(ProductId, LanguageCode, ProductName)]): Option[Product] = {
    val product = for {
      (id, c, n) <- rows.headOption
      t = Translation(lang = c, name =  n)
      p <- Product(id = id, names = NonEmptySet.one(t)).some
    } yield p
    product.map(
      p =>
        rows.drop(1).foldLeft(p) { (a, cols) =>
          val (_, c, n) = cols
          a.copy(names = a.names.add(Translation(lang = c, name = n)))
        }
    )
  }
}
