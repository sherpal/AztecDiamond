package ui

import org.scalajs.dom
import org.scalajs.dom.html

object ConfirmBox extends MessageBox[(Boolean) => Unit] {

  protected var closeCallback: (Boolean) => Unit = (_: Boolean) => {}

  private val yesButton: html.Button = dom.document.createElement("button").asInstanceOf[html.Button]
  addButton(yesButton)
  yesButton.style.marginRight = "10px"
  yesButton.textContent = "Yes"
  yesButton.className = "validateButton"
  yesButton.onclick = (_: dom.MouseEvent) => {
    closeCallback(true)
    hide()
  }

  private val noButton: html.Button = dom.document.createElement("button").asInstanceOf[html.Button]
  addButton(noButton)
  noButton.textContent = "No"
  noButton.className = "cancelButton"
  noButton.onclick = (_: dom.MouseEvent) => {
    closeCallback(false)
    hide()
  }

  def show(title: String, content: String, callback: (Boolean) => Unit): Unit = {
    appear(title, content)
    closeCallback = callback
  }


}
