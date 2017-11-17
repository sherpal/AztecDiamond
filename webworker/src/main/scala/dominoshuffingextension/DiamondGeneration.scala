package dominoshuffingextension

import communication.Communicator
import diamond.{Diamond, GenerationWeight}
import messages._

object DiamondGeneration {

  private def totalNumberOfWeights(order: Int): Double = {
    4 * (order.toDouble * order * order / 3 + order.toDouble * order / 2 + order.toDouble / 6)
  }

  /**
   * Determines the maximum number of weights that we allow to keep in memory.
   */
  private val weightNumberThreshold: Int = (1 to 100).map(j => 4 * j * j).sum

  def computeWeightSliceFrom(order: Int, baseWeight: GenerationWeight): List[GenerationWeight] ={
    val totalNumber = totalNumberOfWeights(baseWeight.n)

    val maxWeightOrder = (order to baseWeight.n)
      .find(j => (order to j).map(n => 4 * n * n).sum > weightNumberThreshold) match {
      case Some(n) => math.max(n - 1, order)
      case None => baseWeight.n
    }

    val maxWeight = computeWeightOfOrder(maxWeightOrder, baseWeight)

    def accumulator(computed: List[GenerationWeight]): List[GenerationWeight] = {
      if (computed.head.n == order) computed
      else {
        Communicator.postMessage(
          WeightComputationStatus(
            math.round((computed.head.n to baseWeight.n).map(n => 4 * n * n).sum / totalNumber * 100).toInt
          )
        )

        accumulator(computed.head.subWeights.asInstanceOf[GenerationWeight] +: computed)
      }
    }

    accumulator(List(maxWeight))
  }

  def computeWeightOfOrder(order: Int, baseWeight: GenerationWeight): GenerationWeight = {
    val totalNumber = totalNumberOfWeights(baseWeight.n)

    def accumulator(weight: GenerationWeight): GenerationWeight = {
      if (weight.n == order) weight
      else {
        Communicator.postMessage(
          WeightComputationStatus(
            math.round((weight.n to baseWeight.n).map(n => 4 * n * n).sum / totalNumber * 100).toInt
          )
        )
        accumulator(weight.subWeights.asInstanceOf[GenerationWeight])
      }
    }

    accumulator(baseWeight)
  }

  def generateDiamondMemoryOptimized(baseWeight: GenerationWeight): Diamond = {
    val totalComputations: Double = (math.pow(baseWeight.n, 2).toInt + baseWeight.n) / 2

    def accumulator(diamond: Diamond, weights: List[GenerationWeight]): Diamond =
      if (diamond.order == baseWeight.n) diamond
      else {
        val order = diamond.order + 1
        val w = if (weights.nonEmpty) weights else {
          computeWeightSliceFrom(order, baseWeight)
        }
        Communicator.postMessage(
          DiamondComputationStatus(
            math.round((order * order + order) / 2 / totalComputations * 100).toInt
          )
        )

        accumulator(w.head.generateDiamond(diamond), w.tail)
      }

    val firstWeights = computeWeightSliceFrom(1, baseWeight)
    accumulator(firstWeights.head.generateOrderOneDiamond, firstWeights.tail)
  }

  def computeAllWeights(weight: GenerationWeight): List[GenerationWeight] = {
    val order = weight.n
    val totalNumber = totalNumberOfWeights(order)

    def computeAllWeightsAcc(computed: List[GenerationWeight]): List[GenerationWeight] = {
      if (computed.head.n == 1)
        computed
      else {

        Communicator.postMessage(
          WeightComputationStatus(
            math.round(computed.map(_.n.toDouble).map(n => 4 * n * n).sum / totalNumber * 100).toInt
          )
        )

        computeAllWeightsAcc(computed.head.subWeights.asInstanceOf[GenerationWeight] +: computed)
      }
    }

    computeAllWeightsAcc(List(weight))
  }

  def generateDiamond(weights: List[GenerationWeight]): Diamond = {
    // in order to go from diamond of order n to n+1, there is a bit more than n Bernoulli's to generate.
    val totalComputations: Double = (math.pow(weights.last.n, 2).toInt + weights.last.n) / 2
    weights.zip(1 to weights.last.n).tail.foldLeft(weights.head.generateOrderOneDiamond)({
      case (diamond, (weight, j)) =>

        Communicator.postMessage(
          DiamondComputationStatus(
            math.round((j * j + j) / 2 / totalComputations * 100).toInt
          )
        )

        weight.generateDiamond(diamond)
    })
  }

}
