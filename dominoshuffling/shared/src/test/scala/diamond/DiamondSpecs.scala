package diamond

import diamond.diamondtypes.*
import utils.Platform

import scala.annotation.tailrec

final class DiamondSpecs extends munit.FunSuite {

  test("The only sub diamond of a full horizontal is another full horizontal") {
    val maxOrder = utils.Platform.platformValue(100, 50)
    for (order <- 2 to maxOrder) {
      val thisDiamond     = UniformDiamond.countingTilingDiamond(order *: EmptyTuple)
      val previousDiamond = UniformDiamond.countingTilingDiamond((order - 1) *: EmptyTuple)

      assertEquals(thisDiamond.subDiamonds.toList, List(previousDiamond))
    }
  }

  def checkRightCounts(diamondType: DiamondType)(args: Iterable[diamondType.ArgType]) = {
    def diamondTypeWithArgs = args
      .filter(arg => diamondType.transformArguments(diamondType.transformArgumentsBack(arg)).fold(_ => false, _ == arg))
      .map(diamondType.withArgs)

    test(s"Diamond have the right number of dominoes for ${diamondType.name}") {
      diamondTypeWithArgs.foreach { dt =>
        val diamond = dt.countingTilingDiamond
        val order   = dt.diamondOrder
        assertEquals(diamond.dominoesNumber, order * (order + 1))
      }
    }
    test(s"Computed order is the right order for ${diamondType.name}") {
      diamondTypeWithArgs.foreach { dt =>
        val diamond = dt.countingTilingDiamond
        val order   = dt.diamondOrder
        assertEquals(diamond.order, order)
      }
    }
    test(s"No dominoes are null in the list for ${diamondType.name}") {
      diamondTypeWithArgs.foreach { dt =>
        val diamond = dt.countingTilingDiamond
        diamond.listOfDominoes.foreach { domino =>
          assert(domino != null)
        }
      }
    }
    test(s"Argument transformation can roundtrip for ${diamondType.name}") {
      diamondTypeWithArgs.foreach { dt =>
        assert(
          Right(dt.args) == dt.diamondType.transformArguments(dt.transformArgumentsBack),
          s"${dt.args} vs ${dt.diamondType.transformArguments(dt.transformArgumentsBack)}"
        )
      }
    }
    test(s"Diamond can be serialized and back for ${diamondType.name}") {
      diamondTypeWithArgs.foreach { dt =>

        def allSubWeights(weights: GenerationWeight): List[GenerationWeight] = {
          @tailrec
          def accumulator(currentWeights: GenerationWeight, acc: List[GenerationWeight]): List[GenerationWeight] =
            if currentWeights.order == 1 then currentWeights +: acc
            else accumulator(currentWeights.subWeights, currentWeights +: acc)

          accumulator(weights, Nil)
        }

        val allWeights    = allSubWeights(dt.makeGenerationWeight)
        val order1Weights = allWeights.head

        val finalDiamond = allWeights.tail.foldLeft(order1Weights.generateOrderOneDiamond)((diamond, weights) =>
          weights.generateDiamond(diamond)
        )

        val roundTripDiamond = Diamond.fromIntsSerialization(finalDiamond.toArray)

        assertEquals(roundTripDiamond, finalDiamond)

      }
    }
  }

  checkRightCounts(UniformDiamond)((1 to 100).map(_ *: EmptyTuple))

  checkRightCounts(AztecHouse)(for {
    h <- 1 to 50
    v <- 1 to 50
  } yield (h, v))

  checkRightCounts(AztecRing)(for {
    inner <- 1 to 50
    outer <- 1 to 50
    if outer > inner
  } yield (inner, outer))

  test("The UniformDiamond.countingTilingDiamond has only one sub diamond") {
    for (order <- 2 to 10) {
      assertEquals(UniformDiamond.countingTilingDiamond(order *: EmptyTuple).numberOfSubDiamonds, BigInt(1))
      assertEquals(UniformDiamond.countingTilingDiamond(order *: EmptyTuple).subDiamonds.length, 1)
    }
  }

  test("0-th indexed sub diamond of UniformDiamond.countingTilingDiamond is the previous one") {
    for (order <- 2 to 10) {
      val diamond    = UniformDiamond.countingTilingDiamond(order *: EmptyTuple)
      val subDiamond = diamond.indexedSubDiamond(0)
      assertEquals(subDiamond, UniformDiamond.countingTilingDiamond((order - 1) *: EmptyTuple))
    }
  }

  test("All sub diamonds of Aztec Ring (3,8) can be retrieved by the indexedSubDiamond method") {
    val diamond               = AztecRing.countingTilingDiamond((3, 8))
    val numberOfSubDiamonds   = diamond.numberOfSubDiamonds
    val subDiamonds           = diamond.subDiamonds
    val allIndexedSubDiamonds = (0 until numberOfSubDiamonds.toInt).map(BigInt(_)).map(diamond.indexedSubDiamond).toList

    assertEquals(numberOfSubDiamonds, BigInt(subDiamonds.length))
    assertEquals(allIndexedSubDiamonds.length, subDiamonds.length)
    assertEquals(allIndexedSubDiamonds.toSet.size, subDiamonds.length)
    assertEquals(allIndexedSubDiamonds.toSet, subDiamonds.toSet)
  }

  test("The aztec ring for counting tiling of the shape (3,8) has 4 sub diamonds") {
    var diamond = AztecRing.countingTilingDiamond((3, 8))

    def assertForThisDiamond(n: Int): Unit = {
      assertEquals(diamond.numberOfSubDiamonds, BigInt(n))
      assertEquals(diamond.subDiamonds.length, n)
      diamond = diamond.indexedSubDiamond(n - 1)
    }

    assertForThisDiamond(4)
    assertForThisDiamond(16)
    assertForThisDiamond(64)
    assertForThisDiamond(16)
    assertForThisDiamond(4)
    assertForThisDiamond(1)
    assertForThisDiamond(1)

    assertEquals(diamond.order, 1)

  }

}
