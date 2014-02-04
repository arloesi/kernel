package kernel.network

import org.vertx.java.core._
import org.vertx.java.core.json._
import org.vertx.java.core.http._
import com.google.inject.Inject

class Handler(
  sourcePrefix:String,sourceSuffix:String,
  targetPrefix:String,targetSuffix:String)

  extends org.vertx.java.core.Handler[HttpServerRequest] {

  override def handle(request:HttpServerRequest) {
    val path = sourcePrefix+request.path.substring(
      targetPrefix.length,
      request.path.length-targetSuffix.length)+sourceSuffix
    request.response().sendFile(path)
  }
}

object Handler {
  class Static(source:String,target:String) extends Handler(source,"",target,"")
  class Html(source:String) extends Handler(source,".html","/","")
}
