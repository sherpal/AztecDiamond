package custommath

import narr.NArray

final class QRootSpecs extends munit.FunSuite {

  val qRootIsWeight = summon[WeightLikeNumber[QRoot]]

  def assertArraysEquals[T](left: NArray[T], right: NArray[T]) = assertEquals(left.toList, right.toList)

  test("(1 + sqrt(2))^2  gets reduced to 3 + 2sqrt(2)") {

    val n                   = NotRational(NArray[(BigInt, BigInt)]((1, 1), (1, 2)), NArray[(BigInt, BigInt)]((1, 1)))
    val square: NotRational = n * n

    assertArraysEquals(square.numerators, NArray[(BigInt, BigInt)]((3, 1), (2, 2)))
  }

}
