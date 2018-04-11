package slidegenerator

import org.scalajs.dom
import org.scalajs.dom.html

final class LoadingBar(slide: html.Element, val minValue: Double, val maxValue: Double) {

  private val div: html.Div = dom.document.createElement("div").asInstanceOf[html.Div]

  slide.appendChild(div)
  div.style.position = "absolute"
  div.style.bottom = {
    val footer = slide.querySelector("footer").asInstanceOf[html.Element]
    footer.offsetHeight + "px"
  }
  div.style.zIndex = "15"
  div.style.width = "100%"
  div.style.height = "5px"
  div.style.backgroundColor = "rgb(50,50,50)"

  private val bar: html.Div = dom.document.createElement("div").asInstanceOf[html.Div]

  div.appendChild(bar)
  bar.style.height = "100%"
  bar.style.width = "100%"

  private var _value: Double = minValue

  def value: Double = _value

  def setValue(x: Double): Unit = {

    _value = math.min(maxValue, math.max(minValue, x))

    val percentage = math.round((value - minValue) / (maxValue - minValue) * 100)

    bar.style.width = percentage + "%"

  }

  setValue(maxValue)

  def setColor(red: Int, green: Int, blue: Int): Unit = {
    bar.style.backgroundColor = s"rgb($red,$green,$blue)"
  }

  def show(): Unit = {
    div.style.display = "block"
  }

  def hide(): Unit = {
    div.style.display = "none"
  }

}
