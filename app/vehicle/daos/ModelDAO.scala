package vehicle.daos

import java.time.Instant
import java.util.UUID
import javax.inject.Singleton

import com.google.inject.Inject
import common.database.PgSlickProfile
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
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

trait ModelTable extends HasDatabaseConfigProvider[PgSlickProfile] {
  import profile.api._

  protected class ModelsTable(tag: Tag) extends Table[DBModel](tag, "models") {
    def id = column[UUID]("id", O.PrimaryKey)
    def name = column[String]("name")
    def slug = column[String]("slug")
    def makeId = column[UUID]("make_id")
    def createdAt = column[Instant]("created_at")

    def * = (id, name, slug, makeId).mapTo[DBModel]

    def make = foreignKey("MAKE_FK", makeId, makes)(_.id)
  }

  protected class MakesTable(tag: Tag) extends Table[DBMake](tag, "makes") {
    def id = column[UUID]("id", O.PrimaryKey)
    def name = column[String]("name")
    def slug = column[String]("slug")
    def createdAt = column[Instant]("created_at")

    def * = (id, name, slug).mapTo[DBMake]
  }

  protected class UserModels(tag: Tag) extends Table[(UUID, UUID)](tag, "user_models") {
    def modelId = column[UUID]("model_id")
    def userId = column[UUID]("user_id")

    def * = (modelId, userId)

    def pk = primaryKey("pk_model_user", (modelId, userId))
    def model = foreignKey("usermodel_model_fk", modelId, models)(_.id)
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

