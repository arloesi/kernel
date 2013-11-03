package kernel.content

import java.util._
import scala.collection.JavaConversions._

import javax.persistence._
import javax.xml.bind._

import org.eclipse.persistence._
import org.eclipse.persistence.internal.jpa.{EntityManagerImpl,EntityManagerFactoryImpl}

object Schema {
  class Factory(schema:Schema) {
    def create() = {schema.openSession()}
  }
}

class Schema(source:String,properties:Map[String,String]) {
  def this(source:String) = this(source,null)

  lazy val factory =
    Persistence.createEntityManagerFactory(source,properties)
      .asInstanceOf[EntityManagerFactoryImpl]

  lazy val classes:Array[Class[_]] =
    factory.getDatabaseSession().getDescriptors().keySet().map(e => e).toArray

  lazy val mappings:Map[String,Class[_]] = {
    val mappings = new HashMap[String,Class[_]]()
    for (i <- classes) { mappings.put(i.getSimpleName().toLowerCase(),i)}
    mappings
  }

  def getMapping(name:String):Class[_] = {
    mappings.get(name)
  }

  def openSession() = {
    factory.createEntityManager().asInstanceOf[EntityManagerImpl]
  }
}