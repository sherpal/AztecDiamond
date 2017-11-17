package diamond

import exceptions.WrongOrderException
import geometry.Face

import scala.reflect.ClassTag

class CustomGenerationWeight(val n: Int)(implicit val tTag: ClassTag[Double])
  extends GenerationWeight with CustomWeightTrait[Double] {

  /**
   * Computes the Weights corresponding to the Diamond of order diamondOrder - 1
   */
  def subWeights: CustomGenerationWeight = if (n == 1)
    throw new WrongOrderException("Can't compute WeightMap of order 0.")
  else {

    val _0 = 0.0

    val newWeights = new CustomGenerationWeight(n - 1)
    Face.activeFaces(n).flatMap(face => {
      val (newPairs, newZeroes) = face.doubleSubWeights(this)

      for (
        (d, w) <- newPairs
        if newWeights.inBoundsDomino(d)
      ) {
        newWeights(d) = w
      }

      newZeroes
    }).filter(newWeights.inBoundsDomino).foreach(domino => newWeights(domino) = _0)

    newWeights

  }


}

object CustomGenerationWeight {
  /**
   * Returns true with probability p, and false with probability 1-p.
   */
  def nextBernoulli(p: Double): Boolean = scala.util.Random.nextDouble < p

}