package diamond

import custommath.{IntegerMethods, QRoot, WeightLikeNumber}
import exceptions.ShouldNotBeThereException
import geometry.*

import scala.concurrent.duration.FiniteDuration
import narr.NArray
import utils.TimerLogger

import scala.annotation.tailrec

/** A Diamond represents an Aztec Diamond with its tiling.
  *
  * In a full Diamond, the number of dominoes is order * (order + 1).
  *
  * Each Vector of the dominoes Vector is a column of the Diamond. Thus, the first Vector has size 2, the second has
  * size 4...
  *
  * For example, an Aztec diamond of order 1 with two horizontal dominoes would have the dominoes Vector Vector(
  * Vector(Some(Domino(Point(0,0), Point(1,0)), Some(Domino(Point(0,1), Point(1,1)))), Vector(None, None) )
  */
//noinspection ScalaUnusedSymbol
final class Diamond(private[diamond] val internalDominoes: NArray[NArray[Option[Domino]]]) {

  private[diamond] lazy val dominoesNumber: Int = internalDominoes.map(d => d.count(_.isDefined)).sum

  private[diamond] lazy val listOfDominoes: NArray[Domino] = {
    val dominoes     = NArray.ofSize[Domino](dominoesNumber)
    var currentIndex = 0
    internalDominoes.foreach(_.foreach(_.foreach { domino =>
      dominoes(currentIndex) = domino
      currentIndex += 1
    }))
    dominoes
  }

  def dominoes: Iterable[Domino] = listOfDominoes

  lazy val order: Int =
    (-1 + IntegerMethods.integerSquareRoot(1 + 4 * dominoesNumber)) / 2

  def weightQRoot(weightTrait: ComputePartitionFunctionWeight): QRoot =
    listOfDominoes.map(weightTrait.apply).product

  def weightDouble(weightTrait: GenerationWeight): Double =
    listOfDominoes.map(weightTrait.apply).product

  def skewDiamond: Boolean = order * (order + 1) != dominoesNumber

  def contains(domino: Domino): Boolean = inBoundsDomino(domino) && {
    val (x, y) = Domino.changeVerticalCoordinates(domino.p1, order)
    internalDominoes(x)(y).contains[Domino](domino)
  }

  def inBoundsPoint(point: Point): Boolean =
    math.abs(point.x - 0.5) + math.abs(point.y - 0.5) <= order

  def inBoundsDomino(domino: Domino): Boolean =
    inBoundsPoint(domino.p1) && inBoundsPoint(domino.p2)

  def inSubGraph(isInSubGraph: Point => Boolean): String = {
    val chars: NArray[NArray[String]] =
      NArray.fill[NArray[String]](2 * order)(NArray.fill[String](2 * order)(" "))

    listOfDominoes.foreach(domino =>
      if isInSubGraph(domino.p1) then {
        val (x1, y1) = (domino.p1.x + order - 1, domino.p1.y + order - 1)
        val (x2, y2) = (domino.p2.x + order - 1, domino.p2.y + order - 1)
//        val color = domino.dominoType(order) match {
//          case NorthGoing =>
//            Console.RED_B
//          case SouthGoing =>
//            Console.GREEN_B
//          case EastGoing =>
//            Console.YELLOW_B
//          case WestGoing =>
//            Console.BLUE_B
//        }
        val black = "" //  Console.BLACK_B
        val color = ""
        chars(y1)(x1) = color + " " + black
        chars(y2)(x2) = color + " " + black
        if (domino.isHorizontal) {
          chars(y1)(x1) = color + "<"
          chars(y2)(x2) = ">" + black
        } else {
          chars(y1)(x1) = color + "n" + black
          chars(y2)(x2) = color + "v" + black
        }
      }
    )

    chars.map(_.mkString("")).mkString("\n")
  }

