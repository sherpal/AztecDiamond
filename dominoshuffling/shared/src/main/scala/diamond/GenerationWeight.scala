package diamond

import exceptions.WrongOrderException
import geometry.{Domino, Face, Point}


trait GenerationWeight extends WeightTrait[Double] {


  /**
   * Generates an aztec diamond according to the weights, given an already computed diamond of order diamondOrder - 1.
   */
  def generateDiamond(subDiamond: Diamond): Diamond = {

    val dominoes = Diamond.emptyArrayDominoes(n)

    Face.activeFaces(n).foreach(face => {
      val newDominoes = face.nextDiamondConstruction(subDiamond, this)
      newDominoes.foreach(domino => {
        val (x,y) = Domino.changeVerticalCoordinates(domino.p1, n)
        dominoes(x)(y) = Some(domino)
      })
    })

    new Diamond(dominoes.map(_.toVector).toVector)
  }

  /**
   * Generates an aztec diamond of order 1.
   */
  def generateOrderOneDiamond: Diamond = n match {
    case 1 =>
      val dominoes = Face(Point(0,0)).randomSquare(this)
      val (d1, d2) = (dominoes.head, dominoes.tail.head)
      if (d1.isHorizontal) {
        new Diamond(
          Vector(
            Vector(Some(d1), Some(d2)),
            Vector(None, None)
          )
        )
      } else {
        new Diamond(
          Vector(
            Vector(Some(d1), None),
            Vector(Some(d2), None)
          )
        )
      }
    case _ =>
      throw new WrongOrderException(s"This WeightMap object is for order $n, not 1.")
  }


}
