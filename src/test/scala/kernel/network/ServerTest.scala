package kernel.network

import org.junit._

import org.mockito.Mockito._
import org.mockito.Matchers._
import org.mockito.BDDMockito._

import org.vertx.java.core._
import org.vertx.java.core.http._

class ServerTest {
  val port = 8080

  var runtime:Runtime = _
  var vertx:Vertx = _
  var httpServer:HttpServer = _
  var server:Server = _

  @Before
  def setup() {
    runtime = mock(classOf[Runtime])
    vertx = mock(classOf[Vertx])
    httpServer = mock(classOf[HttpServer])
    server = spy(new Server(runtime, vertx, port, httpServer))
  }

  @Test
  def startTest() {
    // when
    server.start()

    // then
    verify(httpServer).listen(port)
  }

  @Test
  def stopTest() {
    // when
    server.stop()

    // then
    verify(httpServer).close()
  }
}