package utils

sealed trait Platform

object Platform {
  case object JS  extends Platform
  case object JVM extends Platform

  def platform: Platform = utils.thePlatform
  
  inline transparent def platformValue[A](ifJVM: => A, ifJS: => A): A = platform match {
    case JS => ifJS
    case JVM => ifJVM
  }
}
