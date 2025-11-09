package graphics

import custommath.Complex
import diamond.Diamond
import geometry._
import org.scalajs.dom.html
import org.scalajs.dom.CanvasRenderingContext2D
import diamond.DiamondType

/** Class helper for drawing Diamond on a canvas.
  * @param diamond
  *   the diamond to draw
  * @param isInSubGraph
  *   function indicating which dominoes have to be drawn.
  */
class DiamondDrawer private (
    val diamond: Diamond,
    isInSubGraph: Domino => Boolean,
    canvas: Option[(html.Canvas, CanvasRenderingContext2D)],
    drawWithWatermark: Boolean
) {

  private def isPointInSubGraph(point: Point): Boolean =
    isInSubGraph(Domino(point, point + Point(1, 0))) ||
      isInSubGraph(Domino(point, point + Point(0, 1))) ||
      isInSubGraph(Domino(point + Point(-1, 0), point)) ||
      isInSubGraph(Domino(point + Point(0, -1), point))

  lazy val canvas2D: Canvas2D = {
    val c = canvas match {
      case Some((c, ctx)) => new Canvas2D(c, ctx)
      case None           => new Canvas2D
    }
    c.setSize(2000, 2000)
    c
  }

  private val topMostFullDiamondCoordinate: Int    = diamond.order + 1
  private val rightMostFullDiamondCoordinate: Int  = diamond.order + 1
  private val bottomMostFullDiamondCoordinate: Int = -diamond.order + 1
  private val leftMostFullDiamondCoordinate: Int   = -diamond.order + 1

  private val diamondCenter: Complex = Complex(
    (rightMostFullDiamondCoordinate + leftMostFullDiamondCoordinate) / 2.0,
    (topMostFullDiamondCoordinate + bottomMostFullDiamondCoordinate) / 2.0
  )

  def dominoes: Iterable[Domino] = diamond.dominoes

  def dominoesInSubGraph: Iterable[Domino] = dominoes.filter(isInSubGraph)

  private val topMostSubDiamondCoordinate: Int = dominoesInSubGraph
    .foldLeft(bottomMostFullDiamondCoordinate) { case (curMax, domino) =>
      math.max(curMax, domino.p2.y + 1)
    }

  private val bottomMostSubDiamondCoordinate: Int = dominoesInSubGraph
    .foldLeft(topMostFullDiamondCoordinate) { case (curMin, domino) =>
      math.min(curMin, domino.p1.y)
    }

  private val rightMostSubDiamondCoordinate: Int = dominoesInSubGraph
    .foldLeft(leftMostFullDiamondCoordinate) { case (curMax, domino) =>
      math.max(curMax, domino.p2.x + 1)
    }

  private val leftMostSubDiamondCoordinate: Int = dominoesInSubGraph
    .foldLeft(rightMostFullDiamondCoordinate) { case (curMax, domino) =>
      math.min(curMax, domino.p1.x)
    }

  private val subDiamondCenter: Complex = Complex(
    (rightMostSubDiamondCoordinate + leftMostSubDiamondCoordinate) / 2.0,
    (topMostSubDiamondCoordinate + bottomMostSubDiamondCoordinate) / 2.0
  )

  private val camera: Camera = new Camera(canvas2D)

  def scale1FullDiamondSquareUnit: Int = math.min(
    canvas2D.width / (rightMostFullDiamondCoordinate - leftMostFullDiamondCoordinate),
    canvas2D.height / (topMostFullDiamondCoordinate - bottomMostFullDiamondCoordinate)
  )

  def scale1SubDiamondSquareUnit: Int = math.min(
    canvas2D.width / (rightMostSubDiamondCoordinate - leftMostSubDiamondCoordinate),
    canvas2D.height / (topMostSubDiamondCoordinate - bottomMostSubDiamondCoordinate)
  )

  private def dominoSprites: List[DominoSprite] =
    dominoes.map(DominoSprite(_)).toList

  private def emptyDominoSprites: List[EmptyDominoSprite] =
    dominoes.map(EmptyDominoSprite(_, canvas2D.width * 5 / 2000)).toList

  private def defaultColors(domino: Domino): (Double, Double, Double) =
    domino.dominoType(diamond.order) match {
      case NorthGoing => (1, 0, 0)
      case SouthGoing => (0, 0, 1)
      case EastGoing  => (0, 1, 0)
      case WestGoing  => (1, 1, 0)
    }

  private def rawDraw(
      worldCenter: Complex,
      scaleX: Double,
      scaleY: Double,
      colors: Domino => (Double, Double, Double),
      predicate: Domino => Boolean = _ => true,
      fullDiamond: Boolean = true,
      border: Boolean = false
  ): Unit = {

    camera.worldCenter = worldCenter

    val (worldWidth, worldHeight) = if (fullDiamond) {
      (
        (rightMostFullDiamondCoordinate - leftMostFullDiamondCoordinate) / scaleX,
        (topMostFullDiamondCoordinate - bottomMostFullDiamondCoordinate) / scaleY
      )
    } else {
      val size = math.max(
        rightMostSubDiamondCoordinate - leftMostSubDiamondCoordinate,
        topMostSubDiamondCoordinate - bottomMostSubDiamondCoordinate
      )
      (size / scaleX, size / scaleY)
    }

    camera.worldWidth = worldWidth
    camera.worldHeight = worldHeight

    (if (border) dominoSprites ++ emptyDominoSprites else dominoSprites)
      .filter(sprite => predicate(sprite.domino))
      .foreach { sprite =>
        val (r, g, b) = colors(sprite.domino)
        sprite.setColor(r, g, b)
        camera.drawSprite(sprite)
      }

  }

  def draw(
      worldCenter: Complex = diamondCenter,
      scaleX: Double = 1,
      scaleY: Double = 1,
      border: Boolean = false,
      colors: Domino => (Double, Double, Double) = defaultColors
  ): Unit =
    rawDraw(worldCenter, scaleX, scaleY, colors, border = border)

  def drawSubGraph(
      worldCenter: Complex = subDiamondCenter,
      scaleX: Double = 1,
      scaleY: Double = 1,
      border: Boolean = false,
      colors: Domino => (Double, Double, Double) = defaultColors
  ): Unit =
    rawDraw(
      worldCenter,
      scaleX,
      scaleY,
      colors,
      isInSubGraph,
      fullDiamond = false,
      border = border
    )

  private def lozengeSprites: List[LozengeSprite] =
    dominoes.map(new LozengeSprite(_, diamond.order)).toList

  private def emptyLozengeSprites: List[EmptyLozengeSprite] = dominoes
    .map(new EmptyLozengeSprite(_, diamond.order, canvas2D.width * 5 / 2000))
    .toList

  def drawAsLozenges(
      worldCenter: Complex = diamondCenter,
      scaleX: Double = 1,
      scaleY: Double = 1,
      border: Boolean = false,
      colors: Domino => (Double, Double, Double) = defaultColors
  ): Unit = {
    camera.worldCenter = worldCenter
    camera.worldWidth = (rightMostFullDiamondCoordinate - leftMostFullDiamondCoordinate) / scaleX / 2
    camera.worldHeight = (topMostFullDiamondCoordinate - bottomMostFullDiamondCoordinate) / scaleY * math
      .sqrt(3.0) / 2

    (if (border) lozengeSprites ++ emptyLozengeSprites else lozengeSprites)
      .foreach { sprite =>
        val (r, g, b) = colors(sprite.domino)
        sprite.setColor(r, g, b)
        camera.drawSprite(sprite)
      }

  }

  def drawSubGraphAsLozenges(
      worldCenter: Complex = subDiamondCenter,
      scaleX: Double = 1,
      scaleY: Double = 1,
      border: Boolean = false,
      colors: Domino => (Double, Double, Double) = defaultColors
  ): Unit = {
    camera.worldCenter = Complex(worldCenter.re / 2, worldCenter.im * math.sqrt(3.0) / 2)

    val cameraSize = math.max(
      (rightMostSubDiamondCoordinate - leftMostSubDiamondCoordinate + 1) / scaleX / 2,
      (topMostSubDiamondCoordinate - bottomMostSubDiamondCoordinate) / scaleY * math
        .sqrt(3.0) / 2
    )

    camera.worldWidth = cameraSize
    camera.worldHeight = cameraSize

    (if (border) lozengeSprites ++ emptyLozengeSprites else lozengeSprites)
      .filter(sprite => isInSubGraph(sprite.domino))
      .foreach { sprite =>
        val (r, g, b) = colors(sprite.domino)
        sprite.setColor(r, g, b)
        camera.drawSprite(sprite)
      }

  }

  private def globalPathsSprites: List[PathSprite] =
    diamond.nonIntersectingPaths.map(new PathSprite(_)).toList

  private def subGraphPathsSprites: List[PathSprite] =
    diamond
      .nonIntersectingPathsSubGraph(isPointInSubGraph)
      .map(new PathSprite(_))
      .toList

  def drawNonIntersectingPaths(
      worldCenter: Option[Complex] = None,
      scaleX: Double = 1,
      scaleY: Double = 1,
      subGraph: Boolean = true
  ): Unit = {
    camera.worldCenter = worldCenter.getOrElse(if subGraph then subDiamondCenter else diamondCenter)

    val (worldWidth, worldHeight) = if (!subGraph) {
      (
        (rightMostFullDiamondCoordinate - leftMostFullDiamondCoordinate) / scaleX,
        (topMostFullDiamondCoordinate - bottomMostFullDiamondCoordinate) / scaleY
      )
    } else {
      val size = math.max(
        rightMostSubDiamondCoordinate - leftMostSubDiamondCoordinate,
        topMostSubDiamondCoordinate - bottomMostSubDiamondCoordinate
      )
      (size / scaleX, size / scaleY)
    }

    camera.worldWidth = worldWidth
    camera.worldHeight = worldHeight

    (if (subGraph) subGraphPathsSprites else globalPathsSprites).foreach(
      camera.spriteDrawsItself
    )
  }

  private def applyTransformation(canvasToDrawTo: Canvas2D, rotation: Double, zoomX: Double, zoomY: Double): Unit = {
    canvasToDrawTo.clear()
    canvasToDrawTo.withTransformationMatrix(
      canvasToDrawTo.rotate(0, rotation) * canvasToDrawTo.scale(0, zoomX, zoomY)
    ) {
      canvasToDrawTo.drawCanvas(canvas2D.canvas, 0, canvasToDrawTo.canvas.width, canvasToDrawTo.canvas.height)
    }

    if !scala.scalajs.LinkingInfo.developmentMode && drawWithWatermark then {
      canvasToDrawTo.printWatermark(zoomX, zoomY)
    }
  }

  def drawOnCanvas(canvasToDrawTo: Canvas2D, options: DiamondDrawingOptions): Unit = {
    canvas2D.clear()
    val dominoColors = options.colors.asDoubleFunction(diamond.order)
    if options.drawDominoes then {
      if options.showInFullAztecDiamond && !options.drawDominoesAsLozenges then
        draw(border = options.showBorderOfDominoes, colors = dominoColors)
      else if !options.drawDominoesAsLozenges then
        drawSubGraph(border = options.showBorderOfDominoes, colors = dominoColors)
      else if options.showInFullAztecDiamond then
        drawAsLozenges(border = options.showBorderOfDominoes, colors = dominoColors)
      else drawSubGraphAsLozenges(border = options.showBorderOfDominoes, colors = dominoColors)
    }

    if options.drawNonIntersectingPaths then {
      drawNonIntersectingPaths(subGraph = !options.showInFullAztecDiamond)
    }

    val DiamondDrawingOptions.Transformations(rotation, zoom) = options.transformations
    applyTransformation(canvasToDrawTo, rotation * 2 * math.Pi / 360, zoom, zoom)
  }

  def tikzCode(unit: Double = 1): String = {
    val colors: Map[(Double, Double, Double), String] = Map(
      (1.0, 0.0, 0.0) -> "red",
      (0.0, 1.0, 0.0) -> "green",
      (0.0, 0.0, 1.0) -> "blue",
      (1.0, 1.0, 0.0) -> "yellow",
      (1.0, 0.0, 1.0) -> "purple",
      (0.0, 1.0, 1.0) -> "cyan"
    )

    dominoes
      .map { domino =>
        val color       = defaultColors(domino)
        val lowerLeftX  = domino.p1.x * unit
        val lowerLeftY  = domino.p1.y * unit
        val upperRightX = (domino.p2.x + 1) * unit
        val upperRightY = (domino.p2.y + 1) * unit
        s"\\draw [fill = ${colors(color)}, draw = black] " +
          s"($lowerLeftX,$lowerLeftY) rectangle ($upperRightX,$upperRightY);"
      }
      .mkString("\n") + "\n" +
      DiamondDrawer.emptyDiamondTikzCode(diamond.order, unit = unit)
  }

  def svgCode(
      diamondOrder: Int,
      diamondType: DiamondType.DiamondTypeWithArgs,
      options: DiamondDrawingOptions
  ): String = {
    val colorForDomino = options.colors.asCssFunction(diamondOrder)
    val withBorder     = options.showBorderOfDominoes

    val rotationAngle = -options.transformations.rotationInDegrees.toInt
    val zoom          = options.transformations.zoom

    val dominoesToDraw = dominoes.filter(options.shouldDrawDiamond(diamondType)).toVector
    val points         = dominoesToDraw.flatMap(_.points)

    case class Rectangle(x: Double, y: Double, width: Double, height: Double, color: String) {
      def translate(z: Complex): Rectangle = copy(x = x + z.re, y = y + z.im)
      def scale(s: Double): Rectangle      = copy(x = s * x, y = y * s, width = width * s, height = height * s)

    }

    val rectangles = dominoesToDraw.map { domino =>
      val (width, height) = if domino.isHorizontal then (2, 1) else (1, 2)
      Rectangle(
        domino.p1.x min domino.p2.x,
        -((domino.p1.y max domino.p2.y) - 1),
        width,
        height,
        colorForDomino(domino)
      )
    }

    val leftMost   = rectangles.map(_.x).min
    val rightMost  = rectangles.map(r => r.x + r.width).max
    val topMost    = rectangles.map(_.y).min
    val bottomMost = rectangles.map(r => r.y + r.height).max // in svg coords, so top < bottom

    println((leftMost, rightMost, topMost, bottomMost))

    val center = Complex(rightMost + leftMost, topMost + bottomMost) / 2

    val minSize = 600

    val width  = (rightMost - leftMost) max minSize
    val height = (bottomMost - topMost) max minSize

    val unit = width / (rightMost - leftMost) // arbitrarily chose horizontal as unit

    val svgSize = width max height

    val rectanglesToDraw = rectangles.map(_.translate(-center).scale(unit * zoom))

    val translateX = -rectanglesToDraw.map(_.x).min / zoom
    val translateY = -rectanglesToDraw.map(_.y).min / zoom

    val transformStatement =
      s"""transform="translate($translateX $translateY) rotate($rotationAngle)""""

    val rectanglesByColor = rectanglesToDraw
      .groupBy(_.color)
      .map { case (color, group) =>
        val rectangles = group
          .map { rectangle =>
            val Rectangle(x, y, w, h, _) = rectangle
            s"""<rect x="$x" y="$y" width="$w" height="$h" />"""
          }
          .mkString("\n")
        val stroke =
          if (withBorder) """stroke="black" stroke-width="1"""" else ""
        s"""<g fill="$color" $stroke>
            |$rectangles
            |</g>
            |""".stripMargin
      }
      .mkString("\n")

    s"""<svg xmlns="http://www.w3.org/2000/svg" width="$svgSize" height="$svgSize">
      |<g $transformStatement >
      |$rectanglesByColor
      |</g>
      |</svg>""".stripMargin
  }

  def nonIntersectingPathTikzCode(
      unit: Double = 1,
      rotationAngle: Double = 0,
      subGraph: Boolean = false
  ): String = {
    val paths =
      if (subGraph) diamond.nonIntersectingPathsSubGraph(isPointInSubGraph)
      else diamond.nonIntersectingPaths

    val cos = math.cos(rotationAngle)
    val sin = math.sin(rotationAngle)

    paths
      .map(points => points.map(point => (point.x * unit, (point.y + 0.5) * unit)))
      .map { coordinates =>
        "\\draw " + coordinates
          .map { case (x, y) => (x * cos + y * sin, -x * sin + y * cos) }
          .map { case (x, y) => s"($x,$y)" }
          .mkString(" -- ") + ";"
      }
      .mkString("\n")
  }

}

