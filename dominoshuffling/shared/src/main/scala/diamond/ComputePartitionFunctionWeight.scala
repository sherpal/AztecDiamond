package diamond

import custommath.QRoot

trait ComputePartitionFunctionWeight extends WeightTrait[QRoot] {

  def subWeights: ComputePartitionFunctionWeight

}
