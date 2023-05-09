package diamond.diamondtypes

import custommath.QRoot
import diamond.DiamondType.isInteger
import diamond._
import exceptions.WrongParameterException
import geometry.{Domino, Point}

case object AztecHouse extends DiamondType {
  // (width, height)
  type ArgType = (Int, Int)

  val lozengeTiling: Boolean = false

  val defaultRotation: Int = 0

  def diamondOrder(args: (Int, Int)): Int = (args._1 + args._2 - 2 + 1) / 2

  /** Arguments are of the form (n, h), but ArgType is (width, height), so there's an extra transformation here.
    */
  def transformArguments(args: Seq[Double]): Either[WrongParameterException, (Int, Int)] = {
    val n      = args(0).toInt
    val h      = args(1).toInt
    val width  = n * 2
    val height = h + 1
    Either.cond(
      args.forall(isInteger) && width > 0 && height > 0,
      (width, height),
      new WrongParameterException(
        s"For the shape to be tileable, n must be a positive integer, and h must be a non negative integer " +
          s"(received: ($n, $h))."
      )
    )
  }

  def transformArgumentsBack(arg: ArgType): Seq[Double] = List((arg._1 / 2).toDouble, (arg._2 - 1).toDouble)

  def makeGenerationWeight(args: (Int, Int)): CustomGenerationWeight =
    WeightTrait.aztecHouseWeightsGeneration(args._1, args._2)

  def makeComputationWeight(args: (Int, Int)): CustomComputePartitionFunctionWeight =
    WeightTrait.aztecHouseWeightsPartition(args._1, args._2)

  def countingTilingDiamond(args: (Int, Int)): Diamond = Diamond.handCraftedAztecHouse(args._1, args._2)

  def totalPartitionFunctionToSubGraph(args: (Int, Int), totalPartition: QRoot): QRoot = totalPartition

  def isPointInDiamond(args: (Int, Int)): Point => Boolean =
    (point: Point) => WeightTrait.isInAztecHouse(point, args._1, args._2)

  val argumentNames: List[DiamondType.ArgumentName] =
    List(DiamondType.ArgumentName("Aztec n", 30, 5), DiamondType.ArgumentName("Aztec h", 30, 5))
}
