package diamond

import custommath.QRoot
import diamondtypes._
import exceptions.{NoSuchDiamondType, WrongParameterException}
import geometry.{Domino, Point}

import scala.language.implicitConversions

trait DiamondType {
  type ArgType <: Tuple

  /** Determines whether this type of Diamond is best suited for lozenge tiling */
  val lozengeTiling: Boolean

  /** Set the default rotation for drawing. Hexagons are best suited with pi/6 rotation. */
  val defaultRotation: Int

  val designedForPartitionFunction: Boolean = false

  /** Returns the order of the diamond needed to generate a diamond of this type. */
  def diamondOrder(args: ArgType): Int

  /** Takes a Vector of Doubles with the all the wished arguments for generation or computation.
    *
    * @param args
    *   a Vector with the arguments, expected to match the corresponding ArgType.
    * @return
    *   (success) the corresponding ArgType for later type safety.
    * @return
    *   (failure) WrongParameterException if arguments are not valid. This is checked in the worker!
    */
  def transformArguments(args: Seq[Double]): Either[WrongParameterException, ArgType]

  final def unsafeTransformArguments(args: Seq[Double]): ArgType = transformArguments(args).toTry.get

  def transformArgumentsBack(arg: ArgType): Seq[Double]

  /** The [[GenerationWeight]] necessary for generating a Diamond of this type. */
  def makeGenerationWeight(args: ArgType): GenerationWeight

  /** The [[ComputePartitionFunctionWeight]] necessary for computing the partition function of this type of Diamond */
  def makeComputationWeight(args: ArgType): ComputePartitionFunctionWeight

  /** The Diamond that we used for computing the partition function. It should be designed in such a way that at each
    * step, there is a unique pre-image. It's not mandatory but it certainly lighten the computations a lot.
    * (OutOfMemory could also be an issue.)
    */
  def countingTilingDiamond(args: ArgType): Diamond
  
  def countTiling(args: ArgType): QRoot = {
    val diamond = countingTilingDiamond(args)
    val weights = makeComputationWeight(args)
    val diamondProbability = diamond.probability(weights, _ => (), _ => ())
    val partition = 1 / diamondProbability
    totalPartitionFunctionToSubGraph(args, partition)
  }

  /** Takes the Partition Function for the entire Aztec Diamond, and returns the Partition Function (which is then often
    * only the tiling number) of the sub graph we are interested in.
    */
  def totalPartitionFunctionToSubGraph(args: ArgType, totalPartition: QRoot): QRoot

  /** Predicate for knowing what point is in the sub-graph of this type of Diamond. Used for drawing.
    */
  def isPointInDiamond(args: ArgType): Point => Boolean

  def isInDiamond(args: ArgType): Domino => Boolean =
    (domino: Domino) => isPointInDiamond(args)(domino.p1) && isPointInDiamond(args)(domino.p2)

  /** A space separated version of the toString of the object. */
  def name: String = """[A-Z][a-z]*""".r.findAllIn(toString).mkString(" ")

  /** A List of the names of the arguments to display in the html file, as well as default generation and tiling
    * counting values.
    */
  val argumentNames: List[DiamondType.ArgumentName]

  def withArgs(args: ArgType): DiamondType.DiamondTypeWithArgs = DiamondType.DiamondTypeWithArgs(this)(args)
}

object DiamondType {

  final class DiamondTypeWithArgs(val diamondType: DiamondType)(val args: diamondType.ArgType) {

    def lozengeTiling: Boolean = diamondType.lozengeTiling

    def defaultRotation: Int = diamondType.defaultRotation

    def designedForPartitionFunction: Boolean = diamondType.designedForPartitionFunction

    def name: String = diamondType.name

    def argumentNames: List[ArgumentName] = diamondType.argumentNames

    def diamondOrder: Int = diamondType.diamondOrder(args)

    def makeGenerationWeight: GenerationWeight = diamondType.makeGenerationWeight(args)

    def makeComputationWeight: ComputePartitionFunctionWeight = diamondType.makeComputationWeight(args)

    def countingTilingDiamond: Diamond = diamondType.countingTilingDiamond(args)

    def totalPartitionFunctionToSubGraph(totalPartition: QRoot): QRoot =
      diamondType.totalPartitionFunctionToSubGraph(args, totalPartition)

    val isPointInDiamond: Point => Boolean = diamondType.isPointInDiamond(args)

    val isInDiamond: Domino => Boolean = diamondType.isInDiamond(args)

    def transformArgumentsBack: Seq[Double] = diamondType.transformArgumentsBack(args)

  }

  case class ArgumentName(label: String, defaultGenerationValue: Double, defaultTilingCountingValue: Double)

  def isInteger(d: Double): Boolean = d == math.round(d)

  val diamondTypes: List[DiamondType] = List(
    UniformDiamond,
    AztecHouse,
    AztecRing,
    Rectangle,
    TwoPeriodic,
    DoubleAztecDiamond,
    Hexagon,
    TwoPeriodicRectangle,
    Trapezoidal
  )

  implicit def fromString(str: String): DiamondType = diamondTypes.indexWhere(_.toString == str) match {
    case -1  => throw new NoSuchDiamondType(str)
    case idx => diamondTypes(idx)
  }

  implicit class DiamondTypeFromString(str: String) {
    def toDiamondType: DiamondType = fromString(str)
  }

  def generateDiamond(diamondType: DiamondType)(args: diamondType.ArgType): Diamond = {
    val weights = WeightTrait.computeAllWeights[Double, GenerationWeight](
      diamondType.makeGenerationWeight(args)
    )

    Diamond.generateDiamond(weights)
  }

}
