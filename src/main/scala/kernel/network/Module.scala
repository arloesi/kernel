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
import kernel.service._

class Module(port:Int, source:String, target:String) extends AbstractModule {
    override def configure() {
    }

    @Provides @Singleton
    def provideVertx():Vertx = {
        newVertx()
    }

    @Provides @Singleton
    def provideRequestHandler:Handler.Static = {
        new Handler.Static(source, target)
    }

    @Provides @Singleton
    def provideHttpServer(node:Vertx, handler:RouteMatcher, socket:Socket.Handler):HttpServer = {
        val http = node.createHttpServer().requestHandler(handler)

        node.createSockJSServer(http).installApp(
            new JsonObject().putString("prefix", "/socket"), socket)

        http
    }

    @Provides @Singleton
    def provideServer(vertx:Vertx, server:HttpServer):Server = {
        new Server(Runtime.getRuntime(), vertx, port, server)
    }

    @Provides @Singleton
    def provideServices(services:List[Service]):Map[String,Service] = {
      val map = new LinkedHashMap[String,Service]()

      for(i <- services) {
        map.put(i.name, i)
      }

      map
    }
}
