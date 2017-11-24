package diamond

import custommath.QRoot
import diamondtypes._
import exceptions.{NoSuchDiamondType, WrongParameterException}
import geometry.Domino

import scala.language.implicitConversions


trait DiamondType {
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
   * @throws     WrongParameterException if arguments are not valid. This is checked in the worker!
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

  def isInteger(d: Double): Boolean = d == math.round(d)


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
