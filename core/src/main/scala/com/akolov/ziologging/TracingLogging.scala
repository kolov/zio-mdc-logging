package com.newmotion.locationmanagerviews.common


import com.typesafe.scalalogging.Logger
import org.slf4j.MDC
import zio.{FiberRef, UIO, ZIO}

trait Tracing {
  def tracing: Tracing.Service[Any]
}

object Tracing {

  trait Service[R] {
    def context: UIO[FiberRef[Map[String, String]]]
  }

}

final class TracingLogger(baseLogger: Logger) {


  def trace(t: Throwable)(msg: String): ZIO[Tracing, Nothing, Unit] =
    logWithTracing(baseLogger.trace(_, t))(msg)

  def trace(msg: String): ZIO[Tracing, Nothing, Unit] = logWithTracing(baseLogger.trace(_))(msg)

  def debug(t: Throwable)(msg: String): ZIO[Tracing, Nothing, Unit] =
    logWithTracing(baseLogger.debug(_, t))(msg)

  def debug(msg: String): ZIO[Tracing, Nothing, Unit] = logWithTracing(baseLogger.debug(_))(msg)

  def info(t: Throwable)(msg: String): ZIO[Tracing, Nothing, Unit] =
    logWithTracing(baseLogger.info(_, t))(msg)

  def info(msg: String): ZIO[Tracing, Nothing, Unit] = logWithTracing(baseLogger.info(_))(msg)

  def warn(t: Throwable)(msg: String): ZIO[Tracing, Nothing, Unit] = logWithTracing(baseLogger.warn(_, t))(msg)

  def warn(msg: String): ZIO[Tracing, Nothing, Unit] = logWithTracing(baseLogger.warn(_))(msg)

  def error(t: Throwable)(msg: String): ZIO[Tracing, Nothing, Unit] =
    logWithTracing(baseLogger.error(_, t))(msg)

  def error(msg: String): ZIO[Tracing, Nothing, Unit] = logWithTracing(baseLogger.error(_))(msg)

  private def logWithTracing(f: String => Unit)(msg: String): ZIO[Tracing, Nothing, Unit] = {
    ZIO.accessM[Tracing] { tracing =>
      for {
        ref <- tracing.tracing.context
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

object TracingLogger {

  def tracing(logger: Logger) = new TracingLogger(logger)

}