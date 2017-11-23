package diamond.diamondtypes

import custommath.QRoot
import diamond.DiamondType.isInteger
import diamond._
import exceptions.WrongParameterException
import geometry.Domino

case object Hexagon extends DiamondType {
  type ArgType = (Int, Int, Int)

  val lozengeTiling: Boolean = true

  val defaultRotation: Int = 30

  def diamondOrder(args: (Int, Int, Int)): Int = 2 * args._1 - 1 + args._2

  def transformArguments(args: Vector[Double]): (Int, Int, Int) = {
    val a = args(0).toInt
    val b = args(1).toInt
    val c = args(2).toInt
    if (args.forall(isInteger) && a > 0 && b > 0 && c > 0) {
      if (c > b || c > a || b > a) {
        throw new WrongParameterException(
          s"For implementation reasons, the three sides must be ordered via first >= second >= third (" +
            s"received: $a, $b, $c)."
        )
      }
      (a, b, c)
    } else {
      throw new WrongParameterException(
        s"For the shape to be tileable, Length of the sides of the hexagon must be positive integers."
      )
    }
  }

  def makeGenerationWeight(args: (Int, Int, Int)): CustomGenerationWeight =
    WeightTrait.hexagonWeightGeneration(args._1, args._2, args._3)

  def makeComputationWeight(args: (Int, Int, Int)): CustomComputePartitionFunctionWeight =
    WeightTrait.hexagonWeightPartition(args._1, args._2, args._3)

  def countingTilingDiamond(args: (Int, Int, Int)): Diamond = ???

  def totalPartitionFunctionToSubGraph(args: (Int, Int, Int), totalPartition: QRoot): QRoot = ???

  def isInDiamond(args: (Int, Int, Int)): Domino => Boolean =
    (domino: Domino) => WeightTrait.isInHexagon(domino.p1, args._1, args._2, args._3)

  val argumentNames: List[(String, Double, Double)] =
    List(("First side size", 15, 3), ("Second side size", 15, 3), ("Third side size", 15, 3))
}
