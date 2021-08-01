package su.wps.sweetshop.payments.impl.wirings

import aecor.data.{Committable, ConsumerId, Enriched, EntityEvent}
import cats.data.NonEmptyList
import cats.effect._
import cats.syntax.either._
import fs2.Stream
import fs2.kafka._
import io.circe.Error
import su.wps.sweetshop.auth.api.models.UserCreated
import su.wps.sweetshop.payments.api.models.ChargeRequired
import su.wps.sweetshop.payments.impl.config.KafkaConfig
import su.wps.sweetshop.payments.impl.models.domain.es._
import su.wps.sweetshop.payments.impl.models.implicits.events._
import su.wps.sweetshop.payments.impl.models.{CardLinkId, CustomerId, PaymentId}
import su.wps.sweetshop.payments.impl.serialization.KafkaEventSerializers._
import tofu.lift.Lift
import tofu.syntax.lift._

final case class KafkaWirings[F[_]](
  userCreatedEventsStream: ConsumerId => Stream[F, Committable[F, Either[Error, UserCreated]]],
  cardLinkEventsStream: ConsumerId => Stream[
    F,
    Committable[F, Either[Error, JournalEvent[CardLinkId, CardLinkEvent]]]
  ],
  customerEventsStream: ConsumerId => Stream[
    F,
    Committable[F, Either[Error, JournalEvent[CustomerId, CustomerEvent]]]
  ],
  paymentCreatedEventsStream: ConsumerId => Stream[
    F,
    Committable[F, Either[Error, JournalEvent[PaymentId, PaymentCreated]]]
  ],
  chargeRequiredEventsStream: ConsumerId => Stream[F, Committable[F, Either[Error, ChargeRequired]]]
)

object KafkaWirings {
  def create[I[_]: ConcurrentEffect: ContextShift: Timer, F[_]](
    config: KafkaConfig
  )(implicit lift: Lift[I, F]): KafkaWirings[F] =
    KafkaWirings[F](
      (mkEventStream[I, F, UserCreated](
        _: ConsumerId,
        config.contactPoints,
        config.userCreatedEventsTopic
      )).andThen(_.translate(lift.liftF)),
      (mkEventStream[I, F, JournalEvent[CardLinkId, CardLinkEvent]](
        _: ConsumerId,
        config.contactPoints,
        config.cardLinkEventsTopic
      )).andThen(_.translate(lift.liftF)),
      (mkEventStream[I, F, JournalEvent[CustomerId, CustomerEvent]](
        _: ConsumerId,
        config.contactPoints,
        config.customerEventsTopic
      )).andThen(_.translate(lift.liftF)),
      (mkEventStream[I, F, JournalEvent[PaymentId, PaymentEvent]](
        _: ConsumerId,
        config.contactPoints,
        config.paymentEventsTopic
      ).collect { c =>
          c.value match {
            case Right(v @ EntityEvent(_, _, Enriched(_, e: PaymentCreated))) =>
              c.map(_ => v.map(x => x.copy(event = e)).asRight)
            case Left(err) => c.map(_ => err.asLeft)
          }
        })
        .andThen(_.translate(lift.liftF)),
      (mkEventStream[I, F, ChargeRequired](
        _: ConsumerId,
        config.contactPoints,
        config.chargeRequiredEventsTopic
      )).andThen(_.translate(lift.liftF))
    )

  private[wirings] def mkEventStream[I[_]: ConcurrentEffect: ContextShift: Timer: Lift[*[_], F], F[
    _
  ], A](consumerId: ConsumerId, contactPoints: List[String], topic: String)(
    implicit rd: RecordDeserializer[I, Either[Error, A]]
  ): Stream[I, Committable[F, Either[Error, A]]] = {
    val consumerSettings = ConsumerSettings[I, String, Either[Error, A]]
      .withAutoOffsetReset(AutoOffsetReset.Earliest)
      .withBootstrapServers(contactPoints.mkString(","))
      .withGroupId(consumerId.value)

    for {
      eventConsumer <- KafkaConsumer
        .stream(consumerSettings)
        .evalTap(_.subscribe(NonEmptyList.one(topic)))
      eventStream <- eventConsumer.stream.map(
        m => Committable(m.offset.commit.lift[F], m.record.value)
      )
    } yield eventStream
  }
}
