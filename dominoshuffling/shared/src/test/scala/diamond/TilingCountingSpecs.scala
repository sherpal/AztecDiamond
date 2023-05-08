package diamond

import custommath.QRoot
import diamond.diamondtypes.*
import geometry.Face

final class TilingCountingSpecs extends munit.FunSuite {

  test("Counting of uniform diamond must fit the formula up to 100") {
    for (order <- 1 to 100) {
      val computed =
        UniformDiamond
          .countingTilingDiamond(order *: EmptyTuple)
          .probability(UniformDiamond.makeComputationWeight(order *: EmptyTuple), _ => ())
      assertEquals(computed, 1 / UniformDiamond.theoreticTilingNumber(order))
    }
  }

  def aztecRingTest(args: AztecRing.ArgType, expected: BigInt): Unit = {
    val aztecRing = AztecRing.withArgs(args)
    val weights   = aztecRing.makeComputationWeight
    val diamond   = aztecRing.countingTilingDiamond

    val partition = 1 / aztecRing.countingTilingDiamond.probability(aztecRing.makeComputationWeight, _ => ())

    for (_ <- 1 to 100) {
      val left = 1 / aztecRing.countingTilingDiamond.probability(weights, _ => ())
      val right =  1 / aztecRing.countingTilingDiamond.probability(weights, _ => ())
      assertEquals(left, right)
    }
    assertEquals(aztecRing.totalPartitionFunctionToSubGraph(partition), QRoot.fromBigInt(expected))
  }

  test("Aztec ring values") {
    aztecRingTest((1, 2), 1)
    aztecRingTest((2, 5), 16)
  }

}
