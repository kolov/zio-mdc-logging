package com.newmotion.locationmanagerviews.common


import com.typesafe.scalalogging.Logger
import org.slf4j.MDC
import zio.{FiberRef, UIO, ZIO}
import scala.jdk.CollectionConverters._

trait MdcLogging {
  def mdclogging: MdcLogging.Service[Any]
}

object MdcLogging {

  trait Service[R] {
    def context: UIO[FiberRef[Map[String, String]]]
  }

}

final class MdcLogger(baseLogger: Logger) {


  def trace(t: Throwable)(msg: String): ZIO[MdcLogging, Nothing, Unit] =
    logWithMdc(baseLogger.trace(_, t))(msg)

  def trace(msg: String): ZIO[MdcLogging, Nothing, Unit] = logWithMdc(baseLogger.trace(_))(msg)

  def debug(t: Throwable)(msg: String): ZIO[MdcLogging, Nothing, Unit] =
    logWithMdc(baseLogger.debug(_, t))(msg)

  def debug(msg: String): ZIO[MdcLogging, Nothing, Unit] = logWithMdc(baseLogger.debug(_))(msg)

  def info(t: Throwable)(msg: String): ZIO[MdcLogging, Nothing, Unit] =
    logWithMdc(baseLogger.info(_, t))(msg)

  def info(msg: String): ZIO[MdcLogging, Nothing, Unit] = logWithMdc(baseLogger.info(_))(msg)

  def warn(t: Throwable)(msg: String): ZIO[MdcLogging, Nothing, Unit] = logWithMdc(baseLogger.warn(_, t))(msg)

  def warn(msg: String): ZIO[MdcLogging, Nothing, Unit] = logWithMdc(baseLogger.warn(_))(msg)

  def error(t: Throwable)(msg: String): ZIO[MdcLogging, Nothing, Unit] =
    logWithMdc(baseLogger.error(_, t))(msg)

  def error(msg: String): ZIO[MdcLogging, Nothing, Unit] = logWithMdc(baseLogger.error(_))(msg)

  private def logWithMdc(f: String => Unit)(msg: String): ZIO[MdcLogging, Nothing, Unit] = {
    ZIO.accessM[MdcLogging] { mdc =>
      for {
        ref <- mdc.mdclogging.context
        ctx <- ref.get
        _ <- ZIO.effectTotal {
          MDC.setContextMap(ctx.asJava)
          f(msg)
          MDC.clear()
        }
      } yield ()
    }
  }

}

object MdcLogger {

  def withMdc(logger: Logger) = new MdcLogger(logger)

}
