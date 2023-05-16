package custommath

final class IntegerMethodsSpecs extends munit.FunSuite {
  import IntegerMethods.*

  test("2, 3, 5, 7, 11, 13, 17, 19 are all primes") {
    List(2, 3, 5, 7, 11, 13, 17, 19).foreach(n => assert(isPrime(n), s"$n was not prime"))
  }

  test("-1 is not prime") {
    assert(!isPrime(-1))
  }

  test("1 is not prime") {
    assert(!isPrime(1))
  }

  test("All 100 first primes are indeed primes") {
    primeNumbers.take(100).foreach(n => assert(isPrime(n), s"$n was not prime"))
  }

  test("There are 25 primes smaller than 100") {
    val primes = (1 to 100).map(BigInt(_)).filter(isPrime).toList
    assertEquals(primes.length, 25, s"I found ${primes.mkString(", ")}")
  }

  test("Binary decomposition of 0 is [0]") {
    assertEquals(binaryDecomposition(0).toList, List(0))
  }

  test("Binary decomposition of 1 is [1]") {
    assertEquals(binaryDecomposition(1).toList, List(1))
  }

  test("Binary decomposition of 2 is [1, 0]") {
    assertEquals(binaryDecomposition(2).toList, List(1,0))
  }

}
