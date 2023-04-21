package custommath


object IntegerMethods {


  def integerSquareRoot(n: Int): Int = {
    def integerSquareRootAcc(n: Int, start: Int): Int = {
      def iteration(x: Int): Int = (x + n / x) / 2
      val next = iteration(start)
      if (next == start) next
      else if (next > start) start
      else integerSquareRootAcc(n, next)
    }

    integerSquareRootAcc(n, n)
  }

  def longSquareRoot(n: Long): Long = {
    def longSquareRootAcc(n: Long, start: Long): Long = {
      def iteration(x: Long): Long = (x + n / x) / 2
      val next = iteration(start)
      if (next == start) next
      else if (next > start) start
      else longSquareRootAcc(n, next)
    }

    longSquareRootAcc(n, n)
  }

  def bigIntSquareRoot(n: BigInt): BigInt = {
    if (n == 0) n else {
      def bigIntSquareRootAcc(n: BigInt, start: BigInt): BigInt = {
        def iteration(x: BigInt): BigInt = (x + n / x) / 2
        val next = iteration(start)
        if (next == start) next
        else if (next > start) start
        else bigIntSquareRootAcc(n, next)
      }

      bigIntSquareRootAcc(n, n)
    }
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

  def reduce(a: BigInt, b: BigInt): (BigInt, BigInt) = {
    if (a == 0) (0, 1)
    else if (b == 0) (1, 0)
    else {
      val gcd = euclidGCD(a, b)
      (a / gcd, b / gcd)
    }
  }


  def bigIntDiv(n1: BigInt, n2: BigInt): Double = {
    def divAcc(n1: BigInt, n2: BigInt, decimals: Vector[BigInt]): Double = {
      if (decimals.length >= 18)
        decimals.zipWithIndex.map({ case (d, i) => d.toDouble * math.pow(10, -i) }).sum
      else {
        divAcc((n1 % n2) * 10, n2, decimals :+ (n1 / n2))
      }
    }

    divAcc(n1, n2, Vector())
  }



}
