package business.services

import java.util.UUID

import akka.actor.ActorSystem
import business.daos.BusinessDAO
import business.models.{Business, BusinessCreated}
import com.google.inject.{Inject, Singleton}
import graphql.middleware.FileUpload
import user.User

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class BusinessService @Inject() (
  businessDAO: BusinessDAO,
  actorSystem: ActorSystem
) (implicit ec: ExecutionContext) {

  /**
    * Retrieves the business by Id or throws an Business not found exception
    */
  def getBusinessById(id: UUID): Future[Business] = {
    businessDAO.getById(id).map(_
      .getOrElse(throw new BusinessNotFound(s"Business not found for the given id: ${id.toString}"))
    )
  }

  /**
    * Create business for the given user.
    */
  def createBusiness(name: String, domain: Option[String], subdomain: Option[String], user: User, logo: Option[FileUpload]): Future[Business] = {
    user.businessId match {
      case None =>
        val business = Business(
          id = UUID.randomUUID(),
          name = name,
          subdomain = subdomain,
          domain = domain
        )

        val maybeBusiness = businessDAO.add(business)

        maybeBusiness.foreach(b => actorSystem.eventStream
          .publish(BusinessCreated(b, user)))

        maybeBusiness
      case Some(b) => throw new UserAlreadyHasBusiness(b)
    }
  }

  def getBusinessByDomain(domain: String): Future[Option[Business]] =
    businessDAO.getByDomain(domain)

  def getBusinessBySubdomain(subdomain: String): Future[Option[Business]] =
    businessDAO.getBySubdomain(Some(subdomain))
}

case class BusinessNotFound(message: String) extends Exception(message)

case class UserAlreadyHasBusiness(b: UUID) extends
  Exception(s"User already has business with id: ${b.toString}")
