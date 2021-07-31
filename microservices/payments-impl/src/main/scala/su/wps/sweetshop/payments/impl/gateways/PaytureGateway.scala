package su.wps.sweetshop.payments.impl.gateways

import cats.Monad
import cats.syntax.either._
import cats.syntax.flatMap._
import cats.syntax.functor._
import com.lucidchart.open.xtract.{ParseFailure, ParseSuccess, PartialParseSuccess, XmlReader}
import sttp.client3._
import su.wps.sweetshop.payments.impl.config.PaytureConfig
import su.wps.sweetshop.payments.impl.models.domain.errors._
import su.wps.sweetshop.payments.impl.models.payture.{
  InitCardLinkResult,
  RegisterCustomerResult,
  SuccessChargeResult,
  SuccessPayBlockResult
}
import tofu.logging.Logging
import tofu.syntax.handle._
import tofu.syntax.logging._
import tofu.{Catches, Raise}

import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.util.concurrent.TimeoutException
import scala.xml.{Elem, XML}

trait PaytureGateway[F[_]] {
  def registerCustomer(email: String, password: String): F[RegisterCustomerResult]

  def initCardLink(id: String,
                   email: String,
                   password: String,
                   ip: String,
                   template: String,
                   returnUrl: String): F[InitCardLinkResult]

  def initBlock(id: String,
                amount: Int,
                email: String,
                password: String,
                cardId: String): F[SuccessPayBlockResult]

  def charge(id: String): F[SuccessChargeResult]
}

object PaytureGateway {

  private final class Impl[F[_]: Catches: Logging](
    config: PaytureConfig,
    backend: SttpBackend[F, Any]
  )(implicit F: Monad[F], R: Raise[F, AppErr])
      extends PaytureGateway[F]
      with GatewayLogging {

    def registerCustomer(email: String, password: String): F[RegisterCustomerResult] = {
      val params = Map("VWUserLgn" -> email, "VWUserPsw" -> password)

      sendRequest[RegisterCustomerResult](
        s"${config.endpoint}/Register",
        Map("VWID" -> config.terminalPay, "DATA" -> mkData(params))
      )
    }

    def initCardLink(id: String,
                     email: String,
                     password: String,
                     ip: String,
                     template: String,
                     returnUrl: String): F[InitCardLinkResult] = {
      val params = Map(
        "SessionType" -> "Add",
        "VWUserLgn" -> email,
        "VWUserPsw" -> password,
        "OrderId" -> id,
        "TemplateTag" -> template,
        "IP" -> ip,
        "Url" -> returnUrl
      )

      sendRequest[InitCardLinkResult](
        s"${config.endpoint}/Init",
        Map("VWID" -> config.terminalAdd, "DATA" -> mkData(params))
      )
    }

    def initBlock(id: String,
                  amount: Int,
                  email: String,
                  password: String,
                  cardId: String): F[SuccessPayBlockResult] = {
      val params = Map(
        "SessionType" -> "Block",
        "VWUserLgn" -> email,
        "VWUserPsw" -> password,
        "OrderId" -> id,
        "CardId" -> cardId,
        "Amount" -> amount.toString
      )

      sendRequest[SuccessPayBlockResult](
        s"${config.endpoint}/Pay",
        Map("VWID" -> config.terminalPay, "DATA" -> mkData(params))
      )
    }

    def charge(id: String): F[SuccessChargeResult] =
      sendRequest[SuccessChargeResult](
        s"${config.endpoint}/Charge",
        Map("VWID" -> config.terminalPay, "Password" -> config.password, "OrderId" -> id)
      )

    private[gateways] def mkData(params: Map[String, String]): String =
      URLEncoder
        .encode(
          params.map { case (k, v) => s"$k=$v" }.mkString(";"),
          StandardCharsets.UTF_8.toString
        )

    private[gateways] def sendRequest[A: XmlReader](url: String,
                                                    params: Map[String, String]): F[A] = {
      val req = basicRequest.post(uri"$url").body(params)

      for {
        _ <- debug"${requestToString(req)}"
        resp <- req.send(backend).handleWith[Throwable] {
          case _: TimeoutException => R.raise(TimeoutFailure(url))
          case e: Throwable => R.raise(ConnectionError(e.getMessage))
        }
        _ <- debug"${responseToString(resp)}"
        res <- parseResponse[A](resp)
      } yield res
    }

    private[gateways] def parseResponse[A: XmlReader](
      response: Response[Either[String, String]]
    ): F[A] = {
      def parseContent(content: String): F[A] =
        parseXml(XML.loadString(content)).fold(err => R.raise(ParsingFailure(err)), F.pure)

      if (response.isSuccess) {
        response.body match {
          case Right(content) => parseContent(content)
          case Left(_) => R.raise(ParsingFailure("Empty response body"))
        }
      } else {
        R.raise(UnexpectedStatus(response.code))
      }
    }

    private[gateways] def parseXml[A: XmlReader](xmlElem: Elem): Either[String, A] =
      xmlElem.attribute("Success") match {
        case Some(success) =>
          if (success.text == "True") {
            XmlReader.of[A].read(xmlElem) match {
              case ParseSuccess(x) => x.asRight
              case ParseFailure(e) => e.mkString(",").asLeft
              case PartialParseSuccess(_, e) => e.mkString(",").asLeft
            }
          } else {
            xmlElem.attribute("ErrCode").map(_.text).getOrElse("unknown").asLeft
          }
        case _ =>
          "Incorrect xml format".asLeft
      }
  }
}
