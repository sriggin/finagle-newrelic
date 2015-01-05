

import com.twitter.util._
import com.twitter.finagle._
import com.twitter.finagle.http.{ Request, Response }
import org.jboss.netty.handler.codec.http._
import scala.util.Properties

object Server extends App {
  val port = Properties.envOrElse("PORT", "8080").toInt
  println("Listening on port: " + port)

  val server = Http.serve(s":$port", new TestServer)
  Await.ready(server)
}

class TestServer extends Service[HttpRequest, HttpResponse] {
  def apply(request: HttpRequest): Future[HttpResponse] = {
    val response = Response()
    response.setContentString("Hello from " + request.getUri)
    Future.value(response)
  }
}
