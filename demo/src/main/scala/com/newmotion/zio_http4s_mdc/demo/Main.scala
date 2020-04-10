package com.newmotion.zio_http4s_mdc.demo

import cats.effect.ExitCode
import com.newmotion.zio_http4s_mdc.{Log4sMdcLogger, MdcLogging, MdcLoggingMiddleware}
import org.http4s._
import org.http4s.dsl.Http4sDsl
import org.http4s.implicits._
import org.http4s.server.HttpMiddleware
import org.http4s.server.blaze.BlazeServerBuilder
import zio.ZIO
import zio.interop.catz._
import org.log4s.getLogger

object Main extends zio.App {

  val mdcLogger = Log4sMdcLogger.withMdc(getLogger)

  type AppTask[A] = ZIO[zio.ZEnv with MdcLogging, Throwable, A]

  val dsl = new Http4sDsl[AppTask] {}

  import dsl._

  val route: HttpRoutes[AppTask] = HttpRoutes.of[AppTask] {
    case GET -> Root / "status" =>
      mdcLogger.debug("status route called") *>
        Ok()
  }

  val mdcMiddleware: HttpMiddleware[AppTask] = MdcLoggingMiddleware { req: Request[AppTask] =>
    req.headers.toList.map(h => (h.name.value, h.value)).collect {
      case ("X-Session-Id", v) => ("session_id", v)
      case ("User-Agent", v) => ("user_agent", v)
    }.toMap
  }

  val finalHttpApp: HttpApp[AppTask] = mdcMiddleware(route).orNotFound

  val io: ZIO[zio.ZEnv with MdcLogging, Throwable, Unit] = ZIO.runtime[zio.ZEnv with MdcLogging].flatMap { implicit rts =>
    for {
      _ <- BlazeServerBuilder[AppTask]
        .bindHttp(8080, "0.0.0.0")
        .withHttpApp(finalHttpApp)
        .serve
        .compile[AppTask, AppTask, ExitCode]
        .drain
    } yield ()
  }


  override def run(args: List[String]): ZIO[zio.ZEnv, Nothing, Int] = MdcLoggingMiddleware.provideMDCHolder[Unit](io)
    .fold(e => {
      println(s"Error: $e"); 1
    }
      , _ => 0)
}
