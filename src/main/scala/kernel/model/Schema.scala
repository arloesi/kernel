package kernel.model

import java.lang.reflect.AccessibleObject
import java.lang.reflect.ParameterizedType

import java.io._
import java.util.{List,LinkedList,HashMap,LinkedHashMap,Collection,Set}

import scala.collection.JavaConverters._
import scala.collection.JavaConversions._

import javax.xml.bind._
import javax.xml.bind.annotation._
import javax.xml.transform.stream._
import javax.persistence._

import org.codehaus.jettison._
import org.codehaus.jettison.mapped._

import org.eclipse.persistence.internal.sessions.AbstractSession
import org.eclipse.persistence.descriptors.ClassDescriptor

import org.eclipse.persistence.internal.descriptors.{InstanceVariableAttributeAccessor,MethodAttributeAccessor,PersistenceEntity}
import org.eclipse.persistence.internal.jpa.{EntityManagerImpl,EntityManagerFactoryImpl}
import org.eclipse.persistence.mappings.DatabaseMapping
import org.eclipse.persistence.queries.{FetchGroup,FetchGroupTracker}
import org.eclipse.persistence.jaxb.{MarshallerProperties,UnmarshallerProperties,JAXBMarshaller,JAXBUnmarshaller,JAXBHelper,ObjectGraph,Subgraph}

import org.vertx.java.core.json._

import kernel.model.Utilities._
import kernel.schema._

object Schema {
  val MEDIA_TYPE = "application/json"

  def schemaName(`type`:Class[_], view:Class[_]):String =
      "/"+`type`.getSimpleName().toLowerCase()+"/"+view.getSimpleName().toLowerCase()
}