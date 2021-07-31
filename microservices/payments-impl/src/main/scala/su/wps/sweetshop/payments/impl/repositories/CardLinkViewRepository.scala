package su.wps.sweetshop.payments.impl.repositories

import cats.Functor
import cats.effect.Sync
import cats.syntax.functor._
import su.wps.sweetshop.payments.impl.models.CardLinkId
import su.wps.sweetshop.payments.impl.models.domain.views.CardLinkView
import su.wps.sweetshop.payments.impl.repositories.sql.CardLinkViewSql
import tofu.doobie.LiftConnectionIO

trait CardLinkViewRepository[DB[_]] {
  def findById(id: CardLinkId): DB[Option[CardLinkView]]

  def findFirstByUserId(userId: Int): DB[Option[CardLinkView]]

  def findCountByUserId(userId: Int): DB[Int]

  def set(view: CardLinkView): DB[Unit]
}

object CardLinkViewRepository {
  def create[I[_]: Sync, DB[_]: LiftConnectionIO: Functor]: I[CardLinkViewRepository[DB]] =
    CardLinkViewSql.create[I, DB].map { sql =>
      new Impl[DB](sql)
    }

  private final class Impl[DB[_]: Functor](sql: CardLinkViewSql[DB])
      extends CardLinkViewRepository[DB] {
    def findById(id: CardLinkId): DB[Option[CardLinkView]] =
      sql.findById(id)

    def findFirstByUserId(userId: Int): DB[Option[CardLinkView]] =
      sql.findFirstByUserId(userId)

    def findCountByUserId(userId: Int): DB[Int] =
      sql.findCountByUserId(userId)

    def set(view: CardLinkView): DB[Unit] =
      sql.set(view).void
  }
}
