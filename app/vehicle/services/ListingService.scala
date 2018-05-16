package vehicle.services

import java.util.UUID

import com.google.inject.Inject
import core.Sluggify
import core.Sluggify.StringOps._
import core.storage.daos.FileDAO
import core.storage.models.{File, ImageProperties}
import core.storage.providers.CloudinaryStorageProvider
import graphql.middleware.FileUpload
import user.User
import vehicle._
import vehicle.daos.{ListingDAO, ModelDAO}

import scala.concurrent.{ExecutionContext, Future}

class ListingService @Inject() (
  listingDAO: ListingDAO,
  modelDAO: ModelDAO,
  fileDAO: FileDAO,
  cloudinaryStorageProvider: CloudinaryStorageProvider
) (implicit ec: ExecutionContext) {

  def createListing(
    id: Option[UUID],
    businessId: UUID,
    userId: UUID,
    modelId: UUID,
    title: String,
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
  ): Future[Listing] =
    for {
      // retrieve model or throw not found error
      model <- modelDAO.getModel(modelId)
        .map(_.fold(throw VehicleModelNotFoundException(s"Cannot find model: ${modelId.toString}"))(identity))

      // generate listing slug
      slug <- resolveSlug(title, businessId)

      _id = id.getOrElse(UUID.randomUUID)

      listing = Listing(_id, model.make.get.id, model.id, businessId, title, slug, description, year, kilometers,
        color, bodyType, fuelType, transmissionType, cylinders, engineSize, conditionType, features, Nil, userId, None)

      // save the new listing
      createdListing <- listingDAO.save(listing)

      // upsert user model mapping
      _ <- modelDAO.upsertUserModelMapping(modelId, businessId)
    } yield createdListing

  /**
    * Resolves a slug that doesn't already exist.
    */
  protected  def resolveSlug(title: String, businessId: UUID): Future[String] = listingDAO
    .getAllSlugsStartingWithSlug(title.slug, businessId)
    .map { slugs => Sluggify.resolveSlug(title, slugs) }

  /**
    * Fetch a listing for a specific business id
    */
  def listingsByBusinessId(businessId: UUID): Future[Seq[Listing]] = listingDAO
    .getByBusiness(businessId)


  def addImage(
    id: Option[UUID],
    listingId: UUID,
    file: FileUpload,
    user: User): Future[ImageFileRef] = {

    val fileId = id.getOrElse(UUID.randomUUID())

    for {
      // store file in provider
      storedFile <- cloudinaryStorageProvider.saveBusinessFile(file.file, businessId = user.businessId.get, fileId = fileId)

      // save in files table
      savedFile <- fileDAO.save[ImageProperties](File(
        id = fileId,
        mimeType = file.contentType,
        provider = cloudinaryStorageProvider.Provider,
        providerId = storedFile.id,
        slug = None,
        properties = Some(ImageProperties(file.filename, size = file.fileSize)),
        businessId = user.businessId.get,
        createdBy = user.id
      ))

      // add to listing if listing is defined
      fileRef <- listingDAO.addFiles(listingId, Seq(ImageFileRef(fileId, None))).map(_.head)
    } yield fileRef.copy(file = Some(savedFile))
  }

}
