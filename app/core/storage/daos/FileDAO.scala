package core.storage.daos

import java.time.Instant
import java.util.UUID

import com.google.inject.{Inject, Singleton}
import core.database.PgSlickProfile
import core.TypeTransformer._
import core.storage.models.{File, FileProperties}
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import play.api.libs.json._

import scala.concurrent.{ExecutionContext, Future}

case class DBFile (
  id: UUID,
  mimeType: Option[String],
  provider: String,
  providerId: Option[String],
  slug: Option[String],
  properties: JsValue,
  businessId: UUID,
  createdBy: UUID,
  createdAt: Instant = Instant.now
)

object DBFile {
  implicit def toDBfile[T <: FileProperties](
    file: File[T])(implicit propWrites: Writes[T]): DBFile = DBFile(
    id = file.id,
    mimeType = file.mimeType,
    provider = file.provider,
    providerId = file.providerId,
    slug = file.slug,
    properties = Json.toJson(file.properties),
    businessId = file.businessId,
    createdBy = file.createdBy
  )

  implicit def toFile[T <: FileProperties](
    dbFile: DBFile) (implicit propReads: Reads[T]): File[T] = File[T](
    id = dbFile.id,
    mimeType = dbFile.mimeType,
    provider = dbFile.provider,
    providerId = dbFile.providerId,
    slug = dbFile.slug,
    properties = dbFile.properties.validateOpt[T].get,
    businessId = dbFile.businessId,
    createdBy = dbFile.createdBy,
    createdAt = Some(dbFile.createdAt)
  )
}

trait FileTable extends HasDatabaseConfigProvider[PgSlickProfile] {
  import profile.api._

  protected class FilesTable(tag: Tag) extends Table[DBFile](tag, "files") {
    def id = column[UUID]("id", O.PrimaryKey)
    def mimeType = column[Option[String]]("mime_type")
    def provider = column[String]("provider")
    def providerId = column[Option[String]]("provider_id")
    def slug = column[Option[String]]("slug")
    def properties = column[JsValue]("properties")
    def businessId = column[UUID]("business_id")
    def createdBy = column[UUID]("created_by")
    def createdAt = column[Instant]("created_at")

    def * = (id, mimeType, provider, providerId, slug, properties, businessId, createdBy, createdAt) <>
      ((DBFile.apply _).tupled, DBFile.unapply)
  }

  val files = TableQuery[FilesTable]
}

@Singleton
class FileDAO @Inject() (
  protected val dbConfigProvider: DatabaseConfigProvider
) (implicit  val executionContext: ExecutionContext) extends FileTable {
  import profile.api._

  protected def addAction(dbFile: DBFile) = (files returning files) += dbFile

  /**
    * Saves a file
    */
  def save[T <: FileProperties](file: File[T])
    (implicit propFormats: Format[T]): Future[File[T]] = db.run {
    addAction(file.transformTo[DBFile])
  } map(_.transformTo[File[T]])
}
