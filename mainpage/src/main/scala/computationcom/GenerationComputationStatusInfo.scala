package computationcom

import computationcom.GenerationComputationStatusInfo.Started
import computationcom.GenerationComputationStatusInfo.Ended
import computationcom.GenerationComputationStatusInfo.Ongoing
import computationcom.GenerationComputationStatusInfo.Waiting

sealed trait GenerationComputationStatusInfo {
  def percentage: Int
  def ended: Boolean = this match
    case Started             => false
    case Ended               => true
    case Ongoing(percentage) => false
    case Waiting             => false

}

object GenerationComputationStatusInfo {
  case object Started extends GenerationComputationStatusInfo {
    def percentage: Int = 0
  }
  case object Ended extends GenerationComputationStatusInfo {
    def percentage: Int = 100
  }
  case class Ongoing(percentage: Int) extends GenerationComputationStatusInfo
  case object Waiting extends GenerationComputationStatusInfo {
    def percentage: Int = 0
  }
}
