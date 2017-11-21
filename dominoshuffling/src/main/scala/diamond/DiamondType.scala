package diamond

import custommath.QRoot
import exceptions.{NoSuchDiamondType, WrongParameterException}
import geometry.{Domino, Point}

import scala.language.implicitConversions


sealed trait DiamondType {
  type ArgType

  /** Determines whether this type of Diamond is best suited for lozenge tiling */
  val lozengeTiling: Boolean

  /** Set the default rotation for drawing. Hexagons are best suited with pi/6 rotation. */
  val defaultRotation: Int

  val designedForPartitionFunction: Boolean = false

  /** Returns the order of the diamond needed to generate a diamond of this type. */
  def diamondOrder(args: ArgType): Int

  /**
   * Takes a Vector of Doubles with the all the wished arguments for generation or computation.
   *
   * @param args a Vector with the arguments, expected to match the corresponding ArgType.
   * @return     the corresponding ArgType for later type safety.
   * @throws     WrongParameterException if arguments are not valid.
   */
  def transformArguments(args: Vector[Double]): ArgType

  /** The [[GenerationWeight]] necessary for generating a Diamond of this type. */
  def makeGenerationWeight(args: ArgType): GenerationWeight

  /** The [[ComputePartitionFunctionWeight]] necessary for computing the partition function of this type of Diamond */
  def makeComputationWeight(args: ArgType): ComputePartitionFunctionWeight

  /**
   * The Diamond that we used for computing the partition function.
   * It should be designed in such a way that at each step, there is a unique pre-image. It's not mandatory but it
   * certainly lighten the computations a lot. (OutOfMemory could also be an issue.)
   */
  def countingTilingDiamond(args: ArgType): Diamond


  /**
   * Takes the Partition Function for the entire Aztec Diamond, and returns the Partition Function (which is then often
   * only the tiling number) of the sub graph we are interested in.
   */
  def totalPartitionFunctionToSubGraph(args: ArgType, totalPartition: QRoot): QRoot

  /**
   * Predicate for knowing what domino is in the sub-graph of this type of Diamond.
   * Used for drawing.
   */
  def isInDiamond(args: ArgType): (Domino) => Boolean

  /** A space separated version of the toString of the object. */
  def name: String = """[A-Z][a-z]*""".r.findAllIn(toString).mkString(" ")

  /**
   * A List of the names of the arguments to display in the html file, as well as default generation and tiling counting
   * values.
   */
  val argumentNames: List[(String, Double, Double)]
}


object DiamondType {

  case object UniformDiamond extends DiamondType {
    type ArgType = Int

    val lozengeTiling: Boolean = false

    val defaultRotation: Int = 0

    def diamondOrder(args: Int): Int = args

    def transformArguments(args: Vector[Double]): Int = {
      val order = args(0).toInt
      if (isInteger(args(0)) && order >= 1) {
        order
      } else {
        throw new WrongParameterException(s"Order of diamond must be a positive integer (received: ${args(0)}).")
      }
    }

    def makeGenerationWeight(args: Int): UniformWeightGeneration = new UniformWeightGeneration(args)

    def makeComputationWeight(args: Int): UniformWeightPartition = new UniformWeightPartition(args)

    def countingTilingDiamond(args: Int): Diamond = Diamond.fullHorizontalDiamond(args)

    def totalPartitionFunctionToSubGraph(args: Int, totalPartition: QRoot): QRoot = totalPartition

    def isInDiamond(args: Int): Domino => Boolean = (_: Domino) => true

    def theoreticTilingNumber(order: Int): QRoot = QRoot(BigInt(2) pow (order * (order + 1) / 2), 1)

    val argumentNames: List[(String, Double, Double)] = List(("Diamond Order", 100, 5))
  }

  case object AztecHouse extends DiamondType {
    type ArgType = (Int, Int)

    val lozengeTiling: Boolean = false

    val defaultRotation: Int = 0

    def diamondOrder(args: (Int, Int)): Int = (args._1 + args._2 - 2 + 1) / 2

    /**
     * Arguments are of the form (n, h), but ArgType is (width, height), so there's an extra transformation here.
     */
    def transformArguments(args: Vector[Double]): (Int, Int) = {
      val width = args(0).toInt * 2
      val height = args(1).toInt + 1
      if (args.forall(isInteger) && width > 0 && height > 0) {
        (width, height)
      } else {
        throw new WrongParameterException(
          s"For the shape to be tileable, n must be a positive integer, and h must be a non negative integer " +
            s"(received: (${args(0)}, ${args(1)}))."
        )
      }
    }

    def makeGenerationWeight(args: (Int, Int)): CustomGenerationWeight =
      WeightTrait.aztecHouseWeightsGeneration(args._1, args._2)

    def makeComputationWeight(args: (Int, Int)): CustomComputePartitionFunctionWeight =
      WeightTrait.aztecHouseWeightsPartition(args._1, args._2)

    def countingTilingDiamond(args: (Int, Int)): Diamond = Diamond.handCraftedAztecHouse(args._1, args._2)

    def totalPartitionFunctionToSubGraph(args: (Int, Int), totalPartition: QRoot): QRoot = totalPartition

    def isInDiamond(args: (Int, Int)): Domino => Boolean =
      (domino: Domino) => WeightTrait.isInAztecHouse(domino.p1, args._1, args._2)

    val argumentNames: List[(String, Double, Double)] = List(("Aztec n", 30, 5), ("Aztec h", 30, 5))
  }

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


