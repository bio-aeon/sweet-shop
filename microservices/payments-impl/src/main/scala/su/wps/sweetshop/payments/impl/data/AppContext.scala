package su.wps.sweetshop.payments.impl.data

import tofu.HasLocal
import tofu.logging.Loggable

final case class AppContext(traceId: String)

object AppContext {
  type LocalAppCxt[F[_]] = HasLocal[F, AppContext]

  def empty: AppContext = AppContext("")

  implicit val loggable: Loggable[AppContext] =
    Loggable[String].contramap[AppContext](_.traceId).named("traceId")
}
