package aecor.journal.postgres

import aecor.data.Tagging
import aecor.encoding.{KeyDecoder, KeyEncoder}
import aecor.journal.postgres.PostgresEventJournal.Serializer
import cats.implicits._

final class JournalSchema[K, E](tableName: String, serializer: Serializer[E])(
  implicit keyEncoder: KeyEncoder[K],
  keyDecoder: KeyDecoder[K]
) {
  def journal(tagging: Tagging[K]): PostgresEventJournal[K, E] =
    PostgresEventJournal(tableName, tagging, serializer)
}

object JournalSchema {
  def apply[K, E](tableName: String, serializer: Serializer[E])(
    implicit keyEncoder: KeyEncoder[K],
    keyDecoder: KeyDecoder[K]
  ): JournalSchema[K, E] = new JournalSchema(tableName, serializer)
}
