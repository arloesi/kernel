package kernel.network

import org.vertx.java.core._
import org.vertx.java.core.json._
import org.vertx.java.core.http._
import com.google.inject.Inject

abstract class Handler extends org.vertx.java.core.Handler[HttpServerRequest] {
}