object DiamondDrawer {

  def apply(
      diamond: Diamond,
      isInSubGraph: Domino => Boolean = (_: Domino) => true,
      canvas: Option[(html.Canvas, CanvasRenderingContext2D)] = None,
      drawWithWatermark: Boolean = true
  ): Option[DiamondDrawer] = {
    val diamondDrawer = new DiamondDrawer(diamond, isInSubGraph, canvas, drawWithWatermark)

    if (diamondDrawer.dominoesInSubGraph.isEmpty) None else Some(diamondDrawer)
  }

  private def emptyDiamondTikzCode(order: Int, unit: Double = 1): String = {
    val lowerRightCornerCoordinates: Vector[Point] =
      (0 until order).toVector
        .flatMap(j => Vector(Point(1 + j, -order + 1 + j), Point(2 + j, -order + 1 + j)))
        .drop(1)

    val upperRightCornerCoordinates: Vector[Point] =
      lowerRightCornerCoordinates.map { case Point(x, y) => Point(x, -y + 2) }.reverse

    val upperLeftCornerCoordinates: Vector[Point] =
      upperRightCornerCoordinates.map { case Point(x, y) => Point(-x + 2, y) }.reverse

    val lowerLeftCornerCoordinates: Vector[Point] =
      lowerRightCornerCoordinates.map { case Point(x, y) => Point(-x + 2, y) }.reverse

    val allCoordinates =
      lowerRightCornerCoordinates ++
        upperRightCornerCoordinates ++
        upperLeftCornerCoordinates ++
        lowerLeftCornerCoordinates

    """\draw[thick] """ + allCoordinates
      .map { case Point(x, y) => s"(${x * unit},${y * unit})" }
      .mkString(" -- ") + " -- cycle;"
  }

}
