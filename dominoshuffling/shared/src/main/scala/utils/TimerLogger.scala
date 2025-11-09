package utils

import scala.concurrent.duration.*

trait TimerLogger {
  def log(label: String, time: FiniteDuration): Unit

  def time[T](label: String)(body: => T): T = {
    val startTime = System.currentTimeMillis()
    val res       = body
    val itTook    = (System.currentTimeMillis() - startTime).millis
    log(label, itTook)
    res
  }
}

object TimerLogger {

  val noOp: TimerLogger = (label: String, time: FiniteDuration) => ()

}
