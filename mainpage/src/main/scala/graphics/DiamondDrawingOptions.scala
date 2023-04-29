package graphics

import geometry.Domino
import geometry.NorthGoing
import geometry.SouthGoing
import geometry.EastGoing
import geometry.WestGoing
import diamond.DiamondType
import diamond.Diamond
import diamond.diamondtypes.UniformDiamond

final case class DiamondDrawingOptions(
    drawDominoes: Boolean,
    showInFullAztecDiamond: Boolean,
    drawDominoesAsLozenges: Boolean,
    showBorderOfDominoes: Boolean,
    drawNonIntersectingPaths: Boolean,
    transformations: DiamondDrawingOptions.Transformations,
    colors: DiamondDrawingOptions.DominoColors
) {
  def shouldDrawDiamond(diamondType: DiamondType.DiamondTypeWithArgs): Domino => Boolean =
    if !drawDominoes then _ => false
    else if showInFullAztecDiamond then _ => true
    else diamondType.isInDiamond
}

object DiamondDrawingOptions {

  def default(diamond: Diamond, diamondType: DiamondType): DiamondDrawingOptions = DiamondDrawingOptions(
    drawDominoes = true,
    showInFullAztecDiamond = diamondType == UniformDiamond,
    showBorderOfDominoes = diamond.order <= 30,
    drawDominoesAsLozenges = diamondType.lozengeTiling,
    drawNonIntersectingPaths = false,
    transformations = Transformations(-diamondType.defaultRotation, 1),
    colors = DominoColors.defaultFourTypes
  )

  case class Transformations(
      rotationInDegrees: Double,
      zoom: Double
  )

  case class Color(red: Int, green: Int, blue: Int) {
    def toTripleIn0_1Range: (Double, Double, Double) = (red / 255.0, green / 255.0, blue / 255.0)
  }
  object Color {
    val white  = Color(255, 255, 255)
    val black  = Color(0, 0, 0)
    val red    = Color(255, 0, 0)
    val green  = Color(0, 255, 0)
    val blue   = Color(0, 0, 255)
    val yellow = Color(255, 255, 0)
  }
  sealed trait DominoColors {
    def asFunction(diamondOrder: Int): Domino => Color

    final def asIntFunction(diamondOrder: Int): Domino => (Int, Int, Int) =
      asFunction(diamondOrder) andThen { case Color(red, green, blue) =>
        (red, green, blue)
      }

    final def asDoubleFunction(diamondOrder: Int): Domino => (Double, Double, Double) =
      asFunction(diamondOrder)(_).toTripleIn0_1Range

    final def asCssFunction(diamondOrder: Int): Domino => String =
      asFunction(diamondOrder) andThen { case Color(red, green, blue) => s"rgb($red,$green,$blue)" }
  }
  object DominoColors {
    def defaultFourTypes: FourTypes = FourTypes(Color.red, Color.blue, Color.green, Color.yellow)
    def defaultTwoTypes: TwoTypes   = TwoTypes(Color.white, Color.black)
    def defaultEightTypes: EightTypes =
      EightTypes(Color.red, Color.red, Color.blue, Color.blue, Color.green, Color.green, Color.yellow, Color.yellow)
  }
  case class FourTypes(north: Color, south: Color, east: Color, west: Color) extends DominoColors {
    def asFunction(diamondOrder: Int): Domino => Color = _.dominoType(diamondOrder) match
      case NorthGoing => north
      case SouthGoing => south
      case EastGoing  => east
      case WestGoing  => west

  }
  case class TwoTypes(horizontal: Color, vertical: Color) extends DominoColors {
    def asFunction(diamondOrder: Int): Domino => Color = domino => if domino.isHorizontal then horizontal else vertical
  }
  case class EightTypes(
      evenNorth: Color,
      oddNorth: Color,
      evenSouth: Color,
      oddSouth: Color,
      evenEast: Color,
      oddEast: Color,
      evenWest: Color,
      oddWest: Color
  ) extends DominoColors {
    def asFunction(diamondOrder: Int): Domino => Color = domino =>
      domino.dominoType(diamondOrder) match
        case NorthGoing => if domino.p1.y % 2 == 0 then evenNorth else oddNorth
        case SouthGoing => if domino.p1.y % 2 == 0 then evenSouth else oddSouth
        case EastGoing  => if domino.p1.x % 2 == 0 then evenEast else oddEast
        case WestGoing  => if domino.p1.x % 2 == 0 then evenWest else oddWest

  }

}
