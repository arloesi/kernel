package kernel.network.client

import java.net.URI

import io.netty.buffer._
import io.netty.channel._
import io.netty.handler.codec.http._
import io.netty.handler.codec.http.websocketx._

import kernel.runtime._
import kernel.network._

class Socket(handler:Socket.Handler,client:Client) {
    def this(uri:URI,handler:Socket.Handler) = this(handler,new Client(uri,handler))

    def this(uri:URI) = this(uri,new Socket.Handler(
        WebSocketClientHandshakerFactory.newHandshaker(
            uri, WebSocketVersion.V13, null, false, new DefaultHttpHeaders())))

    def open() = {
        client.connect()
        handler.handshakePromise
    }

    def close() = {
        send(new CloseWebSocketFrame())
        client.channel.closeFuture()
    }

    def send(msg:WebSocketFrame):ChannelFuture = {
        client.channel.writeAndFlush(msg)
    }

    def send(msg:String):ChannelFuture = {
        send(new TextWebSocketFrame(msg))
    }

    def send(msg:ByteBuf):ChannelFuture = {
        send(new BinaryWebSocketFrame(msg))
    }

    def send(msg:Array[Byte]):ChannelFuture = {
        send(Unpooled.copiedBuffer(msg))
    }
}

object Socket {
    class Handler(
        val handshaker:WebSocketClientHandshaker, val response:Event[Response], val frame:Event[Frame])
        extends SimpleChannelInboundHandler[Object] {

        def this(handshaker:WebSocketClientHandshaker) =
            this(handshaker,new Event[Response](), new Event[Frame]())

        var handshakePromise:ChannelPromise = _

        override def channelRead0(ctx:ChannelHandlerContext, msg:Object):Unit = {
            if(msg.isInstanceOf[FullHttpResponse]) {
                if(!handshaker.isHandshakeComplete()) {
                    handshaker.finishHandshake(ctx.channel(), msg.asInstanceOf[FullHttpResponse])
                    handshakePromise.setSuccess()
                }
            }
            else if(msg.isInstanceOf[WebSocketFrame]) {
                frame.send(new Frame(new Connection(ctx.channel()), msg.asInstanceOf[WebSocketFrame]))
            }
        }

        override def exceptionCaught(
            ctx:ChannelHandlerContext, cause:Throwable):Unit = {
            cause.printStackTrace()
            ctx.close()
        }

        override def handlerAdded(ctx:ChannelHandlerContext) {
            handshakePromise = ctx.newPromise()
        }

        override def channelActive(ctx:ChannelHandlerContext) {
            handshaker.handshake(ctx.channel())
        }

        override def channelInactive(ctx:ChannelHandlerContext) {
        }
    }
}
