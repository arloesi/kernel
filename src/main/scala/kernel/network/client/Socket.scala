package kernel.network.client

import java.util.{LinkedList,Set,LinkedHashSet}
import scala.collection.JavaConversions._

import io.netty.buffer._
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.ClientCookieEncoder;
import io.netty.handler.codec.http.DefaultCookie;
import io.netty.handler.codec.http.DefaultHttpRequest;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpVersion;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
// import io.netty.example.securechat.SecureChatSslContextFactory;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpContentDecompressor;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.ssl.SslHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.LastHttpContent;
import io.netty.util.CharsetUtil;
import io.netty.handler.codec.http.HttpObjectAggregator

import java.net.URI;
import kernel.runtime._

class Handler(val response:Event[Response]) extends SimpleChannelInboundHandler[HttpObject] {
    def this() = this(new Event[Response]())

    var httpResponse:Response = _

    override def channelRead0(ctx:ChannelHandlerContext, msg:HttpObject):Unit = {
        if (msg.isInstanceOf[HttpResponse]) {
            httpResponse = new Response(msg.asInstanceOf[HttpResponse])
        }

        if (msg.isInstanceOf[HttpContent]) {
            val content =  msg.asInstanceOf[HttpContent]

            print(content.content().toString(CharsetUtil.UTF_8));

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

class Initializer(handler:Handler) extends ChannelInitializer[SocketChannel] {
    override def initChannel(ch:SocketChannel) {
        // Create a default pipeline implementation.
        val p = ch.pipeline();

        p.addLast("codec", new HttpClientCodec());
        p.addLast("inflater", new HttpContentDecompressor());

        // Uncomment the following line if you don't want to handle HttpChunks.
        // p.addLast("aggregator", new HttpObjectAggregator(1048576));

        p.addLast("handler", handler);
    }
}

object Socket {
    implicit def socket(uri:URI):Socket = new Socket(uri)
    implicit def socket(uri:String):Socket = socket(new URI(uri))
}

class Socket(val uri:URI) {
    var channel:Channel = _
    var group:NioEventLoopGroup = _
    val handler = new Handler()
    def response = handler.response
    def connected = channel != null

    def connect() {
        // Configure the client.
        group = new NioEventLoopGroup();

        val b = new Bootstrap();
        b.group(group)
         .channel(classOf[NioSocketChannel])
         .handler(new Initializer(handler));

        // Make the connection attempt.
        channel = b.connect(uri.getHost(), uri.getPort()).sync().channel();
    }

    def close() {
        if(channel != null) {
            channel.close()
            channel.closeFuture().sync()
            group.shutdownGracefully()
            channel = null
            group = null
        }
    }
}
