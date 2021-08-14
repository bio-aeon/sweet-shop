package su.wps.sweetshop.auth.impl.testing

import cats.effect.{Blocker, ContextShift, IO}
import com.dimafeng.testcontainers.PostgreSQLContainer
import doobie._
import doobie.util.ExecutionContexts
import mouse.any._
import org.specs2.specification.{AfterEach, BeforeAfterAll}

import java.sql.DriverManager
import scala.concurrent.ExecutionContext.Implicits
import scala.io.Source.fromInputStream

trait DbTest extends AfterEach with BeforeAfterAll {
  implicit val cs: ContextShift[IO] = IO.contextShift(Implicits.global)

  private lazy val container: PostgreSQLContainer =
    PostgreSQLContainer()

  implicit lazy val xa: Transactor[IO] = {
    Transactor.fromDriverManager[IO](
      container.driverClassName,
      container.jdbcUrl,
      container.username,
      container.password,
      Blocker.liftExecutionContext(ExecutionContexts.synchronous)
    )
  }

  def beforeAll(): Unit = {
    container.start()
    val sql: String =
      fromInputStream(getClass.getResourceAsStream("/db.sql")).getLines().mkString
    (Class.forName(container.driverClassName).newInstance() |> (
      _ => DriverManager.getConnection(container.jdbcUrl, container.username, container.password)
    ) |> (_.createStatement())) <| (_.execute(sql)) <| (_.close()) |> (_.getConnection.close)
  }

  def afterAll(): Unit =
    container.stop()

  def after: Any = truncateAll()

  protected def truncateAll(): Unit
}
