package diamond.diamondtypes

import custommath.QRoot
import diamond.DiamondType.isInteger
import diamond._
import exceptions.WrongParameterException
import geometry._

case object Hexagon extends DiamondType {
  type ArgType = (Int, Int, Int)

  val lozengeTiling: Boolean = true

  val defaultRotation: Int = 30

  def diamondOrder(args: (Int, Int, Int)): Int = args._1 + args._2 + args._3 - 1

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

  private def isInHexagon(point: Point, a: Int, b: Int, c: Int): Boolean = {
    val order = diamondOrder(a, b, c)

    def xMin(y: Int): Int = {
      val j = y + c - 1
      if (j < a) order - c + 1 - 2 * b - j else order - a - c + 2 - 2 * b + (j - a)
    }

    def xMax(y: Int): Int = {
      val j = y + c - 1
      if (j < c) order - c + 1 + j else order - (j - c)
    }

    point.y >= -c + 1 && point.y <= a &&
      point.x >= xMin(point.y) && point.x <= xMax(point.y)
  }

  private def isWeightOneDomino(domino: Domino, a: Int, b: Int, c: Int, order: Int): Boolean =
    (isInHexagon(domino.p1, a, b, c) && isInHexagon(domino.p2, a, b, c) &&
      domino.dominoType(order) != WestGoing) ||
      (!isInHexagon(domino.p1, a, b, c) && !isInHexagon(domino.p2, a, b, c))


  /**
   * Computes the WeightTrait for generating random lozenge tilings of the hexagon.
   *
   * We can generate a hexagon from an Aztec Diamond by forbidding the [[WestGoing]] dominoes inside the sub graph of
   * the hexagon. For example, the sub graph of a hexagon with each side 1 is
   *   ._._.
   *   |   |
   *   ._._.
   * The sub graph for a hexagon with each side 2 is
   *     ._._.
   *     |   |
   *   ._._._._.
   *   |   |   |
   *   ._._._._.
   *     |   |
   *     ._._.
   * (each weight on the existing edges is then 1.0)
   *
   * The embedding is done simply by putting the sub graph at the right of the Aztec Diamond. Then, you wan show that
   * if embedded in a diamond of order 2 * a - 1 + b, both the hexagon and its complement are tileable.
   *
   * @param args ._1: size of the top right and bottom left sides of the hexagon
   *             ._2: size of the top and bottom sides of the hexagon (those parallel to the horizontal axis)
   *             ._3: size of the top left and bottom right sides of the hexagon
   * @return  the CustomGenerationWeight needed to generate a random tiling.
   */
  def makeGenerationWeight(args: (Int, Int, Int)): CustomGenerationWeight = {
    val (a, b, c) = args
    val order = diamondOrder(a, b, c)

    val weights = new CustomGenerationWeight(order)

    Face.activeFaces(order)
      .flatMap(_.dominoes)
      .foreach(domino => weights(domino) = if (isWeightOneDomino(domino, a, b, c, order)) 1.0 else 0.0)

    weights
  }

  def makeComputationWeight(args: (Int, Int, Int)): CustomComputePartitionFunctionWeight = {
    val (a, b, c) = args
    val order = diamondOrder(a, b, c)

    val weights = new CustomComputePartitionFunctionWeight(order)

    val _1 = QRoot(1, 1)
    val _0 = QRoot(0, 1)

    Face.activeFaces(order)
      .flatMap(_.dominoes)
      .foreach(domino => weights(domino) = if (isWeightOneDomino(domino, a, b, c, order)) _1 else _0)

    weights
  }

  def countingTilingDiamond(args: (Int, Int, Int)): Diamond = {
    val (a, b, c) = args
    val order = diamondOrder(a, b, c)

    println(order)

    val diamondConstruction = new DiamondConstruction(order)

    /** First filling vertical dominoes at the left part of the Hexagon, as well as the mirror or these vertical
     * dominoes outside of the Hexagon. */
    val insideStartingPoint = Point(order, 0) + Point(-(c - 1), -(c - 1)) + Point(-2 * b, 0) + Point(-(a - 1), a - 1)

    (for {
      southEast <- 0 until a
      northEast <- 0 until c
    } yield Point(southEast + northEast, -southEast + northEast) + insideStartingPoint)
      .map(point => Domino(point, point + Point(0, 1)))
      .foreach(diamondConstruction.update)

    val outsideStartingPoint = insideStartingPoint + Point(-1, 0)
    (for {
      southWest <- 0 until a
      northWest <- 0 until c
    } yield Point(-southWest -northWest, -southWest + northWest) + outsideStartingPoint)
      .map(point => Domino(point, point + Point(0, 1)))
      .foreach(diamondConstruction.update)

    /**
     * Next, we fill two full horiztonal diamonds above and below the vertical dominoes we just filled
     */
    diamondConstruction.insertDiamond(
      Diamond.fullHorizontalDiamond(a - 1),
      Point(insideStartingPoint.x - 1, -c)
    )
    diamondConstruction.insertDiamond(
      Diamond.fullHorizontalDiamond(c - 1),
      Point(insideStartingPoint.x - 1, a)
    )

    Face.activeFaces(order)
      .map(_.horizontalDominoes)
      .flatMap(elem => List(elem._1, elem._2))
      .filter(domino => (domino.p1.y > 0) == (domino.dominoType(order) == NorthGoing))
      .filterNot(
        domino => diamondConstruction.isPointOccupied(domino.p1) || diamondConstruction.isPointOccupied(domino.p2)
      )
      .foreach(diamondConstruction.update)

    diamondConstruction.toDiamond
  }

  def totalPartitionFunctionToSubGraph(args: (Int, Int, Int), totalPartition: QRoot): QRoot = {
    val (a, _, c) = args

    totalPartition / UniformDiamond.theoreticTilingNumber(a - 1) / UniformDiamond.theoreticTilingNumber(c - 1)
  }

  def isPointInDiamond(args: (Int, Int, Int)): Point => Boolean =
    (point: Point) => isInHexagon(point, args._1, args._2, args._3)

  val argumentNames: List[(String, Double, Double)] =
    List(("First side size", 15, 3), ("Second side size", 15, 3), ("Third side size", 15, 3))
}
