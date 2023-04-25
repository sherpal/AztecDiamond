package computationcom

sealed trait WeightComputationStatusInfo {
  def percentage: Int
}

object WeightComputationStatusInfo {
  case object Started extends WeightComputationStatusInfo {
    def percentage: Int = 0
  }
  case object Ended extends WeightComputationStatusInfo {
    def percentage: Int = 100
  }
  case class Ongoing(percentage: Int) extends WeightComputationStatusInfo
  case object Waiting extends WeightComputationStatusInfo {
    def percentage: Int = 0
  }
}
