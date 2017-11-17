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




//import exceptions.WrongColorIntException
//import org.scalajs.dom
//import org.scalajs.dom.html
//import org.scalajs.dom.raw.CanvasRenderingContext2D
//import pixigraphics.{PIXITexture, Sprite}

///**
// * A DominoSprite is a Sprite that is thought to be attached to a domino.
// */
//class DominoSprite private (texture: PIXITexture) {
//
//  val sprite: Sprite = new Sprite(texture)
//
//  sprite.anchor.set(0, 1)
//
//  def setColor(color: Int): Unit = {
//    if (color < 0 || color > 0xFFFFFF) {
//      throw new WrongColorIntException(s"$color must be comprised between 0 and ${0xFFFFF}")
//    }
//    sprite.tint = color
//  }
//
//  def width: Int = sprite.width.toInt
//
//  def height: Int = sprite.height.toInt
//
//  def setSize(width: Int, height: Int): Unit = {
//    texture.baseTexture.source.asInstanceOf[html.Canvas].width = width
//    texture.baseTexture.source.asInstanceOf[html.Canvas].height = height
//    texture.update()
//
//    println(width, height, sprite.width, sprite.height)
//  }
//
//
//
//
//}
//
//
//object DominoSprite {
//
//  def rgbToInt(red: Double, green: Double, blue: Double): Int = {
//    val r = (red * 255).toInt
//    val g = (green * 255).toInt
//    val b = (blue * 255).toInt
//    ((r * 256) + g) * 256 + b
//  }
//
//  def rgbToCSSColor(red: Double, green: Double, blue: Double): String =
//    s"rgb(${(red * 255).toInt},${(green * 255).toInt},${(blue * 255).toInt})"
//
////  val horizontalDominoGraphics: PIXIGraphics = new PIXIGraphics()
////    .beginFill(0xFFFFFF)
////    .drawRect(1, 1, 100, 50)
////    .endFill()
////
////  val verticalDominoGraphics: PIXIGraphics = new PIXIGraphics()
////    .beginFill(0xFFFFFF)
////    .drawRect(1, 1 ,50, 100)
////    .endFill()
//
//  private def dominoTexture(width: Int, height: Int): PIXITexture = {
//    val canvas: html.Canvas = dom.document.createElement("canvas").asInstanceOf[html.Canvas]
//    canvas.width = width
//    canvas.height = height
//    val ctx = canvas.getContext("2d").asInstanceOf[CanvasRenderingContext2D]
//    ctx.fillStyle = rgbToCSSColor(1, 1, 1)
//    ctx.fillRect(0, 0, width, height)
//    PIXITexture.fromCanvas(canvas)
//  }
//
//  def apply(dominoNumber: Int, width: Int, height: Int): Traversable[DominoSprite] = {
//    val texture = dominoTexture(width, height)
//    (1 to dominoNumber).map(_ => new DominoSprite(texture))
//  }
//
//  def apply(width: Int, height: Int): DominoSprite = apply(1, width, height).head
//
//}