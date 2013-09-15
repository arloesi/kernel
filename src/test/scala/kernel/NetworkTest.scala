package kernel

import org.junit._
import org.junit.Assert._

import java.net.URL
import java.net.HttpURLConnection

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
        val req = request("GET")

        assertTrue(req.getResponseCode() == 200)
    }

    def request(method:String) = {
        val uri = new URL("http://localhost:8080")

        val req = uri.openConnection().asInstanceOf[HttpURLConnection]
        req.setRequestMethod(method)

        req
    }
}