  override def toString: String = {
    val chars: NArray[NArray[String]] =
      NArray.fill[NArray[String]](2 * order)(NArray.fill[String](2 * order)(" "))

    listOfDominoes.foreach { domino =>
      val (x1, y1) = (domino.p1.x + order - 1, domino.p1.y + order - 1)
      val (x2, y2) = (domino.p2.x + order - 1, domino.p2.y + order - 1)
      val black    = ""
      val color    = ""
      chars(y1)(x1) = color + " " + black
      chars(y2)(x2) = color + " " + black
      if (domino.isHorizontal) {
        chars(y1)(x1) = color + "<"
        chars(y2)(x2) = ">" + black
      } else {
        chars(y1)(x1) = color + "n" + black
        chars(y2)(x2) = color + "v" + black
      }
    }

    chars.map(_.mkString("")).mkString("\n")
  }

  private[diamond] lazy val activeFaces: NArray[Face] = Face.activeFaces(order)

  /** Computes the probability of seeing this domino if generated with the given weights. */
  def probability(
      weights: ComputePartitionFunctionWeight,
      statusCallback: Int => Unit,
      subroutineStatusCallback: Int => Unit
  ): QRoot = TilingNumberComputer(this, weights, statusCallback, subroutineStatusCallback).probability

  lazy val numberOfSubDiamonds: BigInt = activeFaces.map(_.numberOfPreviousDiamond(this)).map(BigInt(_)).product

  /** Returns the sub diamond that correspond to the specified index.
    *
    * Note: it is not guaranteed that
    * {{{
    *   indexedSubDiamond(j) == subDiamonds(j)
    * }}}
    *
    * but rather than
    *
    * {{{
    *   (0 until numberOfSubDiamonds).map(indexedSubDiamond).toSet == subDiamonds.toSet
    * }}}
    *
    * @param diamondIndex
    *   index of the sub diamond to build. Must satisfy 0 <= index < numberOfSubDiamonds
    */
  def indexedSubDiamond(diamondIndex: BigInt)(using timerLogger: TimerLogger = TimerLogger.noOp): Diamond = {
    def fillPossibilities(
        dominoesToFill: NArray[Domino],
        dominoes: NArray[NArray[Option[Domino]]]
    ): Unit =
      dominoesToFill.foreach { domino =>
        val (x, y) = Domino.changeVerticalCoordinates(domino.p1, order - 1)
        dominoes(x)(y) = Some(domino)
      }

    val previousConstructions                            = activeFaces.map(_.previousDiamondConstruction(this))
    val (previousWithOneDomino, previousWithTwoDominoes) = previousConstructions.partition(_.length == 1)
    val faceChoices = timerLogger.time("face choices")(
      IntegerMethods.binaryDecompositionPrependedTo(diamondIndex, previousWithTwoDominoes.length)
    )

    val dominoes = Diamond.emptyArrayDominoes(order - 1)
    timerLogger.time("fill possibilities")(
      previousWithOneDomino.foreach(previous => fillPossibilities(previous(0), dominoes))
    )

    timerLogger.time(s"Iterating over ${previousWithTwoDominoes.length}")(
      for (index <- previousWithTwoDominoes.indices) {
        val previousConstruction      = previousWithTwoDominoes(index)
        val previousConstructionIndex = faceChoices.apply(index)
        fillPossibilities(previousConstruction(previousConstructionIndex), dominoes)
      }
    )

    Diamond(dominoes)
  }

