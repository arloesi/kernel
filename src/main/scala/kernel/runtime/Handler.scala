package kernel.runtime

import java.util.LinkedHashSet
import scala.collection.JavaConversions._

trait Handler[T] {
    def handle(message:T):Unit
}

class Function[T](fun:T=>Unit) extends Handler[T] {
    def handle(message:T):Unit = {fun(message)}
}

class Event[T] {
  val handlers = new LinkedHashSet[Handler[T]]()

  def send(message:T) {
    for(handler <- handlers) {
      handler.handle(message)
    }
  }
}
