package computationcom

import messages.Message
import org.scalajs.dom
import org.scalajs.dom.html
import org.scalajs.dom.URL
import org.scalajs.dom.Worker

import scala.scalajs.js
import scala.scalajs.js.JSConverters._

class TilingNumberCountingWorker(val initialMessage: Message, val blobMaker: BlobMaker)
    extends TilingCounting
    with ComputerWorker {}

object TilingNumberCountingWorker {

  dom.document.getElementById("computePartitionForm").asInstanceOf[html.Form].onsubmit = (event: dom.Event) => {
    event.preventDefault()

    TilingCounting.compute((message: Message) =>
      new TilingNumberCountingWorker(message, BlobMaker.fromScriptElement("scriptWorker"))
    )

    false
  }

}
