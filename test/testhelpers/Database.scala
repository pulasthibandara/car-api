package testhelpers

import org.specs2.execute.{ AsResult, Result }
import org.specs2.mutable.Around
import org.specs2.specification.Scope
import play.api.db.DBApi
import play.api.db.evolutions._
import play.api.test.{HasApp, Injecting}

trait DatabaseIsolation extends Around with Scope with Injecting {
  self: HasApp =>

  abstract override def around[T: AsResult](t: => T): Result = {
    super.around {
      val db = inject[DBApi].database("default")
      Evolutions.applyEvolutions(db)
      try AsResult(t)
      finally Evolutions.cleanupEvolutions(db)
    }
  }
}

trait WithDataEvolutions extends Around with Scope with Injecting {
  self: HasApp =>

  def evolutionData: String
  def cleanUp: String = ""

  abstract override def around[T: AsResult](t: => T): Result = {
    super.around {
      val db = inject[DBApi].database("default")
      try {
        Evolutions.applyEvolutions(db, SimpleEvolutionsReader.forDefault(
          Evolution(99, evolutionData, cleanUp)
        ))
        AsResult(t)
      }
      finally Evolutions.cleanupEvolutions(db)
    }
  }
}