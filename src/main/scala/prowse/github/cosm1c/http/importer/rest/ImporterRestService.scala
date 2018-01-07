package prowse.github.cosm1c.http.importer.rest

import javax.ws.rs.Path

import akka.event.LoggingAdapter
import akka.http.scaladsl.server._
import akka.stream.Materializer
import io.swagger.annotations._
import prowse.github.cosm1c.http.importer.importer.XmlImporter

@Api(produces = "application/json")
@Path("/")
class ImporterRestService()(implicit val log: LoggingAdapter, mat: Materializer) extends Directives {

    val route: Route =
        pathPrefix("import") {
            postXml ~
                postCsv
        }

    @ApiOperation(value = "Upload XML", httpMethod = "POST", consumes = "text/xml")
    @ApiImplicitParams(Array(
        new ApiImplicitParam(name = "xml", value = "XML", required = true, dataTypeClass = classOf[String], paramType = "body")
    ))
    @Path("xml")
    def postXml: Route =
        post {
            path("xml") {
                withoutSizeLimit {
                    extractDataBytes { dataBytes =>
                        onSuccess(
                            dataBytes
                                .via(XmlImporter.xmlToData)
                                .runWith(XmlImporter.dataPersist)(mat)) { _ =>
                            complete("OK")
                        }
                    }
                }
            }
        }

    @ApiOperation(value = "Upload CSV", httpMethod = "POST", consumes = "text/csv")
    @ApiImplicitParams(Array(
        new ApiImplicitParam(name = "csv", value = "CSV", required = true, dataTypeClass = classOf[String], paramType = "body")
    ))
    @Path("csv")
    def postCsv: Route =
        post {
            path("csv") {
                complete("TODO: Import CSV")
            }
        }

}
