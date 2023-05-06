package custommath

final class IntegerMethodsSpecs extends munit.FunSuite {
  import IntegerMethods.*

  test("2, 3, 5, 7, 11, 13, 17, 19 are all primes") {
    List(2, 3, 5, 7, 11, 13, 17, 19).foreach(n => assert(isPrime(n), s"$n was not prime"))
  }

  test("-1 is not prime") {
    assert(!isPrime(-1))
  }

  test("All 100 first primes are indeed primes") {
    primesFrom(2).take(100).foreach(n => assert(isPrime(n), s"$n was not prime"))
  }

}
