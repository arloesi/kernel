package kernel.runtime

import java.io._

object Utilities {
  type Buffer = org.vertx.java.core.buffer.Buffer
  type BufferHandler = org.vertx.java.core.Handler[Buffer]

  implicit def toFile(source:String) = new File(source)

  class BufferWriter(buffer:Buffer) extends Writer {
    override def close() {}
    override def flush() {}

    override def write(source:Array[Char], offset:Int, length:Int) {
      for(i <- offset to length) {
        buffer.appendString(source(i).toString)
      }
    }
  }

  class BufferOutputStream(buffer:Buffer) extends OutputStream {
    override def write(b:Int) {
      buffer.appendByte(b.toByte)
    }
  }
}
