package slidegenerator

import org.scalajs.dom
import org.scalajs.dom.html

final class InputNumberDiv(labelString: String, defaultGenerationValue: Double) {

  private val div: html.Div = dom.document.createElement("div").asInstanceOf[html.Div]
  div.style.width = "401px"
  div.style.marginTop = "2px"

  private val labelDiv: html.Div = dom.document.createElement("div").asInstanceOf[html.Div]
  labelDiv.style.width = "350px"
  labelDiv.style.display = "inline-block"
  private val label: html.Label = dom.document.createElement("label").asInstanceOf[html.Label]
  label.textContent = labelString
  label.style.paddingRight = "1em"
  labelDiv.appendChild(label)

  private val inputDiv: html.Div = dom.document.createElement("div").asInstanceOf[html.Div]
  inputDiv.style.width = "50px"
  inputDiv.style.display = "inline-block"
  private val input: html.Input = dom.document.createElement("input").asInstanceOf[html.Input]
  input.style.width = "50px"
  input.value = defaultGenerationValue.toString
  inputDiv.appendChild(input)

  div.appendChild(labelDiv)
  div.appendChild(inputDiv)

  def remove(elem: html.Element): Unit = {
    elem.removeChild(div)
  }

  def add(elem: html.Element): Unit = {
    elem.appendChild(div)
  }

  def value: Double = input.value.toDouble
}
