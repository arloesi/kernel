package kernel.network

import io.netty.handler.codec.http.websocketx._

class Frame(val connection:Connection, val frame:WebSocketFrame) {
}
