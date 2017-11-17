package computationcom

import messages.Message
import org.scalajs.dom
import org.scalajs.dom.html

final class TilingNumberCountingSocket(val initialMessage: Message) extends ComputerSocket with TilingCounting

object TilingNumberCountingSocket {

  dom.document.getElementById("computePartitionForm").asInstanceOf[html.Form].onsubmit = (event: dom.Event) => {
    event.preventDefault()

    TilingCounting.compute((message: Message) => new TilingNumberCountingSocket(message))

    false
  }

}