package kernel.network

import org.junit._
import org.junit.Assert._

import org.mockito.Mockito._
import org.mockito.Matchers._

import io.netty.util.CharsetUtil

import kernel.network._
import kernel.network.Common._
import kernel.network.client.Socket
import kernel.network.server.Server
import kernel.runtime.Handler.handler

class NetworkTest {
    type ServerRequest = kernel.network.server.Request
    type ClientRequest = kernel.network.client.Request
    type ClientResponse= kernel.network.client.Response

    var server:Server = _

    @Before
    def initialize() {
        server = new Server(8080)
        server.start()
        Thread.sleep(250)
    }

    @After
    def deinitialize() {
        server.stop();
    }

    @Test
    def requestTest() {
        val body = "<div>Test Content</div>"

        val responder = spy(new {
            def respond(request:ServerRequest) {
                request.response.send(body)
            }
        })

        server.request.handlers.add(responder.respond _)

        val receiver = spy(new {
            def receive(response:ClientResponse) {
                val responseCode = response.statusCode
                val responseBody = response.buffer.toString(CharsetUtil.UTF_8)

                assertEquals(responseCode, 200)
                assertEquals(responseBody, body)
            }
        })

        val request = new ClientRequest("http://localhost:8080", receiver.receive _)
        request.send().sync()

        verify(responder).respond(any(classOf[ServerRequest]))
        verify(receiver).receive(any(classOf[ClientResponse]))
    }

    @Test
    def socketTest() {
        val body = "<div>Test Content</div>"

        server.frame.handlers.add(
            (frame:Frame) => print("ip: "+frame.connection.channel.remoteAddress()))

        val socket = new Socket("http://localhost:8080/socket")
        socket.open().sync()
        socket.send(body).sync()
        socket.close().sync()
    }
}
