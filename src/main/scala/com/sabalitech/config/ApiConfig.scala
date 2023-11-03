package com.sabalitech.config

import com.sabalitech.{NonEmptyString, PortNumber}
import pureconfig._
import pureconfig.generic.semiauto._
import eu.timepit.refined.auto._
import eu.timepit.refined.pureconfig._

/**
  * Created by Bomen Derick.
  */
final case class ApiConfig (host: NonEmptyString, port: PortNumber)

object ApiConfig {
  implicit val configReader: ConfigReader[ApiConfig] = deriveReader
}
