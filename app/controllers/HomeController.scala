package controllers

import javax.inject._
import com.mohiva.play.silhouette.api.Silhouette
import com.mohiva.play.silhouette.api.actions.UserAwareRequest

import scala.util.{Failure, Success}
import play.api.mvc._
import play.api.libs.json._
import user.{AuthService, DefaultEnv}
import sangria.parser.QueryParser
import sangria.ast.Document
import sangria.execution._
import sangria.marshalling.playJson._
import graphql.middleware.GraphQLAuthentication.SecurityEnforcer
import business.services.BusinessService
import graphql.filters.BusinessFilter
import graphql.middleware.FileUploadMiddleware
import graphql.{GraphQLSchema, SecureContext}
import play.api.libs.Files.TemporaryFile
import play.api.mvc.MultipartFormData.FilePart
import vehicle.services.{ListingService, TaxonomyService}

import scala.concurrent.{ExecutionContext, Future}

/**
 * This controller creates an `Action` to handle HTTP requests to the
 * application's home page.
 */
@Singleton
class HomeController @Inject()(
  cc: ControllerComponents,
  silhouette: Silhouette[DefaultEnv],
  authService: AuthService,
  listingService: ListingService,
  businessService: BusinessService,
  taxonomyService: TaxonomyService,
)(implicit exec: ExecutionContext) extends AbstractController(cc) {

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

  case class GraphQLRequest(
    query: String,
    variables: JsObject = Json.obj(),
    operationName: Option[String],
    files: Seq[FilePart[TemporaryFile]] = Nil
  )

  implicit val fileJsonReads: Reads[Seq[FilePart[TemporaryFile]]] = (_: JsValue) => JsSuccess(Nil)

  implicit val graphQLRequestReeds: Reads[GraphQLRequest] = Json.using[Json.WithDefaultValues].reads[GraphQLRequest]

  val jsonOrMultipartParser: BodyParser[GraphQLRequest] = parse.using { request =>
    if(request.contentType.getOrElse("") == "multipart/form-data") {
      parse.multipartFormData.map(r => GraphQLRequest(
        query = r.dataParts("query").head,
        variables = r.dataParts.get("variables").map { s =>
          Json.parse(s.head).validate[JsObject].get
        } getOrElse (Json.obj()),
        operationName = r.dataParts.get("operationName").map(_.head),
        files = r.files
      ))
    } else {
      parse.json[GraphQLRequest]
    }
  }

  def graphQL = silhouette.UserAwareAction.async(jsonOrMultipartParser) { implicit request =>
    val GraphQLRequest(query, variables, operationName, files) = request.body
    QueryParser.parse(query) match {
      case Success(queryAst) =>
        executeGraphQL(queryAst, operationName, variables, files)
      case Failure(error) =>
        Future.successful(Ok("error :("))
    }
  }

  private def executeGraphQL(query: Document, operation: Option[String], vars: JsObject, files: Seq[FilePart[TemporaryFile]])
    (implicit request: UserAwareRequest[DefaultEnv, GraphQLRequest]) = {
    Executor.execute(
      GraphQLSchema.SchemaDefinition,
      query,
      SecureContext(
        identity = request.identity,
        business = request.attrs.get(BusinessFilter.businessKey),
        authService = authService,
        listingService = listingService,
        businessService = businessService,
        taxonomyService = taxonomyService,
        files = files,
        ec = exec
      ),
      variables = vars,
      operationName = operation,
      middleware = SecurityEnforcer :: FileUploadMiddleware :: Nil,
      deferredResolver = GraphQLSchema.deferredResolvers
    ).map(Ok(_))
      .recover {
        case error: QueryAnalysisError ⇒ BadRequest(error.resolveError)
        case error: ErrorWithResolver ⇒ InternalServerError(error.resolveError)
      }

  }

}
