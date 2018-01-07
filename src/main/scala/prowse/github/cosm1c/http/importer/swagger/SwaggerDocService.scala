package prowse.github.cosm1c.http.importer.swagger

import com.github.swagger.akka._
import com.github.swagger.akka.model.Info
import prowse.github.cosm1c.http.importer.rest.ImporterRestService

class SwaggerDocService(override val basePath: String) extends SwaggerHttpService {
    override val apiClasses = Set(classOf[ImporterRestService])
    //override val host = "localhost:12345"
    override val info = Info(
        title = "couchbase-importer",
        //version = "",
        description = "An example of importing a large XML file into Couchbase."
        //contact: Option[Contact] = None,
    )
    //override val externalDocs = Some(new ExternalDocs("Core Docs", "http://acme.com/docs"))
    //override val securitySchemeDefinitions = Map("basicAuth" -> new BasicAuthDefinition())
}
