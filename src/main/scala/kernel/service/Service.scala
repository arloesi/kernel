package kernel.service

import java.util._
import scala.collection.JavaConversions._

import kernel.runtime._

class Service(val service:Object) {
  def getMethods():Map[String,Method] = {methods}
  def getEvents():Map[String,Event[_]] = {events}

  val name = service.getClass().getSimpleName().toLowerCase()
  val events = new HashMap[String,Event[_]]()
  val methods = new HashMap[String,Method]()

  for(i <- service.getClass().getMethods()) {
    i.getAnnotation(classOf[Remote]) match {
      case null => ()
      case remote:Remote => {
        val name = remote.name() match {
          case "" => i.getName()
          case name:String => name
        }

        methods.put(name, new Method(
            name,service,i,remote.perm()))
      }
    }
  }

  for(i <- service.getClass().getDeclaredFields()) {
    i.getAnnotation(classOf[Remote]) match {
      case null => ()
      case remote:Remote => {
        i.setAccessible(true)

        val name = remote.name() match {
          case "" => i.getName()
          case name:String => name
        }

        i.get(service) match {
          case event:Event[_] => {
            events.put(name,event)
          }
          case _ => ()
        }
      }
    }
  }
}
