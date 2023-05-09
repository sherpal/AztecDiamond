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
