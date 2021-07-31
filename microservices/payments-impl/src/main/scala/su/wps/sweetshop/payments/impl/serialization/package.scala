package su.wps.sweetshop.payments.impl

import aecor.data.Enriched
import su.wps.sweetshop.payments.impl.models.domain.es.EventMetadata

package object serialization {
  type Event[A] = Enriched[EventMetadata, A]
}
