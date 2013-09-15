package kernel.network.server

import java.net.URI
import org.junit._
import org.junit.Assert._
import java.net.URL
import java.net.HttpURLConnection
import org.apache.commons.io.IOUtils
import kernel.runtime.Handler.handler
import io.netty.util.CharsetUtil

class ServerTest {
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

        server.http.handlers.add(
            (request:Request) => {
              request.response.send(body)
            })

        val req = new kernel.network.client.Request(new URI("http://localhost:8080"))
        req.sendAndWait()

        val ret = req.response.statusCode
        val rsp = req.response.buffer.toString(CharsetUtil.UTF_8)

        assertTrue(ret == 200)
        assertEquals(rsp,body)
    }
}
