package su.wps.sweetshop.payments.impl.data

import tofu.WithLocal
import tofu.logging.Loggable

final case class AppContext(traceId: String)

object AppContext {
  type LocalAppCxt[F[_]] = WithLocal[F, AppContext]

  def empty: AppContext = AppContext("")

  implicit val loggable: Loggable[AppContext] =
    Loggable[String].contramap[AppContext](_.traceId).named("traceId")
}
