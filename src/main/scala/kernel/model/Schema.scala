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

    val MEDIA_TYPE = "application/json"

    class Property(
        val annotation:kernel.schema.Property,
        val mapping:Mapping,
        val database:DatabaseMapping,
        val `type`:Class[_],val view:Class[_],
        val accessor:AccessibleObject) {

        def this(
            annotation:kernel.schema.Property,
            database:DatabaseMapping,
            `type`:Class[_],
            accessor:AccessibleObject) =
            this(annotation,null,database,`type`,null,accessor)

        val schema =
            if(view == null) null
            else schemaName(`type`,view)
    }

    class Mapping(val descr:ClassDescriptor,val view:Class[_], val properties:List[Property]) {
        def name = schemaName(`type`, view)
        def `type` = descr.getJavaClass()
    }

    class Mapper(session:AbstractSession, views:Set[Class[_]]) {
        val mappings = new HashMap[String,Mapping]()

        for(descr <- session.getDescriptors().values()) {
            for(view <- views) {
                build(descr, view)
            }
        }

        def build(descr:ClassDescriptor, view:Class[_]):Mapping = {
            val schemaKey = schemaName(descr.getJavaClass(), view)

            if(mappings.containsKey(schemaKey)) {
                mappings.get(schemaKey)
            }
            else {
                val anno:kernel.schema.Mapping = descr.getJavaClass().getAnnotation(classOf[kernel.schema.Mapping])

                val properties = new LinkedList[Property]()

                for(mapping <- descr.getMappings()) {
                    type P = kernel.schema.Property
                    // Use unwrap to deal with collections.
                    val (accessor,classTy) = unwrapMapping(mapping)

                    accessor.getAnnotation(classOf[P]) match {
                      case null => ()
                      case anno:P => {
                        val v = unwrapView(anno,view)

                        if(view != null) {
                          if(classTy.getAnnotation(classOf[kernel.schema.Mapping]) != null) {
                              properties.add(new Property(anno,build(mapping.getDescriptor(),v),mapping,classTy,v,accessor))
                          } else {
                              properties.add(new Property(anno,mapping,classTy,accessor))
                          }
                        }
                      }
                    }
                }

                val mapping = new Mapping(descr,view,properties)
                mappings.put(schemaKey, mapping)
                mapping
            }
        }
    }
}
