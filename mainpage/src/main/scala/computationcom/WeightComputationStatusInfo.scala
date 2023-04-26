package computationcom

sealed trait WeightComputationStatusInfo {
  def percentage: Int
  def ended: Boolean
}

object WeightComputationStatusInfo {
  case object Started extends WeightComputationStatusInfo {
    def percentage: Int = 0
    def ended: Boolean  = false
  }
  case object Ended extends WeightComputationStatusInfo {
    def percentage: Int = 100
    def ended: Boolean  = true
  }
  case class Ongoing(percentage: Int) extends WeightComputationStatusInfo {
    def ended: Boolean = false
  }
  case object Waiting extends WeightComputationStatusInfo {
    def percentage: Int = 0
    def ended: Boolean  = false
  }
}
