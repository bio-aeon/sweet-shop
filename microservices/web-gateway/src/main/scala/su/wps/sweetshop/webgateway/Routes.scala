package su.wps.sweetshop.webgateway

import cats.effect._
import cats.syntax.applicativeError._
import cats.syntax.either._
import cats.syntax.flatMap._
import io.circe.optics.JsonPath.root
import io.circe.{Json, JsonObject}
import org.http4s.HttpRoutes
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl
import sangria.ast.Document
import sangria.execution.{ExceptionHandler, Executor, HandledException, WithViolations}
import sangria.marshalling.circe._
import sangria.parser.{QueryParser, SyntaxError}
import sangria.schema.Schema
import sangria.validation.AstNodeViolation
import su.wps.sweetshop.webgateway.graphql.{Ctx, SchemaDefinition}

import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}

final class Routes[F[_]](ctx: Ctx, blockingExecutionContext: ExecutionContext)(implicit F: Sync[F],
                                                                               L: LiftIO[F])
    extends Http4sDsl[F] {
  private val queryStringLens = root.query.string
  private val operationNameLens = root.operationName.string
  private val variablesLens = root.variables.obj

  def routes = HttpRoutes.of[F] {
    case req @ POST -> Root / "graphql" =>
      req.as[Json].flatMap(query).flatMap {
        case Right(json) => Ok(json)
        case Left(json) => BadRequest(json)
      }
  }

  private def formatSyntaxError(e: SyntaxError): Json =
    Json.obj(
      "errors" -> Json.arr(
        Json.obj(
          "message" -> Json.fromString(e.getMessage),
          "locations" -> Json.arr(
            Json.obj(
              "line" -> Json.fromInt(e.originalError.position.line),
              "column" -> Json.fromInt(e.originalError.position.column)
            )
          )
        )
      )
    )

  private def formatWithViolations(e: WithViolations): Json =
    Json.obj("errors" -> Json.fromValues(e.violations.map {
      case v: AstNodeViolation =>
        Json.obj(
          "message" -> Json.fromString(v.errorMessage),
          "locations" -> Json.fromValues(
            v.locations.map(
              loc =>
                Json.obj("line" -> Json.fromInt(loc.line), "column" -> Json.fromInt(loc.column))
            )
          )
        )
      case v => Json.obj("message" -> Json.fromString(v.errorMessage))
    }))

  private def formatString(s: String): Json =
    Json.obj("errors" -> Json.arr(Json.obj("message" -> Json.fromString(s))))

  private def formatThrowable(e: Throwable): Json =
    Json.obj(
      "errors" -> Json.arr(
        Json.obj(
          "class" -> Json.fromString(e.getClass.getName),
          "message" -> Json.fromString(e.getMessage)
        )
      )
    )

  def query(request: Json): F[Either[Json, Json]] = {
    val queryString = queryStringLens.getOption(request)
    val operationName = operationNameLens.getOption(request)
    val variables = variablesLens.getOption(request).getOrElse(JsonObject())
    queryString match {
      case Some(qs) => query(qs, operationName, variables)
      case None => fail(formatString("No 'query' property was present in the request."))
    }
  }

  def query(query: String,
            operationName: Option[String],
            variables: JsonObject): F[Either[Json, Json]] =
    QueryParser.parse(query) match {
      case Success(ast) =>
        exec(SchemaDefinition.createSchema(), Sync[F].pure(ctx), ast, operationName, variables)(
          blockingExecutionContext
        )
      case Failure(e @ SyntaxError(_, _, pe)) => fail(formatSyntaxError(e))
      case Failure(e) => fail(formatThrowable(e))
    }

  def exec(schema: Schema[Ctx, Unit],
           userContext: F[Ctx],
           query: Document,
           operationName: Option[String],
           variables: JsonObject)(implicit ec: ExecutionContext): F[Either[Json, Json]] =
    userContext
      .flatMap { ctx =>
        IO.fromFuture {
            IO {
              Executor.execute(
                schema = schema,
                queryAst = query,
                userContext = ctx,
                variables = Json.fromJsonObject(variables),
                operationName = operationName,
                exceptionHandler = ExceptionHandler {
                  case (_, e) => HandledException(e.getMessage)
                }
              )
            }
          }(IO.contextShift(ec))
          .to[F]
      }
      .attempt
      .flatMap {
        case Right(json) => F.pure(json.asRight)
        case Left(err: WithViolations) => fail(formatWithViolations(err))
        case Left(err) => fail(formatThrowable(err))
      }

  def fail(j: Json): F[Either[Json, Json]] =
    F.pure(j.asLeft)
}
