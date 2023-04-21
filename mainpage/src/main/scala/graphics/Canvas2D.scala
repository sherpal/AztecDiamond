package graphics

import custommath.{Complex, Matrix, Vec4}
import org.scalajs.dom
import org.scalajs.dom.html
import org.scalajs.dom.CanvasRenderingContext2D

/** Canvas objects are use to draw stuff on them. The [[html.Canvas]] element
  * created in the html file for actually printing on the screen. This class
  * uses the CanvasRenderingContext2D technology to draw on the canvas.
  */
class Canvas2D(val canvas: html.Canvas, ctx: CanvasRenderingContext2D) {

  /** Creates an instance of Canvas for a pre-existing html.Canvas element. /!\
    * Doing this will bind the context to the canvas, so it can't be done either
    * before or after.
    */
  def this(canvas: html.Canvas) =
    this(canvas, canvas.getContext("2d").asInstanceOf[CanvasRenderingContext2D])

  /** Creates an instance of Canvas with a newly created html.Canvas element.
    * /!\ This automatically binds the context to the canvas, so it can't be
    * done after creation.
    */
  def this() =
    this(dom.document.createElement("canvas").asInstanceOf[html.Canvas])

  /** Returns the width of the [[html.Canvas]] element attached to the Canvas.
    */
  def width: Int = canvas.width

  /** Sets the width of the [[html.Canvas]] element attached to the Canvas. */
  def setWidth(width: Int): Unit =
    canvas.width = width

  /** Returns the height of the [[html.Canvas]] element attached to the Canvas.
    */
  def height: Int = canvas.height

  /** Sets the height of the [[html.Canvas]] element attached to the Canvas. */
  def setHeight(height: Int): Unit =
    canvas.height = height

  /** Sets the width and height of the [[html.Canvas]] element attached to the
    * Canvas.
    */
  def setSize(width: Int, height: Int): Unit = {
    setWidth(width)
    setHeight(height)
  }

  private var _backgroundColor: Vec4 = Vec4(0, 0, 0, 0)
  def backgroundColor: Vec4 = _backgroundColor
  def setBackgroundColor(): Unit =
    setBackgroundColor(0, 0, 0, 0)
  def setBackgroundColor(v: Vec4): Unit =
    _backgroundColor = v
  def setBackgroundColor(
      r: Double,
      g: Double,
      b: Double,
      a: Double = 1.0
  ): Unit =
    setBackgroundColor(Vec4(r, g, b, a))

  var transparent: Boolean = true

  // def changeCoordinates(z: Complex): (Double, Double) = (z.re + canvas.width / 2, canvas.height / 2 - z.im)
  // The change of coordinates in no more needed, it is done by the camera.
  def changeCoordinates(z: Complex): (Double, Double) = (z.re, z.im)

  /** Sets a rectangle area that restrain the drawing area and apply body
    * instructions. We go from cartesian coordinates ((0,0) is at the center of
    * the canvas and y go up) to canvas coordinates ((0,0) at the top left and y
    * go down).
    *
    * @param x
    *   left pixel of area.
    * @param y
    *   top pixel of area.
    * @param width
    *   width of area, in pixels.
    * @param height
    *   height of area, in pixels.
    * @param body
    *   chunk of code to execute while using this particular scissor.
    */
  def withScissor[A](x: Double, y: Double, width: Double, height: Double)(
      body: => A
  ): A = {
    ctx.save()
    ctx.beginPath()
    val (locX, locY) = changeCoordinates(Complex(x, y + height))
    ctx.rect(locX, locY, width, height)
    ctx.clip()
    try body
    finally ctx.restore()
  }

  def drawRectangle(
      z: Complex,
      width: Double,
      height: Double,
      color: Vec4 = Vec4(1.0, 1.0, 1.0, 1.0),
      lineWidth: Int = 0
  ): Unit = {
    if (lineWidth == 0) {
      ctx.fillStyle = color.toCSSColor
      val (locX, locY) = changeCoordinates(z)
      ctx.fillRect(locX, locY, width, height)
    } else {
      ctx.lineWidth = lineWidth
      ctx.strokeStyle = color.toCSSColor
      val (locX, locY) = changeCoordinates(z)
      ctx.beginPath()
      // ctx.rect(locX, locY, width, height)
      ctx.moveTo(locX, locY)
      ctx.lineTo(locX + width, locY)
      ctx.lineTo(locX + width, locY + height)
      ctx.lineTo(locX, locY + height)
      ctx.closePath()
      ctx.stroke()
    }
  }

  def drawDisk(
      center: Complex,
      radius: Double,
      color: Vec4 = Vec4(1, 1, 1, 1),
      segments: Int = 20,
      lineWidth: Int = 0
  ): Unit = {
    ctx.beginPath()
    val (locX, locY) = changeCoordinates(center)
    ctx.arc(locX, locY, radius, 0, 2 * math.Pi)
    if (lineWidth == 0) {
      ctx.fillStyle = color.toCSSColor
      ctx.fill()
    } else {
      ctx.lineWidth = lineWidth
      ctx.strokeStyle = color.toCSSColor
      ctx.stroke()
    }
  }

