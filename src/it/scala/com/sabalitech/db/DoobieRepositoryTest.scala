package com.sabalitech.db

import cats.effect._
import cats.implicits._
import com.sabalitech.BaseItSpec
import com.sabalitech.models._
import com.sabalitech.models.TypeGenerators._
import doobie._
import eu.timepit.refined.auto._
import org.flywaydb.core.Flyway

/**
  * Created by Bomen Derick.
  */
class DoobieRepositoryTest extends BaseItSpec {
  implicit val cs = IO.contextShift(ExecutionContexts.synchronous)

  override def beforeAll(): Unit = {
    super.beforeAll()
    dbConfig.foreach { cfg =>
      val flyway: Flyway = Flyway.configure().dataSource(cfg.url, cfg.user, cfg.password).load()
      val _ = flyway.migrate()
    }
  }

  override def beforeEach(): Unit = {
    dbConfig.foreach { cfg =>
      val flyway: Flyway = Flyway.configure().dataSource(cfg.url, cfg.user, cfg.password).load()
      flyway.clean()
      val _ = flyway.migrate()
    }
  }

  override def afterEach(): Unit = {
    dbConfig.foreach { cfg =>
      val flyway: Flyway = Flyway.configure().dataSource(cfg.url, cfg.user, cfg.password).load()
      flyway.clean()
    }
  }

  "DoobieRepository#loadProduct" when {
    "the product does not exist" must {
      "return an empty list of rows" in {
        dbConfig.map { c =>
          val tx = Transactor
            .fromDriverManager[IO](c.driver, c.url, c.user, c.password)
          val repo = new DoobieRepository(tx)
          forAll("ID") { id: ProductId =>
            for {
              rows <- repo.loadProduct(id)
            } yield {
              rows must be(empty)
            }
          }
        }
      }
    }

    "the product does exist" must {
      "return the correct list of rows" in {
        dbConfig.map { c =>
          val tx = Transactor
            .fromDriverManager[IO](c.driver, c.url, c.user, c.password)
          val repo = new DoobieRepository(tx)
          forAll("product") { p: Product =>
            for {
              _ <- repo.saveProduct(p)
              rows <- repo.loadProduct(p.id)
            } yield {
              rows must not be (empty)
              Product.fromDatabase(rows) must contain(p)
            }
          }
        }
      }
    }
  }

  "DoobieRepository#loadProducts" when {
    "no products exist" must {
      "return an empty list" in {
        dbConfig.map { c =>
          val tx = Transactor
            .fromDriverManager[IO](c.driver, c.url, c.user, c.password)
          val repo = new DoobieRepository(tx)
          val rows = repo.loadProducts().compile.toList
          rows.unsafeRunSync must be(empty)
        }
      }
    }

    "products exist" must {
      "return a list of all product rows" in {
        dbConfig.map { c =>
          val tx = Transactor
            .fromDriverManager[IO](c.driver, c.url, c.user, c.password)
          val repo = new DoobieRepository(tx)
          forAll("products") { ps: List[Product] =>
            for {
              _ <- ps.traverse(repo.saveProduct)
              rows = repo.loadProducts()
                .groupAdjacentBy(_._1)
                .map {
                  case (id, rows) => Product.fromDatabase(rows.toList)
                }
                .collect {
                  case Some(p) => p
                }
                .compile
                .toList
            } yield {
              val products = rows.unsafeRunSync
              products must not be (empty)
              products mustEqual ps
            }
          }
        }
      }
    }
  }

  "DoobieRepository#saveProduct" must {
    "return the number affected database rows and save the product" in {
      dbConfig.map { c =>
        val tx = Transactor
          .fromDriverManager[IO](c.driver, c.url, c.user, c.password)
        val repo = new DoobieRepository(tx)
        forAll("product") { p: Product =>
          for {
            cnt <- repo.saveProduct(p)
            rows <- repo.loadProduct(p.id)
          } yield {
            cnt must be > 0
            rows must not be (empty)
            Product.fromDatabase(rows) must contain(p)
          }
        }
      }
    }
  }

  "DoobieRepository#updateProduct" when {
    "the product does not exist" must {
      "return 0 and not change the database" in {
        dbConfig.map { c =>
          val tx = Transactor
            .fromDriverManager[IO](c.driver, c.url, c.user, c.password)
          val repo = new DoobieRepository(tx)
          forAll("product") { p: Product =>
            for {
              cnt <- repo.updateProduct(p)
              rows <- repo.loadProduct(p.id)
            } yield {
              cnt must be(0)
              rows must be(empty)
            }
          }
        }
      }
    }

    "the product does exist" must {
      "return the number of affected database rows and update the product" in {
        dbConfig.map { c =>
          val tx = Transactor
            .fromDriverManager[IO](c.driver, c.url, c.user, c.password)
          val repo = new DoobieRepository(tx)
          forAll("productA", "productB") { (a: Product, b: Product) =>
            val p = b.copy(id = a.id)
            for {
              _ <- repo.saveProduct(a)
              cnt <- repo.updateProduct(p)
              rows <- repo.loadProduct(p.id)
            } yield {
              cnt must be > 0
              rows must not be (empty)
              Product.fromDatabase(rows) must contain(p)
            }
          }
        }
      }
    }
  }
}
