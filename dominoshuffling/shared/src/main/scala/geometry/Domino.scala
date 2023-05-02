package geometry

import scala.language.implicitConversions
import narr.NArray

/** Two adjacent [[Point]]s put together. They have to be adjacent, and sorted lexicographically. (p1 < p2)
  */
case class Domino(p1: Point, p2: Point) extends Ordered[Domino] {

  override def compare(that: Domino): Int = if (this.p1 != that.p1) this.p1.compare(that.p1)
  else this.p2.compare(that.p2)

  def verticalMove(n: Int): Domino = Domino(p1 + Point(0, n), p2 + Point(0, n))

  def horizontalMove(n: Int): Domino = Domino(p1 + Point(n, 0), p2 + Point(n, 0))

  def isHorizontal: Boolean = p1.x != p2.x

  def isVertical: Boolean = p1.x == p2.x

  def dominoType(n: Int): DominoType = (isHorizontal, (p1.x + p1.y + n) % 2 == 0) match {
    case (true, true)   => NorthGoing
    case (true, false)  => SouthGoing
    case (false, true)  => EastGoing
    case (false, false) => WestGoing
  }

  def points: NArray[Point] = NArray(p1, p2)

}

object Domino {
  private inline def yMin(x: Int, n: Int): Int = if (x > 0) -n + x else -n + 1 - x

  private inline def xMin(y: Int, n: Int): Int = if (y > 0) -n + y else -n + 1 - y

  inline def changeHorizontalCoordinates(point: Point, n: Int): (Int, Int) =
    (point.x - xMin(point.y, n), point.y + n - 1)

  inline def changeVerticalCoordinates(point: Point, n: Int): (Int, Int) =
    (point.x + n - 1, point.y - yMin(point.x, n))

  private val pointsRegex    = """Domino\(Point\(-?\d+,-?\d+\),Point\(-?\d+,-?\d+\)\)""".r
  private val separatorComma = """\),P""".r

  implicit def fromString(stringDomino: String): Domino = {
    val points = separatorComma.split(pointsRegex.findFirstIn(stringDomino).get.drop(7).dropRight(1))

    Domino(points(0) + ")", "P" + points(1))
  }

  implicit class StringToDomino(val str: String) {
    def toDomino: Domino = str
  }

}
