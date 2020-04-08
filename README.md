# Zio MDC Logging

MDC Logging for http4s/Zio 


SLF4J logs any MDC context present in ThreadLocal, but http4s with ZIO executes requests in a fiber, not in a thread.

This library offers :
 - ZIO environment to create FiberRef
 - http4s middleware that sets a context in this FiberRef 
 - a wrapper around a logger that retrieves the context from FiberRef and puts it in the ThreadLocal before logging.
 
The FiberRef containing the context is passed as a ZIO Environment.

# Usage
 
To create http4s middleware, pass your function that creates a context map from the request. Here's middleware that will
create MDC with two fields - `session_id` and `user_agent`

```scala 
 type AppTask[A] = ZIO[zio.ZEnv with MdcTracing, Throwable, A]

 val traceMiddleware: HttpMiddleware[AppTask] = TracingMiddleware { req: Request[AppTask] =>
    req.headers.toList.map(h => (h.name.value, h.value)).collect {
      case ("X-Session-Id", v) => ("session_id", v)
      case ("User-Agent", v) => ("user_agent", v)
    }.toMap
  }

  val finalHttpApp: HttpApp[AppTask] = traceMiddleware(routes).orNotFound
```

Anywhere in the route code, wrap an existing slf4j logger and use it:

    val tracingLogger = TracingLogger.tracing(slf4jLogger)
    
    tracingLogger.debug("status route called") *>
          Ok("OK")
          
Logging is effectful, all logging methods have type `ZIO[MdcTracing, Nothing, Unit]`
          
# Demo
 
     
To run the demo: `sbt demo/run`. The logger is configured to log the two we defined above: 

     <pattern>%highlight(%-5level) %cyan(%logger{15}) - session: [%X{session_id}] agent: [%X{user_agent}] %msg %n</pattern>

Try:

     curl localhost:8080/status -H"X-Session-Id: 101"
     
The log output:

    DEBUG c.a.z.d.Main$ - session: [101] agent: [curl/7.64.1] status route called

 


