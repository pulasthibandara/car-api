package graphql.filters

import akka.stream.Materializer
import business.models.Business
import business.services.BusinessService
import com.google.inject.Inject
import play.api.Configuration
import play.api.libs.typedmap.TypedKey
import play.api.mvc.{Filter, RequestHeader, Result}

import scala.concurrent.{ExecutionContext, Future}

class BusinessFilter @Inject() (
  businessService: BusinessService,
  configuration: Configuration
) (implicit val mat: Materializer, ex: ExecutionContext) extends Filter {

  def apply(nextFilter: RequestHeader => Future[Result])(requestHeader: RequestHeader): Future[Result] = {
    val domain = requestHeader.host.trim.split(":")(0).toLowerCase.stripSuffix(".")
    val subdomain = requestHeader.host.trim.split("\\.")(0).toLowerCase()

    businessService.getBusinessByDomain(domain)
      .flatMap {
        case None => businessService.getBusinessBySubdomain(subdomain)
        case maybeBusiness => Future.successful(maybeBusiness)
      }
      .flatMap {
        case Some(business) => nextFilter(requestHeader
          .addAttr(BusinessFilter.businessKey, business))
        case None => nextFilter(requestHeader)
      }
  }
}

object BusinessFilter {
  val businessKey: TypedKey[Business] = TypedKey("business")
}
