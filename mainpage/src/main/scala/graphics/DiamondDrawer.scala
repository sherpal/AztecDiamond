package graphics

import custommath.Complex
import diamond.Diamond
import geometry._
import org.scalajs.dom.html
import org.scalajs.dom.CanvasRenderingContext2D

/** Class helper for drawing Diamond on a canvas.
  * @param diamond
  *   the diamond to draw
  * @param isInSubGraph
  *   function indicating which dominoes have to be drawn.
  */
class DiamondDrawer private (
    val diamond: Diamond,
    isInSubGraph: (Domino) => Boolean,
    canvas: Option[(html.Canvas, CanvasRenderingContext2D)]
) {

  def isPointInSubGraph(point: Point): Boolean =
    isInSubGraph(Domino(point, point + Point(1, 0))) ||
      isInSubGraph(Domino(point, point + Point(0, 1))) ||
      isInSubGraph(Domino(point + Point(-1, 0), point)) ||
      isInSubGraph(Domino(point + Point(0, -1), point))

  val canvas2D: Canvas2D = canvas match {
    case Some((c, ctx)) => new Canvas2D(c, ctx)
    case None           => new Canvas2D
  }
  canvas2D.setSize(2000, 2000)

  val topMostFullDiamondCoordinate: Int = diamond.order + 1
  val rightMostFullDiamondCoordinate: Int = diamond.order + 1
  val bottomMostFullDiamondCoordinate: Int = -diamond.order + 1
  val leftMostFullDiamondCoordinate: Int = -diamond.order + 1

  val diamondCenter: Complex = Complex(
    (rightMostFullDiamondCoordinate + leftMostFullDiamondCoordinate) / 2.0,
    (topMostFullDiamondCoordinate + bottomMostFullDiamondCoordinate) / 2.0
  )

  def dominoes: Iterable[Domino] = diamond.listOfDominoes

  def dominoesInSubGraph: Iterable[Domino] = dominoes.filter(isInSubGraph)

  val topMostSubDiamondCoordinate: Int = dominoesInSubGraph
    .foldLeft(bottomMostFullDiamondCoordinate)({ case (curMax, domino) =>
      math.max(curMax, domino.p2.y + 1)
    })

  val bottomMostSubDiamondCoordinate: Int = dominoesInSubGraph
    .foldLeft(topMostFullDiamondCoordinate)({ case (curMin, domino) =>
      math.min(curMin, domino.p1.y)
    })

  val rightMostSubDiamondCoordinate: Int = dominoesInSubGraph
    .foldLeft(leftMostFullDiamondCoordinate)({ case (curMax, domino) =>
      math.max(curMax, domino.p2.x + 1)
    })

  val leftMostSubDiamondCoordinate: Int = dominoesInSubGraph
    .foldLeft(rightMostFullDiamondCoordinate)({ case (curMax, domino) =>
      math.min(curMax, domino.p1.x)
    })

  val subDiamondCenter: Complex = Complex(
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
    dominoes.map(new DominoSprite(_)).toList

  private def emptyDominoSprites: List[EmptyDominoSprite] =
    dominoes.map(new EmptyDominoSprite(_, canvas2D.width * 5 / 2000)).toList

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
      colors: (Domino) => (Double, Double, Double),
      predicate: (Domino) => Boolean = (_) => true,
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
      .foreach(sprite => {
        val (r, g, b) = colors(sprite.domino)
        sprite.setColor(r, g, b)
        camera.drawSprite(sprite)
      })

  }

  def draw(
      worldCenter: Complex = diamondCenter,
      scaleX: Double = 1,
      scaleY: Double = 1,
      border: Boolean = false,
      colors: (Domino) => (Double, Double, Double) = defaultColors
  ): Unit = {
    rawDraw(worldCenter, scaleX, scaleY, colors, border = border)
  }

  def drawSubGraph(
      worldCenter: Complex = subDiamondCenter,
      scaleX: Double = 1,
      scaleY: Double = 1,
      border: Boolean = false,
      colors: (Domino) => (Double, Double, Double) = defaultColors
  ): Unit = {
    rawDraw(
      worldCenter,
      scaleX,
      scaleY,
      colors,
      isInSubGraph,
      fullDiamond = false,
      border = border
    )
  }

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
      colors: (Domino) => (Double, Double, Double) = defaultColors
  ): Unit = {
    camera.worldCenter = worldCenter
    camera.worldWidth =
      (rightMostFullDiamondCoordinate - leftMostFullDiamondCoordinate) / scaleX / 2
    camera.worldHeight =
      (topMostFullDiamondCoordinate - bottomMostFullDiamondCoordinate) / scaleY * math
        .sqrt(3.0) / 2

    (if (border) lozengeSprites ++ emptyLozengeSprites else lozengeSprites)
      .foreach(sprite => {
        val (r, g, b) = colors(sprite.domino)
        sprite.setColor(r, g, b)
        camera.drawSprite(sprite)
      })

  }

  def drawSubGraphAsLozenges(
      worldCenter: Complex = subDiamondCenter,
      scaleX: Double = 1,
      scaleY: Double = 1,
      border: Boolean = false,
      colors: (Domino) => (Double, Double, Double) = defaultColors
  ): Unit = {
    camera.worldCenter =
      Complex(worldCenter.re / 2, worldCenter.im * math.sqrt(3.0) / 2)

    val cameraSize = math.max(
      (rightMostSubDiamondCoordinate - leftMostSubDiamondCoordinate + 1) / scaleX / 2,
      (topMostSubDiamondCoordinate - bottomMostSubDiamondCoordinate) / scaleY * math
        .sqrt(3.0) / 2
    )

    camera.worldWidth = cameraSize
    camera.worldHeight = cameraSize

    (if (border) lozengeSprites ++ emptyLozengeSprites else lozengeSprites)
      .filter(sprite => isInSubGraph(sprite.domino))
      .foreach(sprite => {
        val (r, g, b) = colors(sprite.domino)
        sprite.setColor(r, g, b)
        camera.drawSprite(sprite)
      })

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
    camera.worldCenter =
      if (worldCenter.isDefined) worldCenter.get
      else if (subGraph) subDiamondCenter
      else diamondCenter

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
      .map(domino => {
        val color = defaultColors(domino)
        val lowerLeftX = domino.p1.x * unit
        val lowerLeftY = domino.p1.y * unit
        val upperRightX = (domino.p2.x + 1) * unit
        val upperRightY = (domino.p2.y + 1) * unit
        s"\\draw [fill = ${colors(color)}, draw = black] " +
          s"($lowerLeftX,$lowerLeftY) rectangle ($upperRightX,$upperRightY);"
      })
      .mkString("\n") + "\n" +
      DiamondDrawer.emptyDiamondTikzCode(diamond.order, unit = unit)
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
      .map(points =>
        points.map(point => (point.x * unit, (point.y + 0.5) * unit))
      )
      .map(coordinates => {
        "\\draw " + coordinates
          .map({ case (x, y) => (x * cos + y * sin, -x * sin + y * cos) })
          .map({ case (x, y) => s"($x,$y)" })
          .mkString(" -- ") + ";"
      })
      .mkString("\n")
  }

}

