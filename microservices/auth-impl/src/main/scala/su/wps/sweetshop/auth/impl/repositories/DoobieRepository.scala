package su.wps.sweetshop.auth.impl.repositories

import java.sql.Timestamp
import java.time.{ZoneOffset, ZonedDateTime}

import doobie.util.Meta
import doobie.util.log.{ExecFailure, LogHandler, ProcessingFailure, Success}
import org.slf4j.LoggerFactory

abstract class DoobieRepository {
  lazy val log = LoggerFactory.getLogger(classOf[DoobieRepository])

  implicit val zonedDateTimeMeta: Meta[ZonedDateTime] =
    Meta[Timestamp].imap(timestamp => ZonedDateTime.ofInstant(timestamp.toInstant, ZoneOffset.UTC))(
      zonedDateTime => Timestamp.from(zonedDateTime.toInstant)
    )

  implicit val logHandler: LogHandler = {
    LogHandler {
      case Success(s, a, e1, e2) =>
        log.debug(s"""Successful Statement Execution:
                     |
          |  ${s.lines.dropWhile(_.trim.isEmpty).mkString("\n  ")}
                     |
          | arguments = [${a.mkString(", ")}]
                     |   elapsed = ${e1.toMillis} ms exec + ${e2.toMillis} ms processing
                     |   (${(e1 + e2).toMillis} ms total)
        """.stripMargin)
      case ProcessingFailure(s, a, e1, e2, t) =>
        log.error(s"""Failed Resultset Processing:
                     |
          |  ${s.lines.dropWhile(_.trim.isEmpty).mkString("\n  ")}
                     |
          | arguments = [${a.mkString(", ")}]
                     |   elapsed = ${e1.toMillis} ms exec + ${e2.toMillis} ms processing (failed)
                     |   (${(e1 + e2).toMillis} ms total)
                     |   failure = ${t.getMessage}
        """.stripMargin)
      case ExecFailure(s, a, e1, t) =>
        log.error(s"""Failed Statement Execution:
                     |
          |  ${s.lines.dropWhile(_.trim.isEmpty).mkString("\n  ")}
                     |
          | arguments = [${a.mkString(", ")}]
                     |   elapsed = ${e1.toMillis} ms exec (failed)
                     |   failure = ${t.getMessage}
        """.stripMargin)
    }
  }
}
