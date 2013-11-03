package kernel.runtime

import com.google.inject._

import kernel.network._

class Module(port:Int) extends AbstractModule {
    override def configure() {
    }

    @Provides @Singleton
    def provideServer() = {
        new Server(port)
    }
}