  /** Returns the List of all sub diamonds that can generate this one from the algorithm. This is the "inverse"
    * operation of the algorithm.
    */
  private[diamond] def subDiamonds: NArray[Diamond] =
    if order == 1 then NArray.empty[Diamond]
    else {
      val dominoes: NArray[NArray[Option[Domino]]] = Diamond.emptyArrayDominoes(order - 1)

      def fillPossibilities(
          dominoesToFill: NArray[Domino],
          dominoes: NArray[NArray[Option[Domino]]]
      ): Unit =
        dominoesToFill.foreach { domino =>
          val (x, y) = Domino.changeVerticalCoordinates(domino.p1, order - 1)
          dominoes(x)(y) = Some(domino)
        }

      activeFaces
        .foldLeft(NArray[NArray[NArray[Option[Domino]]]](dominoes)) { (dominoesList, face) =>
          val previousConstruction = face.previousDiamondConstruction(this)
          if previousConstruction.length == 1 then {
            dominoesList.foreach(fillPossibilities(previousConstruction(0), _))
            dominoesList
          } else if previousConstruction.length == 2 then {
            val p1 = previousConstruction(0)
            val p2 = previousConstruction(1)
            dominoesList.flatMap { dominoes =>
              val clone: NArray[NArray[Option[Domino]]] = dominoes.map(_.map(identity))
              List(
                {
                  fillPossibilities(p1, dominoes)
                  dominoes
                }, {
                  fillPossibilities(p2, clone)
                  clone
                }
              )
            }
          } else throw new UnsupportedOperationException
        }
        .map(array => Diamond(array))
    }

  def aSubDiamond: Option[Diamond] = Option.unless(order == 1)(indexedSubDiamond(0))

  private[diamond] def allSubDiamonds: NArray[Diamond] =
    this +: subDiamonds.flatMap(_.allSubDiamonds)

  def randomSubDiamond(using timerLogger: TimerLogger = TimerLogger.noOp): Option[Diamond] = Option.unless(order == 1) {
    val number = timerLogger.time(s"number of sub-diamonds order $order")(numberOfSubDiamonds)
    val index  = timerLogger.time("sub-diamond index")(IntegerMethods.nextBigInt(number))
    timerLogger.time("choose sub-diamond")(indexedSubDiamond(index))
  }

  def aRandomSubDiamond: Diamond = randomSubDiamond.get

  def firstSubDiamond: Diamond = indexedSubDiamond(0)

  def nonIntersectingPaths: Vector[Vector[Point]] = {
    def nextPoint(domino: Domino): Point = domino.dominoType(order) match {
      case SouthGoing => domino.p1 + Point(2, 0)  // path: --
      case EastGoing  => domino.p2 + Point(1, -1) // path: /
      case WestGoing  => domino.p1 + Point(1, 1)  // path: \
      case NorthGoing => throw new ShouldNotBeThereException
    }

    def followPath(point: Point): Vector[Point] = {
      @tailrec
      def acc(accumulator: List[Point]): List[Point] = {
        val currentPoint = accumulator.head
        if (inBoundsPoint(currentPoint)) {
          val domino = List(
            Domino(currentPoint, currentPoint + Point(1, 0)),
            Domino(currentPoint, currentPoint + Point(0, 1)),
            Domino(currentPoint + Point(0, -1), currentPoint)
          ).find(contains).get

          acc(nextPoint(domino) +: accumulator)
        } else accumulator
      }

      acc(List(point)).reverse.toVector
    }

    (0 until order).map(j => Point(-order + 1 + j, -j)).toVector.map(followPath)
  }

  def nonIntersectingPathsSubGraph(
      isInSubGraph: Point => Boolean
  ): Vector[Vector[Point]] =
    nonIntersectingPaths
      .flatMap(points =>
        points.tail
          .foldLeft((Vector(Vector(points.head)), isInSubGraph(points.head))) { case ((paths, wasInSubGraph), point) =>
            if (wasInSubGraph) {
              ((paths.head :+ point) +: paths.tail, isInSubGraph(point))
            } else if (isInSubGraph(point)) {
              (Vector(point) +: paths, true)
            } else {
              (paths, false)
            }
          }
          ._1
      )
      .filter(_.length > 1)

  def toArray: NArray[Int] = {
    val allInts = order +: listOfDominoes.flatMap(domino => NArray(domino.p1.x, domino.p1.y, domino.p2.x, domino.p2.y))
    val output  = NArray.ofSize[Int](allInts.length)
    allInts.indices.foreach { idx =>
      output(idx) = allInts(idx)
    }
    output
  }