  case object TwoPeriodic extends DiamondType {
    type ArgType = (Int, Double, Double)

    val lozengeTiling: Boolean = false

    val defaultRotation: Int = 0

    override val designedForPartitionFunction: Boolean = true

    def diamondOrder(args: (Int, Double, Double)): Int = args._1

    def transformArguments(args: Vector[Double]): (Int, Double, Double) = {
      val order = args(0).toInt
      val a = args(1)
      val b = args(2)
      if (isInteger(args(0)) && order > 0 && a > 0 && b > 0) {
        (order, a, b)
      } else {
        throw new WrongParameterException(
          s"For the shape to be tileable, Order must be a positive integer, and a and b must be positive real " +
            s"numbers. Received: " +
            s"(${args(0)}, ${args(1)}, ${args(2)})."
        )
      }
    }

    def makeGenerationWeight(args: ArgType): CustomGenerationWeight =
      WeightTrait.twoPeriodicAztecDiamondGeneration(args._2, args._3, args._1)

    def makeComputationWeight(args: ArgType): CustomComputePartitionFunctionWeight = {
      val a = QRoot.fromRationalDouble(args._2)
      val b = QRoot.fromRationalDouble(args._3)
      WeightTrait.twoPeriodicAztecDiamondPartition(a, b, args._1)
    }

    def countingTilingDiamond(args: (Int, Double, Double)): Diamond = Diamond.fullHorizontalDiamond(args._1)

    def isInDiamond(args: (Int, Double, Double)): Domino => Boolean = (_: Domino) => true

    def totalPartitionFunctionToSubGraph(args: (Int, Double, Double), totalPartition: QRoot): QRoot = totalPartition

    val argumentNames: List[(String, Double, Double)] =
      List(("Diamond Order", 100, 5), ("Weight a", 0.5, 0.5), ("Weight b", 1, 1))
  }

  case object DoubleAztecDiamond extends DiamondType {
    type ArgType = (Int, Int)

    val lozengeTiling: Boolean = false

    val defaultRotation: Int = 0

    def diamondOrder(args: (Int, Int)): Int = 4 * args._1 - 2 * args._2 + 1

    def transformArguments(args: Vector[Double]): (Int, Int) = {
      val order = args(0).toInt
      val overlap = args(1).toInt
      if (args.forall(isInteger) && order > 0 && overlap > 0 && overlap < order) {
        (order, overlap)
      } else {
        throw new WrongParameterException(
          s"For the shape to be tileable, Order and overlap must be positive integers, and overlap < order. " +
            s"Received: " +
            s"(${args(0)}, ${args(1)}))."
        )
      }
    }

    def makeGenerationWeight(args: ArgType): CustomGenerationWeight =
      WeightTrait.doubleAztecDiamondGeneration(args._1, args._2)

    def makeComputationWeight(args: ArgType): CustomComputePartitionFunctionWeight =
      WeightTrait.doubleAztecDiamondPartition(args._1, args._2)

    def countingTilingDiamond(args: (Int, Int)): Diamond = ???

    def totalPartitionFunctionToSubGraph(args: (Int, Int), totalPartition: QRoot): QRoot = ???

    def isInDiamond(args: (Int, Int)): Domino => Boolean =
      (domino: Domino) => WeightTrait.isInRightDoubleAztec(domino.p1, args._1, args._1 - args._2)

    val argumentNames: List[(String, Double, Double)] =
      List(("Order of diamonds", 50, 3), ("Overlap", 10, 1))
  }

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


  private def isInteger(d: Double): Boolean = d == math.round(d)


  val diamondTypes: List[DiamondType] = List(
    UniformDiamond, AztecHouse, AztecRing, Rectangle, TwoPeriodic, DoubleAztecDiamond, Hexagon
  )

  implicit def fromString(str: String): DiamondType = diamondTypes.indexWhere(_.toString == str) match {
    case -1 => throw new NoSuchDiamondType(str)
    case idx => diamondTypes(idx)
  }

  implicit class DiamondTypeFromString(str: String) {
    def toDiamondType: DiamondType = fromString(str)
  }

}
