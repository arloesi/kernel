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

class Writer(context:JAXBContext,factory:EntityManagerFactoryImpl) {
  def write[T<:Object](reader:java.io.Reader, meta:Class[T], objectGraph:ObjectGraph):Object = {
    val unmarshaller = context.createUnmarshaller().asInstanceOf[JAXBUnmarshaller]
    unmarshaller.setProperty(UnmarshallerProperties.MEDIA_TYPE, Schema.MEDIA_TYPE)
    unmarshaller.setProperty(UnmarshallerProperties.JSON_INCLUDE_ROOT, false)
    unmarshaller.setProperty(UnmarshallerProperties.OBJECT_GRAPH, objectGraph)
    unmarshaller.unmarshal(new StreamSource(reader), meta.asInstanceOf[java.lang.reflect.Type]).getValue().asInstanceOf[Object]
  }

  def write[T<:Object](reader:java.io.Reader, meta:Class[T], objectGraph:ObjectGraph, fetchGroup:FetchGroup):Object = {
    val session = factory.createEntityManager().asInstanceOf[EntityManagerImpl]
    val entity = write(reader,session,meta,objectGraph,fetchGroup)
    session.close()
    entity
  }

  def write[T<:Object](reader:java.io.Reader, session:EntityManagerImpl, `type`:Class[T], objectGraph:ObjectGraph, fetchGroup:FetchGroup):Object = {
    val value = write(reader,`type`,objectGraph)
    val descr = session.getAbstractSession().getDescriptor(`type`)

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
          case 0 => {
              entity._persistence_setId(descr.getObjectBuilder()
                  .assignSequenceNumber(entity, session.getAbstractSession()))
              // emit create event
          }
          case _ => {
              // emit update event
          }
        }
      }

      visit(value, fetchGroup)
      session.getTransaction().begin()
      session.merge(value)
      session.getTransaction().commit()
      session.find(`type`,
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
