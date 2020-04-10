package com.newmotion.zio_http4s_mdc

import org.log4s.Logger
import org.slf4j.MDC
import zio.{FiberRef, UIO, ZIO}

trait MdcLogging {
  def mdclogging: MdcLogging.Service[Any]
}

object MdcLogging {

  trait Service[R] {
    def context: UIO[FiberRef[Map[String, String]]]
  }

}

final class Log4sMdcLogger(baseLogger: Logger) {

  def trace(t: Throwable)(msg: String): ZIO[MdcLogging, Nothing, Unit] =
    logWithMdc(baseLogger.trace(t)(_))(msg)

  def trace(msg: String): ZIO[MdcLogging, Nothing, Unit] = logWithMdc(baseLogger.trace(_))(msg)

  def debug(t: Throwable)(msg: String): ZIO[MdcLogging, Nothing, Unit] =
    logWithMdc(baseLogger.debug(t)(_))(msg)

  def debug(msg: String): ZIO[MdcLogging, Nothing, Unit] = logWithMdc(baseLogger.debug(_))(msg)

  def info(t: Throwable)(msg: String): ZIO[MdcLogging, Nothing, Unit] =
    logWithMdc(baseLogger.info(t)(_))(msg)

  def info(msg: String): ZIO[MdcLogging, Nothing, Unit] = logWithMdc(baseLogger.info(_))(msg)

  def warn(t: Throwable)(msg: String): ZIO[MdcLogging, Nothing, Unit] = logWithMdc(baseLogger.warn(t)(_))(msg)

  def warn(msg: String): ZIO[MdcLogging, Nothing, Unit] = logWithMdc(baseLogger.warn(_))(msg)

  def error(t: Throwable)(msg: String): ZIO[MdcLogging, Nothing, Unit] =
    logWithMdc(baseLogger.error(t)(_))(msg)

  def error(msg: String): ZIO[MdcLogging, Nothing, Unit] = logWithMdc(baseLogger.error(_))(msg)

  private def logWithMdc(f: String => Unit)(msg: String): ZIO[MdcLogging, Nothing, Unit] = {
    ZIO.accessM[MdcLogging] { mdc =>
      for {
        ref <- mdc.mdclogging.context
        ctx <- ref.get
        _ <- ZIO.effectTotal {
          ctx.foreach { case (k, v) => MDC.put(k, v) }
          f(msg)
          MDC.clear()
        }
      } yield ()
    }
  }

}

object Log4sMdcLogger {

  def withMdc(logger: Logger) = new Log4sMdcLogger(logger)

}
