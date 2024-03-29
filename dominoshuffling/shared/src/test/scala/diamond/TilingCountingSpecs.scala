package diamond

import custommath.{IntegerMethods, QRoot}
import diamond.diamondtypes.*
import geometry.Face

final class TilingCountingSpecs extends munit.FunSuite {

  test("Counting of uniform diamond must fit the formula up to 100") {
    val maxOrder = utils.Platform.platformValue(100, 50)
    for (order <- 1 to maxOrder) {
      val computed =
        UniformDiamond
          .countingTilingDiamond(order *: EmptyTuple)
          .probability(UniformDiamond.makeComputationWeight(order *: EmptyTuple), _ => (), _ => ())
      assertEquals(computed, 1 / UniformDiamond.theoreticTilingNumber(order))
    }
  }

  def dominoTypeCountTest(diamondType: DiamondType)(args: diamondType.ArgType, expected: BigInt): Unit = {
    val diamondTypeWithArgs = diamondType.withArgs(args)
    val probability = diamondTypeWithArgs.countingTilingDiamond.probability(
      diamondTypeWithArgs.makeComputationWeight,
      _ => (),
      _ => ()
    )

    val partition = 1 / probability

    assertEquals(diamondTypeWithArgs.totalPartitionFunctionToSubGraph(partition), QRoot.fromBigInt(expected))
  }

  test("Aztec ring values") {
    def aztecRingTest(args: AztecRing.ArgType, expected: BigInt): Unit = dominoTypeCountTest(AztecRing)(args, expected)
    aztecRingTest((1, 2), 1)
    aztecRingTest((2, 5), 16)
    aztecRingTest((3, 8), 4096)
  }

  test("Aztec house values") {
    def aztecHouseTest(args: AztecHouse.ArgType, expected: BigInt): Unit =
      dominoTypeCountTest(AztecHouse)(args, expected)
    aztecHouseTest(AztecHouse.transformArguments(List(1, 1)).fold(exc => throw exc, identity), 2)
    aztecHouseTest(AztecHouse.transformArguments(List(5, 5)).fold(exc => throw exc, identity), 21740032)
  }

  test("Rectangle n*2 is fibonacci n+1") {
    def rectangleTest(args: Rectangle.ArgType, expected: BigInt): Unit = dominoTypeCountTest(Rectangle)(args, expected)

    for (n <- 1 to 10) {
      rectangleTest((2, n), IntegerMethods.fibonacci(n + 1))
    }
  }
  
  test("Rectangle is symmetric") {
    for {
      width <- 1 to 10
      height <- 1 to 10
      if (width * height) % 2 == 0
    } {
      assertEquals(Rectangle.countTiling((width, height)), Rectangle.countTiling((height, width)), s"Countings not equals for ${(width, height)}")
    }
  }

}
