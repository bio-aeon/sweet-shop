package su.wps.sweetshop.payments.impl.models.payture

import cats.syntax.apply._
import com.lucidchart.open.xtract.XmlReader
import com.lucidchart.open.xtract.XmlReader.attribute

case class RegisterCustomerResult(Success: String, VWUserLgn: String)

object RegisterCustomerResult {
  implicit val reader: XmlReader[RegisterCustomerResult] =
    (attribute("Success")(XmlReader.stringReader), attribute("VWUserLgn")(XmlReader.stringReader))
      .mapN(apply)
}
