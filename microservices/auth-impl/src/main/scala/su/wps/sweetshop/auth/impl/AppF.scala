package su.wps.sweetshop.auth.impl

import cats.effect.{Blocker, ConcurrentEffect, ContextShift, Resource, Timer}
import cats.syntax.flatMap._
import cats.syntax.functor._
import cats.{Applicative, ~>}
import com.typesafe.config.ConfigFactory
import doobie._
import pureconfig.ConfigSource
import pureconfig.module.catseffect._
import su.wps.sweetshop.auth.impl.config.AppConfig
import pureconfig.generic.auto._

import scala.concurrent.ExecutionContext.Implicits.global

object syntax {
  implicit class ResourceListOption[F[_], A](val fa: F[A]) extends AnyVal {
    def resource(implicit F: Applicative[F]): Resource[F, A] = Resource.eval(fa)
  }
}

class AppF[F[_]: Timer: ContextShift](implicit F: ConcurrentEffect[F]) {
  import syntax._

  case class Resources(appConfig: AppConfig, dbWirings: DbWirings[F])

  def resources: Resource[F, Resources] =
    for {
      appConfig <- loadF[F, AppConfig](
        ConfigSource.fromConfig(ConfigFactory.load()),
        Blocker.liftExecutionContext(global)
      ).resource
      dbWirings <- DbWirings[F](appConfig.db)
      _ <- Resource.make(F.unit)(_ => F.delay(println("Releasing application resources.")))
    } yield Resources(appConfig, dbWirings)

  def launch(r: Resources): F[Unit] = {
    import r._

    implicit val transform: ConnectionIO ~> F = dbWirings.xa.trans

    for {
      serviceWirings <- ServiceWirings[F](appConfig, dbWirings)
      endpointWirings <- EndpointWirings(appConfig, serviceWirings)
      _ <- endpointWirings.launchHttpService
    } yield ()
  }

  def run: Resource[F, Unit] = resources.flatMap(r => launch(r).resource)
}
