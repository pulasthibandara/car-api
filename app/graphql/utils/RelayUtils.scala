package graphql.utils

import graphql.SecureContext
import play.api.libs.json.{Format, Json, OFormat}
import sangria.execution.FieldTag
import sangria.marshalling.FromInput
import sangria.marshalling.FromInput.InputObjectResult
import sangria.marshalling.playJson._
import sangria.relay.{Mutation, MutationLike}
import sangria.schema.{Action, Args, Context, Field, InputField, InputObjectType, InputType, OutputType, ValidOutType, fields}
import sangria.util.tag.@@

import scala.reflect.ClassTag

object RelayUtils {
  case class RelayResult[T](clientMutationId: Option[String], payload: T) extends Mutation
  case class RelayInput[T](clientMutationId: Option[String], payload: T)

  object RelayInput {
    implicit def jsonFormats[T](implicit ft: Format[T]): OFormat[RelayInput[T]] =
      Json.format[RelayInput[T]]
  }

  def createSimpleMutation[Val, Input, Res, Out](
    name: String,
    fieldType: OutputType[Out],
    inputObjectType: InputType[_],
    description: Option[String] = None,
    resolve: (Input, Context[SecureContext, Val]) ⇒ Action[SecureContext, Res],
    tags: List[FieldTag] = Nil,
    complexity: Option[(SecureContext, Args, Double) ⇒ Double] = None,
    deprecationReason: Option[String] = None)
    (implicit  inf: Format[Input], ev: ValidOutType[Res, Out]): Field[SecureContext, Val] = {

    Mutation.fieldWithClientMutationId[SecureContext, Val, RelayResult[Res], RelayInput[Input]](
      fieldName = name,
      typeName = s"${name}Mutation",
      inputFields = List(InputField("payload", inputObjectType)),
      outputFields = fields(Field("payload", fieldType, resolve = _.value.payload)),
      tags = tags,
      fieldDescription = description,
      complexity = complexity,
      mutateAndGetPayload = (input, c) => {
        implicit val ec = c.ctx.ec
        resolve(input.payload, c).map(r => RelayResult[Res](input.clientMutationId, r))
      }
    )
  }
}
