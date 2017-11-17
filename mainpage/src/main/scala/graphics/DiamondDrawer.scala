package graphics

import custommath.Complex
import diamond.Diamond
import geometry._

/**
 * Class helper for drawing Diamond on a canvas.
 * @param diamond      the diamond to draw
 * @param isInSubGraph function indicating which dominoes have to be drawn.
 */
class DiamondDrawer private (val diamond: Diamond, isInSubGraph: (Domino) => Boolean) {

  val canvas2D: Canvas2D = new Canvas2D
  canvas2D.setSize(2000, 2000)

  val topMostFullDiamondCoordinate: Int = diamond.order + 1
  val rightMostFullDiamondCoordinate: Int = diamond.order + 1
  val bottomMostFullDiamondCoordinate: Int = -diamond.order + 1
  val leftMostFullDiamondCoordinate: Int = -diamond.order + 1

  val diamondCenter: Complex = Complex(
    (rightMostFullDiamondCoordinate + leftMostFullDiamondCoordinate) / 2.0,
    (topMostFullDiamondCoordinate + bottomMostFullDiamondCoordinate) / 2.0
  )

  val dominoes: Traversable[Domino] = diamond.listOfDominoes

  val dominoesInSubGraph: Traversable[Domino] = dominoes.filter(isInSubGraph)


  val topMostSubDiamondCoordinate: Int = dominoesInSubGraph
    .foldLeft(bottomMostFullDiamondCoordinate)({ case (curMax, domino) => math.max(curMax, domino.p2.y + 1) })

  val bottomMostSubDiamondCoordinate: Int = dominoesInSubGraph
    .foldLeft(topMostFullDiamondCoordinate)({ case (curMin, domino) => math.min(curMin, domino.p1.y) })

  val rightMostSubDiamondCoordinate: Int = dominoesInSubGraph
    .foldLeft(leftMostFullDiamondCoordinate)({ case (curMax, domino) => math.max(curMax, domino.p2.x + 1)})

  val leftMostSubDiamondCoordinate: Int = dominoesInSubGraph
    .foldLeft(rightMostFullDiamondCoordinate)({ case (curMax, domino) => math.min(curMax, domino.p1.x)})

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


  private lazy val dominoSprites: List[DominoSprite] = dominoes.map(new DominoSprite(_)).toList

  private lazy val emptyDominoSprites: List[EmptyDominoSprite] = dominoes.map(new EmptyDominoSprite(_)).toList

  private def defaultColors(domino: Domino): (Double, Double, Double) = domino.dominoType(diamond.order) match {
    case NorthGoing => (1,0,0)
    case SouthGoing => (0,0,1)
    case EastGoing => (0,1,0)
    case WestGoing => (1,1,0)
  }

  private def rawDraw(worldCenter: Complex, scaleX: Double, scaleY: Double,
                      colors: (Domino) => (Double, Double, Double),
                      predicate: (Domino) => Boolean = (_) => true,
                      fullDiamond: Boolean = true,
                      border: Boolean = false): Unit = {

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
      .filter(sprite => predicate(sprite.domino)).foreach(sprite => {
      val (r, g, b) = colors(sprite.domino)
      sprite.setColor(r, g, b)
      camera.drawSprite(sprite)
    })

  }

  def draw(worldCenter: Complex = diamondCenter, scaleX: Double = 1, scaleY: Double = 1, border: Boolean = false,
           colors: (Domino) => (Double, Double, Double) = defaultColors): Unit = {
    rawDraw(worldCenter, scaleX, scaleY, colors, border = border)
  }

  def drawSubGraph(worldCenter: Complex = subDiamondCenter, scaleX: Double = 1, scaleY: Double = 1,
                   border: Boolean = false,
                   colors: (Domino) => (Double, Double, Double) = defaultColors): Unit = {
    rawDraw(worldCenter, scaleX, scaleY, colors, isInSubGraph, fullDiamond = false, border = border)
  }

  private lazy val lozengeSprites: List[LozengeSprite] = dominoes.map(new LozengeSprite(_, diamond.order)).toList

  private lazy val emptyLozengeSprites: List[EmptyLozengeSprite] = dominoes
    .map(new EmptyLozengeSprite(_, diamond.order)).toList

  def drawAsLozenges(worldCenter: Complex = diamondCenter, scaleX: Double = 1, scaleY: Double = 1,
                     border: Boolean = false,
                     colors: (Domino) => (Double, Double, Double) = defaultColors): Unit = {
    camera.worldCenter = worldCenter
    camera.worldWidth = (rightMostFullDiamondCoordinate - leftMostFullDiamondCoordinate) / scaleX / 2
    camera.worldHeight = (topMostFullDiamondCoordinate - bottomMostFullDiamondCoordinate) / scaleY * math.sqrt(3.0) / 2

    (if (border) lozengeSprites ++ emptyLozengeSprites else lozengeSprites)
      .foreach(sprite => {
      val (r, g, b) = colors(sprite.domino)
      sprite.setColor(r, g, b)
      camera.drawSprite(sprite)
    })

  }

  def drawSubGraphAsLozenges(worldCenter: Complex = subDiamondCenter, scaleX: Double = 1, scaleY: Double = 1,
                             border: Boolean = false,
                             colors: (Domino) => (Double, Double, Double) = defaultColors): Unit = {
    camera.worldCenter = Complex(worldCenter.re / 2, worldCenter.im * math.sqrt(3.0) / 2)

    val cameraSize = math.max(
      (rightMostSubDiamondCoordinate - leftMostSubDiamondCoordinate + 1) / scaleX /2,
      (topMostSubDiamondCoordinate - bottomMostSubDiamondCoordinate) / scaleY * math.sqrt(3.0) / 2
    )

    camera.worldWidth = cameraSize
    camera.worldHeight = cameraSize

    (if (border) lozengeSprites ++ emptyLozengeSprites else lozengeSprites)
      .filter(sprite => isInSubGraph(sprite.domino)).foreach(sprite => {
      val (r, g, b) = colors(sprite.domino)
      sprite.setColor(r, g, b)
      camera.drawSprite(sprite)
    })

  }



}


object DiamondDrawer {

  def apply(diamond: Diamond, isInSubGraph: (Domino) => Boolean = (_: Domino) => true): Option[DiamondDrawer] = {
    val diamondDrawer = new DiamondDrawer(diamond, isInSubGraph)

    if (diamondDrawer.dominoesInSubGraph.isEmpty) None else Some(diamondDrawer)
  }

}