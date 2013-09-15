package kernel.network

import io.netty.buffer.Unpooled
import io.netty.channel.ChannelFutureListener
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInboundHandlerAdapter
import io.netty.util.CharsetUtil
import io.netty.handler.codec.http._
import io.netty.handler.codec.http.HttpHeaders.Names._
import io.netty.handler.codec.http.HttpHeaders._
import io.netty.handler.codec.http.HttpResponseStatus._
import io.netty.handler.codec.http.HttpVersion._
import io.netty.handler.codec.http.websocketx._
import io.netty.channel.ChannelInitializer
import io.netty.channel.socket.SocketChannel
import io.netty.handler.codec.http.HttpServerCodec
import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.ChannelOption
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.nio.NioServerSocketChannel
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame
import io.netty.handler.ssl.SslHandler
import javax.net.ssl.SSLEngine
import io.netty.buffer.ByteBuf

class Request(val context:ChannelHandlerContext, request:HttpRequest, buffer:io.netty.buffer.ByteBuf) {
    val response = new Response(context, buffer, request)
    def headers = request.headers()
    def method = request.getMethod()
}

class Response(val context:ChannelHandlerContext, val buffer:ByteBuf, request:HttpRequest) {
    val response = new DefaultFullHttpResponse(HTTP_1_1, OK, buffer)
    def headers = response.headers()
    def keepAlive = isKeepAlive(request)

    def write(buffer:ByteBuf) {
        this.buffer.writeBytes(buffer)
    }

    def write(content:String) {
        write(Unpooled.copiedBuffer(content, CharsetUtil.US_ASCII))
    }

    def send() {
      if (!keepAlive) {
            context.write(response).addListener(ChannelFutureListener.CLOSE)
        } else {
            response.headers.set(CONNECTION, Values.KEEP_ALIVE)
            context.write(response)
        }
    }
}
