package kernel.network.server

import io.netty.buffer.ByteBuf
import io.netty.handler.codec.http._

import kernel.network.Connection

class Request(val connection:Connection, request:HttpRequest, buffer:io.netty.buffer.ByteBuf) {
    val response = new Response(connection, buffer, request)
    def headers = request.headers()
    def method = request.getMethod()
}

