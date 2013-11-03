package kernel.service

import com.fasterxml.jackson.databind.ObjectMapper

import kernel.runtime.Process._
import kernel.network.Socket

class Method(name:String,service:Object,reflection:java.lang.reflect.Method,perm:String) {
  def getName() = name

  def invoke(mapper:ObjectMapper,socket:Socket,params:String) {
    val args = new Array[Object](reflection.getParameterTypes().length)
    val root = mapper.readTree(params)
    val types = reflection.getParameterTypes()

    val offset =
      if(reflection.getParameterTypes().length > 0 && reflection.getParameterTypes()(0) == socket.getClass()) {
        args(0) = socket; 1
      } else {0}

    args(offset) = mapper.treeToValue(root, types(offset).asInstanceOf[Class[Object]])

    reflection.invoke(service,args:_*)
  }
}
