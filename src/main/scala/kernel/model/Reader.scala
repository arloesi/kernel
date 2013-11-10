package kernel.model

import java.io._
import java.util._

import scala.collection.JavaConversions._

import javax.xml.bind._

import org.codehaus.jettison._
import org.codehaus.jettison.mapped._

import org.eclipse.persistence.jaxb.{
  MarshallerProperties,UnmarshallerProperties,
  JAXBMarshaller,JAXBUnmarshaller,JAXBHelper,
  ObjectGraph,Subgraph}

import org.vertx.java.core.json._
import kernel.runtime._

class Reader(val context:JAXBContext) {
    def read(value:Object,objectGraph:ObjectGraph):String = {
        val config = new Configuration()
        val json = new StringBuilder()

        def impl(value:Object):String = {
          val buffer = new StringWriter()
          val writer = new MappedXMLStreamWriter(new MappedNamespaceConvention(config),buffer)

          val marshaller = context.createMarshaller()
          marshaller.setProperty(MarshallerProperties.MEDIA_TYPE, Mapping.Schema.MEDIA_TYPE)
          marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true)
          marshaller.setProperty(MarshallerProperties.JSON_INCLUDE_ROOT, false)
          marshaller.setProperty(MarshallerProperties.OBJECT_GRAPH, objectGraph)
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
}