  override def equals(that: Any): Boolean = that match {
    case that: Diamond if this.order == that.order =>
      this.internalDominoes
        .zip(that.internalDominoes)
        .forall { case (v1, v2) =>
          v1.zip(v2).forall { case (d1, d2) => d1 == d2 }
        }
    case _ => false
  }

  override def hashCode(): Int = internalDominoes.hashCode

}

//noinspection ScalaUnusedSymbol
object Diamond {

  def fromIntsSerialization(intDiamondIterable: Iterable[Int]): Diamond = {
    val intDiamond = intDiamondIterable.toList

    val order = intDiamond.head

    val dominoes = emptyArrayDominoes(order)

    intDiamond.tail
      .grouped(4)
      .foreach {
        case List(x1, y1, x2, y2) =>
          val domino = Domino(Point(x1, y1), Point(x2, y2))
          val (x, y) = Domino.changeVerticalCoordinates(domino.p1, order)
          dominoes(x)(y) = Some(domino)
        case other => throw new RuntimeException(other.toString)
      }

    Diamond(dominoes)
  }

  final class DiamondGenerationInfo(val diamondType: DiamondType)(
      val diamond: Diamond,
      val timeTaken: FiniteDuration,
      val args: diamondType.ArgType
  ) {
    def diamondTypeWithArgs: DiamondType.DiamondTypeWithArgs = diamondType.withArgs(args)
  }

  final class DiamondCountingInfo(val diamondType: DiamondType)(
      val timeTaken: FiniteDuration,
      val args: diamondType.ArgType,
      val weight: QRoot,
      val probability: QRoot
  ) {
    lazy val diamondTypeWithArgs: DiamondType.DiamondTypeWithArgs = diamondType.withArgs(args)

    lazy val partitionFunction: QRoot = weight / probability

    lazy val subGraphPartition: QRoot = diamondTypeWithArgs.totalPartitionFunctionToSubGraph(partitionFunction)

    lazy val isSubGraphPartitionInteger: Boolean = subGraphPartition == QRoot.fromBigInt(subGraphPartition.toBigInt)

    lazy val scientificNotation: String =
      if (!isSubGraphPartitionInteger) ""
      else {
        val stringNbr = subGraphPartition.toBigInt.toString
        if stringNbr.length < 6 then ""
        else {
          " (" ++ (stringNbr(0).toString ++ "." ++ stringNbr.slice(
            1,
            4
          ) ++ "e+" ++ (stringNbr.length - 1).toString) + ")"
        }
      }

  }

  /** Generate a random tiling of the diamond of order n = weights.last.n.
    *
    * @param weights
    *   The list of all the Weights for diamonds from order 1 to n. (Can be computed using WeightTrait.computeAllWeights
    *   method.
    * @return
    *   A Diamond instance generated randomly.
    */
  def generateDiamond(weights: List[GenerationWeight]): Diamond =
    weights.tail.foldLeft(weights.head.generateOrderOneDiamond) { case (diamond, weight) =>
      weight.generateDiamond(diamond)
    }

  def uniformDiamond(n: Int)(implicit num: WeightLikeNumber[Double]): Diamond =
    generateDiamond((1 to n).toList.map(new UniformWeightGeneration(_)))

  def uniformRectangle(width: Int, height: Int): Diamond =
    generateDiamond(
      WeightTrait.computeAllWeights[Double, CustomGenerationWeight](
        WeightTrait.rectangleWeightsGeneration(width, height)
      )
    )

  def aztecHouse(width: Int, height: Int): Diamond =
    generateDiamond(
      WeightTrait.computeAllWeights[Double, CustomGenerationWeight](
        WeightTrait.aztecHouseWeightsGeneration(width, height)
      )
    )

