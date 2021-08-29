# Tracked Future

[![Join the chat at https://gitter.im/rssh/trackedfuture](https://badges.gitter.im/rssh/trackedfuture.svg)](https://gitter.im/rssh/trackedfuture?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)

##  Overview


  Contains agent, which substitute in bytecode calls to future method wich accepts callbacks
  ( ```Future.apply''' ```map```, ```flatMap``` ```filter``` ... etc) to tracked versions which save origin caller stack.

   Ie. tracked version collect stack trace of origin thread when appropriative construction is created and then,
  when handle exception, merge one with stack trace of this exception; 

 Useful for debugging. 

## Usage

  *  publishLocal  tracked-future to you local repository

  *  when debug, enable agent 
~~~scala
fork := true
javaOptions += s"""-javaagent:${System.getProperty("user.home")}/.ivy2/local/com.github.rssh/trackedfuture_<scalaVersion>/<version>/jars/trackedfuture_2.11-assembly.jar"""
~~~

Use
 * 0.5.0 version for scala 3
 * 0.4.2 for scala 2.13.5

##  Results 

Let's look at the next code:
~~~scala
object Main
{

  def main(args: Array[String]):Unit =
  {
    val f = f0("222")
    try {
       val r = Await.result(f,10 seconds)
    } catch {
       // will print with f0 when agent is enabled
       case ex: Throwable => ex.printStackTrace
    }
  }

  def f0(x:String): Future[Unit] =
  {
    System.err.print("f0:");
    f1(x)
  }

  def f1(x: String): Future[Unit] =
   Future{
     throw new RuntimeException("AAA");
   }

}

~~~

With tracked future agent enabled, instead traces, which ends in top-level executor:

~~~
f0:java.lang.RuntimeException: AAA
  at trackedfuture.example.Main$$anonfun$f1$1.apply(Main.scala:30)
  at trackedfuture.example.Main$$anonfun$f1$1.apply(Main.scala:30)
  at trackedfuture.runtime.TrackedFuture$$anon$1.run(TrackedFuture.scala:21)
  at scala.concurrent.impl.ExecutionContextImpl$AdaptedForkJoinTask.exec(ExecutionContextImpl.scala:121)
  at scala.concurrent.forkjoin.ForkJoinTask.doExec(ForkJoinTask.java:260)
  at scala.concurrent.forkjoin.ForkJoinPool$WorkQueue.runTask(ForkJoinPool.java:1339)
  at scala.concurrent.forkjoin.ForkJoinPool.runWorker(ForkJoinPool.java:1979)
  at scala.concurrent.forkjoin.ForkJoinWorkerThread.run(ForkJoinWorkerThread.java:107)
~~~

you will see traces wich include information: from where future was started:

~~~
f0:java.lang.RuntimeException: AAA
  at trackedfuture.example.Main$$anonfun$f1$1.apply(Main.scala:30)
  at trackedfuture.example.Main$$anonfun$f1$1.apply(Main.scala:30)
  at trackedfuture.runtime.TrackedFuture$$anon$1.run(TrackedFuture.scala:21)
  at scala.concurrent.impl.ExecutionContextImpl$AdaptedForkJoinTask.exec(ExecutionContextImpl.scala:121)
  at scala.concurrent.forkjoin.ForkJoinTask.doExec(ForkJoinTask.java:260)
  at scala.concurrent.forkjoin.ForkJoinPool$WorkQueue.runTask(ForkJoinPool.java:1339)
  at scala.concurrent.forkjoin.ForkJoinPool.runWorker(ForkJoinPool.java:1979)
  at scala.concurrent.forkjoin.ForkJoinWorkerThread.run(ForkJoinWorkerThread.java:107)
  at java.lang.Thread.getStackTrace(Thread.java:1552)
  at trackedfuture.runtime.TrackedFuture$.apply(TrackedFuture.scala:13)
  at trackedfuture.runtime.TrackedFuture$.rapply(TrackedFuture.scala:39)
  at trackedfuture.runtime.TrackedFuture.rapply(TrackedFuture.scala) 
  at trackedfuture.example.Main$.f1(Main.scala:29)
  at trackedfuture.example.Main$.f0(Main.scala:25)
  at trackedfuture.example.Main$.main(Main.scala:13)
  at trackedfuture.example.Main.main(Main.scala)
~~~

## Additional Notes
 
If you want a version with more wrappend methods and with frames cleanup - don't hesitate to submit pull request ;)

