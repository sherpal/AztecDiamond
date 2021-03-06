package dominoshuffingextension

import communication.Communicator
import custommath.QRoot
import diamond.{ComputePartitionFunctionWeight, Diamond}
import messages._

object TilingNumberCounting {

  /**
   * Computes the probability of seeing the domino if generated with the given weights
   */
  def probability(diamond: Diamond, weights: ComputePartitionFunctionWeight): QRoot = {

    val order = weights.n

    val _1 = QRoot(1,1)

    def thisStepProbability(diamond: Diamond, weightMap: ComputePartitionFunctionWeight): QRoot =
      diamond.activeFaces.filter(_.dominoes.count(diamond.contains) == 2).flatMap(face => {
        val (alpha, beta, gamma, delta) = face.getFaceWeights(weightMap)
        if (diamond.contains(face.horizontalDominoes._1)) {
          List(alpha * gamma / (alpha * gamma + beta * delta))
          //alpha * gamma / (alpha * gamma + beta * delta)
        } else {
          List(beta * delta / (alpha * gamma + beta * delta))
          //beta * delta / (alpha * gamma + beta * delta)
        }
      }).toList.product

    /**
     * Every diamond comes with a list of list of coefficients. The outer list reflects the number of such diamonds,
     * and the inner list are the coefficients stored up to that point.
     */
    def probabilityAcc(diamondsAndCoefficients: List[(Diamond, List[List[QRoot]])],
                       weightTrait: ComputePartitionFunctionWeight): QRoot = {

      if (diamondsAndCoefficients.head._1.order == 1) { // all diamonds will be of order 1 at the same time
        diamondsAndCoefficients.map({
          case (d, listOfCoefficients) =>
            val thisStep = thisStepProbability(d, weightTrait)
            listOfCoefficients.map(coefficients => (thisStep +: coefficients).product).sum
        }).sum
      } else {

        val numberOfDiamonds = diamondsAndCoefficients.length.toDouble

        var count = 0

        val newDiamonds =
          diamondsAndCoefficients
            .foldLeft(List[(Diamond, List[List[QRoot]])]())({
              case (diamonds, (d, listOfCoefficients)) =>
                val thisStep = thisStepProbability(d, weightTrait)
                val newCoefficients = listOfCoefficients.map(thisStep +: _)

                Communicator.postMessage(
                  CountingComputationStatusSubroutine(
                    math.round(100 * count / numberOfDiamonds).toInt
                  )
                )

                count += 1
                d.subDiamonds.map((_, newCoefficients)) ++ diamonds
            })

        Communicator.postMessage(
          CountingComputationStatus(
            100 - math.round(weightTrait.n * 100 / order.toDouble).toInt
          )
        )

        probabilityAcc(newDiamonds, weightTrait.subWeights.asInstanceOf[ComputePartitionFunctionWeight])
      }
    }

    probabilityAcc(List((diamond, List(List(_1)))), weights)

  }

}
