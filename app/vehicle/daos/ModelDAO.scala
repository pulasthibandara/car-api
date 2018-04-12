package vehicle.daos

import java.time.Instant
import java.util.UUID
import javax.inject.Singleton

import com.google.inject.Inject
import common.MappedDBTypes
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile
import vehicle.{Make, Model}

import scala.concurrent.{ExecutionContext, Future}

case class DBModel(
  id: UUID,
  name: String,
  slug: String,
  makeId: UUID)

case class DBMake(
  id: UUID,
  name: String,
  slug: String)

trait ModelTable extends HasDatabaseConfigProvider[JdbcProfile] with MappedDBTypes {
  import profile.api._

  protected class ModelsTable(tag: Tag) extends Table[DBModel](tag, "MODELS") {
    def id = column[UUID]("ID", O.PrimaryKey)
    def name = column[String]("NAME")
    def slug = column[String]("SLUG")
    def makeId = column[UUID]("MAKE_ID")
    def createdAt = column[Instant]("CREATED_AT")

    def * = (id, name, slug, makeId).mapTo[DBModel]

    def make = foreignKey("MAKE_FK", makeId, makes)(_.id)
  }

  protected class MakesTable(tag: Tag) extends Table[DBMake](tag, "MAKES") {
    def id = column[UUID]("ID", O.PrimaryKey)
    def name = column[String]("NAME")
    def slug = column[String]("SLUG")
    def createdAt = column[Instant]("CREATED_AT")

    def * = (id, name, slug).mapTo[DBMake]
  }

  protected class UserModels(tag: Tag) extends Table[(UUID, UUID)](tag, "USER_MODELS") {
    def modelId = column[UUID]("MODEL_ID")
    def userId = column[UUID]("USER_ID")

    def * = (modelId, userId)

    def model = foreignKey("MODEL_FK", modelId, models)(_.id)
  }

  val models = TableQuery[ModelsTable]
  val makes = TableQuery[MakesTable]
  val userModels = TableQuery[UserModels]
}

@Singleton
class ModelDAO @Inject() (
  protected val dbConfigProvider: DatabaseConfigProvider
) (implicit ec: ExecutionContext) extends ModelTable {
  import profile.api._

  protected def modelQuery(id: UUID) = models.filter(_.id === id)
    .join(makes)
    .on(_.makeId === _.id)
    .take(1)

  /**
    * Get model and make by model Id.
    */
  def getModel(id: UUID): Future[Option[Model]] = db.run { modelQuery(id).result.headOption }
    .map(maybeModel => maybeModel.map {
      case (model, make) => Model(
        id = model.id,
        name = model.name,
        slug = model.slug,
        make = Make(
          id = make.id,
          name = make.name,
          slug = make.slug
        )
      )
    })

  /**
    * Insert user model mapping if not exist
    */
  def upsertUserModelMapping(modelId: UUID, userId: UUID) = db.run {
    userModels.insertOrUpdate(modelId, userId)
  }
}

