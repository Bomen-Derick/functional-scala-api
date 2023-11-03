package com.sabalitech.api

import cats.Applicative
import cats.effect.Sync
import cats.implicits._
import com.sabalitech.db.Repository
import com.sabalitech.models.Product
import fs2.Stream
import io.circe.syntax.EncoderOps
import org.http4s._
import org.http4s.circe.CirceEntityCodec.circeEntityEncoder
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl
import eu.timepit.refined.auto._


/**
 * Created by Bomen Derick.
 */
class Routes[F[_] : Sync](repo: Repository[F]) extends Http4sDsl[F] {
  implicit def decodeProduct: EntityDecoder[F, Product] = jsonOf

  implicit def encodeProduct[A[_] : Applicative]: EntityEncoder[A, Product] = jsonEncoderOf

  val routes: HttpRoutes[F] = HttpRoutes.of[F] {
    // load a product
    case GET -> Root / "product" / UUIDVar(id) =>
      for {
        rows <- repo.loadProduct(id)
        res <- Ok(Product.fromDatabase(rows))
      } yield res

    // update a product
    case req@PUT -> Root / "product" / UUIDVar(_) =>
      for {
        product <- req.as[Product]
        _ <- repo.updateProduct(product)
        res <- NoContent()
      } yield res

    // load all products
    case GET -> Root / "products" =>
      val prefix: Stream[F, String] = Stream.eval("[".pure[F])
      val suffix: Stream[F, String] = Stream.eval("]".pure[F])
      val products = repo.loadProducts()
        .groupAdjacentBy(_._1)
        .map {
          case (_, rows) => Product.fromDatabase(rows.toList)
        }
        .collect {
          case Some(p) => p
        }
        .map(_.asJson.noSpaces)
        .intersperse(",")
      @SuppressWarnings(Array("org.wartremover.warts.Any"))
      val result: Stream[F, String] = prefix ++ products ++ suffix
      Ok(result)

    // create a product
    case req@POST -> Root / "products" =>
      for {
        product <- req.as[Product]
        _ <- repo.saveProduct(product)
        res <- NoContent()
      } yield res
  }

}
