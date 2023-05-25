package diamond

import custommath.{QRoot, WeightLikeNumber}
import exceptions.NotTileableException
import geometry.{Domino, Face, Point, WestGoing}

import scala.annotation.tailrec
import scala.collection.immutable.TreeSet

/** Describe the behaviour of weights.
  *
  * Given a WeightTrait, the way of generating a Diamond is always the same, and follows the algorithm described in [1].
  * The way the sub weights are computed, the way you recover the weight of a domino and the way you change it are
  * different.
  *
  * A WeightType must be a [[WeightLikeNumber]]. Usually, one of the following two types is used
  *   - Double: if you want efficiency when generating diamonds, this is probably the best choice
  *   - QRoot: if you need full precision when computing probabilities of diamonds.
  *
  * The graph of the Aztec Diamond of order 3 is pictured below.
  *
  * ._. <- horizontalWeights(2 * n - 1) (length 1) \| | ._._._. \| | | | ._._._._._. \| | | | | | ._._._._._. \| | | |
  * ._._._. <- horizontalWeights(1) (length 3 = 2 * 1 + 1) \| | ._. <- horizontalWeights(0) (length 1)
  *
  * \ verticalWeights(0) (length 1) \ verticalWeights(1) (length 3)
  *
  * The graph is invariant under pi/2 rotations, that's why we register horizontal and vertical weights in a dual way.
  * The way we store the weights is just an implementation detail, all we need is that the apply and update methods are
  * consistent with each other.
  *
  * It is also mutable for the purpose of the algorithm.
  *
  * As always, n is the order of the diamond.
  */
trait WeightTrait[WeightType] {

  // protected implicit val num: WeightLikeNumber[WeightType]

  /** Returns the weight associated to the domino.
    */
  def apply(domino: Domino): WeightType

  /** Updates the weight at the domino position
    */
  def update(domino: Domino, weight: WeightType): Unit

  /** Returns the WeightTrait for the sub diamond. The contract is that if a diamond is generated according to the
    * probability distribution induced by the subWeights, then applying the algorithm gives a diamond generated
    * according to this weight.
    */
  def subWeights: WeightTrait[WeightType]

  val n: Int

  def order: Int = n

  /** Returns whether the point is within the graph of a diamond of order n.
    */
  def inBoundsPoint(point: Point): Boolean =
    math.abs(point.x - 0.5) + math.abs(point.y - 0.5) <= n

  /** Returns whether the domino recovers an edge of a diamond of order n.
    */
  def inBoundsDomino(domino: Domino): Boolean =
    inBoundsPoint(domino.p1) && inBoundsPoint(domino.p2)

}

object WeightTrait {

  /** Returns the weight necessary to generate uniform random tiling of the set of points that satisfy the predicate. As
    * a byproduct, you generate a uniform random tiling of the complement.
    *
    * The way it does that is simply putting weight 1 on all the dominoes, except the ones on the boundary.
    *
    * @param n
    *   The order of the embedding diamond.
    * @param isInSubGraph
    *   A predicate discriminating points inside and outside of the sub graph.
    * @return
    *   A WeightMap necessary to generate a random tiling of your graph.
    */
  def injectSubGraphDouble(
      n: Int,
      isInSubGraph: Point => Boolean
  ): CustomGenerationWeight = {
    val weights = new CustomGenerationWeight(n)

    Face
      .activeFaces(n)
      .foreach { face =>
        face.dominoes.foreach { domino =>
          if (isInSubGraph(domino.p1) == isInSubGraph(domino.p2)) {
            weights(domino) = 1.0
          } else {
            weights(domino) = 0.0
          }
        }
      }
    weights
  }

  def injectSubGraphQRoot(
      n: Int,
      isInSubGraph: (Point) => Boolean
  ): CustomComputePartitionFunctionWeight = {
    val weights = new CustomComputePartitionFunctionWeight(n)

    val _0 = QRoot(0, 1)
    val _1 = QRoot(1, 1)

    Face
      .activeFaces(n)
      .foreach { face =>
        face.dominoes.foreach { domino =>
          // is there XOR operator in Scala?
          if (isInSubGraph(domino.p1) == isInSubGraph(domino.p2)) {
            weights(domino) = _1
          } else {
            weights(domino) = _0
          }
        }
      }
    weights

  }

