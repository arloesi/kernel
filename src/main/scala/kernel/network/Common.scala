package kernel.network

import java.net.URI

object Common {
    implicit def uri(uri:String) = new URI(uri)
}
