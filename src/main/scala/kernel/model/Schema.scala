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

  def unwrapView(anno:kernel.schema.Property,name:Class[_]):Class[_] = {
    val views = new LinkedList[Class[_]]()

    if(anno.view() != classOf[Property.DEFAULT]) {
      views.add(anno.view())
    }

    if(anno.views().size > 0) {
      views.addAll(anno.views().toList)
    }

    for(i <- views) {
      if(i.isAssignableFrom(name) || name.isAssignableFrom(i)) {
        val anno = i.getAnnotation(classOf[View])

        if(anno != null && anno.target() != classOf[View.DEFAULT]) {
          return anno.target()
        } else {
          return name
        }
      }
    }

    null
  }

  def unwrapField(a:java.lang.reflect.Field):Class[_] = {
    if(classOf[Collection[_]].isAssignableFrom(a.getType())) {
      a.getGenericType().asInstanceOf[ParameterizedType]
        .getActualTypeArguments()(0)
        .asInstanceOf[Class[_]]
    } else {
      a.getType()
    }
  }

  def unwrapGetMethod(a:java.lang.reflect.Method):Class[_] = {
    if(classOf[Collection[_]].isAssignableFrom(a.getReturnType())) {
      a.getGenericReturnType().asInstanceOf[ParameterizedType]
        .getActualTypeArguments()(0)
        .asInstanceOf[Class[_]]
    } else {
      a.getReturnType()
    }
  }

  def unwrapMapping(mapping:DatabaseMapping) = {
    mapping.getAttributeAccessor() match {
      case a:MethodAttributeAccessor => (a.getGetMethod(),unwrapGetMethod(a.getGetMethod()))
      case a:InstanceVariableAttributeAccessor => (a.getAttributeField(),unwrapField(a.getAttributeField()))
    }
  }

  def classMapping(descr:ClassDescriptor):kernel.schema.Mapping = {
    descr.getJavaClass().getAnnotation(classOf[kernel.schema.Mapping])
  }
}