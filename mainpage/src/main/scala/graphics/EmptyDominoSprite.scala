package graphics

import custommath.{Complex, Vec3}
import geometry.Domino

class EmptyDominoSprite(domino: Domino, lineWidth: Int) extends DominoSprite(domino) {

  override def draw(canvas2D: Canvas2D, x: Double, y: Double, width: Double, height: Double): Unit =
    canvas2D.drawRectangle(Complex(x, y), width, height, color = Vec3(0, 0, 0), lineWidth = math.max(1, lineWidth))

}
