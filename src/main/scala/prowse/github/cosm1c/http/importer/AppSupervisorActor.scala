package prowse.github.cosm1c.http.importer

import akka.actor.{Actor, ActorLogging, ActorSystem}
import akka.event.LoggingAdapter
import akka.http.scaladsl.Http
import akka.http.scaladsl.Http.ServerBinding
import akka.http.scaladsl.model.headers.{`Access-Control-Allow-Credentials`, `Access-Control-Allow-Origin`}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer
import com.typesafe.config.ConfigFactory
import prowse.github.cosm1c.http.importer.rest.ImporterRestService
import prowse.github.cosm1c.http.importer.swagger.SwaggerDocService

import scala.concurrent.{ExecutionContextExecutor, Future}

class AppSupervisorActor extends Actor with ActorLogging {

    private implicit val actorSystem: ActorSystem = context.system
    private implicit val materializer: ActorMaterializer = ActorMaterializer()
    private implicit val executionContext: ExecutionContextExecutor = context.dispatcher
    private implicit val _log: LoggingAdapter = log

    private val config = ConfigFactory.load()
    private val httpPort = config.getInt("app.httpPort")

    private val route: Route =
        decodeRequest {
            encodeResponse {
                respondWithHeaders(
                    `Access-Control-Allow-Origin`.*,
                    `Access-Control-Allow-Credentials`(true)
                ) {
                    pathEndOrSingleSlash {
                        getFromResource("index.html")
                    } ~
                        new ImporterRestService().route ~
                        new SwaggerDocService("/").routes
                }
            }
        }

    @SuppressWarnings(Array("org.wartremover.warts.Var", "org.wartremover.warts.Null"))
    private var bindingFuture: Future[ServerBinding] = _

    override def preStart(): Unit = {
        bindingFuture = Http().bindAndHandle(route, "0.0.0.0", httpPort)
        bindingFuture.onComplete(serverBinding => log.info("Server online - {}", serverBinding))
    }

    override def postStop(): Unit = {
        bindingFuture
            .flatMap { serverBinding =>
                log.info("Server offline - {}", serverBinding)
                serverBinding.unbind()
            }
        ()
    }

    override def receive: Receive = {
        case msg => log.warning("Received unexpected message: {}", msg)
    }

}
