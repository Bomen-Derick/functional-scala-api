package com.sabalitech.config

import com.typesafe.config._
import com.sabalitech.BaseSpec
import com.sabalitech.config.ApiConfigGenerators._
import eu.timepit.refined.auto._
import pureconfig.loadConfig

/**
  * Created by Bomen Derick.
  */
class ApiConfigTest extends BaseSpec {
  "ApiConfig" when {
    "loading invalid config format" must {
      "fail" in {
        val config = ConfigFactory.parseString("{}")
        loadConfig[ApiConfig](config, "api") match {
          case Left(_) => succeed
          case Right(_) => fail("Loading an invalid config must fail!")
        }
      }
    }

    "loading valid config format" when {
      "settings are invalid" must {
        "fail" in {
          forAll("port") { i: Int =>
            whenever(i < 1 || i > 65535) {
              val config = ConfigFactory.parseString(s"""api{"host":"","port":$i}""")
              loadConfig[ApiConfig](config, "api") match {
                case Left(_) => succeed
                case Right(_) => fail("Loading a config with invalid settings must fail!")
              }
            }
          }
        }
      }

      "settings are valid" must {
        "load correct settings" in {
          forAll("input") { expected: ApiConfig =>
            val config =
              ConfigFactory.parseString(
                s"""api{"host":"${expected.host}","port":${expected.port}}"""
              )
            loadConfig[ApiConfig](config, "api") match {
              case Left(e) => fail(s"Parsing a valid configuration must succeed! ($e)")
              case Right(c) => c must be(expected)
            }
          }
        }
      }
    }
  }
}
