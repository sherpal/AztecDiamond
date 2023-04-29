package mainobject.components

import be.doeraene.webcomponents.ui5.*
import com.raquo.laminar.api.L.*
import graphics.DiamondDrawingOptions.*
import org.scalajs.dom
import be.doeraene.webcomponents.ui5.scaladsl.colour.{Colour => UI5Colour}

object DominoColoursForm {

  def apply(initialColors: DominoColors, colorsObserver: Observer[DominoColors]): HtmlElement = {
    val dominoColorsVar = Var(initialColors)
    sectionTag(
      Title.h4("Chose colours for the dominoes"),
      Select(
        _.option("Two types", _.value := "two", _.selected := initialColors.isInstanceOf[TwoTypes]),
        _.option("Four types", _.value := "four", _.selected := initialColors.isInstanceOf[FourTypes]),
        _.option("Eight Types", _.value := "eight", _.selected := initialColors.isInstanceOf[EightTypes]),
        _.events.onChange.map(_.detail.selectedOption.value).map {
          case "two"   => DominoColors.defaultTwoTypes
          case "four"  => DominoColors.defaultFourTypes
          case "eight" => DominoColors.defaultEightTypes
          case _       => DominoColors.defaultFourTypes
        } --> dominoColorsVar.writer
      ),
      child <-- dominoColorsVar.signal.map {
        case twoTypes: TwoTypes     => twoTypesSelector(twoTypes, dominoColorsVar.writer)
        case fourTypes: FourTypes   => fourTypesSelector(fourTypes, dominoColorsVar.writer)
        case eightTypes: EightTypes => eightTypesSelector(eightTypes, dominoColorsVar.writer)
      },
      dominoColorsVar.signal --> colorsObserver
    )
  }

  private def twoTypesSelector(initialColors: TwoTypes, colorsObserver: Observer[DominoColors]): HtmlElement = {
    def obs(f: Color => TwoTypes) = colorsObserver.contramap[Color](f)
    div(
      choseColour(
        "Horizontal",
        Val(initialColors.horizontal),
        obs(color => initialColors.copy(horizontal = color))
      ),
      choseColour(
        "Vertical",
        Val(initialColors.vertical),
        obs(color => initialColors.copy(vertical = color))
      )
    )
  }

  private def fourTypesSelector(initialColors: FourTypes, colorsObserver: Observer[DominoColors]): HtmlElement = {
    def obs(f: Color => FourTypes) = colorsObserver.contramap[Color](f)
    div(
      choseColour(
        "North",
        Val(initialColors.north),
        obs(color => initialColors.copy(north = color))
      ),
      choseColour(
        "South",
        Val(initialColors.south),
        obs(color => initialColors.copy(south = color))
      ),
      choseColour(
        "East",
        Val(initialColors.east),
        obs(color => initialColors.copy(east = color))
      ),
      choseColour(
        "West",
        Val(initialColors.west),
        obs(color => initialColors.copy(west = color))
      )
    )
  }

  private def eightTypesSelector(initialColors: EightTypes, colorsObserver: Observer[DominoColors]): HtmlElement = {
    def obs(f: Color => EightTypes) = colorsObserver.contramap[Color](f)
    div(
      choseColour(
        "Even North",
        Val(initialColors.evenNorth),
        obs(color => initialColors.copy(evenNorth = color))
      ),
      choseColour(
        "Odd North",
        Val(initialColors.oddNorth),
        obs(color => initialColors.copy(oddNorth = color))
      ),
      choseColour(
        "Even South",
        Val(initialColors.evenSouth),
        obs(color => initialColors.copy(evenSouth = color))
      ),
      choseColour(
        "Odd South",
        Val(initialColors.oddSouth),
        obs(color => initialColors.copy(oddSouth = color))
      ),
      choseColour(
        "Even East",
        Val(initialColors.evenEast),
        obs(color => initialColors.copy(evenEast = color))
      ),
      choseColour(
        "Odd East",
        Val(initialColors.oddEast),
        obs(color => initialColors.copy(oddEast = color))
      ),
      choseColour(
        "Even West",
        Val(initialColors.evenWest),
        obs(color => initialColors.copy(evenWest = color))
      ),
      choseColour(
        "Odd West",
        Val(initialColors.oddWest),
        obs(color => initialColors.copy(oddWest = color))
      )
    )
  }

  extension (colour: Color) {
    private def toUI5: UI5Colour = UI5Colour(colour.red, colour.green, colour.blue)
  }

  extension (colour: UI5Colour) {
    private def toAztecDiamond: Color = Color(colour.red, colour.green, colour.blue)
  }

  private def someColourPaletteItems = List(
    UI5Colour.fromString("darkblue"),
    UI5Colour.fromString("pink"),
    UI5Colour.fromIntColour(0x444444),
    UI5Colour(0, 200, 0),
    UI5Colour.green,
    UI5Colour.fromString("darkred"),
    UI5Colour.yellow,
    UI5Colour.blue,
    UI5Colour.fromString("cyan"),
    UI5Colour.orange,
    UI5Colour.fromIntColour(0x5480e7),
    UI5Colour.fromIntColour(0xff6699)
  ).map(colour => ColourPalette.item(_.value := colour))

  private def choseColour(label: String, colourSignal: Signal[Color], colourObserver: Observer[Color]): HtmlElement = {
    val openBus = new EventBus[dom.HTMLElement]
    div(
      ColourPalettePopover(
        _.showAtFromEvents(openBus.events),
        _.events.onItemClick.map(_.detail.color).map(UI5Colour.fromString(_).toAztecDiamond) --> colourObserver,
        someColourPaletteItems,
        _.showRecentColours := true,
        _.showMoreColours   := true
      ),
      div(
        width := "150px",
        display.flex,
        alignItems.center,
        justifyContent.spaceBetween,
        Label(label, marginRight := "1em"),
        ColourPalette(
          _.item(
            _.value <-- colourSignal.map(_.toUI5)
          ),
          inContext(el => ColourPalette.events.onItemClick.mapTo(el.ref) --> openBus.writer)
        )
      )
    )
  }

}
