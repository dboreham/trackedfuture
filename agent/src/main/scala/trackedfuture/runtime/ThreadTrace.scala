package trackedfuture.runtime

import scala.util._

// Object containing a TLS variable referencing a StackTraces object for the current thread
object ThreadTrace {

  val prevTraces = new DynamicVariable[StackTraces](null)

  // Never called because implementer thought it was better to inline everywhere (inline keyword not available until Scala 3)
  // Captures the current thread stack trace and makes a new StackTraces object, linked with any StackTraces (treated as parent) currently 
  // stored in TLS for this thread.
  def retrieveCurrent(): StackTraces = {
    val trace = Thread.currentThread.getStackTrace
    new StackTraces(trace, prevTraces.value)
  }

  // Called in various places to set the TLS reference to parent thread trace (used subsequently also in various places)
  def setPrev(st: StackTraces): Unit = {
    prevTraces.value = st
  }

  // Called everywhere we're making an exception or an exception-like object
  // This is where rubber hits road for the whole exercise.
  def mergeWithPrev(trace: Array[StackTraceElement]): Array[StackTraceElement] = {
    if (prevTraces.value eq null) {
      trace
    } else {
      new StackTraces(trace, prevTraces.value).toTrace
    }
  }

}
