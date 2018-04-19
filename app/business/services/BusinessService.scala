package business.services

import java.util.UUID

import business.daos.BusinessDAO
import business.models.Business
import com.google.inject.{Inject, Singleton}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class BusinessService @Inject() (
  businessDAO: BusinessDAO
) (implicit ec: ExecutionContext) {

  /**
    * Retrieves the business by Id or throws an Business not found exception
    */
  def getBusinessById(id: UUID): Future[Business] = {
    businessDAO.getById(id).map(_
      .getOrElse(throw new BusinessNotFound(s"Business not found for the give id: ${id.toString}"))
    )
  }

  /**
    * Create business.
    */
  def createBusiness(name: String, domain: Option[String], subdomain: Option[String]): Future[Business] = {
    val business = Business(
      id = UUID.randomUUID(),
      name = name,
      subdomain = subdomain,
      domain = domain
    )

    businessDAO.add(business)
  }
}

case class BusinessNotFound(message: String) extends Exception(message)
