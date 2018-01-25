package graphics

import custommath.Complex
import geometry.Domino


/**
 * A DominoSprite is a Sprite that is thought to be attached to a domino.
 */
class DominoSprite(val domino: Domino) extends Sprite {

  setWorldSize(
    if (domino.isHorizontal) 2 else 1,
    if (domino.isHorizontal) 1 else 2
  )

  if (domino.isHorizontal) {
    setWorldPos(Complex(domino.p1.x + 1, domino.p1.y + 0.5))
  } else {
    setWorldPos(Complex(domino.p1.x + 0.5, domino.p1.y + 1))
  }

  def draw(canvas2D: Canvas2D, x: Double, y: Double, width: Double, height: Double): Unit = {

    canvas2D.drawRectangle(Complex(x, y), width, height, color)

  }


}

