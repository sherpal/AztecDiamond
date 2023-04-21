package ui

import custommath.Complex
import mainobject.Loader
import org.scalajs.dom
import org.scalajs.dom.html
import org.scalajs.dom._

object ColorPicker {

  val colorPickerBackground: html.Div =
    dom.document.getElementById("colorPickerBackground").asInstanceOf[html.Div]

  colorPickerBackground.onclick = (_: dom.MouseEvent) => hide()

  val colorPickerDiv: html.Div =
    dom.document.getElementById("colorPicker").asInstanceOf[html.Div]

  /** Converts an HSL color value to RGB. Conversion formula adapted from
    * http://en.wikipedia.org/wiki/HSL_color_space. Assumes h, s, and l are
    * contained in the set [0, 1] and returns r, g, and b in the set [0, 255].
    *
    * @param h
    *   The hue
    * @param s
    *   The saturation
    * @param l
    *   The lightness
    * @return
    *   The RGB representation
    */
  def hslToRgb(h: Double, s: Double, l: Double): (Int, Int, Int) = {

    val (r, g, b) = if (s == 0) {
      (l, l, l); // achromatic
    } else {
      def hue2rgb(p: Double, q: Double, t: Double): Double = {
        val tt = if (t < 0) t + 1 else if (t > 1) t - 1 else t
        if (tt < 1 / 6.0) p + (q - p) * 6 * tt
        else if (tt < 1 / 2.0) q
        else if (tt < 2 / 3.0) p + (q - p) * (2.0 / 3.0 - tt) * 6
        else p
      }

      val q = if (l < 0.5) l * (1 + s) else l + s - l * s
      val p = 2 * l - q
      (hue2rgb(p, q, h + 1 / 3.0), hue2rgb(p, q, h), hue2rgb(p, q, h - 1 / 3.0))
    }

    (
      math.round(r * 255).toInt,
      math.round(g * 255).toInt,
      math.round(b * 255).toInt
    )
  }

  private val canvas: html.Canvas =
    dom.document.getElementById("colorWheel").asInstanceOf[html.Canvas]
  private val ctx: CanvasRenderingContext2D =
    canvas.getContext("2d").asInstanceOf[CanvasRenderingContext2D]

  private val totalRadius: Int = 90
  // private val startingLuminosity: Double = 150

  private def luminosityFromRadius(radius: Double): Int =
    math.round((1 - math.sqrt(radius / 3 / totalRadius)) * 100).toInt
  // math.round((startingLuminosity - radius) / startingLuminosity * 100).toInt

  private def positionToHSL(circlePosition: Complex): (Int, Int, Int) = {
    val hue: Int = math.round(circlePosition.arg / (2 * math.Pi) * 360).toInt
    val luminosity: Int = luminosityFromRadius(circlePosition.modulus)

    (hue, 100, luminosity)
  }

  private val numberOfTurns: Int = 8
  private val pixelPositions = (for {
    radius <- 1 to totalRadius
    t <- 0 to 360 * numberOfTurns
  } yield {
    val angle = t / numberOfTurns.toDouble
    val z = radius * Complex.rotation(angle / 360.0 * 2 * math.Pi) + Complex(
      canvas.width / 2,
      canvas.height / 2
    )

    (radius, angle, z)
  }).grouped(1000).toList

  ctx.beginPath()
  ctx.moveTo(canvas.width / 2 + totalRadius, canvas.height / 2)
  ctx.arc(canvas.width / 2, canvas.height / 2, totalRadius, 0, 2 * math.Pi)
  ctx.closePath()
  ctx.lineWidth = 2
  ctx.strokeStyle = "black"
  ctx.stroke()

  Loader.load(
    pixelPositions.map(list =>
      () =>
        list.foreach({ case (radius, angle, z) =>
          ctx.fillStyle =
            s"hsl(${angle.toInt},100%,${luminosityFromRadius(radius)}%)"
          ctx.fillRect(z.re, z.im, 1, 1)
        })
    ) :+ (() => {
      ctx.beginPath()
      ctx.moveTo(canvas.width / 2 + totalRadius, canvas.height / 2)
      ctx.arc(canvas.width / 2, canvas.height / 2, totalRadius, 0, 2 * math.Pi)
      ctx.closePath()
      ctx.lineWidth = 2
      ctx.strokeStyle = "black"
      ctx.stroke()
    }),
    rate = 1
  )

  private val colorDiv: html.Div =
    dom.document.getElementById("chosenColor").asInstanceOf[html.Div]

  private def cursorToCirclePosition(
      clientX: Double,
      clientY: Double
  ): Complex = {
    val boundingRect = canvas.getBoundingClientRect()
    Complex(
      clientX - boundingRect.left - canvas.width / 2,
      clientY - boundingRect.top - canvas.height / 2
    )
  }

  canvas.onmousemove = (event: dom.MouseEvent) => {
    val circlePosition = cursorToCirclePosition(event.clientX, event.clientY)

    if (circlePosition.modulus < totalRadius) {
      canvas.style.cursor = "crosshair"
      val (h, s, l) = positionToHSL(circlePosition)
      colorDiv.style.backgroundColor = s"hsl($h,$s%,$l%)"
    } else {
      canvas.style.cursor = "default"
    }
  }

  /** Shows the Color Picker, that will call the colorCallback function with the
    * RGB code of selected color.
    */
  def show(
      mouseX: Double,
      mouseY: Double,
      defaultColor: (Int, Int, Int),
      colorCallback: ((Int, Int, Int)) => Unit
  ): Unit = {

    colorDiv.style.backgroundColor =
      s"rgb(${defaultColor._1},${defaultColor._2},${defaultColor._3})"

    colorPickerBackground.style.display = "block"

    colorPickerDiv.style.left = (mouseX.toInt + 5).toString + "px"
    colorPickerDiv.style.top = (mouseY.toInt + 5).toString + "px"

    canvas.onclick = (event: dom.MouseEvent) => {

      val circlePosition = cursorToCirclePosition(event.clientX, event.clientY)

      if (circlePosition.modulus < totalRadius) {
        val (h, s, l) = positionToHSL(circlePosition)
        val (r, g, b) =
          hslToRgb((if (h < 0) h + 360 else h) / 360.0, s / 100.0, l / 100.0)

        colorCallback((r, g, b))
        hide()
      }
    }

  }

  def hide(): Unit = {
    colorPickerBackground.style.display = "none"
  }

  dom.document
    .getElementById("colorPickerCancel")
    .asInstanceOf[html.Button]
    .onclick = (_: dom.MouseEvent) => {
    hide()
  }

}
