package graphics

import custommath.{Complex, Vec4}


/**
 * A Sprite will be used to draw dominoes, or more fancy shapes (like lozenges) to the canvas.
 */
trait Sprite {

  /**
   * Draws the Sprite.
   * The draw method must use the Canvas2D API coordinate system, ie abscissas growing from left to right, and
   * ordinates from top to bottom.
   *
   * @param canvas2D Canvas2D to use to draw the Sprite
   * @param x        Top left abscissa position of the Bounding Box
   * @param y        Top left ordinate position of the Bounding Box
   * @param width    Width of the Bounding Box
   * @param height   Height of the BoundingBox
   */
  def draw(canvas2D: Canvas2D, x: Double, y: Double, width: Double, height: Double): Unit

  /**
   * The World position of the Sprite must be the center of the BoundingBox.
   */
  private var _worldPos: Complex = 0

  def setWorldPos(z: Complex): Unit =
    _worldPos = z

  def worldPos: Complex = _worldPos


  private var _worldWidth: Double = 1

  def setWorldWidth(width: Double): Unit =
    _worldWidth = width

  def worldWidth: Double = _worldWidth


  private var _worldHeight: Double = 1

  def setWorldHeight(height: Double): Unit =
    _worldHeight = height

  def worldHeight: Double = _worldHeight


  def setWorldSize(width: Double, height: Double): Unit = {
    setWorldWidth(width)
    setWorldHeight(height)
  }


  def boundingBox: BoundingBox = {
    val radius = math.sqrt(worldWidth * worldWidth + worldHeight * worldHeight) / 2
    new BoundingBox(-radius, -radius, radius, radius)
  }


  private var _color: Vec4 = Vec4(1,1,1,1)

  def setColor(red: Double, green: Double, blue: Double, alpha: Double = 1.0): Unit = {
    setColor(Vec4(red, green, blue, alpha))
  }

  def setColor(rgb: Vec4): Unit = {
    def clampTo01(x: Double): Double = math.min(1, math.max(x, 0))
    _color = rgb.map(clampTo01)
  }

  def color: Vec4 = _color

  def cssColor: String = _color.toCSSColor

}
