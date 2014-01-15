package kernel.service

import org.junit._

import org.mockito.Mockito._
import org.mockito.Matchers._
import org.mockito.BDDMockito._

import com.fasterxml.jackson.databind.ObjectMapper

import kernel.runtime._
import kernel.network._

class MethodTest {
  type Reflection = java.lang.reflect.Method

  var reflection:Reflection = _
  var mapper:ObjectMapper = _
  var socket:Socket = _
  var method:Method = _
  var service:Object = _

  @Before
  def setup() {
    mapper = mock(classOf[ObjectMapper])
    socket = mock(classOf[Socket])
    reflection = mock(classOf[Reflection])
    service = mock(classOf[Object])
    method = new Method("method", service, reflection, "perm")
  }

  @Test
  def invokeTest() {
    // method.invoke(mapper, socket, params)
  }
}