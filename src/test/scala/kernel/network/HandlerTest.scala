package kernel.network

import org.junit._

import org.mockito.Mockito._
import org.mockito.Matchers._
import org.mockito.BDDMockito._

import org.vertx.java.core._
import org.vertx.java.core.http._

class HandlerTest {
  var request:HttpServerRequest = _
  var response:HttpServerResponse = _

  @Before
  def setup() {
    request = mock(classOf[HttpServerRequest])
    response = mock(classOf[HttpServerResponse])

    given(request.response()).willReturn(response)
  }

  @Test
  def handlerTest() {
    val handler = new Static(
      sourcePrefix="assets/html/",
      sourceSuffix=".html",
      targetPrefix="/static/html/",
      targetSuffix=".htm")

    given(request.path()).willReturn("/static/html/page.htm")

    // when
    handler.handle(request)

    // then
    verify(response).sendFile("assets/html/page.html")
  }

  @Test
  def staticTest() {
    val handler = new Static("assets/","/static/")

    given(request.path()).willReturn("/static/styles/main.css")

    // when
    handler.handle(request)

    // then
    verify(response).sendFile("assets/styles/main.css")
  }

  @Test
  def htmlTest() {
    val handler = new Static("html/",".html","/","")

    given(request.path()).willReturn("/page")

    // when
    handler.handle(request)

    // then
    verify(response).sendFile("html/page.html")
  }
}
