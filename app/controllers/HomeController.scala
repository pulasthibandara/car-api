package controllers

import javax.inject._

import scala.util.{Failure, Success}
import play.api.mvc._
import play.api.libs.json._
import models._
import user.UserDAO
import sangria.parser.QueryParser
import sangria.ast.Document
import sangria.execution._
import sangria.marshalling.playJson._

import scala.concurrent.{ExecutionContext, Future}

/**
 * This controller creates an `Action` to handle HTTP requests to the
 * application's home page.
 */
@Singleton
class HomeController @Inject()(userRepository: UserDAO, cc: ControllerComponents)
                              (implicit exec: ExecutionContext) extends AbstractController(cc) {

  /**
   * Create an Action to render an HTML page with a welcome message.
   * The configuration in the `routes` file means that this method
   * will be called when the application receives a `GET` request with
   * a path of `/`.
   */
  def index = Action {
    Ok(views.html.index("Your new application is ready."))
  }

  def graphiql = Action {
    Ok(views.html.graphiql())
  }

  case class GraphQLRequest(query: String, variables: JsObject = Json.obj(), operationName: Option[String])

  implicit val graphQLRequestReeds: Reads[GraphQLRequest] = Json.using[Json.WithDefaultValues].reads[GraphQLRequest]

  def graphQL = Action.async(parse.json[GraphQLRequest]) { implicit request =>
    val GraphQLRequest(query, variables, operationName) = request.body
    QueryParser.parse(query) match {
      case Success(queryAst) =>
        executeGraphQL(queryAst, operationName, variables)
      case Failure(error) =>
        Future.successful(Ok("error :("))
    }
  }

  private def executeGraphQL(query: Document, operation: Option[String], vars: JsObject) = {
    Executor.execute(
      GraphqlSchema.SchemaDefinition,
      query,
      GraphqlContext(userRepository),
      variables = vars,
      operationName = operation
    ).map(Ok(_))
      .recover {
        case error: QueryAnalysisError ⇒ BadRequest(error.resolveError)
        case error: ErrorWithResolver ⇒ InternalServerError(error.resolveError)
      }

  }

}
