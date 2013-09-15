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
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.ssl.SslHandler;
import javax.net.ssl.SSLEngine;
import kernel.runtime.System._

object Server {
    private class Handler extends ChannelInboundHandlerAdapter {
        override def channelReadComplete(ctx:ChannelHandlerContext) {
            ctx.flush();
        }

        override def channelRead(ctx:ChannelHandlerContext, msg:Object) {
            if (msg.isInstanceOf[HttpRequest]) {
                val req = msg.asInstanceOf[HttpRequest];

                if (is100ContinueExpected(req)) {
                    ctx.write(new DefaultFullHttpResponse(HTTP_1_1, CONTINUE));
                }

                val request = new Request(ctx, req, Unpooled.buffer());
                val response = request.response

                response.write("Test Response")
                response.headers.set(CONTENT_TYPE, "text/plain");
                response.headers.set(CONTENT_LENGTH, response.buffer.readableBytes());

                response.send()
            }
        }

        override def exceptionCaught(ctx:ChannelHandlerContext, cause:Throwable) {
            cause.printStackTrace();
            ctx.close();
        }
    }

    class Initializer extends ChannelInitializer[SocketChannel] {
        override def initChannel(ch:SocketChannel) {
          val p = ch.pipeline()
            p.addLast("codec", new HttpServerCodec());
            // p.addLast("decoder", new HttpRequestDecoder());
            // p.addLast("aggregator", new HttpObjectAggregator(65536));
            // p.addLast("encoder", new HttpResponseEncoder());
            // p.addLast("socket", new WebSocketServerProtocolHandler("/websocket"));
            p.addLast("handler", new Handler());
        }
    }
}

class Server(port:Int) {
    var future:io.netty.channel.ChannelFuture = null
    var channel:io.netty.channel.Channel = null

    def start() = fork (() => {
        val bossGroup = new NioEventLoopGroup();
        val workerGroup = new NioEventLoopGroup();
        try {
            val b = new ServerBootstrap();
            b.option(ChannelOption.SO_BACKLOG, new Integer(1024));
            b.group(bossGroup, workerGroup)
             .channel(classOf[NioServerSocketChannel])
             .childHandler(new Server.Initializer())

            channel = b.bind(port).sync().channel()
            future = channel.closeFuture()
            future.sync()
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    })

    def stop() {
        channel.close()
        future.sync()
    }
}
