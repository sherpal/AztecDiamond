package computationcom

import messages.Message
import org.scalajs.dom
import org.scalajs.dom.html


final class DiamondGenerationSocket(val initialMessage: Message) extends DiamondGenerator with ComputerSocket

object DiamondGenerationSocket {
  DiamondGenerator

  dom.document.getElementById("generateForm").asInstanceOf[html.Form].onsubmit = (event: dom.Event) => {
    event.preventDefault()

    DiamondGenerator.generate((message: Message) => new DiamondGenerationSocket(message))

    false
  }
}