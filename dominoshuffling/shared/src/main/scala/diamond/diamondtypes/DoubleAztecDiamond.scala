package diamond.diamondtypes

import custommath.QRoot
import diamond.DiamondType.isInteger
import diamond._
import exceptions.WrongParameterException
import geometry.Point

case object DoubleAztecDiamond extends DiamondType {
  type ArgType = (Int, Int)

  val lozengeTiling: Boolean = false

  val defaultRotation: Int = 0

  def diamondOrder(args: (Int, Int)): Int = 4 * args._1 - 2 * args._2 + 1

  def transformArguments(args: Seq[Double]): Either[WrongParameterException, (Int, Int)] = {
    val order   = args(0).toInt
    val overlap = args(1).toInt
    Either.cond(
      args.forall(isInteger) && order > 0 && overlap > 0 && overlap < order,
      (order, overlap),
      new WrongParameterException(
        s"For the shape to be tileable, Order and overlap must be positive integers, and overlap < order. " +
          s"Received: " +
          s"(${args(0)}, ${args(1)}))."
      )
    )
  }

  def transformArgumentsBack(arg: ArgType): Seq[Double] = List(arg._1.toDouble, arg._2.toDouble)

  def makeGenerationWeight(args: ArgType): CustomGenerationWeight =
    WeightTrait.doubleAztecDiamondGeneration(args._1, args._2)

  def makeComputationWeight(args: ArgType): CustomComputePartitionFunctionWeight =
    WeightTrait.doubleAztecDiamondPartition(args._1, args._2)

  def countingTilingDiamond(args: (Int, Int)): Diamond = ???

  def totalPartitionFunctionToSubGraph(args: (Int, Int), totalPartition: QRoot): QRoot = ???

  def isPointInDiamond(args: (Int, Int)): Point => Boolean =
    (point: Point) => WeightTrait.isInRightDoubleAztec(point, args._1, args._1 - args._2)

  val argumentNames: List[DiamondType.ArgumentName] =
    List(DiamondType.ArgumentName("Order of diamonds", 50, 3), DiamondType.ArgumentName("Overlap", 10, 1))
}
