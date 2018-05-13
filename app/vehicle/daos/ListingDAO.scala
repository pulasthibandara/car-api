package vehicle.daos

import java.time.Instant
import java.util.UUID

import com.google.inject.{Inject, Singleton}
import core.MappedDBTypes
import core.database.PgSlickProfile
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import vehicle._

import scala.concurrent.{ExecutionContext, Future}

case class DBListing(
  id: UUID,
  makeId: UUID,
  modelId: UUID,
  businessId: UUID,
  title: String,
  slug: String,
  description: String,
  year: Option[Int],
  kilometers: Option[Long],
  color: Option[String],
  bodyType: Option[BodyType.Value],
  fuelType: Option[FuelType.Value],
  transmissionType: Option[TransmissionType.Value],
  cylinders: Option[Int],
  engineSize: Option[Int],
  conditionType: Option[ConditionType.Value],
  features: List[String],
  createdBy: UUID,
  createdAt: Instant = Instant.now)

trait ListingTable extends HasDatabaseConfigProvider[PgSlickProfile] with VehicleEnumDBMappings {
  import profile.api._

  protected class ListingsTable(tag: Tag) extends Table[DBListing](tag, "listings") {
    def id = column[UUID]("id", O.PrimaryKey)
    def makeId = column[UUID]("make_id")
    def modelId = column[UUID]("model_id")
    def businessId = column[UUID]("business_id")
    def title = column[String]("title")
    def slug = column[String]("slug")
    def description = column[String]("description")
    def year = column[Option[Int]]("year")
    def kilometers = column[Option[Long]]("kilometers")
    def color = column[Option[String]]("color")
    def bodyType = column[Option[BodyType.Value]]("body_type")
    def fuelType = column[Option[FuelType.Value]]("fuel_type")
    def transmissionType = column[Option[TransmissionType.Value]]("transmission_type")
    def cylinders = column[Option[Int]]("cylinders")
    def engineSize = column[Option[Int]]("engine_size")
    def conditionType = column[Option[ConditionType.Value]]("condition_type")
    def features = column[List[String]]("features")
    def createdBy = column[UUID]("created_by")
    def createdAt = column[Instant]("created_at")

    def * = (id, makeId, modelId, businessId, title, slug, description, year, kilometers, color,
      bodyType, fuelType, transmissionType, cylinders, engineSize, conditionType, features, createdBy, createdAt) <>
      (DBListing.tupled, DBListing.unapply)
  }

  val listings = TableQuery[ListingsTable]
}

@Singleton
class ListingDAO @Inject() (
  protected val dbConfigProvider: DatabaseConfigProvider
) (implicit ec: ExecutionContext) extends ListingTable {
  import profile.api._

  protected def listingsByBusinessQuery(businessId: UUID) = for {
    l <- listings if l.businessId === businessId
  } yield l

  /**
    * Saves and returns the saved listing.
    */
  def save(listing: Listing): Future[Listing] = {
    val toInsert = DBListing(
      id = listing.id,
      makeId = listing.makeId,
      modelId = listing.modelId,
      businessId = listing.businessId,
      title = listing.title,
      slug = listing.slug,
      description = listing.description,
      year = listing.year,
      kilometers = listing.kilometers,
      color = listing.color,
      bodyType = listing.bodyType,
      fuelType = listing.fuelType,
      transmissionType = listing.transmissionType,
      cylinders = listing.cylinders,
      engineSize = listing.engineSize,
      conditionType = listing.conditionType,
      features = listing.features,
      createdBy = listing.createdBy
    )

    db.run {
      listings += toInsert
    }.map(_ =>
      listing.copy(createdAt = Some(toInsert.createdAt))
    )
  }

  /**
    * Returns all slugs starting with the given slug.
    */
  def getAllSlugsStartingWithSlug(slug: String, businessId: UUID): Future[Seq[String]] = db.run {
    listings.filter(_.slug.startsWith(slug))
      .filter(_.businessId === businessId)
      .map(_.slug)
      .result
  }

  /**
    * Fetch listings for a given business
    */
  def getByBusiness(businessId: UUID): Future[Seq[Listing]] = db.run {
    listingsByBusinessQuery(businessId).result
  } map { listings: Seq[DBListing] =>
    listings.map(l => Listing(
      id = l.id,
      makeId = l.makeId,
      modelId = l.modelId,
      businessId = l.businessId,
      title = l.title,
      slug = l.slug,
      description = l.description,
      year = l.year,
      kilometers = l.kilometers,
      color = l.color,
      bodyType = l.bodyType,
      fuelType = l.fuelType,
      transmissionType = l.transmissionType,
      cylinders = l.cylinders,
      engineSize = l.engineSize,
      conditionType = l.conditionType,
      features = l.features,
      createdBy = l.createdBy,
      createdAt = Some(l.createdAt)
    ))
  }

}
