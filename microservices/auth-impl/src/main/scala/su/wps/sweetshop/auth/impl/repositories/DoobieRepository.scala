package su.wps.sweetshop.auth.impl.repositories

import doobie.util.log.{ExecFailure, LogHandler, ProcessingFailure, Success}
import org.slf4j.LoggerFactory

abstract class DoobieRepository {
  lazy val log = LoggerFactory.getLogger(classOf[DoobieRepository])

  implicit val logHandler: LogHandler = {
    LogHandler {
      case Success(s, a, e1, e2) =>
        log.debug(s"""Successful Statement Execution:
                     |
          |  ${s.linesIterator.dropWhile(_.trim.isEmpty).mkString("\n  ")}
                     |
          | arguments = [${a.mkString(", ")}]
                     |   elapsed = ${e1.toMillis} ms exec + ${e2.toMillis} ms processing
                     |   (${(e1 + e2).toMillis} ms total)
        """.stripMargin)
      case ProcessingFailure(s, a, e1, e2, t) =>
        log.error(s"""Failed Resultset Processing:
                     |
          |  ${s.linesIterator.dropWhile(_.trim.isEmpty).mkString("\n  ")}
                     |
          | arguments = [${a.mkString(", ")}]
                     |   elapsed = ${e1.toMillis} ms exec + ${e2.toMillis} ms processing (failed)
                     |   (${(e1 + e2).toMillis} ms total)
                     |   failure = ${t.getMessage}
        """.stripMargin)
      case ExecFailure(s, a, e1, t) =>
        log.error(s"""Failed Statement Execution:
                     |
          |  ${s.linesIterator.dropWhile(_.trim.isEmpty).mkString("\n  ")}
                     |
          | arguments = [${a.mkString(", ")}]
                     |   elapsed = ${e1.toMillis} ms exec (failed)
                     |   failure = ${t.getMessage}
        """.stripMargin)
    }
  }
}
