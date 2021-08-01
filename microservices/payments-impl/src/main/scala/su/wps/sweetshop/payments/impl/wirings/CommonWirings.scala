package su.wps.sweetshop.payments.impl.wirings

import cats.effect.{Blocker, Concurrent, ContextShift, Sync}
import cats.syntax.flatMap._
import cats.syntax.functor._
import com.typesafe.config.ConfigFactory
import pureconfig.ConfigSource
import pureconfig.module.catseffect.loadF
import sttp.client3.SttpBackend
import sttp.client3.asynchttpclient.cats.AsyncHttpClientCatsBackend
import su.wps.sweetshop.payments.impl.config.{AppConfig, HttpClientConfig}
import su.wps.sweetshop.payments.impl.security.{Bouncy, Crypto}
import tofu.lift.Lift
import tofu.syntax.lift._

import scala.concurrent.ExecutionContext.Implicits.global

final case class CommonWirings[F[_]](appConfig: AppConfig,
                                     crypto: Crypto[F],
                                     sttpBackend: SttpBackend[F, Any])

object CommonWirings {

  def create[I[_]: Sync: ContextShift, F[_]: Concurrent: ContextShift](implicit lift: Lift[F, I]) =
    for {
      appConfig <- loadF[I, AppConfig](
        ConfigSource.fromConfig(ConfigFactory.load()),
        Blocker.liftExecutionContext(global)
      )
      implicit0(bouncy: Bouncy) <- Bouncy.create[I]
      crypto = Crypto.create[F]
      sttpBackend <- mkHttpBackend[I, F](appConfig.httpClient).lift[I]
    } yield CommonWirings(appConfig, crypto, sttpBackend)

  private def mkHttpBackend[I[_]: Sync, F[_]: ContextShift: Concurrent](
    config: HttpClientConfig
  ): F[SttpBackend[F, Any]] =
    AsyncHttpClientCatsBackend.usingConfigBuilder[F] { builder =>
      builder
        .setConnectTimeout(config.connectionTimeout.toMillis.toInt)
        .setReadTimeout(config.readTimeout.toMillis.toInt)
        .setRequestTimeout(config.readTimeout.toMillis.toInt)
    }
}
