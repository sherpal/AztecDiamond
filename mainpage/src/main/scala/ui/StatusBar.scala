package ui

import org.scalajs.dom
import org.scalajs.dom.html
import ui.exceptions.WrongMinMaxValues


/**
 * A StatusBar is a visible representation of the progress of some process.
 * You may put its value between minValue and maxValue, and the length of the bar will adapt automatically.
 */
class StatusBar private (val minValue: Double, val maxValue: Double, val width: Int, val height: Int) {

  private var _color: (Int, Int, Int) = (0, 0, 0)
  def color: (Int, Int, Int) = _color
  def setColor(red: Int, green: Int, blue: Int): Unit = {
    def clamp0_255(x: Int): Int = math.max(0, math.min(255, x))

    _color = (clamp0_255(red), clamp0_255(green), clamp0_255(blue))

    statusDiv.style.backgroundColor = CSSColor
  }
  def CSSColor: String = s"rgb(${color._1},${color._2},${color._3})"


  private var _value: Double = minValue
  def value: Double = _value
  def setValue(newValue: Double): Unit = {
    _value = math.max(minValue, math.min(newValue, maxValue))

    statusDiv.style.width = s"$percentage%"
    if (withText) {
      statusDiv.textContent = s"$percentage%"
    } else {
      statusDiv.textContent = ""
    }
  }

  def percentage: Int = math.round((value - minValue) / (maxValue - minValue) * 100).toInt

  private val div: html.Div = dom.document.createElement("div").asInstanceOf[html.Div]
  div.style.width = s"${width}px"
  div.style.height = s"${height}px"
  div.style.border = "2px solid black"
  div.style.marginBottom = "5px"

  private val statusDiv: html.Div = dom.document.createElement("div").asInstanceOf[html.Div]
  statusDiv.style.height = s"${height}px"
  statusDiv.style.width = "0%"
  statusDiv.style.backgroundColor = CSSColor
  div.appendChild(statusDiv)

  private var _withText: Boolean = false
  def withText: Boolean = _withText
  def setWithText(enabled: Boolean): Unit = {
    _withText = enabled
    setValue(value)
  }

  def setParent(parent: html.Element): Unit = parent.appendChild(div)

}

object StatusBar {

  def apply(minValue: Double, maxValue: Double, width: Int, height: Int): StatusBar = {
    if (minValue >= maxValue) {
      throw new WrongMinMaxValues(s"Min value must be smaller than max value (received: $minValue, $maxValue).")
    }

    new StatusBar(minValue, maxValue, width, height)
  }

}
