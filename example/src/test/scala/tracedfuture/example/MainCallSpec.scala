package trackedfuture.example

import java.util.concurrent.{Future => _, _}

import org.scalatest._
import org.scalatest.concurrent._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent._
import scala.concurrent.duration._
import scala.util._



class MainCallSpec extends FlatSpec with AsyncAssertions
{

  var showException=false

  "MainCall" should "show origin method between future " in {
    callAndCheckMethod( Main.f0("AAA"), "f0")
  }

  "MainCall" should "show origin method with map " in {
    callAndCheckMethod( Main.f3("AAA"), "f3")
  }

  "MainCall" should "show origin method with flatMap " in {
    callAndCheckMethod( Main.fFlatMap0(), "fFlatMap0")
  }

  "MainCall" should "show origin method with filter " in {
    callAndCheckMethod( Main.fFilter0(), "fFilter0")
  }

  "MainCall" should "show origin method with withFilter " in {
    callAndCheckMethod( Main.withFilter0(), "withFilter0")
  }

  "MainCall" should "show origin method with collect " in {
    callAndCheckMethod( Main.fCollect0{case "bbb" => "ccc"}, "fCollect0")
  }

  "MainCall" should "show origin method with onComplete " in {
    var lastError: Option[Throwable] = None 
    val ec = ExecutionContext.fromExecutor(
                Executors.newFixedThreadPool(1),
                e=>lastError=Some(e)
             )
    Main.fOnComplete0(ec)
    Thread.sleep(100)
    assert(lastError.isDefined)
    assert(checkMethod("fOnComplete0",lastError.get))
  }

  "MainCall" should "show origin method with onFailure " in {
    var lastError: Option[Throwable] = None
    val ec = ExecutionContext.fromExecutor(
      Executors.newFixedThreadPool(1),
      e=>lastError=Some(e)
    )
    Main.fOnFailure0(ec)
    Thread.sleep(100)
    assert(lastError.isDefined)
    assert(checkMethod("fOnFailure0",lastError.get))
  }

  "MainCall" should "show origin method with onSuccess " in {
    var lastError: Option[Throwable] = None
    val ec = ExecutionContext.fromExecutor(
      Executors.newFixedThreadPool(1),
      e=>lastError=Some(e)
    )
    Main.fOnSuccess0(ec)
    Thread.sleep(100)
    assert(lastError.isDefined)
    assert(checkMethod("fOnSuccess0",lastError.get))
  }

  "MainCall" should "show origin method with foreach" in {
    var lastError: Option[Throwable] = None 
    val ec = ExecutionContext.fromExecutor(
                Executors.newFixedThreadPool(1),
                e=>lastError=Some(e)
    )
    Main.fForeach0(ec)
    Thread.sleep(100)
    assert(lastError.isDefined)
    assert(checkMethod("fForeach0",lastError.get))
  }

  "MainCall" should "show origin method with transform " in {
    callAndCheckMethod( Main.fTransform0(), "fTransform0")
  }

  "MainCall" should "show origin method with recover " in {
    callAndCheckMethod( Main.fRecover0(), "fRecover0")
  }

  "MainCall" should "show origin method with recoverWith " in {
    callAndCheckMethod( Main.fRecoverWith0(), "fRecoverWith0")
  }

  "MainCall" should "show origin from future for AwaitReady" in {
    var lastError: Exception = null
    val ec = ExecutionContext.fromExecutor(
      Executors.newFixedThreadPool(1)
    )
    try {
      Main.awaitReady0(ec)
    } catch {
      case ex: TimeoutException => lastError = ex
    }

    Thread.sleep(100)
    assert(checkMethod("awaitReady0",lastError))
  }

  "MainCall" should "show origin from future for AwaitResult" in {
    var lastError: Exception = null
    val ec = ExecutionContext.fromExecutor(
      Executors.newFixedThreadPool(1)
    )
    try {
      Main.awaitResult0(ec)
    } catch {
      case ex: TimeoutException => lastError = ex
    }

    Thread.sleep(100)
    assert(checkMethod("awaitResult0",lastError))
  }

  private def callAndCheckMethod(body: =>Future[_],method:String): Unit = {
    val f = body
    val w = new Waiter
    f onComplete {
       case Failure(ex) => 
                           val checked = checkMethod(method,ex)
                           w{ assert(checked) }
                           w.dismiss()
       case _ => if (showException) {
                    System.err.println("w successfull")
                 }
                 w{ assert(false) }
                 w.dismiss()
    }
    w.await{timeout(10 seconds)}
  }

  private def checkMethod(method:String, ex: Throwable): Boolean = {
    if (showException) ex.printStackTrace()
    ex.getStackTrace.toSeq.find( _.getMethodName == method ).isDefined
  }


}
