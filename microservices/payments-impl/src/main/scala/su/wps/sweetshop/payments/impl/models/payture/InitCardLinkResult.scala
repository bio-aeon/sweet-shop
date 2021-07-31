package su.wps.sweetshop.payments.impl.models.payture

import com.lucidchart.open.xtract.XmlReader
import com.lucidchart.open.xtract.XmlReader.attribute

case class InitCardLinkResult(SessionId: String)

object InitCardLinkResult {
  implicit val reader: XmlReader[InitCardLinkResult] =
    attribute("SessionId")(XmlReader.stringReader).map(apply)
}
