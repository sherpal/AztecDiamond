package diamond.diamondtypes

import custommath.QRoot
import diamond.DiamondType.isInteger
import diamond._
import exceptions.WrongParameterException
import geometry.Domino

case object TwoPeriodic extends DiamondType {
  type ArgType = (Int, Double, Double)

  val lozengeTiling: Boolean = false

  val defaultRotation: Int = 0

  override val designedForPartitionFunction: Boolean = true

  def diamondOrder(args: (Int, Double, Double)): Int = args._1

  def transformArguments(args: Vector[Double]): (Int, Double, Double) = {
    val order = args(0).toInt
    val a = args(1)
    val b = args(2)
    if (isInteger(args(0)) && order > 0 && a > 0 && b > 0) {
      (order, a, b)
    } else {
      throw new WrongParameterException(
        s"For the shape to be tileable, Order must be a positive integer, and a and b must be positive real " +
          s"numbers. Received: " +
          s"(${args(0)}, ${args(1)}, ${args(2)})."
      )
    }
  }

  def makeGenerationWeight(args: ArgType): CustomGenerationWeight =
    WeightTrait.twoPeriodicAztecDiamondGeneration(args._2, args._3, args._1)

  def makeComputationWeight(args: ArgType): CustomComputePartitionFunctionWeight = {
    val a = QRoot.fromRationalDouble(args._2)
    val b = QRoot.fromRationalDouble(args._3)
    WeightTrait.twoPeriodicAztecDiamondPartition(a, b, args._1)
  }

  def countingTilingDiamond(args: (Int, Double, Double)): Diamond = Diamond.fullHorizontalDiamond(args._1)

  def isInDiamond(args: (Int, Double, Double)): Domino => Boolean = (_: Domino) => true

  def totalPartitionFunctionToSubGraph(args: (Int, Double, Double), totalPartition: QRoot): QRoot = totalPartition

  val argumentNames: List[(String, Double, Double)] =
    List(("Diamond Order", 100, 5), ("Weight a", 0.5, 0.5), ("Weight b", 1, 1))
}
