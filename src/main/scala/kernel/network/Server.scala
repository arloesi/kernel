package kernel.network

import org.vertx.java.core.json._
import org.vertx.java.core.VertxFactory.newVertx

class Server(val port:Int) {
    val node = newVertx()
    val http = node.createHttpServer()
    val sock = node.createSockJSServer(http)

    http.requestHandler(null)
    sock.installApp(new JsonObject().putString("prefix", "/socket"), null)

    def start() {
        http.listen(port)
    }

    def stop() {
        node.stop()
    }
}
