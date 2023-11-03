//import cats.Applicative.ops.toAllApplicativeOps
import cats.implicits._
import cats.effect.{ExitCode, IO, IOApp}
import com.sabalitech.api.Routes
import com.sabalitech.config.{ApiConfig, DatabaseConfig}
import com.sabalitech.db.{DatabaseMigrator, DoobieRepository, FlywayDatabaseMigrator}
import com.typesafe.config._
import doobie.util.transactor.Transactor
import eu.timepit.refined.auto._
import org.http4s.implicits._
import org.http4s.server.Router
import org.http4s.server.blaze.BlazeServerBuilder
import pureconfig._


object Main extends IOApp{


  @SuppressWarnings(Array(
    "org.wartremover.warts.Any",
    "scalafix:DisableSyntax.null"
  ))
  override def run(args: List[String]): IO[ExitCode] = {
    val migrator: DatabaseMigrator[IO] = new FlywayDatabaseMigrator

    val program = for {
      (apiConfig, dbConfig) <- IO {
        val cfg = ConfigFactory.load
        (
          loadConfigOrThrow[ApiConfig](cfg, "api"),
          loadConfigOrThrow[DatabaseConfig](cfg, "database")
        )
      }
      _ <- migrator.migrate(dbConfig.url, dbConfig.user, dbConfig.password)
      transactor = Transactor
        .fromDriverManager[IO](
          dbConfig.driver,
          dbConfig.url,
          dbConfig.user,
          dbConfig.password
        )
      doobieRepo = new DoobieRepository[IO](transactor)
      routes     = new Routes[IO](doobieRepo).routes
      httpApp    = Router("/" -> routes).orNotFound
      server     = BlazeServerBuilder[IO]
        .bindHttp(apiConfig.port, apiConfig.host)
        .withHttpApp(httpApp)
      fiber       = server
        .resource
        .use(_ => IO.never)
        .as(ExitCode.Success)
    } yield fiber

    program.attempt.unsafeRunSync match {
      case Left(e) =>
        IO {
          println("*** An error occured! ***")
          if (e != null) {
            println(e.getMessage)
          }
          ExitCode.Error
        }
      case Right(r) => r
    }
  }
}