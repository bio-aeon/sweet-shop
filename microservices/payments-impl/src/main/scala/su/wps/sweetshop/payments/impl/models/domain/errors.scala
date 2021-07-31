package su.wps.sweetshop.payments.impl.models.domain

import sttp.model.StatusCode

object errors {
  sealed trait AppErr extends Exception {
    override def getMessage: String = toString
  }

  final case class ParsingFailure(msg: String) extends AppErr {
    override def toString: String = msg
  }

  final case class UnexpectedStatus(code: StatusCode) extends AppErr {
    override def toString: String = s"Unexpected status code $code"
  }

  final case class TimeoutFailure(url: String) extends AppErr {
    override def toString: String = s"Timeout during connect to $url"
  }

  final case class ConnectionError(msg: String) extends AppErr {
    override def toString: String = msg
  }
}
