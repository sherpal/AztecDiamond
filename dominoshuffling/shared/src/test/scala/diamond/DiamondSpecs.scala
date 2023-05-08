package diamond

import diamond.diamondtypes.*
import utils.Platform

final class DiamondSpecs extends munit.FunSuite {

  test("The only sub diamond of a full horizontal is another full horizontal") {
    val maxOrder = utils.Platform.platform match
      case Platform.JS  => 50
      case Platform.JVM => 100
    for (order <- 2 to maxOrder) {
      val thisDiamond     = UniformDiamond.countingTilingDiamond(order *: EmptyTuple)
      val previousDiamond = UniformDiamond.countingTilingDiamond((order - 1) *: EmptyTuple)

      assertEquals(thisDiamond.subDiamonds.toList, List(previousDiamond))
    }
  }

  test("Constructed diamonds have the right number of dominoes") {
    def checkRightCounts(diamondType: DiamondType)(args: Iterable[diamondType.ArgType]) = args
      .filter(arg => diamondType.transformArguments(diamondType.transformArgumentsBack(arg)).isRight)
      .map(diamondType.withArgs)
      .foreach { dt =>
        val diamond = dt.countingTilingDiamond
        val order   = dt.diamondOrder
        assertEquals(diamond.dominoesNumber, order * (order + 1))
        assertEquals(diamond.order, order)
        diamond.listOfDominoes.foreach { domino =>
          assert(domino != null)
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
    } yield (inner, outer))
  }

}
