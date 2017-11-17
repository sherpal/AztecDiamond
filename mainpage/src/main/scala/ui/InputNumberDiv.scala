package ui

import org.scalajs.dom
import org.scalajs.dom.html

class InputNumberDiv(labelString: String) {

  private val div: html.Div = dom.document.createElement("div").asInstanceOf[html.Div]
  div.style.width = "201px"
  div.style.marginTop = "2px"

  private val labelDiv: html.Div = dom.document.createElement("div").asInstanceOf[html.Div]
  labelDiv.style.width = "150px"
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
