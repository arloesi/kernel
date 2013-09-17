package kernel.network.server

import io.netty.buffer._
import io.netty.channel._
import io.netty.handler.codec.http._
import io.netty.handler.codec.http.websocketx._
import io.netty.handler.codec.http.HttpResponseStatus._
import io.netty.handler.codec.http.HttpVersion._
import io.netty.handler.codec.http.HttpHeaders._
import io.netty.handler.codec.http.HttpHeaders.Names._
import kernel.runtime._
import kernel.network._

class Handler(socket:String,val request:Event[Request],val frame:Event[Frame])
    extends SimpleChannelInboundHandler[Object] {

    var handshaker:WebSocketServerHandshaker = _

    override def channelReadComplete(ctx:ChannelHandlerContext) {
        ctx.flush()
    }

    override def channelRead0(ctx:ChannelHandlerContext, msg:Object) {
        if(msg.isInstanceOf[WebSocketFrame]) {
            val frame = msg.asInstanceOf[WebSocketFrame]

            if (frame.isInstanceOf[CloseWebSocketFrame]) {
                handshaker.close(ctx.channel(), frame.retain().asInstanceOf[CloseWebSocketFrame])
            }
            else if (frame.isInstanceOf[PingWebSocketFrame]) {
                ctx.channel().write(new PongWebSocketFrame(frame.content().retain()))
            }
            else {
                this.frame.send(new Frame(new Connection(ctx.channel()), frame))
            }
        }
        else if (msg.isInstanceOf[HttpRequest]) {
            val req = msg.asInstanceOf[HttpRequest]

            if(req.getUri().equals(socket)) {
                if(req.isInstanceOf[FullHttpRequest]) {
                    val request = req.asInstanceOf[FullHttpRequest]
                    val wsFactory = new WebSocketServerHandshakerFactory(getWebSocketLocation(request), null, false)
                    handshaker = wsFactory.newHandshaker(request)

                    if (handshaker == null) {
                        WebSocketServerHandshakerFactory.sendUnsupportedWebSocketVersionResponse(ctx.channel())
                    } else {
                        handshaker.handshake(ctx.channel(), request)
                    }
                }
            }
            else {
                if(is100ContinueExpected(req)) {
                    ctx.write(new DefaultFullHttpResponse(HTTP_1_1, CONTINUE))
                }

                this.request.send(new Request(new Connection(ctx.channel()), req, Unpooled.buffer()))
            }
        }
    }

    def getWebSocketLocation(req:FullHttpRequest):String = {
        return "ws://" + req.headers().get(HOST) + socket
    }
}
