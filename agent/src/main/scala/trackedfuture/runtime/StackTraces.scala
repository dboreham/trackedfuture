package trackedfuture.runtime

import scala.concurrent.Future

// This class captures the "parent" stack trace at a point where we're
// about to context switch to a different thread.
// It needs to recursively capture any "grandparent" and so on "ancestors" thread stacks
// in the case that we are context switching after previously context switching
// (in order to allow the eventual exception stack trace to begin at whatever was the "top level")
// This recursive ancestor structure is captured via the "prev" member, constituting a linked list
// of objects of this class.
class StackTraces(elements: Array[StackTraceElement], prev: StackTraces = null) {

  private var currentFuture: Future[Unit] = _

  // Only called from TrackedAwait.threadLocalTrace
  def getCurrentFuture[T]: Future[Unit] = currentFuture

  // Only called from TrackedFuture.apply
  def setCurrentFuture(f: Future[Unit]): Unit = {
    currentFuture = f
  }

  // Returns the cumulative depth (in frames) of this trace plus all ancestors
  def depth(): Int = {
    val prevDepth = if (prev eq null) 0 else prev.depth()
    prevDepth + elements.length
  }

  // Cast the contents of this object, including parents to an array compatible with JVM exceptions
  // Only called from one place : ThreadTrace.mergeWithPrev
  def toTrace: Array[StackTraceElement] = {
    val trace = new Array[StackTraceElement](depth())
    fill(trace, 0)
    trace
  }

  // Only called from toTrace(), should be private
  def fill(trace: Array[StackTraceElement], startIndex: Int): Int = {
    val nextIndex = startIndex + elements.length
    System.arraycopy(elements, 0, trace, startIndex, elements.length)
    //TODO: think - how it's needed,
    //val inserted = createSeparatorStackTraceElement();
    if (prev eq null) {
      nextIndex
    } else {
      prev.fill(trace, nextIndex)
    }
  }

}


