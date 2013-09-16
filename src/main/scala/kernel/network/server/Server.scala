package kernel.network.server

import io.netty.buffer.Unpooled
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInboundHandlerAdapter
import io.netty.handler.codec.http._
import io.netty.handler.codec.http.HttpHeaders.Names._
import io.netty.handler.codec.http.HttpHeaders._
import io.netty.handler.codec.http.HttpResponseStatus._
import io.netty.handler.codec.http.HttpVersion._
import io.netty.handler.codec.http.websocketx._
import io.netty.channel.ChannelInitializer
import io.netty.channel.socket.SocketChannel
import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.ChannelOption
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.nio.NioServerSocketChannel
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler
import io.netty.channel.socket.nio.NioServerSocketChannel

import kernel.runtime._
import kernel.runtime.System._

object Server {
    private class Handler(socket:String,handlers:Event[Request]) extends SimpleChannelInboundHandler[Object] {
        var handshaker:WebSocketServerHandshaker = _

        override def channelReadComplete(ctx:ChannelHandlerContext) {
            ctx.flush();
        }

        override def channelRead0(ctx:ChannelHandlerContext, msg:Object) {
            if(msg.isInstanceOf[WebSocketFrame]) {
                val frame = msg.asInstanceOf[WebSocketFrame]

                // Check for closing frame
                if (frame.isInstanceOf[CloseWebSocketFrame]) {
                    handshaker.close(ctx.channel(), frame.retain().asInstanceOf[CloseWebSocketFrame]);
                }
                else if (frame.isInstanceOf[PingWebSocketFrame]) {
                    ctx.channel().write(new PongWebSocketFrame(frame.content().retain()));
                }
                else if (frame.isInstanceOf[TextWebSocketFrame]) {
                    val request = frame.asInstanceOf[TextWebSocketFrame].text()
                    ctx.channel().write(new TextWebSocketFrame(request.toUpperCase()));
                }
                else if (frame.isInstanceOf[BinaryWebSocketFrame]) {
                    val request = frame.asInstanceOf[BinaryWebSocketFrame]
                }
            }
            else if (msg.isInstanceOf[HttpRequest]) {
                val req = msg.asInstanceOf[HttpRequest];

                if(req.getUri().equals(socket)) {
                    if(req.isInstanceOf[FullHttpRequest]) {
                        val request = req.asInstanceOf[FullHttpRequest]
                        // Handshake
                        val wsFactory = new WebSocketServerHandshakerFactory(getWebSocketLocation(request), null, false);
                        handshaker = wsFactory.newHandshaker(request);

                        if (handshaker == null) {
                            WebSocketServerHandshakerFactory.sendUnsupportedWebSocketVersionResponse(ctx.channel());
                        } else {
                            handshaker.handshake(ctx.channel(), request);
                        }
                    }
                }
                else {
                    if(is100ContinueExpected(req)) {
                        ctx.write(new DefaultFullHttpResponse(HTTP_1_1, CONTINUE));
                    }

                    handlers.send(new Request(ctx, req, Unpooled.buffer()))
                }
            }
        }

        def getWebSocketLocation(req:FullHttpRequest):String = {
            return "ws://" + req.headers().get(HOST) + socket;
        }
    }

    class Initializer(http:Event[Request]) extends ChannelInitializer[SocketChannel] {
        override def initChannel(ch:SocketChannel) {
            ch.pipeline().addLast(
                new HttpRequestDecoder(),
                new HttpObjectAggregator(65536),
                new HttpResponseEncoder(),
                new Handler("/socket",http))
        }
    }
}

class Server(val port:Int, val http:Event[Request]) {
    def this(port:Int) = this(port,new Event[Request]())

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
             .childHandler(new Server.Initializer(http))

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
