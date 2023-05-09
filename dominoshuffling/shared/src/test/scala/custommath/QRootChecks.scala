package custommath

import org.scalacheck.Properties

import narr.NArray
import org.scalacheck.*
import org.scalacheck.Prop.*

object QRootChecks extends Properties("QRootChecks") {

  val qRootIsWeight = summon[WeightLikeNumber[QRoot]]
  val one           = qRootIsWeight.one
  val zero          = qRootIsWeight.zero

  val signGen = Gen.oneOf(true, false).map(isPos => if isPos then 1 else -1)

  val rationalGen: Gen[Rational] = for {
    num  <- Gen.choose[BigInt](0, 1000)
    den  <- Gen.choose[BigInt](1, 1000)
    sign <- signGen
  } yield Rational(sign * num, den)

  val notRationalGen: Gen[NotRational] = for {
    num       <- Gen.nonEmptyListOf(Gen.choose[BigInt](1, 10)).map(xs => NArray(xs.distinct.sorted: _*))
    numCoeffs <- Gen.listOfN(num.length, Gen.choose[BigInt](1, 10)).map(xs => NArray(xs: _*))
    den       <- Gen.nonEmptyListOf(Gen.choose[BigInt](1, 10)).map(xs => NArray(xs.distinct.sorted: _*))
    denCoeffs <- Gen.listOfN(den.length, Gen.choose[BigInt](1, 10)).map(xs => NArray(xs: _*))
  } yield NotRational(numCoeffs.zip(num), denCoeffs.zip(den))

  val qRootGen: Gen[QRoot] = Gen.frequency(
    1 -> Gen.const(one),
    1 -> Gen.const(zero),
    5 -> rationalGen,
    5 -> notRationalGen
  )

  property("Going from integer is deterministic") = forAll(Arbitrary.arbitrary[BigInt]) { b =>
    QRoot.fromBigInt(b) == QRoot.fromBigInt(b)
  }

  property("one is generated") = exists(qRootGen)(_ == one)
  property("zero is generated") = exists(qRootGen)(_ == zero)
  property("rational is generated") = exists(qRootGen)(_.isRational)
  property("not rational is generated") = exists(qRootGen)(!_.isRational)

  property("one is neutral for *") = forAll(qRootGen)(q => q * one == q && one * q == q)
  property("zero absorb for *") = forAll(qRootGen)(q => q * zero == zero && zero * q == zero)
  property("* is commutative") = forAll(qRootGen, qRootGen)((x, y) => x * y == y * x)
  property("* is associative") = forAll(qRootGen, qRootGen, qRootGen)((x, y, z) => (x * y) * z == x * (y * z))
  property("x/x is one") = forAll(qRootGen)(q => q == zero || q / q == one)

  property("zero is neutral for +") = forAll(qRootGen)(q => q + zero == q && zero + q == q)
  property("+ is commutative") = forAll(qRootGen, qRootGen)((x, y) => x + y == y + x)
  property("+ is associative") = forAll(qRootGen, qRootGen, qRootGen)((x, y, z) => (x + y) + z == x + (y + z))
  property("x - x is zero") = forAll(qRootGen)(q => q - q == zero)

  property("distributivity") = forAll(qRootGen, qRootGen, qRootGen) { (a, x, y) =>
    a * (x + y) == a * x + a * y
  }

}
