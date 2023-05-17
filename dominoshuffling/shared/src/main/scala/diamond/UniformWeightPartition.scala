package diamond

import custommath.QRoot
import exceptions.{NoWeightChangesException, WrongOrderException}
import geometry.Domino

import scala.reflect.ClassTag

class UniformWeightPartition(val n: Int)(implicit val tTag: ClassTag[QRoot]) extends ComputePartitionFunctionWeight {

  def update(domino: Domino, weight: QRoot): Unit = throw new NoWeightChangesException

  private val _1: QRoot = QRoot.one

  /** A Uniform Weight always returns 1.
    */
  def apply(domino: Domino): QRoot = _1

  def subWeightsWithNotification(notification: () => Unit): UniformWeightPartition = n match {
    case 1 =>
      throw new WrongOrderException("Can't compute WeightMap of order 0.")
    case _ =>
      new UniformWeightPartition(n - 1)
  }

  def normalizeDenominator: UniformWeightPartition = this

}
