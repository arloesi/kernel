package kernel.model

import java.lang.reflect.AccessibleObject
import java.lang.reflect.ParameterizedType

import java.io._
import java.util._

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

class Stream(val objectGraph:ObjectGraph, val schema:JsonObject) {
}

object Stream {
    val typeName = scala.collection.Map(
      classOf[String] -> "string",
      classOf[Int] -> "integer",
      classOf[Long] -> "integer",
      classOf[Float] -> "number")

    class Mapping(val mapping:Schema.Mapping,val graph:ObjectGraph, val schema:JsonObject) {
    }

    class Graph(val mappings:java.util.Map[String,Mapping]) {
        def getJsonSchema():String = {
            val json = new JsonObject()

            for(i <- mappings.values()) {
              json.putObject(i.mapping.name, i.schema)
            }

            json.toString()
        }
    }

    class Mapper(context:JAXBContext, schema:Map[String,Schema.Mapping]) {
        val mappings = new HashMap[String,Mapping]()

        for(i <- schema.values()) {
            mappings.put(i.name, build(i))
        }

        def build(mapping:Schema.Mapping):Mapping = {
            val root = new Mapping(mapping, JAXBHelper.getJAXBContext(context).createObjectGraph(mapping.`type`), new JsonObject())

            def addSubGroup(mapping:Schema.Mapping,root:Subgraph) {
                for(i <- mapping.properties) {
                    if(i.schema != null) {
                        val node = root.addSubgraph(i.database.getAttributeName())
                        addSubGroup(schema.get(i.mapping.name),node)
                    } else {
                        root.addAttributeNodes(i.database.getAttributeName())
                    }
                }
            }

            for(i <- mapping.properties) {
                if(i.schema != null) {
                    val node = root.graph.addSubgraph(i.database.getAttributeName())
                    addSubGroup(i.mapping,node)
                } else {
                    root.graph.addAttributeNodes(i.database.getAttributeName())
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
                  if(i.database.isCollectionMapping()) {
                    p.putString("type","array")
                    p.putArray("default", new JsonArray())
                    val x = new JsonObject()
                    p.putObject("items", x)
                    x
                  } else {
                    p.putObject("default", null)
                    p
                  }

                if(!i.database.isRelationalMapping()) {
                  x.putString("type", typeName.get(i.`type`))
                } else {
                  x.putString("$ref", "/"+i.`type`.getSimpleName().toLowerCase()+"/"+i.view.getSimpleName().toLowerCase()+"#")

                  val r = new JsonObject()
                  p.putObject("relation", r)
                  r.putString("property", i.database.getRelationshipPartner().getAttributeName())
                }
            }

            root
        }
    }
}
