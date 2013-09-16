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

import io.netty.channel._;
import io.netty.channel.socket.SocketChannel;
// import io.netty.example.securechat.SecureChatSslContextFactory;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpContentDecompressor;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.ssl.SslHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http._;
import io.netty.handler.codec.http.websocketx._
import io.netty.util.CharsetUtil;
import io.netty.handler.codec.http.HttpObjectAggregator

import java.net.URI;
import kernel.runtime._
import kernel.network._

object Client {
    class Initializer(handler:ChannelInboundHandlerAdapter) extends ChannelInitializer[SocketChannel] {
        override def initChannel(ch:SocketChannel) {
            // Create a default pipeline implementation.
            val p = ch.pipeline();

            p.addLast("codec", new HttpClientCodec());
            p.addLast("inflater", new HttpContentDecompressor());
            p.addLast("aggregator", new HttpObjectAggregator(8192));
            p.addLast("handler", handler);
        }
    }
}

class Client(val uri:URI, val handler:ChannelInboundHandlerAdapter) {
    var channel:Channel = _
    var group:NioEventLoopGroup = _
    def connected = channel != null

    def connect() {
        // Configure the client.
        val group = new NioEventLoopGroup();
        this.group = group

        val b = new Bootstrap();
        b.group(group)
         .channel(classOf[NioSocketChannel])
         .handler(new Client.Initializer(handler));

        // Make the connection attempt.
        channel = b.connect(uri.getHost(), uri.getPort()).sync().channel();

        channel.closeFuture().addListener(new ChannelFutureListener() {
            override def operationComplete(future:ChannelFuture) {
                group.shutdownGracefully()
            }
        })
    }

    def close() = {
        if(channel != null) {
            val ret = channel.close()
            channel = null
            group = null
            ret
        }
        else {
          null
        }
    }
}
