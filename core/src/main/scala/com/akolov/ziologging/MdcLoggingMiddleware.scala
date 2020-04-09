package com.akolov.ziologging


import cats.data.{Kleisli, OptionT}
import com.newmotion.locationmanagerviews.common.MdcLogging
import org.http4s.Request
import org.http4s.server.HttpMiddleware
import zio.blocking.Blocking
import zio.{FiberRef, UIO, ZIO}
import zio.clock.Clock
import zio.console.Console
import zio.random.Random
import zio.system.System


object MdcLoggingMiddleware {


  def apply[E <: MdcLogging](createMdcContext: Request[ZIO[E, Throwable, *]] => Map[String, String]): HttpMiddleware[ZIO[E, Throwable, *]] = { in =>
    Kleisli { req =>
      OptionT(for {
        mdcRef <- ZIO.accessM[MdcLogging](_.mdclogging.context)
        _ <- mdcRef.set(createMdcContext(req))
        resp <- in.run(req).value
      } yield resp)
    }
  }

  def provideMDCHolder[A](io: ZIO[zio.ZEnv with MdcLogging, Throwable, A]): ZIO[zio.ZEnv, Throwable, A] = io.provideSomeM[zio.ZEnv, Throwable] {
    FiberRef.make(Map.empty[String, String]).flatMap { fiberRef =>
      ZIO.access[zio.ZEnv] { e =>
        new Clock with Console with System with Random with Blocking with MdcLogging {

          override val clock: Clock.Service[Any] = e.clock
          override val console: Console.Service[Any] = e.console
          override val system: System.Service[Any] = e.system
          override val random: Random.Service[Any] = e.random
          override val blocking: Blocking.Service[Any] = e.blocking

          override def mdclogging: MdcLogging.Service[Any] = new MdcLogging.Service[Any] {
            override def context: UIO[FiberRef[Map[String, String]]] = ZIO.succeed(fiberRef)
          }
        }
      }
    }
  }

}
