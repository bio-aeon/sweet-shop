package su.wps.sweetshop.payments.impl.models.domain

import aecor.data.{Enriched, EntityEvent}
import aecor.runtime.Eventsourced.Entities
import su.wps.sweetshop.payments.impl.entities.{CardLink, Customer, Payment}
import su.wps.sweetshop.payments.impl.models.{CardLinkId, CustomerId, PaymentId}

package object es {
  type CardLinks[F[_]] = Entities.Rejectable[CardLinkId, CardLink, F, CardLinkCommandRejection]
  type Customers[F[_]] = Entities.Rejectable[CustomerId, Customer, F, CustomerCommandRejection]
  type Payments[F[_]] = Entities.Rejectable[PaymentId, Payment, F, PaymentCommandRejection]

  type JournalEvent[K, A] = EntityEvent[K, Enriched[EventMetadata, A]]
}
