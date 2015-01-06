
import scala.language.implicitConversions

import com.newrelic.api.agent._
import com.twitter.util.{ Future => TwitterFuture, Await }
import com.twitter.finagle._
import com.twitter.finagle.http.{ Request, Response }
import org.jboss.netty.handler.codec.http._
import scala.util.Properties
import scala.concurrent.Future

import scala.concurrent.ExecutionContext.Implicits.global

//main class, creates finagle server
object Server extends App {
  val port = Properties.envOrElse("PORT", "8080").toInt
  println("Listening on port: " + port)

  val server = Http.serve(s":$port", new TestServer)
  Await.ready(server)
}

//finagle service that handles server cals
class TestServer extends Service[HttpRequest, HttpResponse] {
  @Trace(dispatcher = true)
  def apply(request: HttpRequest): TwitterFuture[HttpResponse] = TwitterUtils.scala2Twitter {
    for {
      r1 <- Future {
        val response = Response()
        response.setContentString("Hello from " + request.getUri)
        response
      }
      r2 <- traced(r1)
      r3 <- untraced(r2)
    } yield r3
  }

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

//utility object for converting between scala and twitter futures. all twitter types are prefixed with Twitter. others are scala types
object TwitterUtils {
  import com.twitter.{ util => Twitter }
  import scala.concurrent._
  import scala.concurrent.duration._
  import scala.util._
  import java.util.concurrent.TimeUnit

  implicit def scala2Twitter[T](future: Future[T])(implicit ec: ExecutionContext): Twitter.Future[T] = {
    val promise = new Twitter.Promise[T]
    future onComplete {
      case Success(value) => promise.setValue(value)
      case Failure(cause) => promise.setException(cause)
    }
    promise
  }

  implicit def twitter2Scala[T](future: Twitter.Future[T]): Future[T] = {
    val promise = Promise[T]()
    future respond {
      case Twitter.Return(value) => promise.success(value)
      case Twitter.Throw(ex) => promise.failure(ex)
    }
    promise.future
  }

  implicit def scalaTryToTwitter[T](value: Try[T]): Twitter.Try[T] = value match {
    case Success(t) => Twitter.Return(t)
    case Failure(e) => Twitter.Throw(e)
  }

  implicit def fromScalaDuration(scalaDur: Duration): Twitter.Duration = Twitter.Duration(scalaDur.toMillis, TimeUnit.MILLISECONDS)

  implicit def toScalaDuration(duration: Twitter.Duration) = new FiniteDuration(duration.inUnit(TimeUnit.MILLISECONDS), TimeUnit.MILLISECONDS)
}
