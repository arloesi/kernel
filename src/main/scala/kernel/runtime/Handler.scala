package kernel.runtime

import java.util.LinkedHashSet
import scala.collection.JavaConversions._

trait Handler[T] {
    def handle(message:T):Unit
}

object Handler {
    implicit def handler[T](fun:T=>Unit):Handler[T] =
      new Handler[T]() { override def handle(message:T) = fun(message)}
}

class Event[T] {
  val handlers = new LinkedHashSet[Handler[T]]()

  def send(message:T) {
    for(handler <- handlers) {
      handler.handle(message)
    }
  }
}
