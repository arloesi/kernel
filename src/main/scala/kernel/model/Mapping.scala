package kernel.model

import java.lang.reflect.AccessibleObject
import java.lang.reflect.ParameterizedType

import java.io._
import java.util.{LinkedList,LinkedHashMap,Collection,Set}

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

import kernel.model.Common._
import kernel.schema._

object Mapping {
  val typeName = Map(
      classOf[String] -> "string",
      classOf[Int] -> "integer",
      classOf[Long] -> "integer",
      classOf[Float] -> "number")

  def createGraph[T](builder:Mapping.Builder[T], classes:Set[Class[_]],session:AbstractSession,views:Set[Class[_]]) = {
    val mappings = new LinkedHashMap[String,Mapping[T]]()

    for(i <- classes) {
      i.getAnnotation(classOf[kernel.schema.Mapping]) match {
        case null => ()
        case anno:Serializable => {
          for(v <- anno.views()) {
            if(views.contains(v)) {
              val mapping = new Mapping(mappings,builder,session.getClassDescriptor(i),i,v)
              mappings.put(mapping.name,mapping)
            }
          }
        }
      }
    }

    mappings
  }

  trait Builder[T] {
    def build(mapping:Mapping[T],mappings:java.util.Map[String,Mapping[T]]):T
  }

  class Persistence extends Builder[FetchGroup] {
    def build(mapping:Mapping[FetchGroup],mappings:java.util.Map[String,Mapping[FetchGroup]]):FetchGroup = {
            val root = new FetchGroup()

        def addSubGroup(mapping:Mapping[FetchGroup],group:FetchGroup) {
        for(i <- mapping.properties) {
            if(i.schema != null) {
            val node = new FetchGroup()
            addFetchGroupAttribute(group, i.mapping.getAttributeName(), node)
            addSubGroup(mappings.get(i.schema),node)
            } else {
            group.addAttribute(i.mapping.getAttributeName())
            }
        }
        }

        addSubGroup(mapping,root)

        root
    }
  }

  class MarshallGraph(val graph:ObjectGraph, val schema:JsonObject) {
  }

  class Marshalling(context:JAXBContext) extends Builder[MarshallGraph] {
    def build(mapping:Mapping[MarshallGraph],mappings:java.util.Map[String,Mapping[MarshallGraph]]):MarshallGraph = {
        val root = new MarshallGraph(JAXBHelper.getJAXBContext(context).createObjectGraph(mapping.`type`), new JsonObject())

        def addSubGroup(mapping:Mapping[MarshallGraph],root:Subgraph) {
            for(i <- mapping.properties) {
                if(i.schema != null) {
                    val node = root.addSubgraph(i.mapping.getAttributeName())
                    addSubGroup(mappings.get(i.schema),node)
                } else {
                    root.addAttributeNodes(i.mapping.getAttributeName())
                }
            }
        }

        for(i <- mapping.properties) {
            if(i.schema != null) {
                val node = root.graph.addSubgraph(i.mapping.getAttributeName())
                addSubGroup(mappings.get(i.schema),node)
            } else {
                root.graph.addAttributeNodes(i.mapping.getAttributeName())
            }
        }

        val json = root.schema
        json.putString("id", mapping.name)
        json.putString("type","object")
        val props = new JsonObject()
        json.putObject("properties", props)

        for(i <- mapping.properties) {
            val p = new JsonObject()

            val x =
              if(i.mapping.isCollectionMapping()) {
                p.putString("type","array")
                p.putArray("default", new JsonArray())
                val x = new JsonObject()
                p.putObject("items", x)
                x
              } else {
                p.putObject("default", null)
                p
              }

            if(!i.mapping.isRelationalMapping()) {
              x.putString("type", typeName.get(i.`type`))
            } else {
              x.putString("$ref", "/"+i.`type`.getSimpleName().toLowerCase()+"/"+i.view.getSimpleName().toLowerCase()+"#")

              val r = new JsonObject()
              p.putObject("relation", r)
              r.putString("property", i.mapping.getRelationshipPartner().getAttributeName())
            }
        }

        root
    }
  }

  def unwrapView(anno:Property,name:Class[_]):Class[_] = {
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
}

class Mapping[T](mappings:java.util.Map[String,Mapping[T]],builder:Mapping.Builder[T],val descr:ClassDescriptor,val `type`:Class[_],val view:Class[_]) {
  import Mapping._

  class Property(val annotation:kernel.schema.Property,val mapping:DatabaseMapping,val `type`:Class[_],val view:Class[_],val accessor:AccessibleObject) {
    val schema = if(view == null) null else "/"+`type`.getSimpleName().toLowerCase()+"/"+view.getSimpleName().toLowerCase()
  }

  val name = "/"+`type`.getSimpleName().toLowerCase()+"/"+view.getSimpleName().toLowerCase()
  val properties = new LinkedList[Property]()

  lazy val graph = builder.build(this,mappings)

  for(mapping <- descr.getMappings()) {
    type P = kernel.schema.Property
    // Use unwrap to deal with collections.
    val (accessor,classTy) = unwrapMapping(mapping)

    accessor.getAnnotation(classOf[P]) match {
      case null => ()
      case anno:P => {
        val view = unwrapView(anno,this.view)

        if(view != null) {
          if(classTy.getAnnotation(classOf[kernel.schema.Mapping]) != null) {
            properties.add(new Property(anno,mapping,classTy,view,accessor))
          } else {
            properties.add(new Property(anno,mapping,classTy,null,accessor))
          }
        }
      }
    }
  }
}
