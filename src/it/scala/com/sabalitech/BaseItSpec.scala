package com.sabalitech

import com.sabalitech.config.DatabaseConfig
import com.typesafe.config.ConfigFactory
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach, MustMatchers, WordSpec}
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pureconfig.loadConfig
import eu.timepit.refined.auto._

/**
  * Created by Bomen Derick.
  */

// A base class for our integration  test
abstract class BaseItSpec extends WordSpec
  with MustMatchers
  with ScalaCheckPropertyChecks
  with BeforeAndAfterAll
  with BeforeAndAfterEach {

  protected val config = ConfigFactory.load()
  protected val dbConfig = loadConfig[DatabaseConfig](config, "database")

  override def beforeAll(): Unit = {
    val _ = withClue("Database configuration could not be loaded!") {
      dbConfig.isRight must be(true)
    }
  }
}
