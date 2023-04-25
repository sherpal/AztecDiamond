package diamond.diamondtypes

import custommath.QRoot
import diamond.DiamondType.isInteger
import diamond._
import exceptions.WrongParameterException
import geometry.{Face, Point}

case object TwoPeriodicRectangle extends DiamondType {

  type ArgType = (Int, Int, Double, Double)

  val lozengeTiling: Boolean = false

  val defaultRotation: Int = 0

  override val designedForPartitionFunction: Boolean = true

  def diamondOrder(args: (Int, Int, Double, Double)): Int = WeightTrait.rectangleOrder(args._1, args._2)

  def transformArguments(args: Seq[Double]): Either[WrongParameterException, (Int, Int, Double, Double)] = {
    val width  = args(0).toInt
    val height = args(1).toInt
    val a      = args(2)
    val b      = args(3)
    Either.cond(
      args.take(2).forall(isInteger) && width > 0 && height > 0 && width * height % 2 == 0 && a > 0 && b > 0,
      (width, height, a, b),
      new WrongParameterException(
        s"For the shape to be tileable, Width and Height must be positive integers, their product must be even " +
          s"and a and b must be positive real numbers " +
          s"(received: (${args(0)}, ${args(1)}, $a, $b))."
      )
    )
  }

  def transformArgumentsBack(arg: ArgType): Seq[Double] = List(arg._1.toDouble, arg._2.toDouble, arg._3, arg._4)

  def makeGenerationWeight(args: (Int, Int, Double, Double)): CustomGenerationWeight = {
    val order = diamondOrder(args)

    val a = args._3
    val b = args._4

    val weight = new CustomGenerationWeight(order)

    val pointsInDiamonds = isPointInDiamond(args)

    Face.activeFaces(order).foreach { face =>
      val aFace: Boolean = (face.bottomLeft.y + order) % 2 == 1
      face.dominoes.foreach { domino =>
        if (pointsInDiamonds(domino.p1) != pointsInDiamonds(domino.p2)) {
          weight(domino) = 0
        } else if (pointsInDiamonds(domino.p1)) {
          weight(domino) = if (aFace) a else b
        } else {
          weight(domino) = 1
        }
      }
    }

    weight
  }

  def makeComputationWeight(args: (Int, Int, Double, Double)): CustomComputePartitionFunctionWeight = {
    val order = diamondOrder(args)

    val a = QRoot.fromRationalDouble(args._3)
    val b = QRoot.fromRationalDouble(args._4)

    val weight = new CustomComputePartitionFunctionWeight(order)

    val pointsInDiamonds = isPointInDiamond(args)

    Face.activeFaces(order).foreach { face =>
      val aFace: Boolean = (face.bottomLeft.y + order) % 2 == 1
      face.dominoes.foreach { domino =>
        if (pointsInDiamonds(domino.p1) != pointsInDiamonds(domino.p2)) {
          weight(domino) = 0
        } else if (pointsInDiamonds(domino.p1)) {
          weight(domino) = if (aFace) a else b
        } else {
          weight(domino) = 1
        }
      }
    }

    weight
  }

  def countingTilingDiamond(args: (Int, Int, Double, Double)): Diamond =
    Rectangle.countingTilingDiamond((args._1, args._2))

  def isPointInDiamond(args: (Int, Int, Double, Double)): Point => Boolean =
    (point: Point) => WeightTrait.isInRectangle(point, args._1, args._2)

  def totalPartitionFunctionToSubGraph(args: (Int, Int, Double, Double), totalPartition: QRoot): QRoot = totalPartition

  val argumentNames: List[DiamondType.ArgumentName] = List(
    DiamondType.ArgumentName("Width", 40, 4),
    DiamondType.ArgumentName("Height", 30, 3),
    DiamondType.ArgumentName("Weight a", 0.5, 0.5),
    DiamondType.ArgumentName("Weight b", 1, 1)
  )

}
