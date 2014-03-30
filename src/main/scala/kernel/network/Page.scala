package kernel.network

import org.vertx.java.core._
import org.vertx.java.core.json._
import org.vertx.java.core.http._
import com.google.inject.Inject

import org.apache.velocity.{Template,VelocityContext}
import org.apache.velocity.app.{VelocityEngine}

import kernel.runtime.Utilities._

class Page(source:String,velocity:VelocityEngine) extends Handler {
  override def handle(request:HttpServerRequest) {
    val buffer = new Buffer()
    val writer = new BufferWriter(buffer)

    val template = velocity.getTemplate(source+"/"+request.path()+".html")
    val context = new VelocityContext()

    template.merge(context, writer)

    request.response().headers().set(Utilities.CONTENT_TYPE, Utilities.CONTENT_HTML)
    request.response().end(buffer)
  }
}