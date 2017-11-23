package diamond.diamondtypes

import custommath.QRoot
import diamond.DiamondType.isInteger
import diamond.{Diamond, DiamondType, UniformWeightGeneration, UniformWeightPartition}
import exceptions.WrongParameterException
import geometry.Domino

case object UniformDiamond extends DiamondType {
  type ArgType = Int

  val lozengeTiling: Boolean = false

  val defaultRotation: Int = 0

  def diamondOrder(args: Int): Int = args

  def transformArguments(args: Vector[Double]): Int = {
    val order = args(0).toInt
    if (isInteger(args(0)) && order >= 1) {
      order
    } else {
      throw new WrongParameterException(s"Order of diamond must be a positive integer (received: ${args(0)}).")
    }
  }

  def makeGenerationWeight(args: Int): UniformWeightGeneration = new UniformWeightGeneration(args)

  def makeComputationWeight(args: Int): UniformWeightPartition = new UniformWeightPartition(args)

  def countingTilingDiamond(args: Int): Diamond = Diamond.fullHorizontalDiamond(args)

  def totalPartitionFunctionToSubGraph(args: Int, totalPartition: QRoot): QRoot = totalPartition

  def isInDiamond(args: Int): Domino => Boolean = (_: Domino) => true

  def theoreticTilingNumber(order: Int): QRoot = QRoot(BigInt(2) pow (order * (order + 1) / 2), 1)

  val argumentNames: List[(String, Double, Double)] = List(("Diamond Order", 100, 5))
}
