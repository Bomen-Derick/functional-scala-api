package com.sabalitech.db

import cats.effect._
import com.sabalitech.BaseItSpec
import com.sabalitech.config._
import eu.timepit.refined.auto._
import org.flywaydb.core.Flyway
import org.flywaydb.core.api.FlywayException
/**
  * Created by Bomen Derick.
  */
class FlywayDatabaseMigrationTest extends BaseItSpec {
  override def beforeEach(): Unit = {
    dbConfig.foreach { cfg =>
      val flyway: Flyway = Flyway.configure().dataSource(cfg.url, cfg.user, cfg.password).load()
      val _ = flyway.migrate()
      flyway.clean()
    }
  }

  override def afterEach(): Unit = {
    dbConfig.foreach { cfg =>
      val flyway: Flyway = Flyway.configure().dataSource(cfg.url, cfg.user, cfg.password).load()
      flyway.clean()
    }
  }

  "FlywayDatabaseMigrator#migrate" when {
    "the database is configured and available" when {
      "the database is not up to date" must {
        "return the number of applied migrations" in {
          dbConfig.map { cfg =>
            val migrator: DatabaseMigrator[IO] = new FlywayDatabaseMigrator
            val program = migrator.migrate(cfg.url, cfg.user, cfg.password)
            program.unsafeRunSync must be > 0
          }
        }
      }

      "the database is up to date" must {
        "return zero" in {
          dbConfig.map { cfg =>
            val migrator: DatabaseMigrator[IO] = new FlywayDatabaseMigrator
            val program = migrator.migrate(cfg.url, cfg.user, cfg.password)
            val _ = program.unsafeRunSync
            program.unsafeRunSync must be(0)
          }
        }
      }
    }

    "the database is not available" must {
      "throw an exception" in {
        val cfg = DatabaseConfig(
          driver = "This is no driver name!",
          url = "jdbc://some.host/whatever",
          user = "no-user",
          password = "no-password"
        )
        val migrator: DatabaseMigrator[IO] = new FlywayDatabaseMigrator
        val program = migrator.migrate(cfg.url, cfg.user, cfg.password)
        an[FlywayException] must be thrownBy program.unsafeRunSync
      }
    }
  }
}
