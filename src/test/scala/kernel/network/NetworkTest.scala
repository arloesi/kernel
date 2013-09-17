package kernel.network
import org.junit._
import org.junit.Assert._
import io.netty.util.CharsetUtil
import kernel.network._
import kernel.network.Common._
import kernel.network.client.Socket
import kernel.network.server.Server
import kernel.runtime.Handler.handler

class NetworkTest {
    type ServerRequest = kernel.network.server.Request
    type ClientRequest = kernel.network.client.Request

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
    def httpGetTest() {
        val body = "<div>Test Content</div>"

        server.request.handlers.add(
            (request:ServerRequest) => request.response.send(body))

        val req = new ClientRequest("http://localhost:8080")
        req.send().sync()

        val ret = req.response.statusCode
        val rsp = req.response.buffer.toString(CharsetUtil.UTF_8)

        assertTrue(ret == 200)
        assertEquals(rsp, body)
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
