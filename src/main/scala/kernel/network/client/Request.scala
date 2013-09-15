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

class Request(val socket:Socket) {
    var response:Response = _

    val request = new DefaultHttpRequest(
        HttpVersion.HTTP_1_1, HttpMethod.GET, socket.uri.getRawPath());

    request.headers().set(HttpHeaders.Names.HOST, socket.uri.getHost());
    request.headers().set(HttpHeaders.Names.CONNECTION, HttpHeaders.Values.CLOSE);
    request.headers().set(HttpHeaders.Names.ACCEPT_ENCODING, HttpHeaders.Values.GZIP);

    request.headers().set(
        HttpHeaders.Names.COOKIE,
        ClientCookieEncoder.encode(
            new DefaultCookie("session", "foo")));

    socket.complete.handlers.add((response:Response) => {
        this.response = response
    })

    def headers = request.headers()

    def send() {
        socket.connect()
        socket.channel.writeAndFlush(request)
    }

    def sendAndWait() = {
        send()
        socket.channel.closeFuture().sync()
    }
}

class Response(val response:HttpResponse) {
    val content = new LinkedList[ByteBuf]();
    lazy val buffer = Unpooled.copiedBuffer(content.foldLeft(Unpooled.buffer())((buffer,content) => buffer.writeBytes(content)))
    def statusCode = response.getStatus().code()
}
