package su.wps.sweetshop.payments.impl.models.domain.views

import su.wps.sweetshop.payments.impl.models.CustomerId
import su.wps.sweetshop.payments.impl.models.domain.CustomerStatus

import java.time.ZonedDateTime

final case class CustomerView(id: CustomerId,
                              email: String,
                              password: String,
                              status: CustomerStatus,
                              createdAt: ZonedDateTime,
                              version: Long)
