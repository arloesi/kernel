package kernel.network

import org.vertx.java.core.json._
import org.vertx.java.core.http._
import org.vertx.java.core.sockjs._
import org.vertx.java.core.VertxFactory.newVertx

class Server(val port:Int) {
    val node = newVertx()
    val http = node.createHttpServer()

    http.requestHandler(
        new org.vertx.java.core.Handler[HttpServerRequest] {
            override def handle(request:HttpServerRequest) {
                request.response.end("hello!")
            }
        })

    node.createSockJSServer(http).installApp(new JsonObject().putString("prefix", "/socket"),
        new org.vertx.java.core.Handler[SockJSSocket]() {
            override def handle(socket:SockJSSocket) {
            }
        })

    def start() {
        http.listen(port)
    }

    def stop() {
        node.stop()
    }
}
