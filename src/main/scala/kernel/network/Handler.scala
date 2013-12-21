package kernel.network

import org.vertx.java.core._
import org.vertx.java.core.json._
import org.vertx.java.core.http._

object Handler {
  private type Base = org.vertx.java.core.Handler[HttpServerRequest]

  class Static(source:String,target:String) extends Base {
    override def handle(request:HttpServerRequest) {
      val path = source+"/"+request.path().substring(target.length)
      request.response.sendFile(path)
    }
  }

  class Html(source:String) extends Base {
    override def handle(request:HttpServerRequest) {
      val path = source+"/"+request.path()+".html"
      request.response().sendFile(path)
    }
  }
}
