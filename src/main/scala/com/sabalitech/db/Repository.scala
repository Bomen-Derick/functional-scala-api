package com.sabalitech.db

import com.sabalitech.models._
import fs2.Stream

/**
  * Created by Bomen Derick.
  */
trait Repository[F[_]] {
  def loadProduct(id: ProductId): F[Seq[(ProductId, LanguageCode, ProductName)]]
  def loadProducts(): Stream[F, (ProductId, LanguageCode, ProductName)]
  def saveProduct(p: Product): F[Int]
  def updateProduct(p: Product): F[Int]
}
