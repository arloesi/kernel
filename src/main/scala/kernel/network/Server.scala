package kernel.network

import org.vertx.java.core._
import org.vertx.java.core.json._
import org.vertx.java.core.http._
import org.vertx.java.core.sockjs._
import org.vertx.java.core.VertxFactory.newVertx

class Server(val node:Vertx, val http:HttpServer, val port:Int, val prefix:String) {
    http.requestHandler(
        new org.vertx.java.core.Handler[HttpServerRequest] {
            override def handle(request:HttpServerRequest) {
                println("http: "+request.path())
                request.response.sendFile(prefix+"/"+request.path())
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
