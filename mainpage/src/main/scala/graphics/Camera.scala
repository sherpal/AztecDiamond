package graphics

import custommath.Complex

class Camera(canvas2D: Canvas2D) {

  /** Change coordinates between a world coordinate and local coordinate for the canvas.
    *
    * Recall that canvas coordinates increase to the right and to the bottom. World coordinates increase to the right
    * and to the top.
    */
  def worldToLocal(z: Complex): (Double, Double) = {
    val z0 = z - worldCenter
    (width / 2 + z0.re * scaleX, height / 2 - z0.im * scaleY)
  }

  /** Camera coordinates
    */
  def width: Int  = canvas2D.width
  def height: Int = canvas2D.height

  /** World coordinates
    */
  var worldCenter: Complex = Complex(0, 0)
  var worldWidth: Double   = canvas2D.width
  var worldHeight: Double  = canvas2D.height

  def left: Double                   = worldCenter.re - worldWidth / 2
  def right: Double                  = worldCenter.re + worldWidth / 2
  def bottom: Double                 = worldCenter.im - worldHeight / 2
  def top: Double                    = worldCenter.im + worldHeight / 2
  def cameraBoundingBox: BoundingBox = new BoundingBox(left, top, right, bottom)

  /** World coordinates are multiplied by the scale to know its size in pixel.
    *
    * Example: The canvas is of size 400*300, and the world width is 800. Then, things must be drawn twice as small on
    * the horizontal axis.
    */
  def scaleX: Double = width / worldWidth
  def scaleY: Double = height / worldHeight

  def setScaleX(x: Double): Unit =
    worldWidth = width / x
  def setScaleY(y: Double): Unit =
    worldHeight = height / y
  def setScale(s: Double): Unit = {
    setScaleX(s)
    setScaleY(s)
  }

  def inView(center: Complex, boundingBox: BoundingBox): Boolean =
    center.re + boundingBox.left <= right && center.re + boundingBox.right >= left &&
      center.im + boundingBox.bottom <= top && center.im + boundingBox.top >= bottom

  def drawSprite(sprite: Sprite): Unit = {
    val (x, y) = worldToLocal(sprite.worldPos + Complex(-sprite.worldWidth / 2, sprite.worldHeight / 2))
    val width  = sprite.worldWidth * scaleX
    val height = sprite.worldHeight * scaleY
    sprite.draw(canvas2D, x, y, width, height)
  }

  def spriteDrawsItself(sprite: Sprite): Unit =
    sprite.drawWithCamera(canvas2D, this)

}
