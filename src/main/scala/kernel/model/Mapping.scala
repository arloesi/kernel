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

object Mapping {
  import Schema._

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
