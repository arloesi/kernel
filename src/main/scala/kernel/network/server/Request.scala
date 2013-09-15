package kernel.network.server

import io.netty.buffer.Unpooled
import io.netty.channel.ChannelFutureListener
import io.netty.channel.ChannelHandlerContext
import io.netty.util.CharsetUtil
import io.netty.handler.codec.http._
import io.netty.handler.codec.http.HttpHeaders.Names._
import io.netty.handler.codec.http.HttpHeaders._
import io.netty.handler.codec.http.HttpResponseStatus._
import io.netty.handler.codec.http.HttpVersion._
import io.netty.handler.codec.http.websocketx._
import io.netty.channel.ChannelHandlerContext
import io.netty.buffer.ByteBuf

class Request(val context:ChannelHandlerContext, request:HttpRequest, buffer:io.netty.buffer.ByteBuf) {
    val response = new Response(context, buffer, request)
    def headers = request.headers()
    def method = request.getMethod()
}

class Response(val context:ChannelHandlerContext, val buffer:ByteBuf, request:HttpRequest) {
    var contentType = "text/html"
    val response = new DefaultFullHttpResponse(HTTP_1_1, OK, buffer)

    def headers = response.headers()
    def keepAlive = isKeepAlive(request)

    def write(buffer:ByteBuf) {
        this.buffer.writeBytes(buffer)
    }

    def write(content:String) {
        write(Unpooled.copiedBuffer(content, CharsetUtil.UTF_8))
    }

    def send() {
        headers.set(CONTENT_LENGTH, buffer.readableBytes())
        headers.set(CONTENT_TYPE, contentType)

        if (!keepAlive) {
            context.write(response).addListener(ChannelFutureListener.CLOSE)
        } else {
            response.headers.set(CONNECTION, Values.KEEP_ALIVE)
            context.write(response)
        }
    }

    def send(body:String) {
        write(body)
        send()
    }
}
