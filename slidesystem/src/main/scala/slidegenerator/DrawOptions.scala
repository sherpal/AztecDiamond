package slidegenerator

import geometry.{Domino, DominoType}
import org.scalajs.dom
import org.scalajs.dom.ext.Color
import org.scalajs.dom.html
import slidegenerator.DrawOptions.DrawInfo
import ui.DominoColorSelector

final class DrawOptions(generatorRequest: GeneratorRequest, defaultColors: List[Option[Color]]) {

  private val div: html.Div = dom.document.createElement("div").asInstanceOf[html.Div]

  dom.document.body.appendChild(div)

  div.style = "display: none; z-index: 10; width: 100%; height: 100%; background-color: rgba(50,50,50,0.5); " +
    "position: absolute; top: 0px; left:0px"

  def show(): Unit =
    div.style.display = "block"

  def hide(): Unit =
    div.style.display = "none"

  private val contentDiv: html.Div = dom.document.createElement("div").asInstanceOf[html.Div]

  div.appendChild(contentDiv)
  contentDiv.style = "background-color: white; padding: 20px; margin-top: 200px; max-width: 500px; " +
    "margin-left: auto; margin-right: auto; border-radius: 5px;"

  private val title: html.Heading = dom.document.createElement("h3").asInstanceOf[html.Heading]

  contentDiv.appendChild(title)
  title.textContent = "Plotting Options"

  private val dominoPlottingHeading: html.Heading = dom.document.createElement("h4")
    .asInstanceOf[html.Heading]

  dominoPlottingHeading.textContent = "Domino plotting options"
  dominoPlottingHeading.style = "padding: 2px; margin-bottom: 2px; margin-top: 5px"

  contentDiv.appendChild(dominoPlottingHeading)

  private val drawDominoes: html.Input = {
    val container = dom.document.createElement("div")
    contentDiv.appendChild(container)

    val label = dom.document.createElement("label")
    label.textContent = "Draw dominoes"
    container.appendChild(label)

    val input = dom.document.createElement("input").asInstanceOf[html.Input]
    input.`type` = "checkbox"
    container.appendChild(input)
    input.checked = true

    input
  }

  private val showInFullDomino: html.Input = {
    val container = dom.document.createElement("div")
    contentDiv.appendChild(container)

    val label = dom.document.createElement("label")
    label.textContent = "Show in full domino"
    container.appendChild(label)

    val input = dom.document.createElement("input").asInstanceOf[html.Input]
    input.`type` = "checkbox"
    container.appendChild(input)

    input
  }

  private val drawInLozenges: html.Input = {
    val container = dom.document.createElement("div")
    contentDiv.appendChild(container)

    val label = dom.document.createElement("label")
    label.textContent = "Show in full Aztec Diamond"
    container.appendChild(label)

    val input = dom.document.createElement("input").asInstanceOf[html.Input]
    input.`type` = "checkbox"
    container.appendChild(input)
    input.checked = generatorRequest.diamondType.lozengeTiling

    input
  }

  private val dominoBorders: html.Input = {
    val container = dom.document.createElement("div")
    contentDiv.appendChild(container)

    val label = dom.document.createElement("label")
    label.textContent = "Show border of dominoes"
    container.appendChild(label)

    val input = dom.document.createElement("input").asInstanceOf[html.Input]
    input.`type` = "checkbox"
    container.appendChild(input)
    input.checked = generatorRequest.isInstanceOf[GeneratorSlide]

    input
  }

  private val nonIntersectingPaths: html.Heading = dom.document.createElement("h4").asInstanceOf[html.Heading]
  nonIntersectingPaths.textContent = "Draw non-intersecting paths options"
  nonIntersectingPaths.style = "padding: 2px; margin-bottom: 2px; margin-top: 5px"
  contentDiv.appendChild(nonIntersectingPaths)

  private val drawNonIntersectingPaths: html.Input = {
    val container = dom.document.createElement("div")
    contentDiv.appendChild(container)

    val label = dom.document.createElement("label")
    label.textContent = "Draw non intersecting paths"
    container.appendChild(label)

    val input = dom.document.createElement("input").asInstanceOf[html.Input]
    input.`type` = "checkbox"
    container.appendChild(input)

    input
  }


