package su.wps.sweetshop.payments.impl.models.payture

import com.lucidchart.open.xtract.XmlReader
import com.lucidchart.open.xtract.XmlReader.attribute

case class SuccessChargeResult(Amount: String)

object SuccessChargeResult {
  implicit val reader: XmlReader[SuccessChargeResult] =
    attribute("Amount")(XmlReader.stringReader).map(apply)
}
