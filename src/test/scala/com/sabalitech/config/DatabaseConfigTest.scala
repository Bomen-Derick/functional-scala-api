package com.sabalitech.config

import com.typesafe.config._
import com.sabalitech.config.DatabaseConfigGenerators._
import eu.timepit.refined.auto._
import pureconfig.loadConfig
import com.sabalitech.BaseSpec

/**
  * Created by Bomen Derick.
  */
class DatabaseConfigTest extends BaseSpec{
  "DatabaseConfig" when {
    "loading invalid config format" must {
      "fail" in {
        val config = ConfigFactory.parseString("{}")
        loadConfig[DatabaseConfig](config, "database") match {
          case Left(_) => succeed
          case Right(_) => fail("Loading an invalid config must fail!")
        }
      }
    }

    "loading valid config format" when {
      "settings are invalid" must {
        "fail" in {
          forAll("input") { i: Int =>
            val config = ConfigFactory.parseString(
              """database {
                |  "driver":"",
                |  "url":"",
                |  "user": "",
                |  "pass": ""
                |}""".stripMargin
            )
            loadConfig[DatabaseConfig](config, "database") match {
              case Left(_) => succeed
              case Right(_) => fail("Loading a config with invalid settings must fail!")
            }
          }
        }
      }

      "settings are valid" must {
        "load correct settings" in {
          forAll("input") { expected: DatabaseConfig =>
            val config = ConfigFactory.parseString(
              s"""database {
                 |  "driver": "${expected.driver}",
                 |  "url": "${expected.url}",
                 |  "user": "${expected.user}",
                 |  "pass": "${expected.password}"
                 |}""".stripMargin
            )
            loadConfig[DatabaseConfig](config, "database") match {
              case Left(e) => fail(s"Parsing a valid configuration must succeed! ($e)")
              case Right(c) => c must be(expected)
            }
          }
        }
      }
    }
  }
}
