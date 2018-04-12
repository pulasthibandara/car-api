package vehicle

import java.util.UUID

import com.google.inject.Inject
import vehicle.daos.{ListingDAO, ModelDAO}
import common.Sluggify
import Sluggify.StringOps._

import scala.concurrent.{ExecutionContext, Future}

class ListingService @Inject() (
  listingDAO: ListingDAO,
  modelDAO: ModelDAO
) (implicit ec: ExecutionContext) {
  def createListing(id: Option[UUID], title: String, description: String, modelId: UUID, userId: UUID): Future[Listing] =
    for {
      // retrieve model or throw not found error
      model <- modelDAO.getModel(modelId)
        .map(_.fold(throw VehicleModelNotFoundException(s"Cannot find model: ${modelId.toString}"))(identity))

      // generate listing slug
      slug <- resolveSlug(title, userId)

      _id = id.getOrElse(UUID.randomUUID)
      listing = Listing(_id, model.make.id, model.id, userId, title, description, slug, None)

      // save the new listing
      _ <- listingDAO.save(listing)

      // upsert user model mapping
      _ <- modelDAO.upsertUserModelMapping(modelId, userId)
    } yield listing

  /**
    * Resolves a slug that doesn't already exist.
    */
  protected  def resolveSlug(title: String, userId: UUID): Future[String] = listingDAO
    .getAllSlugsStartingWithSlug(title.slug, userId)
    .map { slugs => Sluggify.resolveSlug(title, slugs) }

}
