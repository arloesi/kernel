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

import kernel.runtime._

object Socket {
  type Service = Object

  type VoidHandler = org.vertx.java.core.Handler[Void]
  type BufferHandler = org.vertx.java.core.Handler[Buffer]
  type SockJSHandler = org.vertx.java.core.Handler[SockJSSocket]

  class ConnectHandler @Inject() (mapper:ObjectMapper) extends SockJSHandler {
    override def handle(socket:SockJSSocket) {
      socket.dataHandler(new MessageHandler(socket, mapper))
      socket.endHandler(new DisconnectHandler(socket))
    }
  }

  class DisconnectHandler(socket:SockJSSocket) extends VoidHandler {
    override def handle(void:Void) {
    }
  }

  class MessageHandler(socket:SockJSSocket, mapper:ObjectMapper) extends BufferHandler {
    override def handle(buffer:Buffer) {
    }
  }
}

class Socket(socket:SockJSSocket) {
}
