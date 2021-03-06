package graphics

import custommath.Vec3
import geometry.Domino

class EmptyLozengeSprite(domino: Domino, diamondOrder: Int, lineWidth: Int)
  extends LozengeSprite(domino, diamondOrder) {

  override def draw(canvas2D: Canvas2D, x: Double, y: Double, width: Double, height: Double): Unit = {
    canvas2D.drawVertices(vertices(x, y, width, height), Vec3(0,0,0),
      lineWidth = math.max(1, lineWidth), cycle = true)
  }

}
