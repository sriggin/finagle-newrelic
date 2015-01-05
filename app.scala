
import com.newrelic.api.agent._
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
  @Trace(dispatcher = true)
  def apply(request: HttpRequest): Future[HttpResponse] = for {
    r1 <- Future {
      val response = Response()
      response.setContentString("Hello from " + request.getUri)
      response
    }
    r2 <- traced(r1)
    r3 <- untraced(r2)
  } yield r3

  @Trace
  def traced(response: Response): Future[Response] = Future {
    Thread.sleep(50)
    response.headers.set("X-Traced", "true")
    response
  }

  def untraced(response: Response): Future[Response] = Future {
    Thread.sleep(25)
    response.headers.set("X-Untraced", "true")
    response
  }
}
