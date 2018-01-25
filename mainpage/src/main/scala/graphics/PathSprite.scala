package graphics

import custommath.Complex
import geometry.Point

final class PathSprite(points: Vector[Point]) extends Sprite {

  setWorldPos(0)

  setWorldHeight(2 * points.map(_.y).map(math.abs).max)
  setWorldWidth(2 * points.map(_.x).map(math.abs).max)

  setColor(0,0,0)

  def draw(canvas2D: Canvas2D, x: Double, y: Double, width: Double, height: Double): Unit = {
    val xUnit = width / worldWidth
    val yUnit = height / worldHeight

    canvas2D.drawLine(
      points.map({ case Point(_x, _y) => Complex(x + xUnit * _x, y + yUnit * (_y + 0.5)) }),
      color = color,
      lineWidth = 5
    )

  }

  override def drawWithCamera(canvas2D: Canvas2D, camera: Camera): Unit = {
    canvas2D.drawLine(
      points.map({ case Point(x, y) => camera.worldToLocal(Complex(x, y + 0.5))}).map({ case (x, y) => Complex(x, y) }),
      color = color,
      lineWidth = 5
    )
  }

}
