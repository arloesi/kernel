package kernel.runtime

import org.junit.Test
import org.mockito.Mockito._
import kernel.network.server.Server

class RunnerTest {
    @Test
    def runTest() {
      // given
      val server = mock(classOf[Server])
      val runner = new Runner(server)

      // when
      runner.run()

      // then
      verify(server).start()
    }
}