  def drawEllipse(
      center: Complex,
      xRadius: Double,
      yRadius: Double,
      rotation: Double = 0,
      color: Vec4 = Vec4(1, 1, 1, 1),
      segments: Int = 20,
      lineWidth: Int = 2
  ): Unit = {
    val vertices = (for (j <- 0 to segments)
      yield center + (xRadius * math.cos(
        j * 2 * math.Pi / segments
      ) + Complex.i * (
        yRadius * math.sin(j * 2 * math.Pi / segments)
      )) * Complex.rotation(rotation)).toVector
    drawVertices(vertices, color, lineWidth)
  }

  def drawLine(
      vertices: Seq[Complex],
      color: Vec4 = Vec4(1, 1, 1, 1),
      lineWidth: Int = 2
  ): Unit = {
    ctx.lineWidth = lineWidth
    drawVertices(vertices, color, lineWidth)
  }

  def drawVertices(
      vertices: Seq[Complex],
      color: Vec4,
      lineWidth: Int = 0,
      cycle: Boolean = false
  ): Unit =
    if (vertices.nonEmpty && vertices.tail.nonEmpty) {
      val canvasSpaceVertices = vertices.map(changeCoordinates)
      ctx.beginPath()
      ctx.moveTo(canvasSpaceVertices.head._1, canvasSpaceVertices.head._2)
      canvasSpaceVertices.tail.foreach({ case (x, y) => ctx.lineTo(x, y) })
      if (lineWidth == 0) {
        ctx.closePath()
        ctx.fillStyle = color.toCSSColor
        ctx.fill()
      } else {
        if (cycle) {
          ctx.closePath()
        }
        ctx.lineWidth = lineWidth
        ctx.strokeStyle = color.toCSSColor
        ctx.stroke()
      }
    }

  def drawImage(
      image: html.Image,
      topLeft: Complex,
      width: Double,
      height: Double
  ): Unit = {
    val (dx, dy) = changeCoordinates(topLeft)
    ctx.drawImage(image, dx, dy, width, height)
  }

  def drawCanvas(
      canvas: html.Canvas,
      topLeft: Complex,
      width: Double,
      height: Double
  ): Unit = {
    val (dx, dy) = changeCoordinates(topLeft)
    ctx.drawImage(canvas, dx, dy, width, height)
  }

  def drawTexture(
      tex: html.Image,
      topLeft: Complex,
      width: Double,
      height: Double
  ): Unit = {
    val (dx, dy) = changeCoordinates(topLeft)
    ctx.drawImage(tex, dx, dy, width, height)
  }

  private var registeredFont = ctx.font
  private def setFont(font: String): Unit = if (font != registeredFont) {
    ctx.font = font
    registeredFont = font
  }
  def font: String = ctx.font

  def print(
      texts: Seq[(String, String)],
      z: Complex,
      width: Double,
      height: Double,
      xOffset: Double = 0,
      yOffset: Double = 0,
      font: String = "20px monospace",
      textAlign: String = "left",
      textBaseLine: String = "middle",
      alpha: Double = 1.0
  ): Unit = {
    ctx.font = font
    ctx.textAlign = textAlign
    ctx.textBaseline = textBaseLine

    var left: Double = 0
    val (x, y) = changeCoordinates(z)
    val (xOff, yOff) =
      (xOffset, yOffset) // yOffset grows when text must be lower
    texts.foreach({ case (text, color) =>
      ctx.fillStyle = color
      ctx.fillText(text, x + xOff + left, y + yOff)
      left += ctx.measureText(text).width
    })
  }

  def textWidth(text: String, font: String): Double = {
    setFont(font)
    ctx.measureText(text).width
  }

  def textWidth(c: Char, font: String): Double = textWidth(c.toString, font)

  // TODO: adapt the zoom
  def printWatermark(scaleX: Double, scaleY: Double): Unit = {
    val name = List(65, 110, 116, 111, 105, 110, 101, 32, 68, 111, 101, 114, 97,
      101, 110, 101)
      .map(_.toChar)
      .mkString
    val info = List(
      104, 116, 116, 112, 115, 58, 47, 47, 115, 105, 116, 101, 115, 46, 117, 99,
      108, 111, 117, 118, 97, 105, 110, 46, 98, 101, 47, 97, 122, 116, 101, 99,
      100, 105, 97, 109, 111, 110, 100, 47
    )
      .map(_.toChar)
      .mkString
//    val info = List(40, 73, 82, 77, 80, 32, 69, 110, 116, 101, 114, 116, 97, 105, 110, 109, 101, 110, 116, 41)
//      .map(_.toChar)
//      .mkString

    val font = "10px quicksand"

    val maxTextWidth = math.max(textWidth(name, font), textWidth(info, font))
    val textHeight = 20

    print(
      List((name, "#CCC")),
      Complex(width - textWidth(name, font) - 10, height - textHeight),
      maxTextWidth,
      textHeight / 2,
      font = font
    )

    print(
      List((info, "#CCC")),
      Complex(width - textWidth(info, font) - 10, height - textHeight / 2),
      maxTextWidth,
      textHeight / 2,
      font = font
    )
  }

