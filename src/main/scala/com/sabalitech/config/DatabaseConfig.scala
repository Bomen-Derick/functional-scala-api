package com.sabalitech.config

import com.sabalitech._
import pureconfig._
import pureconfig.generic.semiauto._
import eu.timepit.refined.auto._
import eu.timepit.refined.pureconfig._

/**
  * Created by Bomen Derick.
  */
final case class DatabaseConfig (
    driver: NonEmptyString,
    url: DatabaseUrl,
    user: DatabaseLogin,
    password: DatabasePassword
    )

object DatabaseConfig {
  implicit val configReader: ConfigReader[DatabaseConfig] = deriveReader
}
