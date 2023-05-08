package custommath

import narr.NArray

final class QRootSpecs extends munit.FunSuite {

  val qRootIsWeight = summon[WeightLikeNumber[QRoot]]

  test("(1 + sqrt(2))^2  gets reduced to 3 + 2sqrt(2)") {

    val n                   = NotRational(NArray[(BigInt, BigInt)]((1, 1), (1, 2)), NArray[(BigInt, BigInt)]((1, 1)))
    val square: NotRational = n * n

    assertEquals(square.numerators.toList, NArray[(BigInt, BigInt)]((3, 1), (2, 2)).toList)
  }

  test("1/sqrt(2) equals sqrt(2) / 2") {
    assertEquals(
      qRootIsWeight.oneOverRoot2,
      NotRational(NotRational.intCoefficientsArray(1 -> 2), NotRational.intCoefficientsArray(2 -> 1))
    )
  }

}