  /** Returns the weights necessary to generate a uniformly random tiling of the graph containing the points in the
    * given collection.
    *
    * @param n
    *   the order of the embedding diamond.
    * @param pointsInSubGraph
    *   a collection of points in the sub-graph.
    * @return
    *   the weights necessary to generate the graph containing the points.
    */
  def injectSubGraphDouble(
      n: Int,
      pointsInSubGraph: Iterable[Point]
  ): CustomGenerationWeight = {
    val pointsSet = pointsInSubGraph.toSet
    injectSubGraphDouble(n, pointsSet.contains(_: Point))
  }

  def injectSubGraphQRoot(
      n: Int,
      pointsInSubGraph: Iterable[Point]
  ): CustomComputePartitionFunctionWeight = {
    val pointsSet = pointsInSubGraph.toSet
    injectSubGraphQRoot(n, pointsSet.contains(_: Point))
  }

  /** Returns the Weight necessary to generate the sub graph according to the weights defined by the subGraphWeights
    * map.
    *
    * @param n
    *   the size of the embedding diamond
    * @param subGraphWeights
    *   the weights for the dominoes inside the sub graph
    * @return
    *   the WeightTrait that will generate the entire diamond.
    */
  def injectSubGraphDouble(
      n: Int,
      subGraphWeights: Map[Domino, Double]
  ): CustomGenerationWeight = {
    val pointsInGraph: TreeSet[Point] =
      subGraphWeights.keys
        .flatMap(d => Seq(d.p1, d.p2))
        .foldLeft(TreeSet[Point]())(_ + _)

    val isInSubGraph: (Point) => Boolean = pointsInGraph.contains

    val weights = injectSubGraphDouble(n, isInSubGraph)

    Face
      .activeFaces(n)
      .foreach { face =>
        face.dominoes.foreach { domino =>
          subGraphWeights.get(domino) match {
            case Some(w) =>
              weights(domino) = w
            case _ =>
          }
        }
      }

    weights
  }

  def injectSubGraphQRoot(
      n: Int,
      subGraphWeights: Map[Domino, Double]
  ): CustomComputePartitionFunctionWeight = {
    val pointsInGraph: TreeSet[Point] =
      subGraphWeights.keys
        .flatMap(d => Seq(d.p1, d.p2))
        .foldLeft(TreeSet[Point]())(_ + _)

    val isInSubGraph: (Point) => Boolean = pointsInGraph.contains

    val weights = injectSubGraphQRoot(n, isInSubGraph)

    Face
      .activeFaces(n)
      .foreach { face =>
        face.dominoes.foreach { domino =>
          subGraphWeights.get(domino) match {
            case Some(w) =>
              weights(domino) = w
            case _ =>
          }
        }
      }

    weights
  }

  def isInHexagon(point: Point, a: Int, b: Int, c: Int): Boolean = {
    val order = 2 * a - 1 + b

    def xMin(y: Int): Int = {
      val j = y + c - 1
      if (j < a) order - c + 1 - 2 * b - j
      else order - a - c + 2 - 2 * b + (j - a)
    }

    def xMax(y: Int): Int = {
      val j = y + c - 1
      if (j < c) order - c + 1 + j else order - (j - c)
    }

    point.y >= -c + 1 && point.y <= a &&
    point.x >= xMin(point.y) && point.x <= xMax(point.y)
  }

  // format: off
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
   * @param a size of the top right and bottom left sides of the hexagon
   * @param b size of the top and bottom sides of the hexagon (those parallel to the horizontal axis)
   * @param c size of the top left and bottom right sides of the hexagon
   * @return  the WeightTrait needed to generate a random tiling.
   */
  // format: on
  def hexagonWeightGeneration(
      a: Int,
      b: Int,
      c: Int
  ): CustomGenerationWeight = {
    val order = 2 * a - 1 + b

    val weights = new CustomGenerationWeight(order)

    def isWeightOneDomino(domino: Domino): Boolean =
      (isInHexagon(domino.p1, a, b, c) && isInHexagon(domino.p2, a, b, c) &&
        domino.dominoType(order) != WestGoing) ||
        (!isInHexagon(domino.p1, a, b, c) && !isInHexagon(domino.p2, a, b, c))

    Face
      .activeFaces(order)
      .flatMap(_.dominoes.filter(isWeightOneDomino))
      .foreach(weights(_) = 1.0)

    weights
  }

  def hexagonWeightGeneration(n: Int): CustomGenerationWeight =
    hexagonWeightGeneration(n, n, n)

