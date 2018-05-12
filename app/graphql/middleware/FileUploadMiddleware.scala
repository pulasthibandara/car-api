package graphql.middleware

import graphql.SecureContext
import play.api.libs.Files.TemporaryFile
import play.api.mvc.MultipartFormData.FilePart
import sangria.execution.{Middleware, MiddlewareFromScalar, MiddlewareQueryContext}
import sangria.ast
import sangria.schema._
import sangria.validation.{ValueCoercionViolation, Violation}

case class UploadedFileNotFound(error: String) extends ValueCoercionViolation(error)

object FileUploadMiddleware extends Middleware[SecureContext]
  with MiddlewareFromScalar[SecureContext] {
  type QueryVal = Unit
  type FileUpload = FilePart[TemporaryFile]

  val FileUploadType = ScalarType[FileUpload]("FileRef",
    Some("The key name of the attached file, The content-type needs to be multipart/form-data to use this type"),
    coerceUserInput = {
      case s: String => Right(new WrapStringToFileUpload(s).asInstanceOf[FileUpload])
      case _ => Left(UploadedFileNotFound("Not a valid file reference"))
    },
    coerceOutput = (f, _) => ast.StringValue(f.toString),
    coerceInput = {
      case ast.StringValue(s, _, _, _, _) => Right(new WrapStringToFileUpload(s).asInstanceOf[FileUpload])
      case _ => Left(UploadedFileNotFound("Not a valid file reference"))
    }
  )

  override def beforeQuery(context: MiddlewareQueryContext[SecureContext, _, _]): QueryVal = ()

  override def afterQuery(queryVal: QueryVal, context: MiddlewareQueryContext[SecureContext, _, _]): Unit = ()

  override def fromScalar(value: Any, inputType: InputType[_], ctx: SecureContext): Option[Either[Violation, Any]] =
    inputType match {
      case FileUploadMiddleware.FileUploadType => Some(getFileFromContext(value.asInstanceOf[WrapStringToFileUpload].key, ctx))
      case _ ⇒ None
    }

  protected def getFileFromContext(key:String, ctx: SecureContext): Either[UploadedFileNotFound, FileUpload] =
    ctx.files.find(_.key == key) match {
      case Some(file) => Right(file)
      case None => Left(UploadedFileNotFound(
        s"""File $key is not found in the context.
           |  Make sure that the content-type is multipart/form-data
           |  and that the file is attached to the $key filed.
         """.stripMargin))
    }

  private class WrapStringToFileUpload(override val key: String) extends FileUpload(key, null, null, null)
}
