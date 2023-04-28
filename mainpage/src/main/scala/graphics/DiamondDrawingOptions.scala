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
)

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
    def asFunction(diamondOrder: Int): Domino => (Double, Double, Double)
  }
  object DominoColors {
    def defaultFourTypes: FourTypes = FourTypes(Color.red, Color.blue, Color.green, Color.yellow)
    def defaultTwoTypes: TwoTypes   = TwoTypes(Color.white, Color.black)
    def defaultEightTypes: EightTypes =
      EightTypes(Color.red, Color.red, Color.blue, Color.blue, Color.green, Color.green, Color.yellow, Color.yellow)
  }
  case class FourTypes(north: Color, south: Color, east: Color, west: Color) extends DominoColors {
    private val northTriple = north.toTripleIn0_1Range
    private val southTriple = south.toTripleIn0_1Range
    private val eastTriple  = east.toTripleIn0_1Range
    private val westTriple  = west.toTripleIn0_1Range
    def asFunction(diamondOrder: Int): Domino => (Double, Double, Double) = _.dominoType(diamondOrder) match
      case NorthGoing => northTriple
      case SouthGoing => southTriple
      case EastGoing  => eastTriple
      case WestGoing  => westTriple

  }
  case class TwoTypes(horizontal: Color, vertical: Color) extends DominoColors {
    private val horizontalTriple = horizontal.toTripleIn0_1Range
    private val verticalTriple   = vertical.toTripleIn0_1Range
    def asFunction(diamondOrder: Int): Domino => (Double, Double, Double) = domino =>
      if domino.isHorizontal then horizontalTriple else verticalTriple
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

    private val evenNorthTriple = evenNorth.toTripleIn0_1Range
    private val oddNorthTriple  = oddNorth.toTripleIn0_1Range
    private val evenSouthTriple = evenSouth.toTripleIn0_1Range
    private val oddSouthTriple  = oddSouth.toTripleIn0_1Range
    private val evenEastTriple  = evenEast.toTripleIn0_1Range
    private val oddEastTriple   = oddEast.toTripleIn0_1Range
    private val evenWestTriple  = evenWest.toTripleIn0_1Range
    private val oddWestTriple   = oddWest.toTripleIn0_1Range

    def asFunction(diamondOrder: Int): Domino => (Double, Double, Double) = domino =>
      domino.dominoType(diamondOrder) match
        case NorthGoing => if domino.p1.y % 2 == 0 then evenNorthTriple else oddNorthTriple
        case SouthGoing => if domino.p1.y % 2 == 0 then evenSouthTriple else oddSouthTriple
        case EastGoing  => if domino.p1.x % 2 == 0 then evenEastTriple else oddEastTriple
        case WestGoing  => if domino.p1.x % 2 == 0 then evenWestTriple else oddWestTriple

  }

}