  def hexagonWeightPartition(
      a: Int,
      b: Int,
      c: Int
  ): CustomComputePartitionFunctionWeight = {
    val order = 2 * a - 1 + b

    val weights = new CustomComputePartitionFunctionWeight(order)

    def isWeightOneDomino(domino: Domino): Boolean =
      (isInHexagon(domino.p1, a, b, c) && isInHexagon(domino.p2, a, b, c) &&
        domino.dominoType(order) != WestGoing) ||
        (!isInHexagon(domino.p1, a, b, c) && !isInHexagon(domino.p2, a, b, c))

    Face
      .activeFaces(order)
      .flatMap(_.dominoes.filter(isWeightOneDomino))
      .foreach(weights(_) = QRoot(1, 1))

    weights
  }

  def isInRectangle(point: Point, width: Int, height: Int): Boolean =
    -width / 2 + 1 <= point.x && point.x <= (width + 1) / 2 && -height / 2 + 1 <= point.y && point.y <= (height + 1) / 2

  def rectangleOrder(width: Int, height: Int): Int =
    (width + height - 2 + 1) / 2 // +1 needed in case either
  // m or n in odd

  def rectangleWeightsGeneration(
      width: Int,
      height: Int
  ): CustomGenerationWeight =
    injectSubGraphDouble(
      rectangleOrder(width, height),
      (point: Point) => isInRectangle(point, width, height)
    )

  def rectangleWeightsPartition(
      width: Int,
      height: Int
  ): CustomComputePartitionFunctionWeight = {
    val order =
      (width + height - 2 + 1) / 2 // +1 needed in case either m or n in odd
    injectSubGraphQRoot(
      order,
      (point: Point) => isInRectangle(point, width, height)
    )
  }

  /** Returns whether the Point point is in the Aztec House of specified width and height.
    */
  def isInAztecHouse(point: Point, width: Int, height: Int): Boolean =
    (-width / 2 + 1 <= point.x && point.x <= (width + 1) / 2
      && -height / 2 + 1 <= point.y && point.y <= (height + 1) / 2) || point.y > (height + 1) / 2

  /** Returns the WeightMap able to generate a random tiling of an Aztec house.
    */
  def aztecHouseWeightsGeneration(
      width: Int,
      height: Int
  ): CustomGenerationWeight =
    injectSubGraphDouble(
      (width + height - 2 + 1) / 2,
      (point: Point) => isInAztecHouse(point, width, height)
    )

  def aztecHouseWeightsPartition(
      width: Int,
      height: Int
  ): CustomComputePartitionFunctionWeight =
    injectSubGraphQRoot(
      (width + height - 2 + 1) / 2,
      (point: Point) => isInAztecHouse(point, width, height)
    )

  def isInRightDoubleAztec(point: Point, n: Int, l: Int): Boolean =
    Diamond.inBoundsPoint(point, Point(n, 0), n) || Diamond.inBoundsPoint(
      point,
      Point(n + l + 1, l),
      n
    )

  def isInLeftDoubleAztec(point: Point, n: Int, l: Int): Boolean =
    Diamond.inBoundsPoint(point, Point(-n, 0), n) || Diamond.inBoundsPoint(
      point,
      Point(-n - 1 - l, l),
      n
    )

  def isInDoubleAztecDiamond(
      point: Point,
      order: Int,
      overlap: Int
  ): Boolean = {
    val n = order
    val l = order - overlap

    isInLeftDoubleAztec(point, n, l) || isInRightDoubleAztec(point, n, l)
  }

  /** Returns the WeightMap able to generate a random tiling of a Double Aztec diamond as in Adler-Johansson-van
    * Moerbeke.
    */
  def doubleAztecDiamondGeneration(
      order: Int,
      overlap: Int
  ): CustomGenerationWeight = {
    val totalOrder = 4 * order - 2 * overlap + 1

    // some helper values
    val n = order
    val l = order - overlap

    def oneWeight(domino: Domino): Boolean =
      isInRightDoubleAztec(domino.p1, n, l) == isInRightDoubleAztec(
        domino.p2,
        n,
        l
      ) &&
        isInLeftDoubleAztec(domino.p1, n, l) == isInLeftDoubleAztec(
          domino.p2,
          n,
          l
        )

    val weights = new CustomGenerationWeight(totalOrder)

    Face
      .activeFaces(totalOrder)
      .foreach(_.dominoes.foreach(domino => weights(domino) = if (oneWeight(domino)) 1.0 else 0.0))

    weights
  }

