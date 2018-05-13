package vehicle.daos

import java.time.Instant
import java.util.UUID
import javax.inject.Singleton

import com.google.inject.Inject
import core.database.PgSlickProfile
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

  protected class UserModels(tag: Tag) extends Table[(UUID, UUID)](tag, "business_models") {
    def modelId = column[UUID]("model_id")
    def businessId = column[UUID]("business_id")

    def * = (modelId, businessId)

    def pk = primaryKey("pk_model_business", (modelId, businessId))
    def model = foreignKey("businessmodel_model_fk", modelId, models)(_.id)
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

  protected def makesAndModelsQuery() = for {
    make <- makes
    model <- models if model.makeId === make.id
  } yield (make, model)

  /**
    * Get model and make by model Id.
    */
  def getModel(id: UUID): Future[Option[Model]] = db.run { modelQuery(id).result.headOption }
    .map(maybeModel => maybeModel.map {
      case (model, make) => Model(
        id = model.id,
        name = model.name,
        slug = model.slug,
        makeId = make.id,
        make = Some(Make(
          id = make.id,
          name = make.name,
          slug = make.slug
        ))
      )
    })

  def getModels(ids: Seq[UUID]): Future[Seq[Model]] = db.run {
    models.filter(_.id.inSet(ids)).result
  }.map(_.map(m => Model(
    id = m.id,
    name = m.name,
    slug = m.slug,
    makeId = m.makeId,
    make = None
  )))

  def getMakes(ids: Seq[UUID]): Future[Seq[Make]] = db.run {
    makes.filter(_.id.inSet(ids)).result
  }.map(_.map(m => Make(
    id = m.id,
    name = m.name,
    slug = m.slug
  )))

  def getAllMakes(): Future[Seq[Make]] = db.run {
    makes.result
  } map(_.map(m => Make(
    id = m.id,
    name = m.name,
    slug = m.slug
  )))

  def getModelsByMakeIds(ids: Seq[UUID]): Future[Seq[Model]] = db.run {
    models.filter(_.makeId.inSet(ids)).result
  } map(_.map(m => Model(
    id = m.id,
    name = m.name,
    slug = m.slug,
    makeId = m.makeId,
    make = None
  )))

  /**
    * Insert user model mapping if not exist
    */
  def upsertUserModelMapping(modelId: UUID, businessId: UUID) = db.run {
    userModels.insertOrUpdate(modelId, businessId)
  }

}

