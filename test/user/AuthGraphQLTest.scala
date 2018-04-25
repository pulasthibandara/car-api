package user

import business.services.BusinessService
import org.specs2.mutable.Specification
import play.api.test.{HasApp, Injecting, WithApplication}
import play.api.libs.json._
import sangria.macros._
import sangria.marshalling.playJson._
import sangria.execution._
import _root_.models._
import common.Concurrent._
import graphql.middleware.GraphQLAuthentication
import graphql.{GraphQLSchema, SecureContext}
import graphql.schema.{AuthGraphQLImplicits, AuthProviderCredentials, SignupData}
import org.specs2.mock.Mockito
import org.specs2.specification.Scope
import sangria.schema.{ObjectType, Schema}
import sangria.schema._
import testhelpers.{DatabaseIsolation, WithDataEvolutions}
import vehicle.ListingService

import scala.concurrent.ExecutionContext

class AuthGraphQLTest(implicit ec: ExecutionContext) extends Specification with Mockito with AuthGraphQLImplicits {
  import graphql.middleware.GraphQLAuthentication._

  sequential

  trait UserData extends WithDataEvolutions {
    self: HasApp =>

    override def evolutionData: String =
      """
        | INSERT INTO users (id, email, first_name, last_name, created_at) VALUES ('b58ec222-2cbd-4509-9f00-e13ea2bb14de', 'pulasthi1989@gmail.com', 'pulasthi', 'bandara', '2018-04-17 01:14:31.490000');
        | INSERT INTO login_info (id, provider_id, provider_key, created_at) VALUES (1, 'credentials', 'pulasthi1989@gmail.com', null);
        | INSERT INTO user_login_info (user_id, login_info_id, created_at) VALUES ('b58ec222-2cbd-4509-9f00-e13ea2bb14de', 1, '2018-04-17 01:14:31.524320');
        | INSERT INTO password_info (hasher, password, salt, login_info_id, created_at) VALUES ('bcrypt-sha256', '$2a$10$giaAKoxtUjFHbx8CIR2OBudPkbGje6Sa5xmt415srKpB2NgypQO3q', null, 1, '2018-04-17 01:14:31.542000');
      """.stripMargin
  }

  trait WithSecureContext {
    val authService = mock[AuthService]
    val listingService = mock[ListingService]
    val businessService = mock[BusinessService]

    def secureContext = SecureContext(None, authService, listingService, businessService, ec)
  }

  "signup a user" in new WithApplication with Injecting with DatabaseIsolation with WithSecureContext {
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

    override  val authService = inject[AuthService]

    val variables: JsValue = Json.toJson(Map("signupData" -> signupData))
    val result = Executor.execute(
      GraphQLSchema.SchemaDefinition,
      query,
      secureContext,
      variables = variables).await

    (result \ "data" \ "signUp" \ "firstName").as[String] must_== "pulasthi"
    (result \ "errors").isInstanceOf[JsUndefined] must_== true
  }


  "authenticate a user" in new WithApplication with Injecting with DatabaseIsolation with UserData with WithSecureContext  {
    lazy val query =
      graphql"""
        mutation login($$email: String!, $$password: String!) {
          login(email: $$email, password: $$password)
        }
        """

    val email = "pulasthi1989@gmail.com"
    val password = "testing"

    val variables: JsValue = Json.toJson(Map(
      "email" -> email,
      "password" -> password,
    ))

    override val authService = inject[AuthService]
    val result = Executor.execute(
      GraphQLSchema.SchemaDefinition,
      query,
      secureContext,
      variables = variables).await

    (result \ "data" \ "login").as[String] must beAnInstanceOf[String]
  }

  "security enforcer stops unauthorized requests" in new Scope with WithSecureContext {
    val query =
      graphql"""
        query {
          secureContent
        }
        """

    val securedQuery = ObjectType(
      "Query",
      fields[SecureContext, Unit](Field(
        "secureContent",
        StringType,
        tags = GraphQLAuthentication.Authorized :: Nil,
        resolve = c => "success!"
      ))
    )

    val result = Executor.execute(
      Schema(securedQuery),
      query,
      secureContext,
      middleware = SecurityEnforcer :: Nil
    ).await

    (result \ "errors").isInstanceOf[JsDefined] must_== true
  }

  "security enforcer allows authorized requests" in new Scope with WithSecureContext {
    val query =
      graphql"""
        query {
          secureContent
        }
        """

    val securedQuery = ObjectType(
      "Query",
      fields[SecureContext, Unit](Field(
        "secureContent",
        StringType,
        tags = GraphQLAuthentication.Authorized :: Nil,
        resolve = c => "success!"
      ))
    )

    val user = mock[User]

    val result = Executor.execute(
      Schema(securedQuery),
      query,
      secureContext,
      middleware = SecurityEnforcer :: Nil
    ).await

    (result \ "errors").isInstanceOf[JsUndefined] must_== true
  }
}
