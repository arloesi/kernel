package kernel.runtime

import org.junit._
import org.mockito.Mockito._

class HandlerTest {
  var param:Object = _
  var callable:Function1[Object,Unit] = _
  val func = (x:Object) => {callable.apply(x)}

  @Before
  def setup() {
    param = mock(classOf[Object])
    callable = mock(classOf[Function1[Object, Unit]])
  }

  @Test
  def conversionTest() {
    // given
    val handler:Handler[Object] = func

    // when
    handler.handle(param)

    // then
    verify(callable).apply(param)
  }

  @Test
  def sendTest() {
    // given
    val event = new Event[Object]()
    event.handlers.add(func)

    // when
    event.send(param)

    // then
    verify(callable).apply(param)
  }
}
