package kernel.network

import java.util._
import scala.collection.JavaConversions._

import com.google.inject._
import com.google.common.collect.Maps._

import org.vertx.java.core._
import org.vertx.java.core.http._
import org.vertx.java.core.json._
import org.vertx.java.core.VertxFactory.newVertx

import kernel.network._

class Module(port:Int) extends AbstractModule {
  import Socket.{ConnectHandler}

  override def configure() {
  }

  @Provides @Singleton
  def provideVertx():Vertx = {
    newVertx()
  }

  @Provides @Singleton
  def provideHttpServer(node:Vertx, handler:RouteMatcher, socket:ConnectHandler):HttpServer = {
    val http = node.createHttpServer().requestHandler(handler)

    node.createSockJSServer(http).installApp(
      new JsonObject().putString("prefix", "/socket"), socket)

    http
  }

  @Provides @Singleton
  def provideServer(runtime:Runtime, vertx:Vertx, server:HttpServer):Server = {
    new Server(runtime, vertx, port, server)
  }
}
