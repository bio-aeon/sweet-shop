package su.wps.sweetshop.payments.impl

import cats.data.Chain
import cats.effect.concurrent.Ref
import cats.effect.{Clock, ContextShift, IO, LiftIO, Sync}
import cats.mtl.MonadState
import cats.syntax.functor._
import cats.{Id, ~>}
import com.olegpy.meow.effects._
import com.olegpy.meow.hierarchy.deriveMonadState
import fs2.concurrent.InspectableQueue
import io.circe.Json
import mouse.any._
import org.specs2.mutable.Specification
import su.wps.sweetshop.payments.impl.entities.EventsourcedPayment
import su.wps.sweetshop.payments.impl.models.PaymentId
import su.wps.sweetshop.payments.impl.models.domain.es.{
  PaymentAuthorized,
  PaymentCreated,
  PaymentEvent,
  Payments
}
import su.wps.sweetshop.payments.impl.testing.RefRuntime
import su.wps.sweetshop.payments.impl.testing.syntax._
import tofu.lift.Lift
import tofu.syntax.lift._

import scala.concurrent.ExecutionContext.Implicits

class EventsourcedPaymentSpec extends Specification {
  implicit val shift: ContextShift[IO] = IO.contextShift(Implicits.global)

  implicit val clock: Clock[IO] = Clock.create[IO]

  implicit val lift: Lift[IO, Id] = Lift.byFunK(λ[IO ~> Id](_.unsafeRunSync()))

  val paymentId: PaymentId = PaymentId("1")

  case class ProgramState(events: RefRuntime.InnerState[PaymentId, PaymentEvent])

  object ProgramState {
    def empty(events: Map[PaymentId, Chain[PaymentEvent]] = Map.empty): ProgramState =
      (for {
        i <- Ref.of[IO, Map[PaymentId, Chain[PaymentEvent]]](events)
        q <- InspectableQueue.unbounded[IO, (PaymentId, PaymentEvent)]
      } yield ProgramState(RefRuntime.InnerState(i, q))).lift[Id]
  }

  "EventsourcedPayment should" >> {
    "create & authorize payment" >> {
      val (payments, state) = mkPayments[IO].lift[Id]
      val payment = payments(paymentId)
      val test = for {
        _ <- payment.create(1, 100, Json.obj())
        r0 <- payment.authorize
        r1 <- state.get
      } yield (r0, r1)

      val (r0, r1) = test.lift[Id]
      val store = r1.events.store.get.lift[Id]
      val queueEvents = r1.events.queue.dequeueCurrent.lift[Id]
      r0 must beRight(())
      store(paymentId) mustEqual Chain(PaymentCreated(1, 100, Json.obj()), PaymentAuthorized)
      queueEvents must haveLength(2)
    }
  }

  private def mkPayments[F[_]: Clock: Sync: LiftIO]: F[(Payments[F], Ref[F, ProgramState])] = {
    def wire(implicit MS: MonadState[F, ProgramState]) =
      RefRuntime[F, PaymentId](EventsourcedPayment.behavior(Clock[F]))

    Ref[F].of(ProgramState.empty()).map { refState =>
      refState.runState(implicit monadState => wire) |> ((_, refState))
    }
  }
}
