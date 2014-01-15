package kernel.model

import kernel.runtime._
import kernel.schema._
import kernel.schema.Mapped

object SchemaMock {
  val SCHEMA = Array(classOf[Node])

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
