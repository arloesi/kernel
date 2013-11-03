package kernel.network

import org.vertx.java.core._
import org.vertx.java.core.json._
import org.vertx.java.core.http._

object Request {
    class Handler(prefix:String) extends org.vertx.java.core.Handler[HttpServerRequest] {
        override def handle(request:HttpServerRequest) {
            println("http: "+request.path())
            request.response.sendFile(prefix+"/"+request.path())
        }
    }
}
