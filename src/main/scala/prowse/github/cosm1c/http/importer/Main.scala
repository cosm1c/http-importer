package prowse.github.cosm1c.http.importer

import java.util.logging.{Level, LogManager}

import com.typesafe.scalalogging.LazyLogging
import org.slf4j.bridge.SLF4JBridgeHandler

object Main extends LazyLogging {

    // Redirect all logging calls to SLF4J
    LogManager.getLogManager.reset()
    SLF4JBridgeHandler.install()
    java.util.logging.Logger.getLogger("global").setLevel(Level.FINEST)

    def main(args: Array[String]): Unit = {
        Thread.currentThread().setUncaughtExceptionHandler((_: Thread, e: Throwable) => {
            logger.error("UncaughtException on main thread", e)
        })

        akka.Main.main(Array(classOf[AppSupervisorActor].getName))
    }

}
