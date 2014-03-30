package kernel.network

import org.vertx.java.core._
import org.vertx.java.core.json._
import org.vertx.java.core.http._
import com.google.inject.Inject

class Static(sourcePrefix:String,sourceSuffix:String,targetPrefix:String,targetSuffix:String) extends Handler {
  def this(source:String,target:String) = this(source,"",target,"")

  override def handle(request:HttpServerRequest) {
    val path = sourcePrefix+request.path.substring(
      targetPrefix.length,
      request.path.length-targetSuffix.length)+sourceSuffix
    request.response().sendFile(path)
  }
}