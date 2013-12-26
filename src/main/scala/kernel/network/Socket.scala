package kernel.network

import java.util._
import scala.collection.JavaConversions._

import org.vertx.java.core._
import org.vertx.java.core.json._
import org.vertx.java.core.sockjs._
import org.vertx.java.core.buffer._

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.google.inject.Inject

import kernel.service._
import kernel.runtime._

object Socket {
  type VoidHandler = org.vertx.java.core.Handler[Void]
  type BufferHandler = org.vertx.java.core.Handler[Buffer]
  type SockJSHandler = org.vertx.java.core.Handler[SockJSSocket]

  class ConnectHandler @Inject() (mapper:ObjectMapper, services:Map[String,Service]) extends SockJSHandler {
    override def handle(socket:SockJSSocket) {
      socket.dataHandler(new MessageHandler(socket, mapper, services))
      socket.endHandler(new DisconnectHandler(socket))
    }
  }

  class DisconnectHandler(socket:SockJSSocket) extends VoidHandler {
    override def handle(void:Void) {
    }
  }

  class MessageHandler(socket:SockJSSocket, mapper:ObjectMapper, services:Map[String,Service]) extends BufferHandler {
    override def handle(buffer:Buffer) {
      val json = mapper.readTree(buffer.toString())

      val session = json.get("session").textValue()
      val service = json.get("service").textValue()
      val method = json.get("method").textValue()

      val event = json.get("event") match {
        case event:JsonNode => event.textValue()
        case null => null
      }

      services.get(service) match {
        case service:Service => {
          method match {
            case "subscribe" => {
              service.getEvents().get(event) match {
                case e:Event[_] => ()
                  // event.subscribe(connection, x => connection.dispatch(event,x))
              }
            }

            case "unsubscribe" => {
              service.getEvents().get(event) match {
                case e:Event[_] => () // e.unsubscribe(connection)
              }
            }

            case method:String => {
              service.getMethods().get(method) match {
                case m:Method => {
                  val params = json.get("params").toString()
                  m.invoke(mapper,new Socket(socket),params)
                }
              }
            }
          }
        }
      }
    }
  }
}

class Socket(socket:SockJSSocket) {
}
