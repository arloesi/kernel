package kernel.network.server

import io.netty.bootstrap.ServerBootstrap
import io.netty.buffer._
import io.netty.channel._
import io.netty.channel.socket.SocketChannel
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.nio.NioServerSocketChannel
import io.netty.handler.codec.http._
import io.netty.handler.codec.http.HttpHeaders.Names._
import io.netty.handler.codec.http.HttpHeaders._
import io.netty.handler.codec.http.HttpResponseStatus._
import io.netty.handler.codec.http.HttpVersion._
import io.netty.handler.codec.http.websocketx._

import kernel.runtime._
import kernel.runtime.System._
import kernel.network._

class Server(val port:Int, val handler:Handler, bossGroup:NioEventLoopGroup, workerGroup:NioEventLoopGroup) {
    def this(port:Int) = this(port,
        new Handler("/socket",new Event[Request](), new Event[Frame]()),
        new NioEventLoopGroup(), new NioEventLoopGroup())

    def request = handler.request
    def frame = handler.frame

    var future:io.netty.channel.ChannelFuture = null
    var channel:io.netty.channel.Channel = null

    def start() {
        val b = new ServerBootstrap()
        b.option(ChannelOption.SO_BACKLOG, new Integer(1024))
        b.group(bossGroup, workerGroup)
            .channel(classOf[NioServerSocketChannel])
            .childHandler(new ChannelInitializer[SocketChannel]() {
                override def initChannel(ch:SocketChannel) {
                    ch.pipeline().addLast(
                        new HttpRequestDecoder(),
                        new HttpObjectAggregator(65536),
                        new HttpResponseEncoder(),
                        handler)
                }
            })

        channel = b.bind(port).sync().channel()
        future = channel.closeFuture()

        future.addListener(
            new ChannelFutureListener() {
                override def operationComplete(future:ChannelFuture) {
                    bossGroup.shutdownGracefully()
                    workerGroup.shutdownGracefully()
                }
            })
    }

    def stop() {
        channel.close()
        future.sync()
    }
}
