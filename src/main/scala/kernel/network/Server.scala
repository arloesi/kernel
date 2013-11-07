package kernel.network

import org.vertx.java.core._
import org.vertx.java.core.http._

class Server(vert:Vertx, port:Int, http:HttpServer) {
    private val hook = new Thread() {
        override def run() {
            unblock()
        }
    }

    def start() {
        http.listen(port)
    }

    def stop() {
        http.close()
    }

    def block() {
        Runtime.getRuntime().addShutdownHook(hook)

        synchronized {
            this.wait()
        }
    }

    def unblock() {
        Runtime.getRuntime().removeShutdownHook(hook)
        this.notify()
    }

    def run() {
        start()
        block()
        stop()
    }
}
