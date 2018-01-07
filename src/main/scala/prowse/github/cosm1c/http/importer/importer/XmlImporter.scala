package prowse.github.cosm1c.http.importer.importer

import java.util.Objects.isNull

import akka.event.LoggingAdapter
import akka.stream.alpakka.xml.scaladsl.XmlParsing
import akka.stream.alpakka.xml.{Characters, EndElement, ParseEvent, StartElement}
import akka.stream.scaladsl.{Flow, Sink}
import akka.stream.stage.{GraphStage, GraphStageLogic, InHandler, OutHandler}
import akka.stream.{Attributes, FlowShape, Inlet, Outlet}
import akka.util.ByteString
import akka.{Done, NotUsed}
import prowse.github.cosm1c.http.importer.importer.XmlImporter.ProteinEntry

import scala.concurrent.Future

object XmlImporter {

    final case class ProteinEntry(id: String, name: String, keyWords: Seq[String])

    def xmlToData: Flow[ByteString, ProteinEntry, NotUsed] =
        Flow[ByteString]
            .via(XmlParsing.parser)
            .via(XmlParsing.subslice("ProteinDatabase" :: Nil))
            .via(new XmlImporter)

    def dataPersist()(implicit log: LoggingAdapter): Sink[ProteinEntry, Future[Done]] =
        Sink.foreach(data => log.info("Data: {}", data))
}

/*
 * <ProteinDatabase>
 *   ...
 *   <ProteinEntry id="?">
 *     ...
 *     <protein>
 *       <name>?</name>
 *     </protein>
 *     ...
 *     <keywords>
 *      <keyword>?</keyword>
 *      <keyword>?</keyword>
 *     </keywords>
 *     ...
 *   </ProteinEntry>
 *   ...
 * </ProteinDatabase>
 */
class XmlImporter extends GraphStage[FlowShape[ParseEvent, ProteinEntry]] {

    private val in = Inlet[ParseEvent]("XmlImporter.in")
    private val out = Outlet[ProteinEntry]("XmlImporter.out")

    override val shape: FlowShape[ParseEvent, ProteinEntry] = FlowShape.of(in, out)

    @SuppressWarnings(Array("org.wartremover.warts.Var", "org.wartremover.warts.Null"))
    override def createLogic(attr: Attributes): GraphStageLogic =
        new GraphStageLogic(shape) {
            private var toPull = false
            private var id: String = _
            private var name: String = _
            private var keywords: Seq[String] = Seq.empty
            private var inProteinEntry = false
            private var inProtein = false
            private var inName = false
            private var inKeywords = false
            private var inKeyword = false

            setHandler(in, new InHandler {
                override def onPush(): Unit = {
                    toPull = true
                    val elem = grab(in)
                    elem match {
                        case StartElement("ProteinEntry", attributes) if !inProteinEntry =>
                            inProteinEntry = true
                            id = attributes("id")
                            name = null
                            keywords = Seq.empty

                        case EndElement("ProteinEntry") if inProteinEntry =>
                            inProteinEntry = false
                            if (isNull(name)) fail(out, new RuntimeException(s"ProteinEntry id={} missing name"))
                            push(out, ProteinEntry(id, name, keywords))
                            toPull = false

                        case StartElement("protein", _) if inProteinEntry =>
                            inProtein = true

                        case EndElement("protein") if inProteinEntry =>
                            inProtein = false

                        case StartElement("name", _) if inProtein =>
                            inName = true

                        case EndElement("name") if inProtein =>
                            inName = false

                        case Characters(chars) if inName =>
                            name = chars

                        case StartElement("keywords", _) if inProteinEntry =>
                            inKeywords = true

                        case EndElement("keywords") if inProteinEntry =>
                            inKeywords = false

                        case StartElement("keyword", _) if inKeywords =>
                            inKeyword = true

                        case EndElement("keyword") if inKeywords =>
                            inKeyword = false

                        case Characters(chars) if inKeyword =>
                            keywords = keywords :+ chars

                        case _ => // ignore all other XML
                    }

                    if (toPull) pull(in)
                }
            })

            setHandler(out, new OutHandler {
                override def onPull(): Unit = {
                    pull(in)
                }
            })
        }

}
