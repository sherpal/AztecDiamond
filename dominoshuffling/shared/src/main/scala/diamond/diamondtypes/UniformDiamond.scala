package diamond.diamondtypes

import custommath.QRoot
import diamond.DiamondType.isInteger
import diamond.{Diamond, DiamondType, UniformWeightGeneration, UniformWeightPartition}
import exceptions.WrongParameterException
import geometry.{Domino, Point}

case object UniformDiamond extends DiamondType {
  type ArgType = Int *: EmptyTuple

  val lozengeTiling: Boolean = false

  val defaultRotation: Int = 0

  def diamondOrder(args: Int *: EmptyTuple): Int = args._1

  def transformArguments(args: Seq[Double]): Either[WrongParameterException, Int *: EmptyTuple] = {
    val order = args(0).toInt
    Either.cond(
      isInteger(args(0)) && order >= 1,
      order *: EmptyTuple,
      new WrongParameterException(s"Order of diamond must be a positive integer (received: ${args(0)}).")
    )
  }

  def transformArgumentsBack(arg: ArgType): Seq[Double] = List(arg._1.toDouble)

  def makeGenerationWeight(args: Int *: EmptyTuple): UniformWeightGeneration = new UniformWeightGeneration(args._1)

  def makeComputationWeight(args: Int *: EmptyTuple): UniformWeightPartition = new UniformWeightPartition(args._1)

  def countingTilingDiamond(args: Int *: EmptyTuple): Diamond = Diamond.fullHorizontalDiamond(args._1)

  def totalPartitionFunctionToSubGraph(args: Int *: EmptyTuple, totalPartition: QRoot): QRoot = totalPartition

  def isPointInDiamond(args: Int *: EmptyTuple): Point => Boolean = (_: Point) => true

  def theoreticTilingNumber(order: Int): QRoot = QRoot(BigInt(2) pow (order * (order + 1) / 2), 1)

  val argumentNames: List[DiamondType.ArgumentName] = List(DiamondType.ArgumentName("Diamond Order", 100, 5))
}