object DiamondDrawer {

  def apply(
      diamond: Diamond,
      isInSubGraph: (Domino) => Boolean = (_: Domino) => true,
      canvas: Option[(html.Canvas, CanvasRenderingContext2D)] = None
  ): Option[DiamondDrawer] = {
    val diamondDrawer = new DiamondDrawer(diamond, isInSubGraph, canvas)

    if (diamondDrawer.dominoesInSubGraph.isEmpty) None else Some(diamondDrawer)
  }

  def emptyDiamondTikzCode(order: Int, unit: Double = 1): String = {
    val lowerRightCornerCoordinates: Vector[Point] =
      (0 until order).toVector
        .flatMap(j =>
          Vector(Point(1 + j, -order + 1 + j), Point(2 + j, -order + 1 + j))
        )
        .drop(1)

    val upperRightCornerCoordinates: Vector[Point] =
      lowerRightCornerCoordinates
        .map({ case Point(x, y) => Point(x, -y + 2) })
        .reverse

    val upperLeftCornerCoordinates: Vector[Point] =
      upperRightCornerCoordinates
        .map({ case Point(x, y) => Point(-x + 2, y) })
        .reverse

    val lowerLeftCornerCoordinates: Vector[Point] =
      lowerRightCornerCoordinates
        .map({ case Point(x, y) => Point(-x + 2, y) })
        .reverse

    val allCoordinates =
      lowerRightCornerCoordinates ++
        upperRightCornerCoordinates ++
        upperLeftCornerCoordinates ++
        lowerLeftCornerCoordinates

    """\draw[thick] """ + allCoordinates
      .map({ case Point(x, y) => s"(${x * unit},${y * unit})" })
      .mkString(" -- ") + " -- cycle;"
  }

}
