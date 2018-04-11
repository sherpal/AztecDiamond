package computationcom

import messages.Message
import org.scalajs.dom
import org.scalajs.dom.html



final class DiamondGenerationWorker(val initialMessage: Message) extends DiamondGenerator with ComputerWorker

object DiamondGenerationWorker {
  DiamondGenerator

  dom.document.getElementById("generateForm").asInstanceOf[html.Form].onsubmit = (event: dom.Event) => {
    event.preventDefault()

    DiamondGenerator.generate((message: Message) => new DiamondGenerationWorker(message))

    false
  }
}
