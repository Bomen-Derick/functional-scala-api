package com.sabalitech.models

import cats.Order
import cats.derived
import io.circe.generic.semiauto._
import io.circe.{Decoder, Encoder}
import io.circe.refined._

/**
  * Created by Bomen Derick.
  */
final case class Translation (lang: LanguageCode, name: ProductName)

object Translation {
  implicit val decode: Decoder[Translation] = deriveDecoder[Translation]
  implicit val encode: Encoder[Translation] = deriveEncoder[Translation]
  implicit val order: Order[Translation] = {
    import derived.auto.order._
    derived.semi.order[Translation]
  }
}