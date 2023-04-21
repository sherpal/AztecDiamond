package diamond.diamondtypes

import custommath.QRoot
import diamond.{CustomComputePartitionFunctionWeight, CustomGenerationWeight, Diamond, DiamondType}
import geometry.{Domino, Face, Point, WestGoing}

case object Trapezoidal extends DiamondType {

  private def alphaFunction(x: Double): Double = x match {
    case _ if x < 1.0 / 5 => 2 * x
    case _ if x < 3.0 / 5 => x + 1.0 / 5
    case _ if x < 4.0 / 5 => 2 * x - 2.0 / 5
    case _ => 2 * x
  }

  private val n: Int = 200

  private val alphaValues: Vector[Int] =
    (1 to n).map(j => n * alphaFunction(j.toDouble / n)).map(math.round).map(_.toInt).toVector

//  private val alphaValues: Vector[Int] = Vector(
//    1, 3, 6, 10, 12, 14
//  )

  println(alphaValues.toSet.size, n)
  println(alphaValues.mkString(", "))

  private val a_n: Int = alphaValues.last

  private val (a, b, c) = (n, a_n - n, n)

  private val order: Int = Hexagon.diamondOrder(a, b, c)

  private val startLeftX: Int = n - a_n + 1

  type ArgType = Unit

  val lozengeTiling: Boolean = true

  val defaultRotation: Int = 0

  def diamondOrder(args: Unit): Int = Hexagon.diamondOrder((a, b, c))

  def transformArguments(args: Vector[Double]): Unit = {}

  private def isInHexagon(point: Point): Boolean = {
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

  private def isWeightOneDomino(domino: Domino): Boolean =
    (isInHexagon(domino.p1) && isInHexagon(domino.p2) &&
      domino.dominoType(order) != WestGoing) ||
      (!isInHexagon(domino.p1) && !isInHexagon(domino.p2))

  def makeGenerationWeight(args: Unit): CustomGenerationWeight = {
    val weights = new CustomGenerationWeight(order)

    // putting zero weights at the Hexagon boundary
    Face.activeFaces(order)
      .flatMap(_.dominoes)
      .foreach(domino => weights(domino) = if (isWeightOneDomino(domino)) 1.0 else 0.0)


    // putting zero weights at the middle of the Hexagon
    (startLeftX to order by 2).map(j => Domino(Point(j, 0), Point(j, 1)))
        .foreach(weights(_) = 0.0)

    // changing weights around alpha values
    alphaValues.foreach(j => {
      val verticalDominoX = startLeftX + 2 * j - 2
      weights(Domino(Point(verticalDominoX, 0), Point(verticalDominoX, 1))) = 1.0
      if (j > 1) {
        weights(Domino(Point(verticalDominoX - 1, 0), Point(verticalDominoX, 0))) = 0.0
        weights(Domino(Point(verticalDominoX - 1, 1), Point(verticalDominoX, 1))) = 0.0
      }
      if (j < a_n) {
        weights(Domino(Point(verticalDominoX, 0), Point(verticalDominoX + 1, 0))) = 0.0
        weights(Domino(Point(verticalDominoX, 1), Point(verticalDominoX + 1, 1))) = 0.0
      }
    })


    weights
  }

  def makeComputationWeight(args: Unit): CustomComputePartitionFunctionWeight = {

    val weights = new CustomComputePartitionFunctionWeight(order)

    val _1 = QRoot(1, 1)
    val _0 = QRoot(0, 1)

    Face.activeFaces(order)
      .flatMap(_.dominoes)
      .foreach(domino => weights(domino) = if (isWeightOneDomino(domino)) _1 else _0)

    // putting zero weights at the middle of the Hexagon
    (startLeftX to order by 2).map(j => Domino(Point(j, 0), Point(j, 1)))
      .foreach(weights(_) = _0)

    // changing weights around alpha values
    alphaValues.foreach(a_j => {
      val verticalDominoX = startLeftX + 2 * a_j - 2
      weights(Domino(Point(verticalDominoX, 0), Point(verticalDominoX, 1))) = _1
      if (a_j > 1) {
        weights(Domino(Point(verticalDominoX - 1, 0), Point(verticalDominoX, 0))) = _0
        weights(Domino(Point(verticalDominoX - 1, 1), Point(verticalDominoX, 1))) = _0
      }
      if (a_j < a_n) {
        weights(Domino(Point(verticalDominoX, 0), Point(verticalDominoX + 1, 0))) = _0
        weights(Domino(Point(verticalDominoX, 1), Point(verticalDominoX + 1, 1))) = _0
      }
    })

    weights
  }

  def isPointInDiamond(args: Unit): Point => Boolean =
    (point: Point) => Hexagon.isPointInDiamond((a, b, c))(point) && point.y > 0 && (
     point.y > 1 || (point.x + 2 - startLeftX) % 2 == 1 || !alphaValues.contains((point.x + 2 - startLeftX) / 2))

  def countingTilingDiamond(args: Unit): Diamond = ???

  def totalPartitionFunctionToSubGraph(args: Unit, totalPartition: QRoot): QRoot = ???

  val argumentNames: List[(String, Double, Double)] = Nil

}
