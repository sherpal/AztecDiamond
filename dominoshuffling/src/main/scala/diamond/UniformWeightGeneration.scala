package diamond


import exceptions.{NoWeightChangesException, WrongOrderException}
import geometry.Domino

import scala.reflect.ClassTag

class UniformWeightGeneration(val n: Int)(implicit val tTag: ClassTag[Double]) extends GenerationWeight {

  def update(domino: Domino, weight: Double): Unit = throw new NoWeightChangesException

  /**
   * A Uniform Weight always returns 1.
   */
  def apply(domino: Domino): Double = 1.0

  def subWeights: UniformWeightGeneration = n match {
    case 1 =>
      throw new WrongOrderException("Can't compute WeightMap of order 0.")
    case _ =>
      new UniformWeightGeneration(n - 1)
  }

}
