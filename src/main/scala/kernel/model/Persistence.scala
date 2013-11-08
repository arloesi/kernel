package kernel.model

import java.util._
import scala.collection.JavaConversions._
import javax.persistence.Persistence._
import org.eclipse.persistence._
import org.eclipse.persistence.internal.jpa.{EntityManagerImpl,EntityManagerFactoryImpl}

object Persistence {
    class Properties(val properties:HashMap[String,String]) {
    }

    class Factory(properties:Properties) {
        def create(name:String):EntityManagerFactoryImpl = {
            createEntityManagerFactory(name,properties.properties)
              .asInstanceOf[EntityManagerFactoryImpl]
        }
    }
}
