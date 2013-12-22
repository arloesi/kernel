package kernel.runtime

import org.junit._
import org.mockito.Mockito._

class HandlerTest {
  @Test
  def conversionTest() {
    // given
    val param = mock(classOf[Object])
    val callable:Function1[Object, Unit] = mock(classOf[Function1[Object, Unit]])
    val handler:Handler[Object] = (x:Object) => {callable.apply(x)}

    // when
    handler.handle(param)

    // then
    verify(callable).apply(param)
  }
}

class EventTest {

}