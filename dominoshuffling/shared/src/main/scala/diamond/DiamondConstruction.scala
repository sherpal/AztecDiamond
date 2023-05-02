package diamond

import geometry.{Domino, Point}
import narr.NArray

class DiamondConstruction(val order: Int) {

  private val dominoes: NArray[NArray[Option[Domino]]] =
    Diamond.emptyArrayDominoes(order)

  def update(domino: Domino): Unit = {
    val (x, y) = Domino.changeVerticalCoordinates(domino.p1, order)
    dominoes(x)(y) = Some(domino)
  }

  def dominoesNumber: Int = dominoes.map(d => d.count(_.isDefined)).sum

  def inBoundsPoint(point: Point): Boolean =
    math.abs(point.x - 0.5) + math.abs(point.y - 0.5) <= order

  def inBoundsDomino(domino: Domino): Boolean =
    inBoundsPoint(domino.p1) && inBoundsPoint(domino.p2)

  def contains(domino: Domino): Boolean = inBoundsDomino(domino) && {
    val (x, y) = Domino.changeVerticalCoordinates(domino.p1, order)
    dominoes(x)(y) match {
      case Some(d) => d == domino
      case None    => false
    }
  }

  def isPointOccupied(point: Point): Boolean = List[Domino](
    Domino(point, point + Point(1, 0)),
    Domino(point + Point(-1, 0), point),
    Domino(point, point + Point(0, 1)),
    Domino(point + Point(0, -1), point)
  ).exists(contains)

  def possibleDominoesOn(point: Point): List[Domino] =
    if (isPointOccupied(point)) Nil
    else {
      point.adjacentPoints
        .filter(inBoundsPoint)
        .filter(!isPointOccupied(_))
        .map(p => if (p < point) Domino(p, point) else Domino(point, p))
    }

  def forcedDominoes: List[Domino] =
    DiamondConstruction
      .allPoints(order)
      .map(possibleDominoesOn)
      .filter(ds => ds.nonEmpty && ds.tail.isEmpty)
      .flatten
      .toList
      .distinct

  def fillForcedDominoes(): Unit = {
    var dominoesToFill = forcedDominoes
    while (dominoesToFill.nonEmpty) {
      dominoesToFill.foreach(this() = _)
      dominoesToFill = forcedDominoes
    }
  }

  def insertDiamond(diamond: Diamond, center: Point = Point(0, 0)): Unit =
    diamond.listOfDominoes
      .map { case Domino(p1, p2) => Domino(p1 + center, p2 + center) }
      .foreach(this() = _)

  def toDiamond: Diamond = new Diamond(dominoes)

}

object DiamondConstruction {

  def allPoints(order: Int): Iterable[Point] = (for {
    y <- 1 to order
    x <- -order + y to order + 1 - y
  } yield Point(x, y)).flatMap { case Point(x, y) =>
    List(Point(x, y), Point(x, -y + 1))
  }

}
