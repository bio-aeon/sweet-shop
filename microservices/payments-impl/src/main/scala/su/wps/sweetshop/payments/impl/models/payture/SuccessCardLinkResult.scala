package su.wps.sweetshop.payments.impl.models.payture

final case class SuccessCardLinkResult(OrderId: String,
                                       CardNumber: String,
                                       CardId: String,
                                       CardHolder: String,
                                       ExpDate: String,
                                       TransactionDate: String)
