package kernel.model

import java.lang.reflect.ParameterizedType

import java.io._
import java.util._

import scala.collection.JavaConverters._
import scala.collection.JavaConversions._

import javax.xml.bind._
import javax.xml.bind.annotation._
import javax.xml.transform.stream._
import javax.persistence._

import com.google.common.collect.Sets._
import org.codehaus.jettison._
import org.codehaus.jettison.mapped._

import org.eclipse.persistence.internal.sessions.AbstractSession
import org.eclipse.persistence.descriptors.ClassDescriptor

import org.eclipse.persistence.internal.descriptors.{
  InstanceVariableAttributeAccessor,MethodAttributeAccessor,
  PersistenceEntity}

import org.eclipse.persistence.internal.jpa.{
  EntityManagerImpl,EntityManagerFactoryImpl}

import org.eclipse.persistence.mappings.DatabaseMapping

import org.eclipse.persistence.queries.{
  FetchGroup,FetchGroupTracker}

import org.eclipse.persistence.jaxb.{
  MarshallerProperties,UnmarshallerProperties,
  JAXBMarshaller,JAXBUnmarshaller,JAXBHelper,
  ObjectGraph,Subgraph}

import org.vertx.java.core.json._
import kernel.runtime._

object Schema {
    val MEDIA_TYPE = "application/json"

    class Graph(val mappings:Map[String,Mapping[Mapping.MarshallGraph]]) {
        def getJsonSchema():String = {
            val json = new JsonObject()

            for(i <- mappings.values()) {
              json.putObject(i.name, i.graph.schema)
            }

            json.toString()
        }
    }

    class Properties(val properties:Map[String,String]) {
    }
}

class Schema(
    val context:JAXBContext, factory:EntityManagerFactoryImpl,
    storageGraph:Storage.Graph, mapperGraph:Schema.Graph) {

  class View(val fetchGroup:FetchGroup, val objectGraph:Mapping.MarshallGraph) {}

  def getView(name:String):View = {
    val p = storageGraph.mappings.get(name).graph
    val m = mapperGraph.mappings.get(name).graph
    new View(p,m)
  }

  def getView(`class`:String,view:String):View = {
    getView("/"+view+"/"+`class`)
  }

  def getView(`class`:Class[_],view:Class[_]):View = {
    getView(`class`.getSimpleName().toLowerCase(),view.getSimpleName().toLowerCase())
  }
}
