package graphql.middleware

import com.mohiva.play.silhouette.api.exceptions.NotAuthorizedException
import graphql.SecureContext
import sangria.execution._
import sangria.schema.Context

object GraphQLAuthentication  {
  case object Authorized extends FieldTag

  object SecurityEnforcer extends Middleware[SecureContext] with MiddlewareBeforeField[SecureContext] {
    type QueryVal = Unit
    type FieldVal = Unit

    override def beforeQuery(context: MiddlewareQueryContext[SecureContext, _, _]): Unit = ()

    override def afterQuery(queryVal: QueryVal, context: MiddlewareQueryContext[SecureContext, _, _]): Unit = ()

    override def beforeField(
      queryVal: QueryVal,
      mctx: MiddlewareQueryContext[SecureContext, _, _],
      ctx: Context[SecureContext, _]
    ): BeforeFieldResult[SecureContext, Unit] = {
      val requireAuth = ctx.field.tags contains Authorized

      if (requireAuth)
        ctx.ctx.identity.fold(throw new NotAuthorizedException("Needs Authentication!"))(identity)

      continue
    }
  }
}
