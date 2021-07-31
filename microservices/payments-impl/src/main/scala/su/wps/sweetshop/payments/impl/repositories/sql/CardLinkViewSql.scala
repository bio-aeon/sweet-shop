package su.wps.sweetshop.payments.impl.repositories.sql

import cats.effect.Sync
import cats.syntax.functor._
import cats.tagless.syntax.functorK._
import cats.tagless.{Derive, FunctorK}
import doobie._
import doobie.implicits._
import doobie.postgres.implicits._
import su.wps.sweetshop.payments.impl.models.CardLinkId
import su.wps.sweetshop.payments.impl.models.domain.CardLinkStatus
import su.wps.sweetshop.payments.impl.models.domain.views.CardLinkView
import tofu.doobie.LiftConnectionIO

trait CardLinkViewSql[DB[_]] {
  def findById(id: CardLinkId): DB[Option[CardLinkView]]

  def findFirstByUserId(userId: Int): DB[Option[CardLinkView]]

  def findCountByUserId(userId: Int): DB[Int]

  def set(view: CardLinkView): DB[Int]
}

object CardLinkViewSql extends DefaultCardLinkViewSql {
  implicit def functorK: FunctorK[CardLinkViewSql] = Derive.functorK
}

abstract sealed class DefaultCardLinkViewSql {
  def create[I[_]: Sync, DB[_]](implicit L: LiftConnectionIO[DB]): I[CardLinkViewSql[DB]] =
    Slf4jDoobieLogHandler.create[I].map(implicit logger => new Impl().mapK(L.liftF))

  private final class Impl(implicit lh: LogHandler) extends CardLinkViewSql[ConnectionIO] {
    val tableName = "card_links"
    val table: Fragment = Fragment.const(tableName)

    def findById(id: CardLinkId): ConnectionIO[Option[CardLinkView]] =
      (fr"select * from" ++ table ++ fr"where id = $id").query[CardLinkView].option

    def findFirstByUserId(userId: Int): ConnectionIO[Option[CardLinkView]] =
      (fr"select * from" ++ table ++
        fr"where user_id = $userId and status = ${CardLinkStatus.Active: CardLinkStatus} limit 1")
        .query[CardLinkView]
        .option

    def findCountByUserId(userId: Int): ConnectionIO[Int] =
      (fr"select count(*) from" ++ table ++
        fr"where user_id = $userId and status = ${CardLinkStatus.Active: CardLinkStatus}")
        .query[Int]
        .unique

    def set(view: CardLinkView): ConnectionIO[Int] =
      Update[CardLinkView](setViewQuery).run(view)

    private val setViewQuery =
      s"""insert into $tableName
      (id, user_id, ext_card_id, masked_pan, status, created_at, version)
      values (?, ?, ?, ?, ?, ?, ?)
      on conflict (id)
      do update set
       user_id = excluded.user_id,
       ext_card_id = excluded.ext_card_id,
       masked_pan = excluded.masked_pan,
       status = excluded.status,
       created_at = excluded.created_at,
       version = excluded.version;"""
  }
}
