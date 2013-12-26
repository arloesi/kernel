package kernel.network

import java.util._
import org.junit._

import org.mockito.Mockito._
import org.mockito.Matchers._
import org.mockito.BDDMockito._

import org.vertx.java.core._
import org.vertx.java.core.http._
import org.vertx.java.core.sockjs._

import com.fasterxml.jackson.databind.ObjectMapper

import kernel.service._

import Socket._

class SocketTest {
  @Before
  def setup() {
  }

  @Test
  def connectTest() {
    // given
    val mapper = mock(classOf[ObjectMapper])
    val services = mock(classOf[Map[String,Service]])
    val handler = new ConnectHandler(mapper, services)
    val socket = mock(classOf[SockJSSocket])

    // when
    handler.handle(socket)

    // then
    verify(socket).dataHandler(any(classOf[MessageHandler]))
    verify(socket).endHandler(any(classOf[DisconnectHandler]))
  }
}