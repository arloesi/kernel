package kernel.runtime

import scala.actors.Actor

object System {
  def fork(fun:() => Unit) {
    val actor = new Actor() {
      override def act() {fun()}
    }
    actor.start()
  }
}
