package kernel.content

import java.io._
import java.util._
import scala.collection.JavaConversions._

import javax.persistence._
import org.eclipse.persistence.config._
import org.eclipse.persistence.internal.jpa.EntityManagerImpl

import org.vertx.java.core.http._
import org.vertx.java.core.buffer._

import kernel.runtime._

class Handler(schema:Schema,mapper:Mapper) {
  lazy val script = "this.content = {schema:"+mapper.getJsonSchema()+"};\n"

  def respond(request:HttpServerRequest) {
    request.response.headers.add("Content-Type", "application/json")

    if(request.params.get("schema") != null) {
      request.response.headers.add("Content-Type", "text/javascript")
      request.response.end(script)
    } else {
      val session = schema.openSession()

      val id = request.params.get("id")
      val `type` = request.params.get("type")
      val mapping = schema.getMapping(`type`).asInstanceOf[Class[Object]]
      val view = mapper.getView(`type`, request.params.get("view"))

      request.method.toLowerCase() match {
        case "get" => select(request,session,view,mapping,id)
        case "put" => update(request,session,view,mapping)
        case "post" => update(request,session,view,mapping)
        case "delete" => destroy(request,session,mapping,id)
      }

      session.close()
    }
  }

  def select(request:HttpServerRequest,session:EntityManager,view:mapper.View,mapping:Class[_],id:String) {
    val query = session.createQuery(
      if(id != null) "from "+mapping.getSimpleName()+" where id="+id
      else "select i from "+mapping.getSimpleName()+" i")
    query.setHint(QueryHints.FETCH_GROUP,view.fetchGroup)
    val entity =
      if(id != null) query.getSingleResult()
      else query.getResultList()
    val json = mapper.marshal(entity, view)
    request.response.end(json)
  }

  def update[T<:Object](request:HttpServerRequest,session:EntityManagerImpl,view:mapper.View,mapping:Class[T]) {
    request.bodyHandler(new org.vertx.java.core.Handler[Buffer]() {
        override def handle(buffer:Buffer) {
          val entity = mapper.unmarshalAndMerge(new StringReader(buffer.toString()), mapping, view)
          val json = mapper.marshal(entity, view)
          request.response.end(json)
        }
    })
  }

  def destroy(request:HttpServerRequest,session:EntityManager,mapping:Class[_],id:String) {
    session.getTransaction().begin()
    session.createQuery("delete from "+mapping.getSimpleName()+" where id="+id).executeUpdate()
    session.getTransaction().commit()
    request.response.end()
  }
}
