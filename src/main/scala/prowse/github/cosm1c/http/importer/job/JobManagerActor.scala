package prowse.github.cosm1c.http.importer.job

import java.time.LocalDateTime

import akka.NotUsed
import akka.actor.{Actor, ActorLogging, Props}
import akka.stream._
import akka.stream.scaladsl.{BroadcastHub, Keep, Sink, Source}
import prowse.github.cosm1c.http.importer.job.JobManagerActor._
import prowse.github.cosm1c.http.importer.util.ReplyStatus
import prowse.github.cosm1c.http.importer.util.ReplyStatus.{ReplyFailure, ReplySuccess}

import scala.concurrent.duration._
import scala.util.{Failure, Success}

object JobManagerActor {

    def props()(implicit mat: Materializer): Props = Props(new JobManagerActor())

    final case class JobInfo(jobId: Long,
                             description: String,
                             startDateTime: LocalDateTime,
                             endDateTime: Option[LocalDateTime] = None,
                             error: Option[String] = None,
                             curr: Option[Int] = None,
                             total: Option[Int] = None)


    final case object ListRunningJobs

    final case class GetJobInfo(jobId: Long)

    final case class KillJob(jobId: Long)

    final case class SubscribeJob(jobId: Long, sink: Sink[Int, _])

    // Example Job Config
    final case class CreateJob(description: String,
                               total: Int)

    private final case class JobCompleted(jobId: Long,
                                          endDateTime: LocalDateTime,
                                          error: Option[String] = None)

    private final case class JobUpdate(jobId: Long, curr: Int)

}

@SuppressWarnings(Array("org.wartremover.warts.Var"))
class JobManagerActor()(implicit mat: Materializer) extends Actor with ActorLogging {

    private var jobCounter: Long = 0L
    private var jobInfos = Map.empty[Long, JobInfo]
    private var jobStreams = Map.empty[Long, Source[Int, NotUsed]]
    private var jobKillSwitches = Map.empty[Long, UniqueKillSwitch]

    override def receive: Receive = {

        case ListRunningJobs => sender() ! jobInfos.values

        case GetJobInfo(jobId) =>
            sender() ! jobInfos.get(jobId)

        case KillJob(jobId) =>
            val reply: ReplyStatus.Reply = jobKillSwitches.get(jobId) match {
                case Some(killSwitch) =>
                    killSwitch.shutdown()
                    ReplySuccess

                case None => ReplyFailure
            }
            sender() ! reply

        case SubscribeJob(jobId, sink) =>
            val reply: ReplyStatus.Reply = jobStreams.get(jobId) match {
                case Some(jobStream) =>
                    jobStream.runWith(sink)
                    ReplySuccess

                case None => ReplyFailure
            }
            sender() ! reply

        case CreateJob(description, total) =>
            jobCounter += 1
            val jobId = jobCounter
            // Example only
            val jobStream = Source(1 to total)
                .throttle(1, 100.milliseconds, 1, ThrottleMode.shaping)
                .buffer(1, OverflowStrategy.dropHead)
            val (killSwitch, hubSource) = jobStream
                .viaMat(KillSwitches.single)(Keep.right)
                .toMat(BroadcastHub.sink)(Keep.both)
                .run()
            hubSource
                .runWith(Sink.foreach { curr =>
                    self ! JobUpdate(jobId, curr)
                })
                .onComplete {
                    case Success(_) =>
                        log.info("[JOB {}] stream complete", jobId)
                        self ! JobCompleted(jobId, LocalDateTime.now)

                    case Failure(exception) =>
                        log.error(exception, "[JOB {}] stream failed", jobId)
                        self ! JobCompleted(jobId, LocalDateTime.now, error = Some(exception.getMessage))
                }(context.dispatcher)
            val jobInfo = JobInfo(jobId, description, LocalDateTime.now())
            jobInfos += jobId -> jobInfo
            jobStreams += jobId -> hubSource
            jobKillSwitches += jobId -> killSwitch
            log.info("[{}] Started", jobId)
            sender() ! jobInfo

        case JobCompleted(jobId, _, _) =>
            // TODO: store result of job
            jobInfos -= jobId
            jobStreams -= jobId
            jobKillSwitches -= jobId

        case JobUpdate(jobId, curr) =>
            jobInfos.get(jobId)
                .foreach(jobInfo =>
                    jobInfos += jobId -> jobInfo.copy(curr = Some(curr)))

    }

}
