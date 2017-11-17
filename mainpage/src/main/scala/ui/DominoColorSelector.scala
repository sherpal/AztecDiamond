package ui

import computationcom.{DiamondGenerationWorker, DiamondGenerator}
import geometry._
import org.scalajs.dom
import org.scalajs.dom.html


/**
 * A DominoColorSelector allows to chose the color for certain types of dominoes.
 * @param dominoesDescriptions A list of pairs with
 *                             First element: a String describing what kind of dominoes are involved (i.e., North Going)
 *                             Second element: a predicate determining what dominoes are involved.
 */
class DominoColorSelector(
                           enclosingDiv: html.Div,
                           dominoesDescriptions: List[(String, (Domino) => Boolean, (Int, Int, Int))]
                         ) {


  private val colorSelectors = dominoesDescriptions.map({ case (dominoDescription, predicate, color) =>
    val div: html.Div = dom.document.createElement("div").asInstanceOf[html.Div]
    div.style.height = "1em"
    div.style.lineHeight = "1em"
    div.style.marginBottom = "5px"

    val label: html.Label = dom.document.createElement("label").asInstanceOf[html.Label]
    label.style.display = "inline-block"
    label.textContent = dominoDescription
    label.style.marginRight = "15px"
    label.style.width = "170px"
    div.appendChild(label)

    val colorSelector: html.Div = dom.document.createElement("div").asInstanceOf[html.Div]
    colorSelector.style.cursor = "pointer"
    colorSelector.style.backgroundColor = s"rgb(${color._1},${color._2},${color._3})"
    colorSelector.style.border = "1px solid black"
    colorSelector.style.width = "35px"
    colorSelector.style.height = "1em"
    colorSelector.style.display = "inline-block"
    div.appendChild(colorSelector)

    colorSelector.onclick = (event: dom.MouseEvent) => {
      val colorSelectorCurrentColors = """\d+""".r.findAllIn(colorSelector.style.backgroundColor).map(_.toInt).toVector
      ColorPicker.show(
        event.clientX, event.clientY,
        (colorSelectorCurrentColors(0), colorSelectorCurrentColors(1), colorSelectorCurrentColors(2)),
        (color: (Int, Int, Int)) => colorSelector.style.backgroundColor = s"rgb(${color._1},${color._2},${color._3})"
      )
    }

    enclosingDiv.appendChild(div)

    (colorSelector, predicate)
  })

  def dominoColors: (Domino) => (Int, Int, Int) = {
    val colors: Map[html.Div, (Int, Int, Int)] = colorSelectors.map(_._1)
          .map(div => div -> {
            val colors = """\d+""".r.findAllIn(div.style.backgroundColor).toVector.map(_.toInt)
            (colors(0), colors(1), colors(2))
          })
          .toMap

    (domino: Domino) =>
      colorSelectors.find(_._2.apply(domino)) match {
        case Some((selector, _)) =>
          colors(selector)
        case None =>
          if (scala.scalajs.LinkingInfo.developmentMode) {
            dom.console.warn(s"could not find color of $domino")
          }
          (0, 0, 0)
      }
  }



}

object DominoColorSelector {

  def order: Int = DiamondGenerator.currentDiamond.order

  private val twoDominoTypesSelector: html.Div = dom.document.getElementById("twoDominoTypesSelector")
    .asInstanceOf[html.Div]

  private val fourDominoTypesSelector: html.Div = dom.document.getElementById("fourDominoTypesSelector")
    .asInstanceOf[html.Div]

  private val eightDominoTypesSelector: html.Div = dom.document.getElementById("eightDominoTypesSelector")
    .asInstanceOf[html.Div]

  private val dominoSelectorMap: Map[Int, DominoColorSelector] = Map(
    2 -> new DominoColorSelector(
      twoDominoTypesSelector,
      List(
        ("Horizontal domino", _.isHorizontal, (255, 255, 255)),
        ("Vertical domino", _.isVertical, (0, 0, 0))
      )
    ),
    4 -> new DominoColorSelector(
      fourDominoTypesSelector,
      DominoType.types.map(dominoType => (
        dominoType.name + " domino",
        (domino: Domino) => domino.dominoType(order) == dominoType,
        dominoType.defaultColor
      ))
    ),
    8 -> new DominoColorSelector(
      eightDominoTypesSelector,
      List(
        (
          "Even " + NorthGoing.name,
          (domino: Domino) => domino.dominoType(order) == NorthGoing && domino.p1.y % 2 == 0,
          NorthGoing.defaultColor
        ),
        (
          "Odd " + NorthGoing.name,
          (domino: Domino) => domino.dominoType(order) == NorthGoing && math.abs(domino.p1.y % 2) == 1,
          NorthGoing.defaultColor
        ),
        (
          "Even " + SouthGoing.name,
          (domino: Domino) => domino.dominoType(order) == SouthGoing && domino.p1.y % 2 == 0,
          SouthGoing.defaultColor
        ),
        (
          "Odd " + SouthGoing.name,
          (domino: Domino) => domino.dominoType(order) == SouthGoing && math.abs(domino.p1.y % 2) == 1,
          SouthGoing.defaultColor
        ),
        (
          "Even " + EastGoing.name,
          (domino: Domino) => domino.dominoType(order) == EastGoing && domino.p1.x % 2 == 0,
          EastGoing.defaultColor
        ),
        (
          "Odd " + EastGoing.name,
          (domino: Domino) => domino.dominoType(order) == EastGoing && math.abs(domino.p1.x % 2) == 1,
          EastGoing.defaultColor
        ),
        (
          "Even " + WestGoing.name,
          (domino: Domino) => domino.dominoType(order) == WestGoing && domino.p1.x % 2 == 0,
          WestGoing.defaultColor
        ),
        (
          "Odd " + WestGoing.name,
          (domino: Domino) => domino.dominoType(order) == WestGoing && math.abs(domino.p1.x % 2) == 1,
          WestGoing.defaultColor
        )
      )
    )
  )

  private val divSelectorsMap: Map[Int, html.Div] = Map(
    2 -> twoDominoTypesSelector,
    4 -> fourDominoTypesSelector,
    8 -> eightDominoTypesSelector
  )

  private val colorNumber: html.Select = dom.document.getElementById("colorNumber").asInstanceOf[html.Select]

  private var currentSelector: DominoColorSelector = dominoSelectorMap(4)

  colorNumber.onchange = (_: dom.Event) => {
    divSelectorsMap.values.foreach(_.style.display = "none")
    val chosen = colorNumber.value.toInt
    divSelectorsMap(chosen).style.display = "block"
    currentSelector = dominoSelectorMap(colorNumber.value.toInt)
  }

  def dominoColors: (Domino) => (Int, Int, Int) = currentSelector.dominoColors

  def dominoColorsDouble: (Domino) => (Double, Double, Double) = {
    val withIntColors = dominoColors
    (domino: Domino) => {
      val (r, g, b) = withIntColors.apply(domino)
      (r / 255.0, g / 255.0, b / 255.0)
    }
  }

  private val applyColors: html.Input = dom.document.getElementById("applyColorSettings").asInstanceOf[html.Input]

  applyColors.onclick = (_: dom.MouseEvent) => DiamondGenerator.drawDrawer()

}
