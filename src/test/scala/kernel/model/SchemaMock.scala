package kernel.model

import scala.collection.JavaConversions._

import com.google.common.collect.Sets._

import kernel.runtime._
import kernel.schema._
import kernel.schema.Mapped

object SchemaMock {
  trait SIMPLE
  trait DETAIL

  val VIEWS = {
    val views:Array[Class[_]] = Array(classOf[SIMPLE],classOf[DETAIL])
    val result:java.util.Set[Class[_]] = newLinkedHashSet(views.toSeq)
    result
  }

  val SCHEMA = Array(classOf[Node], classOf[MappedClass])

  @Mapping
  class Node extends Mapped {
  }

  @Mapping
  class MappedClass extends Mapped {
  }

  @Mapping
  class UnmappedClass extends Mapped {
  }
}
