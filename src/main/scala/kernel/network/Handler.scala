package kernel.network

import org.vertx.java.core._
import org.vertx.java.core.json._
import org.vertx.java.core.http._

class Handler(source:String,target:String) extends org.vertx.java.core.Handler[HttpServerRequest] {
    override def handle(request:HttpServerRequest) {
      val path = source+"/"+request.path().substring(target.length)
      println("source: "+request.path())
      println("target: "+path)
      request.response.sendFile(path)
    }
}
