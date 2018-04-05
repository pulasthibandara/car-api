package user

import org.specs2.mutable.Specification
import play.api.test.{ WithApplication, Injecting }
import play.api.libs.json._
import sangria.macros._
import sangria.marshalling.playJson._
import sangria.execution._
import models._

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}

class AuthGraphQLTest(implicit ec: ExecutionContext) extends Specification {
  import user.AuthGraphQL._

  "run a  application" in new WithApplication with Injecting {
    lazy val query =
      graphql"""
      mutation signUp($$signupData: SignupData!) {
        signUp(
          authProvider: $$signupData
        ) {
          firstName
          lastName
          email
          createdAt
        }
      }
      """

    val signupData= SignupData(Some(AuthProviderCredentials(
      firstName = "pulasthi",
      lastName = "bandara",
      email = "pulasthi1989@gmail.com",
      password = "testing"
    )))

    val userService = inject[UserService]
    val variables: JsValue = Json.toJson(Map("signupData" -> signupData))
    val result = Await.result(Executor.execute(GraphqlSchema.SchemaDefinition, query, GraphqlContext(userService),
      variables = variables
    ), Duration.Inf)

    (result \ "data" \ "signUp" \ "firstName").as[String] must_== "pulasthi"
  }

}
