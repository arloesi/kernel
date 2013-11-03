package kernel.network

import org.vertx.java.core._
import org.vertx.java.core.json._
import org.vertx.java.core.sockjs._
import org.vertx.java.core.buffer._

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper

object Socket {
    class Handler(mapper:ObjectMapper) extends org.vertx.java.core.Handler[SockJSSocket] {
        override def handle(socket:SockJSSocket) {
            socket.endHandler(new org.vertx.java.core.Handler[Void]() {
              override def handle(void:Void) {
                // disconnect
              }
            })

            socket.dataHandler(new org.vertx.java.core.Handler[Buffer]() {
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
                          case event:Event[Any] => event.subscribe(connection, x => connection.dispatch(event,x))
                        }
                      }

                      case "unsubscribe" => {
                        service.getEvents().get(event) match {
                          case e:Event[Any] => e.unsubscribe(connection)
                        }
                      }

                      case method:String => {
                        service.getMethods().get(method) match {
                          case m:Method => {
                            val params = json.get("params").toString()
                            val s = Socket.this.session.load(session)
                            m.invoke(mapper,connection,params)
                          }
                        }
                      }
                    }
                  }
                }
              }
            })
        }
    }
}

class Socket {
}
