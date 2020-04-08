package com.akolov.ziomdclogging.demo

import cats.effect.ExitCode
import com.akolov.ziologging.TracingMiddleware
import com.newmotion.locationmanagerviews.common.{MdcTracing, TracingLogger}
import com.typesafe.scalalogging.LazyLogging
import org.http4s._
import org.http4s.dsl.Http4sDsl
import org.http4s.implicits._
import org.http4s.server.HttpMiddleware
import org.http4s.server.blaze.BlazeServerBuilder
import zio.ZIO
import zio.interop.catz._


object Main extends zio.App with LazyLogging {

  val tracingLogger = TracingLogger.tracing(logger)

  type AppTask[A] = ZIO[zio.ZEnv with MdcTracing, Throwable, A]

  val dsl = new Http4sDsl[AppTask] {}

  import dsl._

  val route: HttpRoutes[AppTask] = HttpRoutes.of[AppTask] {
    case GET -> Root / "status" =>
      tracingLogger.debug("status route called") *>
        Ok("OK")
  }

  val traceMiddleware: HttpMiddleware[AppTask] = TracingMiddleware { req: Request[AppTask] =>
    req.headers.toList.map(h => (h.name.value, h.value)).collect {
      case ("X-Session-Id", v) => ("session_id", v)
      case ("User-Agent", v) => ("user_agent", v)
    }.toMap
  }

  val finalHttpApp: HttpApp[AppTask] = traceMiddleware(route).orNotFound

  val io: ZIO[zio.ZEnv with MdcTracing, Throwable, Unit] = ZIO.runtime[zio.ZEnv with MdcTracing].flatMap { implicit rts =>
    for {
      _ <- BlazeServerBuilder[AppTask]
        .bindHttp(8080, "0.0.0.0")
        .withHttpApp(finalHttpApp)
        .serve
        .compile[AppTask, AppTask, ExitCode]
        .drain
    } yield ()
  }


  override def run(args: List[String]): ZIO[zio.ZEnv, Nothing, Int] = TracingMiddleware.provideMDCHolder[Unit](io)
    .fold(_ => 1
      , _ => 0)
}
