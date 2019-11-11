package su.wps.sweetshop.webgateway.graphql

import sangria.macros.derive._
import sangria.marshalling.circe._
import sangria.schema._
import su.wps.sweetshop.auth.api.dto.UserDto
import su.wps.sweetshop.errors.dto.ErrorResultDto

object SchemaDefinition {
  implicit val ErrorType = deriveObjectType[Unit, ErrorResultDto.Error](ObjectTypeName("Error"))

  val ValidationFailedType = deriveObjectType[Unit, ErrorResultDto.ValidationFailed](
    ObjectTypeName("ValidationFailed"),
    ReplaceField("errors", Field("errors", ListType(ErrorType), resolve = _.value.errors.toList))
  )

  val BusinessLogicErrorType =
    deriveObjectType[Unit, ErrorResultDto.BusinessLogicError](
      ObjectTypeName("BusinessLogicError"),
      ObjectTypeDescription("Business logic error")
    )

  val NotFoundType = ObjectType(
    "NotFound",
    "Resource not found.",
    fields[Ctx, ErrorResultDto.NotFound.type](
      Field("message", StringType, resolve = _ => "Resource not found.")
    )
  )

  val ForbiddenType = ObjectType(
    "Forbidden",
    "Access denied.",
    fields[Ctx, ErrorResultDto.Forbidden.type](
      Field("message", StringType, resolve = _ => "Access denied.")
    )
  )

  val UnauthenticatedType = ObjectType(
    "Unauthenticated",
    "User is not authenticated.",
    fields[Ctx, ErrorResultDto.Unauthenticated.type](
      Field("message", StringType, resolve = _ => "User is not authenticated.")
    )
  )

  val UserType = deriveObjectType[Unit, UserDto](ObjectTypeName("User"))

  def unionType(types: List[ObjectType[Ctx, _]]) = types

  val Query =
    ObjectType(
      "Query",
      fields[Ctx, Unit](
        Field(
          "me",
          UnionType(
            "AuthenticatedUserResult",
            types = unionType(List(UserType, UnauthenticatedType))
          ),
          resolve = ctx => ctx.ctx.me
        )
      )
    )

  def createSchema() = Schema(Query)
}