  private val dominoColorsHeading: html.Heading = dom.document.createElement("h4").asInstanceOf[html.Heading]
  dominoColorsHeading.textContent = "Chose colors for the dominoes"
  dominoColorsHeading.style = "padding: 2px; margin-bottom: 2px; margin-top: 5px"
  contentDiv.appendChild(dominoColorsHeading)

  private val colorNumber: html.Select = {
    val select = dom.document.createElement("select").asInstanceOf[html.Select]

    contentDiv.appendChild(select)

    val opt = List(("Two types", 2), ("Four types", 4))
        .map({
          case (label, value) =>
            val option = dom.document.createElement("option").asInstanceOf[html.Option]
            option.label = label
            option.value = value.toString
            select.appendChild(option)
            option
        })

    if (generatorRequest.diamondType.lozengeTiling || generatorRequest.isInstanceOf[MovieMakerSlide]) {
      opt(1).selected = true
    } else {
      opt.head.selected = true
    }

    select
  }

  private val twoDominoTypesSelector: html.Div = dom.document.createElement("div")
    .asInstanceOf[html.Div]

  private val fourDominoTypesSelector: html.Div = dom.document.createElement("div")
    .asInstanceOf[html.Div]

  contentDiv.appendChild(twoDominoTypesSelector)
  contentDiv.appendChild(fourDominoTypesSelector)

  private def getDefaultColor(colorIdx: Int): Option[(Int, Int, Int)] = {
    if (defaultColors.length > colorIdx && defaultColors(colorIdx).isDefined) {
      val color = defaultColors(colorIdx).get
      Some((color.r, color.g, color.b))
    } else None
  }

  private val dominoSelectorMap: Map[Int, DominoColorSelector] = Map(
    2 -> new DominoColorSelector(
      twoDominoTypesSelector,
      List(
        (
          "Horizontal domino", _.isHorizontal,
          getDefaultColor(0) match {
            case Some(color) => color
            case None => (255,255,255)
          }
        ),
        (
          "Vertical domino", _.isVertical,
          getDefaultColor(1) match {
            case Some(color) => color
            case None => (0,0,0)
          }
        )
      )
    ),
    4 -> new DominoColorSelector(
      fourDominoTypesSelector,
      DominoType.types.zipWithIndex.map( {
        case (dominoType, idx) =>
          (
            dominoType.name + " domino",
            (domino: Domino) => domino.dominoType(generatorRequest.order) == dominoType,
            getDefaultColor(idx + 2) match {
              case Some(color) => color
              case None => dominoType.defaultColor
            }
          )
      })
    )
  )

  private val divSelectorsMap: Map[Int, html.Div] = Map(
    2 -> twoDominoTypesSelector,
    4 -> fourDominoTypesSelector
  )

  private var currentSelector: DominoColorSelector = _

  private def changeColorSelector(): Unit = {
    divSelectorsMap.values.foreach(_.style.display = "none")
    val chosen = colorNumber.value.toInt
    divSelectorsMap(chosen).style.display = "block"
    currentSelector = dominoSelectorMap(colorNumber.value.toInt)
  }

  changeColorSelector()

  colorNumber.addEventListener("change", (_: dom.Event) => changeColorSelector())

  private def dominoColors: (Domino) => (Int, Int, Int) = currentSelector.dominoColors

  private def dominoColorsDouble: (Domino) => (Double, Double, Double) = {
    val withIntColors = dominoColors
    (domino: Domino) => {
      val (r, g, b) = withIntColors.apply(domino)
      (r / 255.0, g / 255.0, b / 255.0)
    }
  }

  private val closeButton: html.Button = dom.document.createElement("button").asInstanceOf[html.Button]
  closeButton.style.display = "block"
  closeButton.textContent = "Apply"
  contentDiv.appendChild(closeButton)

  closeButton.addEventListener("click", (_: dom.MouseEvent) => {
    hide()
    generatorRequest.drawDiamond()
  })


  def drawInfo: DrawInfo = DrawInfo(
    drawDominoes.checked,
    showInFullDomino.checked,
    drawInLozenges.checked,
    dominoBorders.checked,
    drawNonIntersectingPaths.checked,
    dominoColorsDouble
  )

}

object DrawOptions {

  final case class DrawInfo(
                            drawDominoes: Boolean,
                            showInFullAztecDiamond: Boolean,
                            drawDominoesAsLozenge: Boolean,
                            showBorderOfDominoes: Boolean,
                            drawNonIntersectingPaths: Boolean,
                            dominoColors: (Domino) => (Double, Double, Double)
                           )

}