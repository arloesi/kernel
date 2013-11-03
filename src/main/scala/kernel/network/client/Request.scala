package kernel.network.client

import java.util.{LinkedList,Set,LinkedHashSet}
import scala.collection.JavaConversions._

import io.netty.buffer._
import io.netty.bootstrap.Bootstrap;
import io.netty.channel._;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.ssl.SslHandler;
import io.netty.handler.codec.http._;
import io.netty.handler.codec.http.websocketx._;
import io.netty.util.CharsetUtil;

import java.net.URI

import kernel.runtime._
import kernel.runtime.Handler._

import kernel.network._

class Request(val handler:Request.Handler,val client:Client) {
    def this(uri:URI,handler:Request.Handler) = this(handler,new Client(uri,handler))
    def this(uri:URI) = this(uri,new Request.Handler())
    def this(uri:URI, respond:Response=>Unit) = this(null)

    def response = handler.httpResponse

    val request = new DefaultHttpRequest(
        HttpVersion.HTTP_1_1, HttpMethod.GET, client.uri.getRawPath());

    request.headers().set(HttpHeaders.Names.HOST, client.uri.getHost());
    request.headers().set(HttpHeaders.Names.CONNECTION, HttpHeaders.Values.CLOSE);
    request.headers().set(HttpHeaders.Names.ACCEPT_ENCODING, HttpHeaders.Values.GZIP);

    def headers = request.headers()

    def send() = {
        if(!client.connected) {
            client.connect()
        }

        client.channel.writeAndFlush(request)
        client.channel.closeFuture()
    }
}

object Request {
    class Handler(val response:Event[Response]) extends SimpleChannelInboundHandler[Object] {
        def this() = this(new Event[Response]())
        var httpResponse:Response = _

        override def channelRead0(ctx:ChannelHandlerContext, msg:Object):Unit = {
            if (msg.isInstanceOf[HttpResponse]) {
                httpResponse = new Response(msg.asInstanceOf[HttpResponse])
            }

            if (msg.isInstanceOf[HttpContent]) {
                val content =  msg.asInstanceOf[HttpContent]

                httpResponse.content.add(Unpooled.copiedBuffer(content.content()))

                if (content.isInstanceOf[LastHttpContent]) {
                    this.response.send(httpResponse)
                }
            }
        }

        override def exceptionCaught(
            ctx:ChannelHandlerContext, cause:Throwable):Unit = {
            cause.printStackTrace();
            ctx.close();
        }
    }
}
