package kernel.network

import java.io._
import java.util._
import scala.collection.JavaConversions._

import org.apache.olingo.odata2.api.ODataService
import org.apache.olingo.odata2.api.ODataServiceFactory
import org.apache.olingo.odata2.api.commons.HttpHeaders
import org.apache.olingo.odata2.api.commons.HttpStatusCodes
import org.apache.olingo.odata2.api.commons.ODataHttpMethod
import org.apache.olingo.odata2.api.exception.MessageReference
import org.apache.olingo.odata2.api.exception.ODataBadRequestException
import org.apache.olingo.odata2.api.exception.ODataException
import org.apache.olingo.odata2.api.exception.ODataHttpException
import org.apache.olingo.odata2.api.exception.ODataMethodNotAllowedException
import org.apache.olingo.odata2.api.exception.ODataNotAcceptableException
import org.apache.olingo.odata2.api.exception.ODataNotImplementedException
import org.apache.olingo.odata2.api.processor.ODataContext
import org.apache.olingo.odata2.api.processor.ODataRequest
import org.apache.olingo.odata2.api.processor.ODataResponse
import org.apache.olingo.odata2.core.ODataContextImpl
import org.apache.olingo.odata2.core.ODataRequestHandler
import org.apache.olingo.odata2.core.exception.ODataRuntimeException
import org.apache.olingo.odata2.api.ODataServiceFactory

import org.vertx.java.core.http.HttpServerRequest

import kernel.runtime.Utilities._

class Model(basePath:String,serviceFactory:ODataServiceFactory) extends org.vertx.java.core.Handler[HttpServerRequest] {
  import Model._

  override def handle(request:HttpServerRequest) {
        /*if (req.getHeader(HttpHeaders.ACCEPT) != null && req.getHeader(HttpHeaders.ACCEPT).isEmpty()) {
          createNotAcceptableResponse(req, ODataNotAcceptableException.COMMON, resp);
        }*/
      request.bodyHandler(new BufferHandler() {
        override def handle(buffer:Buffer) {
          val odataRequest = ODataRequest.method(ODataHttpMethod.valueOf(request.method().toUpperCase()))
            // FIXME: .contentType(RestUtil.extractRequestContentType(req.getContentType()).toContentTypeString())
            .contentType("applocation/json")
            // FIXME: .acceptHeaders(RestUtil.extractAcceptHeaders(req.getHeader(HttpHeaders.ACCEPT)))
            // FIXME: .acceptableLanguages(RestUtil.extractAcceptableLanguage(req.getHeader(HttpHeaders.ACCEPT_LANGUAGE)))
            .pathInfo(buildPathInfo(basePath,request))
            .queryParameters(request.params())
            .requestHeaders(request.headers())
            .body(new ByteArrayInputStream(buffer.getBytes()))
            .build()

          val context = new ODataContextImpl(odataRequest, serviceFactory);

          val service = serviceFactory.createService(context);
          context.setService(service);
          service.getProcessor().setContext(context);

          val requestHandler = new ODataRequestHandler(serviceFactory, service, context);
          val odataResponse = requestHandler.handle(odataRequest);

          request.response().setStatusCode(odataResponse.getStatus().getStatusCode())
          request.response().headers().set("Content-Type", odataResponse.getContentHeader())

          for(i <- odataResponse.getHeaderNames()) {
            request.response().headers().set(i, odataResponse.getHeader(i))
          }

          odataResponse.getEntity() match {
            case entity:String => {
              request.response().end(entity)
            }

            case stream:InputStream => {
              var data:Int = 0
              val buffer = new Buffer()

              while((data = stream.read()) != -1) {
                buffer.appendByte(data.toByte)
              }

              request.response().end(buffer)
              stream.close()
            }
          }
        }
      })
  }
}

object Model {
  implicit def toMap(map:org.vertx.java.core.MultiMap):Map[String,String] = {
    val result = new LinkedHashMap[String,String]()

    for(i <- map.entries()) {
      result.put(i.getKey(), i.getValue())
    }

    result
  }

  implicit def toMapOfLists(map:org.vertx.java.core.MultiMap):Map[String,List[String]] = {
    val result = new LinkedHashMap[String,List[String]]()

    for(i <- map.names()) {
      result.put(i, map.getAll(i))
    }

    result
  }

  def buildPathInfo(basePath:String,request:HttpServerRequest):org.apache.olingo.odata2.api.uri.PathInfo = {
    null
  }
}