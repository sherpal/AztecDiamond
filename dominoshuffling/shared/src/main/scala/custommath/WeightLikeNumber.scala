package custommath

import scala.math.Numeric.DoubleIsFractional
import scala.language.implicitConversions

/**
 * When executing the algorithm, we need weights that behave like Fractional with three constants: 0, 1 and 1/sqrt(2).
 */
trait WeightLikeNumber[T] extends Fractional[T] {

  def zero: T

  def one: T

  def oneOverRoot2: T

  implicit def fromInt(x: Int): T

}


object WeightLikeNumber {

  /**
   * Double obviously satisfy these conditions.
   */
  implicit object DoubleIsWeightLikeNumber extends DoubleIsFractional with WeightLikeNumber[Double] {

    override def zero: Double = 0.0

    override def one: Double = 1.0

    private val _oneOverRoot2: Double = 1 / math.sqrt(2)

    override def oneOverRoot2: Double = _oneOverRoot2

    override def compare(x: Double, y: Double): Int = x compare y

  }


}