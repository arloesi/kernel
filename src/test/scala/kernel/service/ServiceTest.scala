package kernel.service

import org.junit._
import org.junit.Assert._

import org.mockito.Mockito._
import org.mockito.Matchers._
import org.mockito.BDDMockito._

import kernel.runtime._

class ServiceTest {
  import ServiceTest._

  def serviceTest() {
    val service = new Service(new ServiceMock())

    assertTrue(service.getEvents().size() == 1)
    assertTrue(service.getEvents().containsKey("event"))

    assertTrue(service.getMethods().size() == 1)
    assertTrue(service.getMethods().containsKey("method"))
  }
}

object ServiceTest {
  class ServiceMock {
    @Remote
    val event = new Event[String]()

    @Remote
    def method() {
    }
  }
}