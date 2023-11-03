package com.sabalitech.db

import cats.effect.IO
import com.sabalitech._
import eu.timepit.refined.auto._
import org.flywaydb.core.Flyway

/**
  * Created by Bomen Derick.
  */
class FlywayDatabaseMigrator extends DatabaseMigrator[IO] {
  override def migrate(
    url: DatabaseUrl,
    user: DatabaseLogin,
    password: DatabasePassword): IO[Int] =
    IO {
      val flyway: Flyway =
        Flyway.configure()
          .dataSource(url, user, password)
          .load()
      flyway.migrate()
    }
}
