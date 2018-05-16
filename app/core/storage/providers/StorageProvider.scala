package core.storage.providers

import java.io.File
import java.util.UUID

import scala.concurrent.Future

case class UploadResult(id: Option[String], version: Option[String])

trait StorageProvider {
  type StorageOptionsType = Map[String, Any]
  type UploadResultType = UploadResult

  protected val defaultStorageOptions: Map[String, Any] = Map()

  def saveBusinessFile(file: File, businessId: UUID, fileId: UUID, options: StorageOptionsType = defaultStorageOptions): Future[UploadResultType] =
    save(file = file, path = toBusinessStoragePath(businessId, fileId), options)

  def save(file: File, path: String, options: StorageOptionsType = defaultStorageOptions): Future[UploadResultType]

  protected def toBusinessStoragePath(businessId: UUID, fileId: UUID) =
    s"business/${businessId.toString}/${fileId.toString}"
}
