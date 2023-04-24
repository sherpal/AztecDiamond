package graphics

import custommath._
import diamond.WeightTrait
import geometry.{Domino, Face}
import graphics.WeightDrawer.{ActiveFaceSprite, PointSprite, WeightSprite}

import scala.collection.Seq

class WeightDrawer(weights: WeightTrait[QRoot]) {

  val canvas2D: Canvas2D = new Canvas2D
  canvas2D.setSize(500, 500)
  canvas2D.transparent = false
  canvas2D.setBackgroundColor(Vec3(1, 1, 1))

  private val camera: Camera = new Camera(canvas2D)

  val topMost: Int    = weights.n + 1
  val rightMost: Int  = weights.n + 1
  val bottomMost: Int = -weights.n + 1
  val leftMost: Int   = -weights.n + 1

  val center: Complex = Complex(
    (rightMost + leftMost) / 2.0,
    (topMost + bottomMost) / 2.0
  )

  private val points: Seq[PointSprite] = Face
    .activeFaces(weights.n)
    .flatMap(_.dominoes)
    .flatMap(d => List(d.p1, d.p2))
    .map(p => PointSprite(p.x, p.y))

  private val dominoes: Seq[WeightSprite] = Face
    .activeFaces(weights.n)
    .flatMap(_.dominoes)
    .map(d => WeightSprite(d, weights(d)))

  private val activeFaces: Seq[ActiveFaceSprite] =
    Face.activeFaces(weights.n).map(ActiveFaceSprite.apply)
  private val activeFacesColor: Vec4 =
    if (weights.n % 2 == 0) Vec4(1, 0, 0, 0.2) else Vec4(0, 1, 0, 0.2)

  activeFaces.foreach(_.setColor(activeFacesColor))

  def draw(
      worldCenter: Complex = center,
      scaleX: Double = 1,
      scaleY: Double = 1
  ): Unit = {

    camera.worldCenter = worldCenter
    camera.worldWidth = (rightMost - leftMost) / scaleX
    camera.worldHeight = (topMost - bottomMost) / scaleY

    (activeFaces ++ points ++ dominoes).foreach(camera.drawSprite)
  }

}

object WeightDrawer {

  final private case class PointSprite(x: Int, y: Int) extends Sprite {

    val radius: Double = 3

    setWorldPos(Complex(x + 0.5, y + 0.5))
    setWorldSize(1, 1)

    def draw(
        canvas2D: Canvas2D,
        x: Double,
        y: Double,
        width: Double,
        height: Double
    ): Unit = {

      val centerX = x + width / 2
      val centerY = y + height / 2

      canvas2D.drawDisk(Complex(centerX, centerY), radius, Vec3(1, 0, 0))

    }

  }

  final private case class ActiveFaceSprite(face: Face) extends Sprite {

    import Complex.i

    val x: Int = face.bottomLeft.x
    val y: Int = face.bottomLeft.y

    setWorldPos(x + 1 + (y + 1) * i)
    setWorldSize(1, 1)

    def draw(
        canvas2D: Canvas2D,
        x: Double,
        y: Double,
        width: Double,
        height: Double
    ): Unit =
      canvas2D.drawRectangle(x + y * i, width, height, color)

  }

  final private case class WeightSprite(domino: Domino, weight: QRoot) extends Sprite {

    setWorldSize(
      if (domino.isHorizontal) 2 else 1,
      if (domino.isHorizontal) 1 else 2
    )

    if (domino.isHorizontal) {
      setWorldPos(Complex(domino.p1.x + 1, domino.p1.y + 0.5))
    } else {
      setWorldPos(Complex(domino.p1.x + 0.5, domino.p1.y + 1))
    }

    def draw(
        canvas2D: Canvas2D,
        x: Double,
        y: Double,
        width: Double,
        height: Double
    ): Unit = {

      val center = Complex(x + width / 2, y + height / 2)

      weight match {
        case weight: Rational =>
          val numWidth =
            canvas2D.textWidth(weight.numerator.toString, canvas2D.font)
          val denWidth =
            canvas2D.textWidth(weight.denominator.toString, canvas2D.font)

          canvas2D.print(
            List((weight.numerator.toString, Vec4(0, 0, 0, 1).toCSSColor)),
            center - numWidth / 2,
            width,
            height,
            font = "10px monospace"
          )

          if (weight.denominator != 1 && weight.numerator != 0) {
            canvas2D.print(
              List((weight.denominator.toString, Vec4(0, 0, 0, 1).toCSSColor)),
              center - denWidth / 2 + 12 * Complex.i,
              width,
              height,
              font = "10px monospace"
            )

            val lineLength = math.max(numWidth, denWidth)

            canvas2D.drawLine(
              List(
                center - lineLength / 2 + 6 * Complex.i,
                center + lineLength / 2 + 6 * Complex.i
              ),
              Vec3(0, 0, 0),
              lineWidth = 1
            )
          }
        case _: NotRational =>
        // TODO: useless for now, so I'll leave it for later.
      }

    }

  }

}
