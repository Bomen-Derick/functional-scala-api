package com.sabalitech.db

import com.sabalitech._

/**
  * Created by Bomen Derick.
  */
trait DatabaseMigrator[F[_]] {
  def migrate(
      url: DatabaseUrl,
      user: DatabaseLogin,
      password: DatabasePassword
     ): F[Int]
}