  def clear(): Unit = {
    ctx.clearRect(0, 0, canvas.width, canvas.height)
    if (!transparent) {
      ctx.fillStyle = backgroundColor.toCSSColor
      ctx.fillRect(0, 0, canvas.width, canvas.height)
    }
  }

  // transformation matrix
  private val eye4: Matrix = Matrix.eye(4)
  private var _transformationMatrix: Matrix = eye4
  private var _storedTransformationMatrix: Matrix = eye4

  def resetTransformationMatrix(): Unit = {
    _transformationMatrix = eye4
    ctx.setTransform(1, 0, 0, 1, 0, 0)
  }

  /** Moves the center of the matrix transformation.
    * @param center
    *   The center of the rotation, in cartesian coordinate. 0 is the center of
    *   the canvas, and
    * -w/2 + h/2 i is the top left.
    * @param matrix
    *   The (3d) transformation matrix to apply.
    * @return
    *   The (3d) transformation matrix to give to "withTransform" method.
    */
  private def withCenter(center: Complex)(matrix: Matrix): Matrix = {
    val localCenter = Complex(width / 2.0 + center.re, height / 2.0 - center.im)
    translate(localCenter.re, localCenter.im) * matrix * translate(
      -localCenter.re,
      -localCenter.im
    )
  }

//  /**
//   * Rotates the shapes of angle radians in a counterclockwise direction.
//   *
//   * @param angle angle we want to rotate the shape, in radians.
//   * @param dim   dim can only be 3 for Canvas2D.
//   */
//  def rotate(angle: Double, dim: Int = 3): Unit = {
//    // need -angle since CanvasRenderingContext2D takes clockwise direction (which is non sense, by the way).
//    _transformationMatrix = Matrix.zRotation3d(-angle) * _transformationMatrix
//    setTransform()
//  }

  /** A matrix for canvas 2d rotation.
    *
    * @param center
    *   The center of the rotation, in cartesian coordinate. 0 is the center of
    *   the canvas, and
    * -w/2 + h/2 i is the top left.
    * @param angle
    *   The angle of the rotation. Positive value for counter clockwise
    *   rotation.
    * @return
    *   The (3d) transformation matrix to give to "withTransform" method.
    */
  def rotate(center: Complex, angle: Double): Matrix =
    withCenter(center)(Matrix.zRotation3d(-angle))

//  /** Scales according to x and y. The z scaling is always 1 for 2d canvases. */
//  def scale(sx: Double, sy: Double, sz: Double = 1): Unit = {
//    _transformationMatrix = Matrix.scaling3d(sx, sy, sz) * _transformationMatrix
//    setTransform()
//  }

  /** A matrix for canvas 2d scaling.
    *
    * @param center
    *   The center of the rotation, in cartesian coordinate. 0 is the center of
    *   the canvas, and
    * -w/2 + h/2 i is the top left.
    * @param sx
    *   Horizontal scaling.
    * @param sy
    *   Vertical scaling.
    */
  def scale(center: Complex, sx: Double, sy: Double): Matrix =
    withCenter(center)(Matrix.scaling3d(sx, sy, 1))

//  /** Translates the origin by dx, dy. The z translation is always 0 for 2d canvases. */
//  def translate(dx: Double, dy: Double, dz: Double = 0): Unit = {
//    _transformationMatrix = Matrix.translation3d(dx, dy, dz) * _transformationMatrix
//    setTransform()
//  }

  def translate(dx: Double, dy: Double): Matrix =
    Matrix.translation3d(dx, dy, 1)

  /** Stores the transformation matrix. */
  def storeTransformationMatrix(): Unit = {
    _storedTransformationMatrix = _transformationMatrix
  }

  /** Sets the transformation matrix to the stored matrix. */
  def restoreTransformationMatrix(): Unit = {
    _transformationMatrix = _storedTransformationMatrix
    setTransform()
  }

  private def setTransform(): Unit = {
    ctx.setTransform(
      _transformationMatrix(0, 0),
      _transformationMatrix(1, 0),
      _transformationMatrix(0, 1),
      _transformationMatrix(1, 1),
      _transformationMatrix(0, 3),
      _transformationMatrix(1, 3)
    )
  }

  def withTransformationMatrix[A](matrix: Matrix)(body: => A): A = {
    storeTransformationMatrix()
    _transformationMatrix = matrix * _transformationMatrix
    setTransform()
    try body
    finally resetTransformationMatrix()
  }

  def toDataURL: String = canvas.toDataURL("image/png")

}

object Canvas2D {
  def rgbToInt(red: Double, green: Double, blue: Double): Int = {
    val r = (red * 255).toInt
    val g = (green * 255).toInt
    val b = (blue * 255).toInt
    ((r * 256) + g) * 256 + b
  }

  def rgbToCSSColor(red: Double, green: Double, blue: Double): String =
    s"rgb(${(red * 255).toInt},${(green * 255).toInt},${(blue * 255).toInt})"

}
