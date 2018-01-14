package prowse.github.cosm1c.http.importer.rest

import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport
import io.circe._
import io.circe.generic.semiauto._
import io.circe.java8.time.TimeInstances
import prowse.github.cosm1c.http.importer.job.JobManagerActor.JobInfo

trait RestJsonProtocol extends FailFastCirceSupport with TimeInstances {
    implicit val jobInfoDecoder: Decoder[JobInfo] = deriveDecoder[JobInfo]
    implicit val jobInfoEncoder: Encoder[JobInfo] = deriveEncoder[JobInfo]
}
