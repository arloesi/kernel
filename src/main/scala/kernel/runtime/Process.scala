package kernel.runtime

import scala.actors.Actor

object Process {
  def fork(fun:() => Unit) {
    val actor = new Actor() {
      override def act() {fun()}
    }
    actor.start()
  }
}
