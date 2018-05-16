package graphql.middleware

import java.io.File

import graphql.SecureContext
import play.api.libs.json.{Json, Reads}
import play.api.mvc.MultipartFormData.FilePart
import sangria.execution.{Middleware, MiddlewareFromScalar, MiddlewareQueryContext}
import sangria.ast
import sangria.schema._
import sangria.validation.{ValueCoercionViolation, Violation}

import scala.util.Try

case class UploadedFileNotFound(error: String) extends ValueCoercionViolation(error)
case class FileUpload(key: String, filename: String, contentType: Option[String], tempPath: String) {
  lazy val file: File = new File(tempPath)
  lazy val fileSize: Option[Long] = Try(file.length).toOption
}

object FileUpload {
  implicit val fileUploadReads: Reads[FileUpload] = Json.reads[FileUpload]

  implicit val FileUploadType: ScalarType[FileUpload] = ScalarType[FileUpload]("FileRef",
    Some("The key name of the attached file, The content-type needs to be multipart/form-data to use this type"),
    coerceUserInput = {
      case s: String => Right(FileUpload(s, null, null, null))
      case _ => Left(UploadedFileNotFound("Not a valid file reference"))
    },
    coerceOutput = (f, _) => ast.ObjectValue(
      "key" -> ast.StringValue(f.key),
      "filename" -> ast.StringValue(f.filename),
      "contentType" -> f.contentType.map(s => ast.StringValue(s)).getOrElse(ast.NullValue()),
      "tempPath" -> ast.StringValue(f.tempPath)
    ),
    coerceInput = {
      case ast.StringValue(s, _, _, _, _) => Right(FileUpload(s, null, null, null))
      case _ => Left(UploadedFileNotFound("Not a valid file reference"))
    }
  )
}

object FileUploadMiddleware extends Middleware[SecureContext]
  with MiddlewareFromScalar[SecureContext] {
  type QueryVal = Unit

  override def beforeQuery(context: MiddlewareQueryContext[SecureContext, _, _]): QueryVal = ()

  override def afterQuery(queryVal: QueryVal, context: MiddlewareQueryContext[SecureContext, _, _]): Unit = ()

  override def fromScalar(value: Any, inputType: InputType[_], ctx: SecureContext): Option[Either[Violation, Any]] =
    inputType match {
      case FileUpload.FileUploadType => Some(getFileFromContext(value.asInstanceOf[FileUpload].key, ctx))
      case _ ⇒ None
    }

  protected def getFileFromContext(key:String, ctx: SecureContext): Either[UploadedFileNotFound, FileUpload] =
    ctx.files.find(_.key == key) match {
      case Some(FilePart(key, fileName, contentType, ref)) => Right(FileUpload(key, fileName, contentType, ref.path.toString))
      case None => Left(UploadedFileNotFound(
        s"""File $key is not found in the context.
           |  Make sure that the content-type is multipart/form-data
           |  and that the file is attached to the $key filed.
         """.stripMargin))
    }
}
