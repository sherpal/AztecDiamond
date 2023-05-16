package custommath

import narr.NArray

import scala.annotation.tailrec

object IntegerMethods {

  private def primesFrom(n: BigInt): LazyList[BigInt] =
    if isPrime(n) then n #:: primesFrom(n + 1) else primesFrom(n + 1)

  lazy val primeNumbers = primesFrom(2)

  private val precomputedPrimesNumber = 1000
  lazy val precomputedPrimes          = NArray[BigInt](primeNumbers.take(precomputedPrimesNumber).toList: _*)

  def bigIntPow(base: BigInt, exp: Long): BigInt = if exp == 0 then BigInt(1)
  else {
    val sqrt = bigIntPow(base, exp / 2)

    sqrt * sqrt * (if exp % 2 == 1 then base else 1)
  }

  def integerSquareRoot(n: Int): Int = {
    def integerSquareRootAcc(n: Int, start: Int): Int = {
      def iteration(x: Int): Int = (x + n / x) / 2
      val next                   = iteration(start)
      if (next == start) next
      else if (next > start) start
      else integerSquareRootAcc(n, next)
    }

    integerSquareRootAcc(n, n)
  }

  def longSquareRoot(n: Long): Long = {
    def longSquareRootAcc(n: Long, start: Long): Long = {
      def iteration(x: Long): Long = (x + n / x) / 2
      val next                     = iteration(start)
      if (next == start) next
      else if (next > start) start
      else longSquareRootAcc(n, next)
    }

    longSquareRootAcc(n, n)
  }

  def bigIntSquareRoot(n: BigInt): BigInt =
    if (n == 0) n
    else {
      def bigIntSquareRootAcc(n: BigInt, start: BigInt): BigInt = {
        def iteration(x: BigInt): BigInt = (x + n / x) / 2
        val next                         = iteration(start)
        if (next == start) next
        else if (next > start) start
        else bigIntSquareRootAcc(n, next)
      }

      bigIntSquareRootAcc(n, n)
    }

  def isPerfectSquare(n: Long): Boolean = {
    val sqrt = longSquareRoot(n)
    n == sqrt * sqrt
  }

  def isPerfectSquare(n: BigInt): Boolean = {
    val sqrt = bigIntSquareRoot(n)
    n == sqrt * sqrt
  }

  def euclidGCD(a: Long, b: Long): Long = if (b == 0) a else euclidGCD(b, a % b)

  def reduce(a: Long, b: Long): (Long, Long) = {
    val gcd = euclidGCD(a, b)
    (a / gcd, b / gcd)
  }

  def euclidGCD(a: BigInt, b: BigInt): BigInt = a gcd b

  def reduce(a: BigInt, b: BigInt): (BigInt, BigInt) =
    if (a == 0) (0, 1)
    else if (b == 0) (1, 0)
    else {
      val gcd = euclidGCD(a, b)
      (a / gcd, b / gcd)
    }

  def bigIntDiv(n1: BigInt, n2: BigInt): Double = {
    def divAcc(n1: BigInt, n2: BigInt, decimals: Vector[BigInt]): Double =
      if (decimals.length >= 18)
        decimals.zipWithIndex.map { case (d, i) => d.toDouble * math.pow(10, -i) }.sum
      else {
        divAcc((n1 % n2) * 10, n2, decimals :+ (n1 / n2))
      }

    divAcc(n1, n2, Vector())
  }

  /** Returns the prime number decomposition of the specified [[BigInt]].
    *
    *   - If the number is negative, then prepend -1 to the list.
    *   - For 0 or 1, returns the empty list.
    *   - For -1, returns List(-1)
    */
  def primeNumberDecomposition(n: BigInt): List[BigInt] = {
    val isPositive = n >= 0
    val x          = if isPositive then n else -n

    def primeNumberAcc(
        currentPrime: BigInt,
        primeAttemptIndex: Int,
        remainingToDecompose: BigInt,
        primesFound: List[BigInt]
    ): List[BigInt] =
      if remainingToDecompose <= 1 then if remainingToDecompose == 0 then Nil else primesFound
      else if currentPrime == 2 then {
        if remainingToDecompose % 2 == 0 then
          primeNumberAcc(currentPrime, primeAttemptIndex, remainingToDecompose / 2, 2 +: primesFound)
        else primeNumberAcc(3, 1, remainingToDecompose, primesFound)
      } else {
        if remainingToDecompose % currentPrime == 0 then
          primeNumberAcc(
            currentPrime,
            primeAttemptIndex,
            remainingToDecompose / currentPrime,
            currentPrime +: primesFound
          )
        else {
          val nextIndex = primeAttemptIndex + 1
          primeNumberAcc(
            if nextIndex < precomputedPrimesNumber then precomputedPrimes(nextIndex) else currentPrime + 2,
            nextIndex,
            remainingToDecompose,
            primesFound
          )
        }
      }

    val primes = primeNumberAcc(2, 0, x, Nil)

    if isPositive then primes else -1 +: primes
  }

  def positiveDivisers(n: BigInt): List[BigInt] = {
    val posN = if n < 0 then -n else n
    (if posN == 1 then Nil else List(n)) ++ (BigInt(1) to bigIntSquareRoot(posN)).filter(n % _ == 0).toList
  }

  def isPrime(n: BigInt): Boolean = n != 0 && (positiveDivisers(n) match {
    case _ :: _ :: Nil => true
    case _             => false
  })

  /** Given a [[BigInt]] n, returns to numbers p and q such that
    *
    *   - p * abs(p) * q == n
    *   - there is no square (except 1 and 0) dividing q
    */
  def biggestSquareDecomposition(n: BigInt): (BigInt, BigInt) = if n == 0 then (0, 0)
  else {
    val primes = primeNumberDecomposition(n)

    def findBiggestSquareAcc(
        remainingPrimes: List[BigInt],
        numbersForSquare: List[BigInt],
        numbersNotForSquare: Set[BigInt]
    ): (BigInt, BigInt) =
      remainingPrimes match {
        case Nil => (numbersForSquare.product, numbersNotForSquare.product)
        case head :: next =>
          if numbersNotForSquare.contains(head) then // we can form a new number for square
            findBiggestSquareAcc(next, head +: numbersForSquare, numbersNotForSquare - head)
          else // this will wait in the "not for square" bag
            findBiggestSquareAcc(next, numbersForSquare, numbersNotForSquare + head)
      }

    findBiggestSquareAcc(primes, Nil, Set.empty)
  }

  @tailrec
  def binaryDecomposition(n: BigInt): NArray[Int] = if n < 0 then binaryDecomposition(-n)
  else {
    @tailrec
    def accumulator(remaining: BigInt, acc: List[Int]): List[Int] = if remaining < 2 then remaining.toInt +: acc
    else {
      accumulator(remaining / 2, (remaining % 2).toInt +: acc)
    }

    NArray(accumulator(n, Nil): _*)
  }

}
