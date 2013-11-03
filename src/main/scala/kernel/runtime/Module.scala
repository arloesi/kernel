package kernel.runtime

import com.google.inject._

import org.vertx.java.core._
import org.vertx.java.core.http._
import org.vertx.java.core.VertxFactory.newVertx

import kernel.network._

class Module(port:Int) extends AbstractModule {
    override def configure() {
    }

    @Provides @Singleton
    def provideVertx():Vertx = {
        newVertx()
    }

    @Provides @Singleton
    def provideHttpServer(node:Vertx):HttpServer = {
        node.createHttpServer()
    }

    @Provides @Singleton
    def provideServer(node:Vertx, http:HttpServer):Server = {
        new Server(node, http, port, "dist")
    }
}
