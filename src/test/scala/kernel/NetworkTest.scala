package kernel

import org.junit._
import org.junit.Assert._

import java.net.URL
import java.net.HttpURLConnection

import org.apache.commons.io.IOUtils

import kernel.network.Request
import kernel.network.Server

class NetworkTest {
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
            (request:Request) => request.response.send(body))

        val req = request("GET")
        val rsp = IOUtils.toString(req.getInputStream())

        assertTrue(req.getResponseCode() == 200)
        assertEquals(rsp,body)
    }

    def request(method:String) = {
        val uri = new URL("http://localhost:8080")

        val req = uri.openConnection().asInstanceOf[HttpURLConnection]
        req.setRequestMethod(method)

        req
    }
}
