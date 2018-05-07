package vehicle.services

import java.util.UUID

import com.google.inject.{Inject, Singleton}
import vehicle.{Make, Model}
import vehicle.daos.ModelDAO

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class TaxonomyService @Inject() (
  modelDAO: ModelDAO
) (implicit ec: ExecutionContext) {
  def getAllMakes: Future[Seq[Make]]  = modelDAO.getAllMakes

  def getModelsByIds(ids: Seq[UUID]): Future[Seq[Model]] =
    modelDAO.getModels(ids)

  def getModelsByMakes(makeIds: Seq[UUID]): Future[Seq[Model]] =
    modelDAO.getModelsByMakeIds(makeIds)
}
