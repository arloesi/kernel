package kernel.model

import java.util._
import scala.collection.JavaConversions._

import com.google.inject.Inject
import com.google.inject.assistedinject.Assisted

import javax.persistence.Persistence._
import org.eclipse.persistence._
import org.eclipse.persistence.internal.jpa.{EntityManagerImpl,EntityManagerFactoryImpl}
import org.eclipse.persistence.queries.{FetchGroup}
import kernel.model.Utilities._

object Storage {
    import Schema.schemaName

    class Mapping(val mapping:Schema.Mapping, val fetchGroup:FetchGroup) {
    }

    class Properties(val properties:HashMap[String,String]) {
    }

    class Mapper(schema:Map[String,Schema.Mapping]) {
        val mappings = new HashMap[String,Mapping]()

        for(i <- schema.entrySet()) {
            mappings.put(i.getKey(), build(i.getValue()))
        }

        def build(mapping:Schema.Mapping):Mapping = {
            val root = new FetchGroup()

            def addSubGroup(mapping:Schema.Mapping,group:FetchGroup) {
                for(i <- mapping.properties) {
                    if(i.schema != null) {
                        val node = new FetchGroup()
                        addFetchGroupAttribute(group, i.database.getAttributeName(), node)
                        addSubGroup(schema.get(i.schema),node)
                    } else {
                        group.addAttribute(i.database.getAttributeName())
                    }
                }
            }

            addSubGroup(mapping,root)
            new Mapping(mapping,root)
        }
      }
}
