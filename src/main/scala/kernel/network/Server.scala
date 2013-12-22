package kernel.network

import org.vertx.java.core._
import org.vertx.java.core.http._

class Server(runtime:Runtime, vert:Vertx, port:Int, http:HttpServer) {
    import runtime.{addShutdownHook,removeShutdownHook}

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
        addShutdownHook(hook)

        synchronized {
          wait()
        }
    }

    def unblock() {
      removeShutdownHook(hook)
      notify()
    }

    def run() {
        start()
        block()
        stop()
    }
}
