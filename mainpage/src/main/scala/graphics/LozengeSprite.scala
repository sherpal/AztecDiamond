package graphics

import custommath.Complex
import geometry._

class LozengeSprite(val domino: Domino, diamondOrder: Int) extends Sprite {

  if (domino.isHorizontal) {
    setWorldSize(1.5, math.sqrt(3.0) / 2)

    val center = Complex(domino.p1.x + 1, domino.p1.y + 0.5)
    setWorldPos(Complex(center.re * 0.5, center.im * math.sqrt(3.0) / 2))
  } else {
    setWorldSize(1, math.sqrt(3.0))

    val center = Complex(domino.p1.x + 0.5, domino.p1.y + 1)
    setWorldPos(Complex(center.re * 0.5, center.im * math.sqrt(3.0) / 2))
  }

  def vertices(x: Double, y: Double, width: Double, height: Double): List[Complex] =
    domino.dominoType(diamondOrder) match {
    case EastGoing =>
      List(
        Complex(x + width / 2, y),
        Complex(x + width, y + height / 2),
        Complex(x + width / 2, y + height),
        Complex(x, y + height / 2)
      )
    case WestGoing =>
      List(
        Complex(x + width / 2, y),
        Complex(x + width, y + height / 2),
        Complex(x + width / 2, y + height),
        Complex(x, y + height / 2)
      )
    case SouthGoing =>
      List(
        Complex(x, y + height),
        Complex(x + width / 3, y),
        Complex(x + width, y),
        Complex(x + width * 2 / 3, y + height)
      )
    case NorthGoing =>
      List(
        Complex(x, y),
        Complex(x + width * 2 / 3, y),
        Complex(x + width, y + height),
        Complex(x + width / 3, y + height)
      )
  }

  def draw(canvas2D: Canvas2D, x: Double, y: Double, width: Double, height: Double): Unit = {

    canvas2D.drawVertices(vertices(x, y, width, height), color)

  }

}
