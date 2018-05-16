package graphql.utils

import graphql.SecureContext
import play.api.libs.json._
import sangria.execution.FieldTag
import sangria.marshalling.playJson._
import sangria.relay.Mutation
import sangria.schema._

object RelayUtils {
  case class RelayResult[T](clientMutationId: Option[String], payload: T) extends Mutation
  case class RelayInput[T](clientMutationId: Option[String], payload: T)

  object RelayInput {
    implicit def jsonReads[T](implicit ft: Reads[T]): Reads[RelayInput[T]] =
      Json.reads[RelayInput[T]]
  }

  implicit def optionFormat[T: Format]: Format[Option[T]] = new Format[Option[T]]{
    override def reads(json: JsValue): JsResult[Option[T]] = json.validateOpt[T]

    override def writes(o: Option[T]): JsValue = o match {
      case Some(t) ⇒ implicitly[Writes[T]].writes(t)
      case None ⇒ JsNull
    }
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
    (implicit  inf: Reads[Input], ev: ValidOutType[Res, Out]): Field[SecureContext, Val] = {

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
