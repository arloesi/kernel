package kernel.security

import java.util._
import scala.collection.JavaConversions._

abstract class Authority {
    val permissions:Set[Permission] = new LinkedHashSet[Permission]()
}
