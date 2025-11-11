package utils

import org.scalajs.dom

import scala.concurrent.duration.FiniteDuration

def consoleTimerLoggerDev: TimerLogger =
  if scala.scalajs.LinkingInfo.developmentMode then
    (label: String, time: FiniteDuration) => dom.console.log(s"<$label> ${time.toMillis} ms")
  else TimerLogger.noOp
