package kernel.network.client

import java.util.LinkedList
import scala.collection.JavaConversions._

import io.netty.buffer._
import io.netty.handler.codec.http.HttpResponse

class Response(val response:HttpResponse) {
    val content = new LinkedList[ByteBuf]();
    lazy val buffer = Unpooled.copiedBuffer(content.foldLeft(Unpooled.buffer())((buffer,content) => buffer.writeBytes(content)))
    def statusCode = response.getStatus().code()
}
