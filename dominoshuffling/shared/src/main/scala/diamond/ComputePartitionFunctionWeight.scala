package diamond

import custommath.QRoot

trait ComputePartitionFunctionWeight extends WeightTrait[QRoot] {

  final def subWeights: ComputePartitionFunctionWeight = subWeightsWithNotification(() => ())
  
  def subWeightsWithNotification(notification: () => Unit): ComputePartitionFunctionWeight

  def normalizeDenominator: ComputePartitionFunctionWeight

}
