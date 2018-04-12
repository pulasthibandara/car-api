package vehicle.daos

import java.time.Instant
import java.util.UUID

import com.google.inject.{Inject, Singleton}
import common.MappedDBTypes
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile
import vehicle.Listing

import scala.concurrent.{ExecutionContext, Future}

case class DBListing(
  id: UUID,
  makeId: UUID,
  modelId: UUID,
  userId: UUID,
  title: String,
  description: String,
  slug: String,
  createdAt: Option[Instant] = None)

trait ListingTable extends HasDatabaseConfigProvider[JdbcProfile] with MappedDBTypes {
  import profile.api._

  protected class ListingsTable(tag: Tag) extends Table[DBListing](tag, "LISTINGS") {
    def id = column[UUID]("ID", O.PrimaryKey)
    def makeId = column[UUID]("MAKE_ID")
    def modelId = column[UUID]("MODEL_ID")
    def userId = column[UUID]("User_ID")
    def title = column[String]("TITLE")
    def description = column[String]("DESCRIPTION")
    def slug = column[String]("SLUG")
    def createdAt = column[Option[Instant]]("CREATED_AT")

    def * = (id, makeId, modelId, userId, title, description, slug, createdAt).mapTo[DBListing]
  }

  val listings = TableQuery[ListingsTable]
}

@Singleton
class ListingDAO @Inject() (
  protected val dbConfigProvider: DatabaseConfigProvider
) (implicit ec: ExecutionContext) extends ListingTable {
  import profile.api._

  /**
    * Saves and returns the saved listing.
    */
  def save(listing: Listing): Future[Listing] = {
    val toInsert = DBListing(
      id = listing.id,
      makeId = listing.makeId,
      modelId = listing.modelId,
      userId = listing.userId,
      title = listing.title,
      description = listing.description,
      slug = listing.slug
    )

    db.run {
      (listings returning listings) += toInsert
    }.map(listing =>
      Listing(
        id = listing.id,
        makeId = listing.makeId,
        modelId = listing.modelId,
        userId = listing.userId,
        title = listing.title,
        description = listing.description,
        slug = listing.slug,
        createdAt = listing.createdAt
      )
    )
  }

  /**
    * Returns all slugs starting with the given slug.
    */
  def getAllSlugsStartingWithSlug(slug: String, userId: UUID): Future[Seq[String]] = db.run {
    listings.filter(_.slug.startsWith(slug))
      .filter(_.userId === userId)
      .map(_.slug)
      .result
  }
}
