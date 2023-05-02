package geometry

import narr.NArray

import scala.language.implicitConversions

/** A Point is a point of the square lattice.
  */
case class Point(x: Int, y: Int) extends Ordered[Point] {

  override def compare(that: Point): Int = if (this.x != that.x) this.x - that.x
  else this.y - that.y

  inline def +(that: Point): Point = Point(this.x + that.x, this.y + that.y)

  def toVectorCoordinate(diamondOrder: Int): (Int, Int) = {
    def yMin(x: Int) = if (x < 1) -diamondOrder + 1 - x else -diamondOrder + x
    (x + diamondOrder, y - yMin(x) + 1)
  }

  def adjacentPoints: NArray[Point] =
    NArray(this + Point(1, 0), this + Point(-1, 0), this + Point(0, 1), this + Point(0, -1))
}

object Point {

  implicit def fromString(stringPoint: String): Point =
    try {
      val coords = stringPoint.drop("Point(".length).dropRight(1).split(",").map(_.toInt)
      Point(coords(0), coords(1))
    } catch {
      case _: Throwable => throw new ClassCastException(stringPoint + " is not a valid Point")
    }

  implicit class StringToPoint(val str: String) {
    def toPoint: Point = str
  }

}