  /** Returns an Aztec diamond that matches with the weights of an aztec house, and that will generate only on pre-image
    * along the deconstruction algorithm.
    *
    * The way it works is:
    *   - putting horizontal dominoes in an Aztec diamond of order width / 2 at the bottom
    *   - putting vertical dominoes everywhere else.
    *
    * @param width
    *   width of the house. Must be even.
    * @param height
    *   height of the rectangular part of the house. May be either even or odd.
    */
  def handCraftedAztecHouse(width: Int, height: Int): Diamond = {
    val order = (width + height - 2 + 1) / 2

    val bottomDiamondCenterOrdinate = -height / 2
    val bottomDiamondCenter         = Point(0, bottomDiamondCenterOrdinate)
    val bottomDiamondOrder          = order + bottomDiamondCenterOrdinate

    val dominoes = emptyArrayDominoes(order)

    val (inBottomDiamond, outBottomDiamond) = Face
      .activeFaces(order)
      .flatMap(_.dominoes)
      .filter(domino =>
        inBoundsPoint(
          domino.p1,
          bottomDiamondCenter,
          bottomDiamondOrder
        ) == inBoundsPoint(
          domino.p2,
          bottomDiamondCenter,
          bottomDiamondOrder
        )
      )
      .partition(domino => inBoundsPoint(domino.p1, bottomDiamondCenter, bottomDiamondOrder))

    inBottomDiamond
      .filter(_.isHorizontal)
      .filter(domino =>
        (domino.dominoType(
          order
        ) == NorthGoing) == (domino.p1.y > bottomDiamondCenterOrdinate)
      )
      .foreach { domino =>
        val (x, y) = Domino.changeVerticalCoordinates(domino.p1, order)
        dominoes(x)(y) = Some(domino)
      }

    outBottomDiamond
      .filter(_.isVertical)
      .filter(domino => (domino.dominoType(order) == EastGoing) == (domino.p1.x > 0))
      .foreach { domino =>
        val (x, y) = Domino.changeVerticalCoordinates(domino.p1, order)
        dominoes(x)(y) = Some(domino)
      }

    Diamond(dominoes)
  }

  private def uniformHexagon(a: Int, b: Int, c: Int): Diamond =
    generateDiamond(
      WeightTrait.computeAllWeights[Double, CustomGenerationWeight](
        WeightTrait.hexagonWeightGeneration(a, b, c)
      )
    )

  def uniformHexagon(n: Int): Diamond = uniformHexagon(n, n, n)

  def doubleAztecDiamond(order: Int, overlap: Int): Diamond =
    generateDiamond(
      WeightTrait.computeAllWeights[Double, CustomGenerationWeight](
        WeightTrait.doubleAztecDiamondGeneration(order, overlap)
      )
    )

  def fullHorizontalDiamond(order: Int): Diamond = {
    val dominoes = emptyArrayDominoes(order)
    Face
      .activeFaces(order)
      .flatMap(face => List(face.horizontalDominoes._1, face.horizontalDominoes._2))
      .filter(domino => (domino.dominoType(order) == NorthGoing) == (domino.p1.y > 0))
      .foreach { domino =>
        val (x, y) = Domino.changeVerticalCoordinates(domino.p1, order)
        dominoes(x)(y) = Some(domino)
      }
    Diamond(dominoes)
  }

  def emptyArrayDominoes(order: Int): NArray[NArray[Option[Domino]]] = {
    val dominoes = NArray.ofSize[NArray[Option[Domino]]](2 * order)

    for (j <- 1 to order) {
      dominoes(j - 1) = NArray.fill[Option[Domino]](2 * j)(None)
      dominoes(2 * order - j) = NArray.fill[Option[Domino]](2 * j)(None)
    }
    dominoes
  }

  /** Returns whether point is in the diamond of order order centered at center.
    *
    * The default diamond is centered at Point(0,0).
    */
  def inBoundsPoint(point: Point, center: Point, order: Int): Boolean =
    math.abs(point.x - 0.5 - center.x) + math.abs(
      point.y - 0.5 - center.y
    ) <= order

}
