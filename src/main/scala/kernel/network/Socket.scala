package kernel.network

import org.vertx.java.core._
import org.vertx.java.core.json._
import org.vertx.java.core.sockjs._

object Socket {
    class Handler extends org.vertx.java.core.Handler[SockJSSocket] {
        override def handle(socket:SockJSSocket) {
        }
    }
}
