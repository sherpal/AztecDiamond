package custommath

import scala.language.implicitConversions


/**
 * QRoot are numbers designed to handle weights that can appear during computations of weights.
 * The restriction is that all weights at the beginning must be rational or quotients of sums of square roots of natural
 * numbers.
 *
 * Note that even if all beginning weights are rational, it may happen at some point that we have to use 1/sqrt(2) and
 * therefore leaving rational numbers. After that, sums and quotients are made.
 * Most often that not, however, rational numbers are enough and this is why we make two distinct concrete classes.
 */
// TODO: optimize and clean all of this!
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

}

/**
 * The NotRational class is a QRoot that is not rational.
 * It is a sum of square root of integers divided by a sum of root of integers.
 * The Vectors numerators and denominators are composed of the squares of the elements in the sums.
 * The sign is +1 ou -1, and
 */
case class NotRational(numerators: Vector[BigInt], denominators: Vector[BigInt], sign: Int = 1)
  extends QRoot {

  def *(that: QRoot): QRoot = that match {
    case q: Rational =>
      val numSquare = q.numerator * q.numerator
      val denSquare = q.denominator * q.denominator
      NotRational(
        numerators.map(_ * numSquare), denominators.map(_ * denSquare), sign * (if (that.isPositive) 1 else -1)
      )
    case NotRational(num, den, s) =>
      NotRational(NotRational.vectorProduct(num, numerators), NotRational.vectorProduct(den, denominators), sign * s)
  }

  def +(that: QRoot): QRoot = {
    val q = that.toNotRational

    NotRational(
      (NotRational.vectorProduct(numerators, q.denominators) ++
        NotRational.vectorProduct(denominators, q.numerators)).sorted,
      NotRational.vectorProduct(denominators, q.denominators),
      sign * -q.sign
    )
  }

  def inverse: QRoot = NotRational(denominators, numerators)

  def isPositive: Boolean = sign > 0

  def unary_- : NotRational = NotRational(numerators, denominators, -sign)

  def toDouble: Double = numerators.map(n => math.sqrt(n.toDouble)).sum /
    denominators.map(n => math.sqrt(n.toDouble)).sum

  def toLong: Long = toDouble.toLong // TODO

  def toBigInt: BigInt = BigInt(toLong) // TODO

  def toInt: Int = toDouble.toInt

  def toNotRational: NotRational = this

  def toRational: Rational = Rational(
    numerators.map(IntegerMethods.bigIntSquareRoot).sum,
    denominators.map(IntegerMethods.bigIntSquareRoot).sum
  )


  def isRational: Boolean = numerators.forall(IntegerMethods.isPerfectSquare) &&
    denominators.forall(IntegerMethods.isPerfectSquare)


  override def equals(that: Any): Boolean = that match {
    case that: Int => isRational && (toRational equals QRoot.fromInt(that))
    case that: BigInt => isRational && (toRational equals QRoot.fromBigInt(that))
    case that: Rational => this.toRational equals that
    case that: NotRational => this.numerators.length == that.numerators.length &&
      this.denominators.length == that.denominators.length &&
      this.numerators.zip(that.numerators).forall(elem => elem._1 == elem._2) &&
      this.denominators.zip(that.denominators).forall(elem => elem._1 == elem._2)
    case _ => false
  }


  override def toString: String = "(" + numerators.map(n => s"v$n").mkString(" + ") + ") / (" +
    denominators.map(n => s"v$n").mkString(" + ") + ")"

}

object NotRational {

  private def vectorProduct(as: Vector[BigInt], bs: Vector[BigInt]): Vector[BigInt] = {
    (for {
      a <- as
      b <- bs
    } yield a * b).sorted
  }

  implicit def fromInt(n: Int): NotRational = NotRational(Vector(n * n), Vector(1))

}


class Rational(val numerator: BigInt, val denominator: BigInt) extends QRoot {

  def *(that: QRoot): QRoot = that match {
    case q: Rational =>
      val isPositive = (q.numerator * numerator > 0) == (q.denominator * denominator > 0)
      val (a, b) = IntegerMethods.reduce(QRoot.abs(q.numerator * numerator), QRoot.abs(q.denominator * denominator))
      new Rational(if (isPositive) a else -a, b)
    case notRational: NotRational =>
      notRational * this
  }

  def +(that: QRoot): QRoot = that match {
    case q: Rational =>
      val newNum = q.numerator * denominator + q.denominator * numerator
      val newDen = q.denominator * denominator
      val isPositive = (newNum > 0) == (newDen > 0)
      val (a, b) = IntegerMethods.reduce(QRoot.abs(newNum), QRoot.abs(newDen))
      new Rational(if (isPositive) a else -a, b)
    case notRational: NotRational =>
      notRational + this
  }

  def inverse: Rational = new Rational(denominator, numerator)

  def isPositive: Boolean = (numerator > 0) == (denominator > 0)

  def unary_- : Rational = new Rational(-numerator, denominator)

  def toDouble: Double = (BigDecimal(numerator) / BigDecimal(denominator)).toDouble

  def toLong: Long = (numerator / denominator).toLong

  def toBigInt: BigInt = numerator / denominator

  def toInt: Int = toBigInt.toInt

  def toNotRational: NotRational = NotRational(
    Vector(numerator * numerator), Vector(denominator * denominator), if (isPositive) 1 else -1
  )

  def toRational: Rational = this


  def isRational: Boolean = true

  override def toString: String = s"$numerator/$denominator"

  override def equals(that: Any): Boolean = that match {
    case that: Int => denominator == 1 && numerator == that
    case that: BigInt => denominator == 1 && numerator == that
    case that: NotRational => that.isRational && (that.toRational equals this)
    case that: Rational => that.numerator == this.numerator && that.denominator == this.denominator
    case _ => false
  }

  override def hashCode: Int = numerator.hashCode + denominator.hashCode

}

object Rational {

  def apply(num: BigInt, den: BigInt): Rational = {
    val isPositive = (num > 0) == (den > 0)
    val (a, b) = IntegerMethods.reduce(QRoot.abs(num), QRoot.abs(den))
    new Rational(if (isPositive) a else -a, b)
  }

  implicit def fromInt(n: Int): Rational = Rational(n, 1)

}


object QRoot {

  implicit def fromDouble(d: Double): QRoot = QRoot(d.toLong, 1)
  implicit def fromInt(n: Int): QRoot = Rational(n, 1)
  implicit def fromBigInt(n: BigInt): QRoot = Rational(n, 1)

  def fromRationalDouble(d: Double): QRoot = {
    def from(n: Long): Stream[Long] = n #:: from(10 * n)

    val exponent = from(1).find(n => math.round(n * d) == n * d).get

    QRoot(math.round(exponent * d), exponent)
  }

  def abs(n: BigInt): BigInt = if (n >= 0) n else -n

  def apply(num: BigInt, den: BigInt): QRoot = Rational.apply(num, den)

  import scala.language.implicitConversions


  implicit object QRootIsWeightLikeNumber extends WeightLikeNumber[QRoot] {
    override def zero: QRoot = QRoot(0, 1)

    override def one: QRoot = QRoot(1, 1)

    override def oneOverRoot2: QRoot = NotRational(Vector(1), Vector(2))

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

    override def compare(x: QRoot, y: QRoot): Int = java.lang.Double.compare(x.toDouble, y.toDouble)
  }


}