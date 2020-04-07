package com.akolov.ziologging


import cats.data.{Kleisli, OptionT}
import com.newmotion.locationmanagerviews.common.Tracing
import org.http4s.Request
import org.http4s.server.HttpMiddleware
import zio.blocking.Blocking
import zio.{FiberRef, UIO, ZIO}
import zio.clock.Clock
import zio.console.Console
import zio.random.Random
import zio.system.System


object TracingMiddleware {


  def tracingMiddleware[E <: Tracing](createMdcContext: Request[ZIO[E, Throwable, *]] => Map[String, String]): HttpMiddleware[ZIO[E, Throwable, *]] = { in =>
    Kleisli { req =>
      OptionT(for {
        mdcRef <- ZIO.accessM[Tracing](_.tracing.context)
        _ <- mdcRef.set(createMdcContext(req))
        resp <- in.run(req).value
      } yield resp)
    }
  }

  def provideMDCHolder[A](io: ZIO[zio.ZEnv with Tracing, Throwable, A]): ZIO[zio.ZEnv, Throwable, A] = io.provideSomeM[zio.ZEnv, Throwable] {
    FiberRef.make(Map.empty[String, String]).flatMap { fiberRef =>
      ZIO.access[zio.ZEnv] { e =>
        new Clock with Console with System with Random with Blocking with Tracing {

          override val clock: Clock.Service[Any] = e.clock
          override val console: Console.Service[Any] = e.console
          override val system: System.Service[Any] = e.system
          override val random: Random.Service[Any] = e.random
          override val blocking: Blocking.Service[Any] = e.blocking

          override def tracing: Tracing.Service[Any] = new Tracing.Service[Any] {
            override def context: UIO[FiberRef[Map[String, String]]] = ZIO.succeed(fiberRef)
          }
        }
      }
    }
  }

}
