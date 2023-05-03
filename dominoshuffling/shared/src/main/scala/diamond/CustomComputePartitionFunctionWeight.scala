package diamond

import custommath.{IntegerMethods, QRoot, Rational}
import exceptions.WrongOrderException
import geometry.{Domino, Face}

import scala.reflect.ClassTag
import narr.NArray

class CustomComputePartitionFunctionWeight(val n: Int)(implicit
    val tTag: ClassTag[QRoot],
    val atTag: ClassTag[NArray[QRoot]]
) extends ComputePartitionFunctionWeight
    with CustomWeightTrait[QRoot] {

  def makeArray(size: Int): NArray[QRoot]                 = NArray.ofSize[QRoot](size)
  def makeArrayOfArrays(size: Int): NArray[NArray[QRoot]] = NArray.ofSize[NArray[QRoot]](size)

  /** Computes the Weights corresponding to the Diamond of order diamondOrder - 1
    */
  def subWeights: CustomComputePartitionFunctionWeight = if (n == 1)
    throw new WrongOrderException("Can't compute WeightMap of order 0.")
  else {

    val _0 = QRoot(0, 1)

    val newWeights = new CustomComputePartitionFunctionWeight(n - 1)
    Face
      .activeFaces(n)
      .flatMap { face =>
        val (newPairs, newZeroes) = face.qRootSubWeights(this)

        newPairs.foreach { (d, w) =>
          if newWeights.inBoundsDomino(d) then {
            newWeights(d) = w
          }
        }

        newZeroes
      }
      .foreach { domino =>
        if newWeights.inBoundsDomino(domino) then newWeights(domino) = _0
      }

    newWeights

  }

  /** Returns a WeightMap with all the weights of this WeightMap multiplied by the coefficient. This does not change the
    * probability distribution induced over the Aztec diamonds.
    */
  def normalize(coefficient: QRoot): CustomComputePartitionFunctionWeight = {

    val newWeights = new CustomComputePartitionFunctionWeight(n)
    Face
      .activeFaces(n)
      .flatMap(_.dominoes)
      .toList
      .foreach((domino: Domino) => newWeights(domino) = apply(domino) * coefficient)
    newWeights
  }

  def normalizeDenominator: CustomComputePartitionFunctionWeight =
    normalize(
      QRoot(
        Face
          .activeFaces(n)
          .flatMap(_.dominoes)
          .map(apply)
          .filter(_.isInstanceOf[Rational])
          .map(_.asInstanceOf[Rational])
          .map(_.denominator)
          .fold(BigInt(0))(IntegerMethods.euclidGCD),
        1
      )
    )

}
