# Zio MDC Logging

SLF4J logs and MDC coontext present in ThreadLocal, but ZIO requests are executed in a fiber, not in a thread.


# Usage
 
See the demo application for an example how to tie all together.

# Demo
 
The logger is configured to log two MDC values: 

     <pattern>%highlight(%-5level) %cyan(%logger{15}) - session: [%X{session_id}] agent: [%X{user_agent}] %msg %n</pattern>
     
To run the demo: `sbt demo/run`:

     curl localhost:8080/status -H"X-Session-Id: 101"
     
The log:

    DEBUG c.a.z.d.Main$ - session: [101] agent: [curl/7.64.1] status route called

 


