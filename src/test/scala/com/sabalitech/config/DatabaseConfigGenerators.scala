package com.sabalitech.config

import com.sabalitech.DatabasePassword
import eu.timepit.refined.api.RefType
import eu.timepit.refined.auto._
import org.scalacheck.{Arbitrary, Gen}
/**
  * Created by Bomen Derick.
  */
object DatabaseConfigGenerators {
  val DefaultPassword: DatabasePassword = "secret"

  val genDatabaseConfig: Gen[DatabaseConfig] = for {
    gp <- Gen.nonEmptyListOf(Gen.alphaNumChar)
    p = RefType.applyRef[DatabasePassword](gp.mkString).getOrElse(DefaultPassword)
  } yield DatabaseConfig(
    driver = "org.postgresql.Driver",
    url = "jdbc:postgresql://localhost:5422/test-database",
    user = "pure",
    password = p
  )

  implicit val arbitraryDatabaseConfig: Arbitrary[DatabaseConfig] = Arbitrary(genDatabaseConfig)

}