  def doubleAztecDiamondPartition(
      order: Int,
      overlap: Int
  ): CustomComputePartitionFunctionWeight = {
    val totalOrder = 4 * order - 2 * overlap + 1

    // some helper values
    val n = order
    val l = order - overlap

    def oneWeight(domino: Domino): Boolean =
      isInRightDoubleAztec(domino.p1, n, l) == isInRightDoubleAztec(
        domino.p2,
        n,
        l
      ) &&
        isInLeftDoubleAztec(domino.p1, n, l) == isInLeftDoubleAztec(
          domino.p2,
          n,
          l
        )

    val weights = new CustomComputePartitionFunctionWeight(totalOrder)

    val _0 = QRoot(0, 1)
    val _1 = QRoot(1, 1)

    Face
      .activeFaces(totalOrder)
      .foreach(_.dominoes.foreach(domino => weights(domino) = if (oneWeight(domino)) _1 else _0))

    weights
  }

  def isInDiamondRing(
      point: Point,
      innerOrder: Int,
      outerOrder: Int
  ): Boolean =
    Diamond.inBoundsPoint(point, Point(0, 0), outerOrder) && !Diamond
      .inBoundsPoint(point, Point(0, 0), innerOrder)

  /** Returns the WeightMap necessary to generate an Aztec ring with specified inner and outer order.
    *
    * Conditions on inner and outer orders for being tileable is shown in [1, Proposition 1].
    */
  def diamondRingGeneration(
      innerOrder: Int,
      outerOrder: Int
  ): CustomGenerationWeight =
    if (outerOrder > innerOrder && ((outerOrder - innerOrder) % 2 == 0 || outerOrder >= 3 * innerOrder - 1)) {
      injectSubGraphDouble(
        outerOrder,
        (point: Point) => isInDiamondRing(point, innerOrder, outerOrder)
      )
    } else {
      throw new NotTileableException
    }

  def diamondRingPartition(
      innerOrder: Int,
      outerOrder: Int
  ): CustomComputePartitionFunctionWeight =
    if (outerOrder > innerOrder && ((outerOrder - innerOrder) % 2 == 0 || outerOrder >= 3 * innerOrder - 1)) {
      injectSubGraphQRoot(
        outerOrder,
        (point: Point) => isInDiamondRing(point, innerOrder, outerOrder)
      )
    } else {
      throw new NotTileableException
    }

  def twoPeriodicAztecDiamondGeneration(
      a: Double,
      b: Double,
      order: Int
  ): CustomGenerationWeight = {
    val weight = new CustomGenerationWeight(order)

    val (aFaces, bFaces) = Face
      .activeFaces(order)
      .partition(face => (face.bottomLeft.y + order) % 2 == 1)

    aFaces.flatMap(_.dominoes).foreach(weight(_) = a)
    bFaces.flatMap(_.dominoes).foreach(weight(_) = b)

    weight
  }

  /** Computes the WeightMap for a double periodic aztec diamond of order order. The a weight value starts at the top of
    * the diamond.
    */
  def twoPeriodicAztecDiamondPartition(
      a: QRoot,
      b: QRoot,
      order: Int
  ): CustomComputePartitionFunctionWeight = {
    val weight = new CustomComputePartitionFunctionWeight(order)

    val (aFaces, bFaces) = Face
      .activeFaces(order)
      .partition(face => (face.bottomLeft.y + order) % 2 == 1)

    aFaces.flatMap(_.dominoes).foreach(weight(_) = a)
    bFaces.flatMap(_.dominoes).foreach(weight(_) = b)

    weight
  }

  /** Computes all the weights from weight to the Weights for diamond order 1. The head of the list is the WeightMap for
    * diamond of order 1, and the last is the argument weight.
    *
    * This can be memory-heavy and should be adapted to avoid Out of Memory issues when generating Diamonds of large
    * sizes. When memory is not an issue, it is the most efficient way to generate diamonds. Remark: when using
    * [[UniformWeightGeneration]], memory is never an issue because of the special treatment that we do in this case.
    */
  def computeAllWeights[WeightType, Weight <: WeightTrait[WeightType]](
      weight: Weight
  ): List[Weight] = {
    def computeAllWeightsAcc(computed: List[Weight]): List[Weight] =
      if (computed.head.n == 1)
        computed
      else
        computeAllWeightsAcc(
          computed.head.subWeights.asInstanceOf[Weight] +: computed
        )

    computeAllWeightsAcc(List(weight))
  }
}
