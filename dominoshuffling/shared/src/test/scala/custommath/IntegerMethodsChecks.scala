package custommath

import org.scalacheck.*
import org.scalacheck.Prop.*

object IntegerMethodsChecks extends Properties("IntegerMethods") {
  import IntegerMethods.*

  def abs(x: BigInt): BigInt = if x < 0 then -x else x

  val bigIntGen: Gen[BigInt] = Arbitrary.arbitrary[BigInt]
  val smallBigIntGen: Gen[BigInt] =
    Gen.frequency(10 -> Gen.choose[BigInt](-1000000, 1000000), 1 -> Gen.oneOf(BigInt(1), BigInt(0), BigInt(-1)))

  val smallPositiveBigIntGen: Gen[BigInt] =
    Gen.frequency(10 -> Gen.choose[BigInt](1, 1000000), 1 -> Gen.const(BigInt(1)))

  def primeUpToGen(n: BigInt): Gen[BigInt] = Gen.oneOf(primeNumbers.takeWhile(_ <= n).toList)

  property("Product of primes is the number") = forAll(smallBigIntGen) { x =>
    x == 0 || {
      val primes = primeNumberDecomposition(x)
      primes.product == x
    }
  }

  property("Product of primes start with -1 iff number is negative") = forAll(smallBigIntGen) { x =>
    val primes = primeNumberDecomposition(x)
    if x == 1 || x == 0 then Prop(primes == Nil)
    else
      primes match {
        case Nil       => falsified :| s"List of prime was empty for $x"
        case head :: _ => Prop((head == -1) == (x < 0))
      }
  }

  property("biggest square factorization") = forAll(smallBigIntGen) { x =>
    val (p, q) = biggestSquareDecomposition(x)

    x == p * abs(p) * q
  }

  property("biggest square factorization finds the biggest square") = forAll(smallBigIntGen) { x =>
    val (_, q) = biggestSquareDecomposition(x)

    val qPrimes = primeNumberDecomposition(q)

    qPrimes.groupBy(identity).map(_._2).toList.foldLeft(proved) { (proofStatus, occurrences) =>
      proofStatus && Prop(occurrences.length == 1)
    }
  }

  property("gcd of two primes is 1") = forAll(primeUpToGen(1000), primeUpToGen(1000)) { (p1, p2) =>
    p1 == p2 || euclidGCD(p1, p2) == 1
  }

  property("gcd of n and mn is n") = forAll(smallPositiveBigIntGen, smallPositiveBigIntGen) { (n, m) =>
    euclidGCD(n, m * n) == n
  }

  property("big square root of x^2 is abs(x)") = forAll(bigIntGen) { x =>
    bigIntSquareRoot(x * x) == abs(x)
  }

}
