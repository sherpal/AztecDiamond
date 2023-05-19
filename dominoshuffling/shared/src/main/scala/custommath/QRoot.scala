package custommath

import scala.language.implicitConversions

import narr.NArray

/** QRoot are numbers designed to handle weights that can appear during computations of weights. The restriction is that
  * all weights at the beginning must be rational or quotients of sums of square roots of natural numbers.
  *
  * Note that even if all beginning weights are rational, it may happen at some point that we have to use 1/sqrt(2) and
  * therefore leaving rational numbers. After that, sums and quotients are made. Most often that not, however, rational
  * numbers are enough and this is why we make two distinct concrete classes.
  */
sealed abstract class QRoot {

  def *(that: QRoot): QRoot

  def /(that: QRoot): QRoot = this * that.inverse

  def +(that: QRoot): QRoot

  def -(that: QRoot): QRoot = this + (-that)

  def inverse: QRoot

  def isPositive: Boolean

  def unary_- : QRoot

  def toDouble: Double

  def toInt: Int

  def toLong: Long

  def toBigInt: BigInt

  def toNotRational: NotRational

  def toRational: Rational

  def isRational: Boolean

  final def **(n: Int): QRoot = if n == 0 then QRoot.one
  else {
    val sqrt = **(n / 2)
    if n % 2 == 0 then sqrt * sqrt
    else sqrt * sqrt * this
  }

}

/** The NotRational class is a QRoot that is not rational. It is a sum of square root of integers divided by a sum of
  * root of integers. The Vectors numerators and denominators are composed of the squares of the elements in the sums.
  * The sign is +1 ou -1, and
  */
case class NotRational private (
    numerators: NArray[(BigInt, BigInt)],
    denominators: NArray[(BigInt, BigInt)]
) extends QRoot {

  def *(that: QRoot): QRoot = that match {
    case q: Rational =>
      val num = q.numerator
      val den = q.denominator
      NotRational(
        numerators.map((c_x, x) => ((c_x * num, x))),
        denominators.map((c_x, x) => ((c_x * den, x)))
      )
    case that: NotRational => this * that
  }

  def *(that: NotRational): NotRational = NotRational(
    NotRational.vectorProduct(that.numerators, numerators),
    NotRational.vectorProduct(that.denominators, denominators)
  )

  def +(that: QRoot): NotRational = {
    val q = that.toNotRational

    NotRational(
      NotRational.vectorProduct(numerators, q.denominators) ++
        NotRational.vectorProduct(denominators, q.numerators),
      NotRational.vectorProduct(denominators, q.denominators)
    )
  }

  def inverse: QRoot = NotRational(denominators, numerators)

  def isPositive: Boolean = ???

  def unary_- : NotRational = NotRational(numerators.map((c, x) => (-c, x)), denominators)

  def toDouble: Double = numerators.map((c, n) => c.toDouble * math.sqrt(n.toDouble)).sum /
    denominators.map((c, n) => c.toDouble * math.sqrt(n.toDouble)).sum

  def toLong: Long = toDouble.toLong // TODO

  def toBigInt: BigInt = toRational.toBigInt

  def toInt: Int = toDouble.toInt

  def toNotRational: NotRational = this

  def toRational: Rational = Rational(
    numerators.map((c, x) => c * IntegerMethods.bigIntSquareRoot(x)).sum,
    denominators.map((c, x) => c * IntegerMethods.bigIntSquareRoot(x)).sum
  )

  def isRational: Boolean = numerators.forall((_, x) => IntegerMethods.isPerfectSquare(x)) &&
    denominators.forall((_, x) => IntegerMethods.isPerfectSquare(x))

  override def equals(that: Any): Boolean = that match {
    case that: Int         => isRational && (toRational equals QRoot.fromInt(that))
    case that: BigInt      => isRational && (toRational equals QRoot.fromBigInt(that))
    case that: Rational    => this.toRational equals that
    case that: NotRational => (this - that).toNotRational.numerators.length == 0
    case _                 => false
  }

  override def toString: String =
    "(" + numerators.map(n => s"v$n").mkString(" + ") + ") / (" +
      denominators.map(n => s"v$n").mkString(" + ") + ")"

}

object NotRational {

  def apply(
      numerators: NArray[(BigInt, BigInt)],
      denominators: NArray[(BigInt, BigInt)]
  ): NotRational = new NotRational(sanitizeFactorArray(numerators), sanitizeFactorArray(denominators))

  private[custommath] def sanitizeFactorArray(arr: NArray[(BigInt, BigInt)]): NArray[(BigInt, BigInt)] = {
    val biggestSquaresExtracted = arr.map { (c, x) =>
      val (p, q) = IntegerMethods.biggestSquareDecomposition(x)
      (c * p, q)
    }
    val sumTheGroups = NArray(biggestSquaresExtracted.groupBy(_._2).toSeq: _*).map { (x, coeffs) =>
      coeffs.map(_._1).sum -> x
    }

    sumTheGroups.filter((c, x) => c != 0 && x != 0).sortBy(_.swap)
  }

  def coefficientsArray(values: (BigInt, BigInt)*): NArray[(BigInt, BigInt)] = NArray(values: _*)
  def intCoefficientsArray(values: (Int, Int)*): NArray[(BigInt, BigInt)] = coefficientsArray(
    values.map((c, x) => BigInt(c) -> BigInt(x)): _*
  )

