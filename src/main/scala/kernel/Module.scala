package kernel

import com.google.inject._

import org.vertx.java.core._
import org.vertx.java.core.http._
import org.vertx.java.core.json._
import org.vertx.java.core.VertxFactory.newVertx

import kernel.network._

class Module(port:Int, assets:String) extends AbstractModule {
    override def configure() {
    }

    @Provides @Singleton
    def provideVertx():Vertx = {
        newVertx()
    }

    @Provides @Singleton
    def provideRequestHandler:Request.Handler = {
        new Request.Handler(assets)
    }

    @Provides @Singleton
    def provideHttpServer(node:Vertx, handler:Request.Handler, socket:Socket.Handler):HttpServer = {
        val http = node.createHttpServer().requestHandler(handler)

        node.createSockJSServer(http).installApp(
            new JsonObject().putString("prefix", "/socket"), socket)

        http
    }

    @Provides @Singleton
    def provideServer(vertx:Vertx, server:HttpServer):Server = {
        new Server(vertx, port, server)
    }
}
