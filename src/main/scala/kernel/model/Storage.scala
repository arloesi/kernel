package kernel.model

import java.util._
import scala.collection.JavaConversions._

import com.google.inject.Inject
import com.google.inject.assistedinject.Assisted

import javax.persistence.Persistence._
import org.eclipse.persistence._
import org.eclipse.persistence.internal.jpa.{EntityManagerImpl,EntityManagerFactoryImpl}
import org.eclipse.persistence.queries.{FetchGroup}

object Storage {
    class Properties(val properties:HashMap[String,String]) {
    }

    class Graph(val mappings:Map[String,Mapping[FetchGroup]]) {
    }
}