  private def vectorProduct(
      as: NArray[(BigInt, BigInt)],
      bs: NArray[(BigInt, BigInt)]
  ): NArray[(BigInt, BigInt)] = for {
    (c_a, a) <- as
    (c_b, b) <- bs
  } yield (c_a * c_b, a * b)

  implicit def fromInt(n: Int): NotRational =
    NotRational(NArray[(BigInt, BigInt)]((1, n * n)), NArray[(BigInt, BigInt)]((1, 1)))

}

final class Rational(val numerator: BigInt, val denominator: BigInt) extends QRoot {

  def *(that: QRoot): QRoot = that match {
    case q: Rational =>
      val isPositive =
        (q.numerator * numerator > 0) == (q.denominator * denominator > 0)
      val (a, b) = IntegerMethods.reduce(
        QRoot.abs(q.numerator * numerator),
        QRoot.abs(q.denominator * denominator)
      )
      new Rational(if (isPositive) a else -a, b)
    case notRational: NotRational =>
      notRational * this
  }

  def +(that: QRoot): QRoot = that match {
    case q: Rational =>
      val newNum     = q.numerator * denominator + q.denominator * numerator
      val newDen     = q.denominator * denominator
      val isPositive = (newNum > 0) == (newDen > 0)
      val (a, b)     = IntegerMethods.reduce(QRoot.abs(newNum), QRoot.abs(newDen))
      new Rational(if (isPositive) a else -a, b)
    case notRational: NotRational =>
      notRational + this
  }

  def inverse: Rational = new Rational(denominator, numerator)

  def isPositive: Boolean = (numerator > 0) == (denominator > 0)

  def unary_- : Rational = new Rational(-numerator, denominator)

  def toDouble: Double =
    (BigDecimal(numerator) / BigDecimal(denominator)).toDouble

  def toLong: Long = (numerator / denominator).toLong

  def toBigInt: BigInt = numerator / denominator

  def toInt: Int = toBigInt.toInt

  def toNotRational: NotRational = NotRational(
    NArray(numerator   -> BigInt(1)),
    NArray(denominator -> BigInt(1))
  )

  def toRational: Rational = this

  def isRational: Boolean = true

  override def toString: String = s"$numerator/$denominator"

  override def equals(that: Any): Boolean = that match {
    case that: Int         => denominator == 1 && numerator == that
    case that: BigInt      => denominator == 1 && numerator == that
    case that: NotRational => that.isRational && (that.toRational equals this)
    case that: Rational =>
      that.numerator == this.numerator && that.denominator == this.denominator
    case _ => false
  }

  override def hashCode: Int = numerator.hashCode + denominator.hashCode

}

object Rational {

  def apply(num: BigInt, den: BigInt): Rational = {
    val isPositive = (num > 0) == (den > 0)
    val (a, b)     = IntegerMethods.reduce(QRoot.abs(num), QRoot.abs(den))
    new Rational(if (isPositive) a else -a, b)
  }

  implicit def fromInt(n: Int): Rational = Rational(n, 1)

}

object QRoot {

  val one: QRoot  = QRoot(1, 1)
  val zero: QRoot = QRoot(0, 1)

  implicit def fromDouble(d: Double): QRoot = QRoot(d.toLong, 1)
  implicit def fromInt(n: Int): QRoot       = Rational(n, 1)
  implicit def fromBigInt(n: BigInt): QRoot = Rational(n, 1)

  def fromRationalDouble(d: Double): QRoot = {
    def from(n: Long): LazyList[Long] = n #:: from(10 * n)

    val exponent = from(1).find(n => math.round(n * d) == n * d).get

    QRoot(math.round(exponent * d), exponent)
  }

  def abs(n: BigInt): BigInt = if n >= 0 then n else -n

  def apply(num: BigInt, den: BigInt): QRoot = Rational.apply(num, den)

  def notRational(
      numerators: NArray[(BigInt, BigInt)],
      denominators: NArray[(BigInt, BigInt)]
  ): NotRational = NotRational(numerators, denominators)
  
  def sqrtOf(n: BigInt): QRoot = notRational(
    NotRational.coefficientsArray(BigInt(1) -> n),
    NotRational.intCoefficientsArray(1 -> 1)
  )

  import scala.language.implicitConversions

  given WeightLikeNumber[QRoot] = new WeightLikeNumber[QRoot] {
    override def zero: QRoot = QRoot.zero

    override def one: QRoot = QRoot.one

    override val oneOverRoot2: QRoot =
      NotRational(NArray[(BigInt, BigInt)](BigInt(1) -> BigInt(1)), NArray[(BigInt, BigInt)](BigInt(1) -> 2))

    override def plus(x: QRoot, y: QRoot): QRoot = x + y

    override def minus(x: QRoot, y: QRoot): QRoot = x - y

    override def times(x: QRoot, y: QRoot): QRoot = x * y

    override def div(x: QRoot, y: QRoot): QRoot = x / y

    override def negate(x: QRoot): QRoot = -x

    override def fromInt(x: Int): QRoot = QRoot(x, 0)

    override def toInt(x: QRoot): Int = x.toInt

    override def toLong(x: QRoot): Long = x.toLong

    override def toFloat(x: QRoot): Float = x.toDouble.toFloat

    override def toDouble(x: QRoot): Double = x.toDouble

    override def compare(x: QRoot, y: QRoot): Int =
      java.lang.Double.compare(x.toDouble, y.toDouble)

    def parseString(str: String): Option[QRoot] = None
  }

}
