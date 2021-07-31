package su.wps.sweetshop.payments.impl.models.payture

import com.lucidchart.open.xtract.XmlReader
import com.lucidchart.open.xtract.XmlReader.attribute

case class SuccessPayBlockResult(MerchantOrderId: String)

object SuccessPayBlockResult {
  implicit val reader: XmlReader[SuccessPayBlockResult] =
    attribute("MerchantOrderId")(XmlReader.stringReader).map(apply)
}
