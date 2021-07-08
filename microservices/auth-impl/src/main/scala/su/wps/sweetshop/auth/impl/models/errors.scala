package su.wps.sweetshop.auth.impl.models

object errors {
  sealed trait AppErr extends Exception {
    override def getMessage: String = toString
  }

  final case class SMSCodeNotFound(code: String) extends AppErr {
    override def toString: String = s"Not found sms code $code"
  }

  final case class IncorrectSMSCode(code: String) extends AppErr {
    override def toString: String = s"Sms code $code is incorrect or expired"
  }

  case object TooFrequentSMSCodeGeneration extends AppErr {
    override def toString: String = "Code can be generated no more than once every 2 minutes"
  }
}
