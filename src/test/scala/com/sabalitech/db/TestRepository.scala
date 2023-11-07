package com.sabalitech.db

import cats.effect._
import cats.implicits._
import com.sabalitech.models._
import fs2.Stream

//import scala.collection.immutable._
/**
  * Created by Bomen Derick.
  */
class TestRepository[F[_]: Effect](data: Seq[Product]) extends Repository[F]{
  override def loadProduct(id: ProductId): F[Seq[(ProductId, LanguageCode, ProductName)]] = {
    val empt = Seq.empty[String]
    data.find(_.id === id) match {
      case None => Seq.empty[(ProductId, LanguageCode, ProductName)].pure[F]
      case Some(p) =>
        val ns = p.names.toNonEmptyList.toList.to[Seq]
        ns.map(n =>(p.id, n.lang, n.name)).pure[F]
//        ns.map(n => (p.id, n.lang, n.name)).pure[F]
    }
  }

  override def loadProducts(): Stream[F, (ProductId, LanguageCode, ProductName)] = {
    val rows = data.flatMap { p =>
      val ns = p.names.toNonEmptyList.toList.to[Seq]
      ns.map(n => (p.id, n.lang, n.name))
    }
    Stream.emits(rows)
  }

  override def saveProduct(p: Product): F[Int] =
    data.find(_.id === p.id).fold(0.pure[F])(_ => 1.pure[F])

  override def updateProduct(p: Product): F[Int] =
    data.find(_.id === p.id).fold(0.pure[F])(_ => 1.pure[F])

}
