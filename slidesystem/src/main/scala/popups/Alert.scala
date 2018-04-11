package popups

import org.scalajs.dom
import org.scalajs.dom.html

object Alert {

  private val div: html.Div = dom.document.createElement("div").asInstanceOf[html.Div]

  dom.document.body.appendChild(div)

  div.style = "display: none; z-index: 10; width: 100%; height: 100%; background-color: rgba(50,50,50,0.5); " +
    "position: absolute; top: 0px; left:0px"

  private val contentDiv: html.Div = dom.document.createElement("div").asInstanceOf[html.Div]

  div.appendChild(contentDiv)
  contentDiv.style = "background-color: white; padding: 20px; margin-top: 200px; max-width: 500px; " +
    "margin-left: auto; margin-right: auto"

  private val title: html.Heading = dom.document.createElement("h3").asInstanceOf[html.Heading]

  contentDiv.appendChild(title)

  private val messageContent: html.Paragraph = dom.document.createElement("p").asInstanceOf[html.Paragraph]

  contentDiv.appendChild(messageContent)

  private val closeButton: html.Button = dom.document.createElement("button").asInstanceOf[html.Button]

  closeButton.textContent = "Close"
  contentDiv.appendChild(closeButton)

  closeButton.addEventListener("click", (_: dom.Event) => div.style.display = "none")

  def showAlert(titleStr: String, message: String): Unit = {
    div.style.display = "block"

    title.textContent = titleStr

    messageContent.textContent = message
  }

}