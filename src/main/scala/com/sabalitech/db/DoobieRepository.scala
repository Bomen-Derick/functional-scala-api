package com.sabalitech.db

import cats.effect.Sync
import cats.implicits.{catsKernelStdOrderForTuple3, catsKernelStdOrderForUUID}
import com.sabalitech.models._
import doobie.implicits._
import doobie.util.transactor.Transactor
import doobie.util.update._
import doobie.postgres.implicits._
import doobie.refined.implicits._
import eu.timepit.refined.auto._

/**
  * Created by Bomen Derick.
  */
class DoobieRepository[F[_]: Sync](tx: Transactor[F]) extends Repository[F] {
  override def loadProduct(id: ProductId): F[Seq[(ProductId, LanguageCode, ProductName)]] =
    sql"""SELECT products.id, names.lang_code, names.name
            FROM products
            JOIN names ON products.id = names.product_id
            WHERE products.id = $id"""
      .query[(ProductId, LanguageCode, ProductName)]
      .to[Seq]
      .transact(tx)

  override def loadProducts(): fs2.Stream[F, (ProductId, LanguageCode, ProductName)] =
    sql"""SELECT products.id, names.lang_code, names.name
            FROM products
            JOIN names ON products.id = names.product_id
            ORDER BY products.id"""
      .query[(ProductId, LanguageCode, ProductName)]
      .stream
      .transact(tx)

  override def saveProduct(p: Product): F[Int] = {
    val namesSql =
      "INSERT INTO names (product_id, lang_code, name) VALUES (?, ?, ?)"
    val namesValues = p.names.map(t => (p.id, t.lang, t.name))
    val program = for {
      pi <- sql"INSERT INTO products (id) VALUES(${p.id})".update.run
      ni <- Update[(ProductId, LanguageCode, ProductName)](namesSql)
        .updateMany(namesValues)
    } yield pi + ni
    program.transact(tx)
  }

  override def updateProduct(p: Product): F[Int] = {
    val namesSql =
      "INSERT INTO names (product_id, lang_code, name) VALUES (?, ?, ?)"
    val namesValues = p.names.map(t => (p.id, t.lang, t.name))
    val program = for {
      dl <- sql"DELETE FROM names WHERE product_id = ${p.id}".update.run
      ts <- Update[(ProductId, LanguageCode, ProductName)](namesSql)
        .updateMany(namesValues)
    } yield dl + ts
    program.transact(tx)
  }
}
