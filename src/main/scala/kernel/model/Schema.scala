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

  val MEDIA_TYPE = "application/json"

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

  def marshal(value:Object,view:View=null):String = {
    val config = new Configuration()
    val json = new StringBuilder()

    def impl(value:Object):String = {
      val buffer = new StringWriter()
      val writer = new MappedXMLStreamWriter(new MappedNamespaceConvention(config),buffer)

      val marshaller = context.createMarshaller()
      marshaller.setProperty(MarshallerProperties.MEDIA_TYPE, MEDIA_TYPE)
      marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true)
      marshaller.setProperty(MarshallerProperties.JSON_INCLUDE_ROOT, false)
      marshaller.setProperty(MarshallerProperties.OBJECT_GRAPH, view.objectGraph)
      marshaller.marshal(value, writer)

      buffer.toString()
    }

    value match {
      case list:List[_] => {
        val iter = list.iterator()
        json.append("[")

        while(iter.hasNext()) {
          val i = iter.next()
          json.append(impl(i.asInstanceOf[Object]))

          if(iter.hasNext()) {
            json.append(",")
          }
        }

        json.append("]")
      }
      case entity:Object => {
        json.append(impl(entity))
      }
    }

    json.toString()
  }

  def unmarshal[T<:Object](reader:Reader,meta:Class[T],view:View=null):Object = {
    val unmarshaller = context.createUnmarshaller().asInstanceOf[JAXBUnmarshaller]
    unmarshaller.setProperty(UnmarshallerProperties.MEDIA_TYPE, MEDIA_TYPE)
    unmarshaller.setProperty(UnmarshallerProperties.JSON_INCLUDE_ROOT, false)
    unmarshaller.setProperty(UnmarshallerProperties.OBJECT_GRAPH, view.objectGraph)
    unmarshaller.unmarshal(new StreamSource(reader),meta.asInstanceOf[java.lang.reflect.Type]).getValue().asInstanceOf[Object]
  }

  def unmarshalAndMerge[T<:Object](reader:Reader,meta:Class[T],view:View):Object = {
    val session = factory.createEntityManager().asInstanceOf[EntityManagerImpl]
    val entity = unmarshalAndMerge(reader,session,meta,view)
    session.close()
    entity
  }

  def unmarshalAndMerge[T<:Object](reader:Reader,session:EntityManagerImpl,meta:Class[T],view:View):Object = {
    val value = unmarshal(reader,meta,view)
    val descr = session.getAbstractSession().getDescriptor(meta)

    def merge(value:Object):T = {
      def visit(value:Object,group:FetchGroup) {
        val descr = session.getAbstractSession().getDescriptor(value)

        for(m <- descr.getMappings()) {
          group.getGroup(m.getAttributeName()) match {
            case group:FetchGroup => {
              m.getAttributeValueFromObject(value) match {
                case collection:Collection[_] => {
                  collection.foreach(x => visit(x.asInstanceOf[Object],group))
                }
                case entity:Object => {
                  visit(entity,group)
                }
              }
            }
            case _ => ()
          }
        }

        val tracker = value.asInstanceOf[FetchGroupTracker]
        tracker._persistence_setFetchGroup(group)
        tracker._persistence_setSession(session.getAbstractSession())

        val entity = value.asInstanceOf[PersistenceEntity]
        entity._persistence_getId().asInstanceOf[Long] match {
          case 0 => entity._persistence_setId(descr.getObjectBuilder()
              .assignSequenceNumber(entity, session.getAbstractSession()))
          case _ => ()
        }
      }

      visit(value, view.fetchGroup)
      session.getTransaction().begin()
      session.merge(value)
      session.getTransaction().commit()
      session.find(meta,
        value.asInstanceOf[PersistenceEntity]
          ._persistence_getId()
          .asInstanceOf[Long])
    }

    value match {
      case list:List[_] => {
        val result = new LinkedList[T]()

        for(i <- list) {
          result.add(merge(i.asInstanceOf[T]))
        }

        result
      }

      case entity:Object => {
        merge(entity)
      }
    }
  }
}
