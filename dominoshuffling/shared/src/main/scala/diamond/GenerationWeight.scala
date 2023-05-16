package diamond

import exceptions.WrongOrderException
import geometry.{Domino, Face, Point}
import narr.NArray

trait GenerationWeight extends WeightTrait[Double] {

  /** Generates an aztec diamond according to the weights, given an already computed diamond of order diamondOrder - 1.
    */
  def generateDiamond(subDiamond: Diamond): Diamond = {
    val dominoes = Diamond.emptyArrayDominoes(n)

    Face.activeFaces(n).foreach { face =>
      val newDominoes = face.nextDiamondConstruction(subDiamond, this)
      newDominoes.foreach { domino =>
        val (x, y) = Domino.changeVerticalCoordinates(domino.p1, n)
        dominoes(x)(y) = Some(domino)
      }
    }

    Diamond(dominoes)
  }

  /** Generates an aztec diamond of order 1.
    */
  def generateOrderOneDiamond: Diamond = n match {
    case 1 =>
      val dominoes = Face(Point(0, 0)).randomSquare(this)
      val (d1, d2) = (dominoes.head, dominoes.tail.head)
      if (d1.isHorizontal) {
        new Diamond(
          NArray(
            NArray[Option[Domino]](Some(d1), Option(d2)),
            NArray[Option[Domino]](Option.empty[Domino], Option.empty[Domino])
          )
        )
      } else {
        new Diamond(
          NArray(
            NArray(Some(d1), None),
            NArray(Some(d2), None)
          )
        )
      }
    case _ =>
      throw new WrongOrderException(s"This WeightMap object is for order $n, not 1.")
  }

}
