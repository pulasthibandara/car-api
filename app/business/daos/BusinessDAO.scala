package business.daos

import java.time.Instant
import java.util.UUID

import business.models.Business
import com.google.inject.Inject
import common.database.PgSlickProfile
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}

import scala.concurrent.{ExecutionContext, Future}

case class DBBusiness(id: UUID, name: String, subdomain: Option[String], domain: Option[String], createdAt: Instant = Instant.now)

trait BusinessesTable extends HasDatabaseConfigProvider[PgSlickProfile] {
  import profile.api._

  protected class Businesses(tag: Tag) extends Table[DBBusiness](tag, "businesses") {
    def id = column[UUID]("id", O.PrimaryKey)
    def name = column[String]("name")
    def subdomain = column[Option[String]]("subdomain", O.Unique)
    def domain = column[Option[String]]("domain", O.Unique)
    def createdAt = column[Instant]("created_at")

    def * = (id, name, subdomain, domain, createdAt).mapTo[DBBusiness]
  }

  val businesses = TableQuery[Businesses]
}

class BusinessDAO @Inject() (
  protected val dbConfigProvider: DatabaseConfigProvider
) (implicit ec: ExecutionContext) extends BusinessesTable with BusinessDBImplicitWrappers {
  import profile.api._

  protected def addAction(business: DBBusiness) =
    (businesses returning businesses) += business

  /**
    * Saves an instance of a business.
    */
  def add(business: Business): Future[Business] =
    db.run { addAction(business) } map(_.toBusiness)

  /**
    * Fetches the business by the domain.
    */
  def getByDomain(domain: String): Future[Option[Business]] =
    db.run {
      businesses.filter(_.domain.toLowerCase === domain.toLowerCase)
        .result
        .headOption
    } map(_.map(_.toBusiness))

  /**
    * Fetches the business by the subdomain.
    */
  def getBySubdomain(subdomain: Option[String]): Future[Option[Business]] =
    db.run {
      businesses.filter(_.subdomain.toLowerCase === subdomain.map(_.toLowerCase))
        .result
        .headOption
    } map(_.map(_.toBusiness))

  /**
    * Fetches the business by its id.
    */
  def getById(id: UUID): Future[Option[Business]] =
    db.run {
      businesses.filter(_.id === id)
        .result
        .headOption
    } map(_.map(_.toBusiness))

}

trait BusinessDBImplicitWrappers {
  implicit class ToDBBusiness(b: Business) {
    def toDBBusiness: DBBusiness = DBBusiness(
      id = b.id,
      name = b.name,
      subdomain = b.subdomain,
      domain = b.domain
    )
  }

  implicit class ToBusiness(b: DBBusiness) {
    def toBusiness: Business = Business(
      id = b.id,
      name = b.name,
      subdomain = b.subdomain,
      domain = b.domain
    )
  }

  implicit def toBusiness(b: DBBusiness): Business = Business(
    id = b.id,
    name = b.name,
    subdomain = b.subdomain,
    domain = b.domain
  )

  implicit def toDBBusiness(b: Business): DBBusiness = DBBusiness(
    id = b.id,
    name = b.name,
    subdomain = b.subdomain,
    domain = b.domain
  )
}
