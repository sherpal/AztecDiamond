package diamond.diamondtypes

import custommath.QRoot
import diamond.DiamondType.isInteger
import diamond._
import exceptions.WrongParameterException
import geometry.{Domino, Point}

case object AztecRing extends DiamondType {
  type ArgType = (Int, Int)

  val lozengeTiling: Boolean = false

  val defaultRotation: Int = 0

  def diamondOrder(args: (Int, Int)): Int = args._2

  def transformArguments(args: Vector[Double]): (Int, Int) = {
    val inner = args(0).toInt
    val outer = args(1).toInt
    if (args.forall(isInteger) && inner > 0 && outer >= inner) {
      if ((outer - inner) % 2 == 0 || outer >= 3 * inner - 1)
        (inner, outer)
      else
        throw new WrongParameterException(
          s"For the shape to be tileable, Inner and outer orders must satisfy (outerOrder - innerOrder) mod 2 == 0" +
            s" or outerOrder >= 3 * innerOrder - 1."
        )
    } else {
      throw new WrongParameterException(
        s"For the shape to be tileable, Inner and outer orders must be positive integers satisfying inner <= outer " +
          s"(received: (${args(0)}, ${args(1)}))."
      )
    }
  }

  def makeGenerationWeight(args: (Int, Int)): CustomGenerationWeight =
    WeightTrait.diamondRingGeneration(args._1, args._2)

  def makeComputationWeight(args: (Int, Int)): CustomComputePartitionFunctionWeight =
    WeightTrait.diamondRingPartition(args._1, args._2)

  def countingTilingDiamond(args: (Int, Int)): Diamond = if ((args._2 - args._1) % 2 == 0)
    Diamond.fullHorizontalDiamond(args._2)
  else {
    val diamondConstruction: DiamondConstruction = new DiamondConstruction(args._2)
    diamondConstruction.insertDiamond(Diamond.fullHorizontalDiamond(args._1))

    /** Filling outer layers to go back to the outer = 3 * inner - 1 case */
    def fillLayer(diamondLayerOrder: Int): Unit = {
      (for {
        y <- 1 to diamondLayerOrder
        x <- List(-diamondLayerOrder + y, diamondLayerOrder - y)
      } yield List(Point(x, y), Point(x, -y + 1)))
        .flatten
        .map(p => Domino(p, p + Point(1, 0)))
        .foreach(diamondConstruction() = _)
    }

    for (layer <- 3 * args._1 + 1 to args._2 by 2) fillLayer(layer)

    List(Point(args._1, args._1), Point(args._1, -args._1), Point(-args._1, args._1), Point(-args._1, -args._1))
      .map((Diamond.fullHorizontalDiamond(args._1 - 1), _))
      .foreach({ case (diamond, center) => diamondConstruction.insertDiamond(diamond, center) })


    diamondConstruction.fillForcedDominoes()
    diamondConstruction.toDiamond
  }

  def totalPartitionFunctionToSubGraph(args: (Int, Int), totalPartition: QRoot): QRoot =
    totalPartition / UniformDiamond.theoreticTilingNumber(args._1)

  def isInDiamond(args: (Int, Int)): Domino => Boolean =
    (domino: Domino) => WeightTrait.isInDiamondRing(domino.p1, args._1, args._2)

  val argumentNames: List[(String, Double, Double)] = List(("Inner order", 20, 3), ("Outer order", 40, 8))
}
