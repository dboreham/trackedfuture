package trackedfuture.runtime

import java.util.concurrent.TimeoutException

import trackedfuture.util.ThreadLocalIterator

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Awaitable}

// Hooks for the Await object.
// Hooks the ready and result methods but actually the only action taken is to catch and re-throw
// TimeoutExceptions. Other exceptions are bubbled up. Presumably this is because those exceptions
// will have already been chained inside the future body.
// timeout exceptions originate outside the body and so will not have been chained.
object TrackedAwait {
  def ready[T](awaitOriginal: Await.type, awaitable: Awaitable[T], atMost: Duration): awaitable.type = {
    try {
      awaitOriginal.ready(awaitable, atMost)
    } catch {
      case ex: TimeoutException => {
        ThreadTrace.setPrev(threadLocalTrace(awaitable))
        ex.setStackTrace(ThreadTrace.mergeWithPrev(ex.getStackTrace))
        throw ex
      }
    }
  }

  def result[T](awaitOriginal: Await.type, awaitable: Awaitable[T], atMost: Duration): T = {
    try {
      awaitOriginal.result(awaitable, atMost)
    } catch {
      case ex: TimeoutException => {
        ThreadTrace.setPrev(threadLocalTrace(awaitable))
        ex.setStackTrace(ThreadTrace.mergeWithPrev(ex.getStackTrace))
        throw ex
      }
    }
  }

  // Confusing because awaitable == future
  // What's going on here is : we want to find the parent stack traces for the Future upon which 
  // we were waiting when the timeout exception was thrown.
  // We do this by arranging to have stored a set of tuples mapping future references to parent traces
  // we now look in this map to see if we have parent traces for our target future.
  // problem with this approach seems to be that the parent traces are associated with a TLS key and therefore 
  // will only include data for "live" threads. It appears there may be a race condition where the thread is re-used
  // before we get here. Possibly the line below does something more clever -- don't yet understand it all.
  private def threadLocalTrace[T](awaitable: Awaitable[T]): StackTraces = {
    val trace = new ThreadLocalIterator[StackTraces](classOf[StackTraces]).iterator.find(_.getCurrentFuture == awaitable)
    if (trace == null) new StackTraces(Array()) else trace.get
  }
}
