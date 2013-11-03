package kernel.runtime

import scala.actors.Actor
import com.hazelcast.core._

object System {
  def fork(fun:() => Unit) {
    val actor = new Actor() {
      override def act() {fun()}
    }
    actor.start()
  }
}
