package kernel.runtime

import java.io._

object Utilities {
  type Buffer = org.vertx.java.core.buffer.Buffer
  type BufferHandler = org.vertx.java.core.Handler[Buffer]

  implicit def toFile(source:String) = new File(source)
}
