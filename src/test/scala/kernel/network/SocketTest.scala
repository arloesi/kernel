package kernel.network

import java.util._
import org.junit._

import org.apache.commons.io.IOUtils

import org.mockito.{Matchers}
import org.mockito.Mockito._
import org.mockito.Matchers._
import org.mockito.BDDMockito._

import org.vertx.java.core._
import org.vertx.java.core.http._
import org.vertx.java.core.sockjs._

import com.fasterxml.jackson.databind.ObjectMapper

import kernel.runtime._
import kernel.runtime.Utilities._
import kernel.service._

import Socket._

class SocketTest {
  var mapper:ObjectMapper = _
  var services:Map[String,Service] = _
  var socket:SockJSSocket = _
  var handler:ConnectHandler = _
  var methods:Map[String,Method] = _
  var service:Service = _
  var method:Method = _

  @Before
  def setup() {
    mapper = new ObjectMapper()
    socket = mock(classOf[SockJSSocket])
    services = new HashMap[String,Service]()
    methods = new HashMap[String,Method]()
    handler = new ConnectHandler(mapper, services)

    service = mock(classOf[Service])
    method = mock(classOf[Method])

    given(service.getMethods()).willReturn(methods)

    services.put("main", service)
    methods.put("method", method)
  }

  @Test
  def connectTest() {
    // when
    handler.handle(socket)

    // then
    verify(socket).dataHandler(any(classOf[MessageHandler]))
    verify(socket).endHandler(any(classOf[DisconnectHandler]))
  }

  @Test
  def methodTest() {
    // given
    val json = IOUtils.toString(getClass().getClassLoader().getResourceAsStream("fixtures/socket-method.json"))
    val handler = new MessageHandler(socket, mapper, services)

    // when
    handler.handle(new Buffer(json))

    // then
    verify(method).invoke(any(classOf[ObjectMapper]), any(classOf[Socket]), Matchers.eq("{\"id\":\"abc\"}"))
  }
}