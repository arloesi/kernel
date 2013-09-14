package kernel.handler

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
import kernel.runtime.{Responder => Res}

class Request {

}

class Response {

}

class Responder extends kernel.runtime.Responder[Request,Response] {
    override def respond(request:Request):Response = {
        null
    }
}

class CustomTextFrameHandler extends SimpleChannelInboundHandler[TextWebSocketFrame] {
    protected override def channelRead0(ctx:ChannelHandlerContext, frame:TextWebSocketFrame):Unit = {
        val request = frame.text();
        ctx.channel().writeAndFlush(new TextWebSocketFrame(request.toUpperCase()));
    }
}

class Handler extends ChannelInboundHandlerAdapter {
    private val CONTENT =
            Unpooled.unreleasableBuffer(Unpooled.copiedBuffer("Hello World", CharsetUtil.US_ASCII));

    override def channelReadComplete(ctx:ChannelHandlerContext) {
        ctx.flush();
    }

    override def channelRead(ctx:ChannelHandlerContext, msg:Object) {
        if (msg.isInstanceOf[HttpRequest]) {
            val req = msg.asInstanceOf[HttpRequest];

            if (is100ContinueExpected(req)) {
                ctx.write(new DefaultFullHttpResponse(HTTP_1_1, CONTINUE));
            }
            val keepAlive = isKeepAlive(req);
            val response = new DefaultFullHttpResponse(HTTP_1_1, OK, CONTENT.duplicate());
            response.headers().set(CONTENT_TYPE, "text/plain");
            response.headers().set(CONTENT_LENGTH, response.content().readableBytes());

            if (!keepAlive) {
                ctx.write(response).addListener(ChannelFutureListener.CLOSE);
            } else {
                response.headers().set(CONNECTION, Values.KEEP_ALIVE);
                ctx.write(response);
            }
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

class Server(port:Int) {
    def run() {
        val bossGroup = new NioEventLoopGroup();
        val workerGroup = new NioEventLoopGroup();
        try {
            val b = new ServerBootstrap();
            b.option(ChannelOption.SO_BACKLOG, new Integer(1024));
            b.group(bossGroup, workerGroup)
             .channel(classOf[NioServerSocketChannel])
             .childHandler(new Initializer());

            val ch = b.bind(port).sync().channel();
            ch.closeFuture().sync();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
}
