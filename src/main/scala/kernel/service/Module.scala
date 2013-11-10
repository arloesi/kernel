package kernel.service

import java.util._
import scala.collection.JavaConversions._

import com.google.inject._

class Module extends AbstractModule {
    override def configure() {
    }

    @Provides @Singleton
    def provideServices(services:List[Service]):Map[String,Service] = {
        val map = new HashMap[String,Service]()

        for(service <- services) {
            map.put(service.name, service)
        }

        map
    }
}
