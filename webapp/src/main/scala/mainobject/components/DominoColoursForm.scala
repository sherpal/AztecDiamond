package mainobject.components

import be.doeraene.webcomponents.ui5.*
import com.raquo.laminar.api.L.*
import graphics.DiamondDrawingOptions.*
import org.scalajs.dom
import be.doeraene.webcomponents.ui5.scaladsl.colour.{Colour => UI5Colour}

object DominoColoursForm {

  def apply(initialColors: DominoColors, colorsObserver: Observer[DominoColors]): HtmlElement = {
    val dominoColorsVar           = Var(initialColors)
    val openColourPaletteBus      = new EventBus[dom.HTMLElement]
    val selectedColourObserverVar = Var(Observer.empty[Color])
    val selectedColourBus         = new EventBus[Color]
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
      ColourPalettePopover(
        _.showAtFromEvents(openColourPaletteBus.events),
        _.events.onItemClick
          .map(_.detail.color)
          .map(UI5Colour.fromString(_).toAztecDiamond) --> selectedColourBus.writer,
        someColourPaletteItems,
        _.showRecentColours := true,
        _.showMoreColours   := true
      ),
      selectedColourBus.events
        .withCurrentValueOf(selectedColourObserverVar.signal) --> Observer[(Color, Observer[Color])]((color, obs) =>
        obs.onNext(color)
      ),
      child <-- dominoColorsVar.signal.map {
        case twoTypes: TwoTypes =>
          twoTypesSelector(
            twoTypes,
            openColourPaletteBus.writer,
            dominoColorsVar.writer,
            selectedColourObserverVar.writer
          )
        case fourTypes: FourTypes =>
          fourTypesSelector(
            fourTypes,
            openColourPaletteBus.writer,
            dominoColorsVar.writer,
            selectedColourObserverVar.writer
          )
        case eightTypes: EightTypes =>
          eightTypesSelector(
            eightTypes,
            openColourPaletteBus.writer,
            dominoColorsVar.writer,
            selectedColourObserverVar.writer
          )
      },
      dominoColorsVar.signal --> colorsObserver
    )
  }

  private def twoTypesSelector(
      initialColors: TwoTypes,
      openColourPaletteObserver: Observer[dom.HTMLElement],
      colorsObserver: Observer[DominoColors],
      colourObserverObserver: Observer[Observer[Color]]
  ): HtmlElement = {
    def obs(f: Color => TwoTypes) = colorsObserver.contramap[Color](f)
    div(
      choseColour(
        "Horizontal",
        Val(initialColors.horizontal),
        openColourPaletteObserver,
        obs(color => initialColors.copy(horizontal = color)),
        colourObserverObserver
      ),
      choseColour(
        "Vertical",
        Val(initialColors.vertical),
        openColourPaletteObserver,
        obs(color => initialColors.copy(vertical = color)),
        colourObserverObserver
      )
    )
  }

  private def fourTypesSelector(
      initialColors: FourTypes,
      openColourPaletteObserver: Observer[dom.HTMLElement],
      colorsObserver: Observer[DominoColors],
      colourObserverObserver: Observer[Observer[Color]]
  ): HtmlElement = {
    def obs(f: Color => FourTypes) = colorsObserver.contramap[Color](f)
    div(
      choseColour(
        "North",
        Val(initialColors.north),
        openColourPaletteObserver,
        obs(color => initialColors.copy(north = color)),
        colourObserverObserver
      ),
      choseColour(
        "South",
        Val(initialColors.south),
        openColourPaletteObserver,
        obs(color => initialColors.copy(south = color)),
        colourObserverObserver
      ),
      choseColour(
        "East",
        Val(initialColors.east),
        openColourPaletteObserver,
        obs(color => initialColors.copy(east = color)),
        colourObserverObserver
      ),
      choseColour(
        "West",
        Val(initialColors.west),
        openColourPaletteObserver,
        obs(color => initialColors.copy(west = color)),
        colourObserverObserver
      )
    )
  }

  private def eightTypesSelector(
      initialColors: EightTypes,
      openColourPaletteObserver: Observer[dom.HTMLElement],
      colorsObserver: Observer[DominoColors],
      colourObserverObserver: Observer[Observer[Color]]
  ): HtmlElement = {
    def obs(f: Color => EightTypes) = colorsObserver.contramap[Color](f)
    div(
      choseColour(
        "Even North",
        Val(initialColors.evenNorth),
        openColourPaletteObserver,
        obs(color => initialColors.copy(evenNorth = color)),
        colourObserverObserver
      ),
      choseColour(
        "Odd North",
        Val(initialColors.oddNorth),
        openColourPaletteObserver,
        obs(color => initialColors.copy(oddNorth = color)),
        colourObserverObserver
      ),
      choseColour(
        "Even South",
        Val(initialColors.evenSouth),
        openColourPaletteObserver,
        obs(color => initialColors.copy(evenSouth = color)),
        colourObserverObserver
      ),
      choseColour(
        "Odd South",
        Val(initialColors.oddSouth),
        openColourPaletteObserver,
        obs(color => initialColors.copy(oddSouth = color)),
        colourObserverObserver
      ),
      choseColour(
        "Even East",
        Val(initialColors.evenEast),
        openColourPaletteObserver,
        obs(color => initialColors.copy(evenEast = color)),
        colourObserverObserver
      ),
      choseColour(
        "Odd East",
        Val(initialColors.oddEast),
        openColourPaletteObserver,
        obs(color => initialColors.copy(oddEast = color)),
        colourObserverObserver
      ),
      choseColour(
        "Even West",
        Val(initialColors.evenWest),
        openColourPaletteObserver,
        obs(color => initialColors.copy(evenWest = color)),
        colourObserverObserver
      ),
      choseColour(
        "Odd West",
        Val(initialColors.oddWest),
        openColourPaletteObserver,
        obs(color => initialColors.copy(oddWest = color)),
        colourObserverObserver
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

  private def choseColour(
      label: String,
      colourSignal: Signal[Color],
      openPopoverObserver: Observer[dom.HTMLElement],
      colourObserver: Observer[Color],
      colourObserverObserver: Observer[Observer[Color]]
  ): HtmlElement =
    div(
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
          inContext(el =>
            ColourPalette.events.onItemClick.mapTo(el.ref) --> Observer.combine(
              openPopoverObserver,
              Observer[dom.HTMLElement](_ => colourObserverObserver.onNext(colourObserver))
            )
          )
        )
      )
    )

}
