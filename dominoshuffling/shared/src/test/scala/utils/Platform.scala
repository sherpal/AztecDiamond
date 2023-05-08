package utils

sealed trait Platform

object Platform {
  case object JS  extends Platform
  case object JVM extends Platform

  def platform: Platform = utils.thePlatform
}
