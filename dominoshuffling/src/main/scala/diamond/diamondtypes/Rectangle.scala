package diamond.diamondtypes

import custommath.QRoot
import diamond.DiamondType.isInteger
import diamond._
import exceptions.WrongParameterException
import geometry.{Domino, Point}

case object Rectangle extends DiamondType {
  type ArgType = (Int, Int)

  val lozengeTiling: Boolean = false

  val defaultRotation: Int = 0

  def diamondOrder(args: (Int, Int)): Int = WeightTrait.rectangleOrder(args._1, args._2)

  def rectangleTilingNumber(m: Int, n: Int): Long = math.round((for {
    j <- 1 to m/2
    k <- 1 to n/2
  } yield 4 * (math.pow(math.cos(math.Pi * j / (m+1)), 2) + math.pow(math.cos(math.Pi * k / (n+1)), 2))).product)

  def transformArguments(args: Vector[Double]): (Int, Int) = {
    val width = args(0).toInt
    val height = args(1).toInt
    if (args.forall(isInteger) && width > 0 && height > 0 && width * height % 2 == 0) {
      (width, height)
    } else {
      throw new WrongParameterException(
        s"For the shape to be tileable, Width and Height must be positive integers, and their product must be even " +
          s"(received: (${args(0)}, ${args(1)}))."
      )
    }
  }

  def makeGenerationWeight(args: (Int, Int)): CustomGenerationWeight =
    WeightTrait.rectangleWeightsGeneration(args._1, args._2)

  def makeComputationWeight(args: (Int, Int)): CustomComputePartitionFunctionWeight =
    WeightTrait.rectangleWeightsPartition(args._1, args._2)

  def countingTilingDiamond(args: (Int, Int)): Diamond = {
    val width = args._1
    val height = args._2
    val order = WeightTrait.rectangleOrder(width, height)
    val diamondConstruction = new DiamondConstruction(order)

    def fillRectangle(w: Int, h: Int, horizontal: Boolean, center: Point = Point(0, 0)): Unit = {
      if (w > 0 && h > 0) {
        if (horizontal) {
          if (w == 2) {
            (-h / 2 + 1 to h / 2)
              .map(Point(0, _))
              .map(_ + center)
              .map(p => Domino(p, p + Point(1, 0)))
              .foreach(diamondConstruction() = _)
          } else {
            (-w / 2 + 1 until w / 2 by 2)
              .flatMap(j => List(Point(j, h / 2), Point(j, -h / 2 + 1)))
              .map(_ + center)
              .map(p => Domino(p, p + Point(1, 0)))
              .foreach(diamondConstruction() = _)

            (-h / 2 + 2 until h / 2 - 1 by 2)
              .flatMap(j => List(Point(w / 2, j), Point(-w / 2 + 1, j)))
              .map(_ + center)
              .map(p => Domino(p, p + Point(0, 1)))
              .foreach(diamondConstruction() = _)
          }
        } else {
          if (h == 2) {
            (-w / 2 + 1 to w / 2)
              .map(Point(_, 0))
              .map(_ + center)
              .map(p => Domino(p, p + Point(0, 1)))
              .foreach(diamondConstruction() = _)
          } else {
            (-w / 2 + 2 until w / 2 - 1 by 2)
              .flatMap(j => List(Point(j, h / 2), Point(j, -h / 2 + 1)))
              .map(_ + center)
              .map(p => Domino(p, p + Point(1, 0)))
              .foreach(diamondConstruction() = _)

            (-h / 2 + 1 until h / 2 by 2)
              .flatMap(j => List(Point(w / 2, j), Point(-w / 2 + 1, j)))
              .map(_ + center)
              .map(p => Domino(p, p + Point(0, 1)))
              .foreach(diamondConstruction() = _)
          }

        }

        fillRectangle(w - 2, h - 2, horizontal, center)
      }
    }

    fillRectangle(width + width % 2, height + height % 2, height % 2 == 1 || width % 2 == 0)
    diamondConstruction.fillForcedDominoes()

    diamondConstruction.toDiamond
  }

  def totalPartitionFunctionToSubGraph(args: (Int, Int), totalPartition: QRoot): QRoot = totalPartition

  def isInDiamond(args: (Int, Int)): Domino => Boolean =
    (domino: Domino) => WeightTrait.isInRectangle(domino.p1, args._1, args._2)

  val argumentNames: List[(String, Double, Double)] = List(("Width", 40, 4), ("Height", 30, 3))
}
