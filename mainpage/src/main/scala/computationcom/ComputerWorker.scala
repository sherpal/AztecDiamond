package computationcom

import messages.{Message, WorkerLoaded}
import org.scalajs.dom
import org.scalajs.dom.raw.URL
import org.scalajs.dom.webworkers.Worker
import ui.AlertBox

import scala.scalajs.js.JSConverters._

trait ComputerWorker extends Computer {
  private val worker = new Worker(URL.createObjectURL(DiamondGenerationWorker.blob))

  worker.onmessage = (event: dom.MessageEvent) => {
    event.data match {
      case s: String =>
        if (scala.scalajs.LinkingInfo.developmentMode) {
          println(s"received $s")
        }
        receiveMessage(WorkerLoaded())
        postMessage(initialMessage)
      case _ =>
        try {
          receiveMessage(Message.decode(event.data.asInstanceOf[scala.scalajs.js.Array[Byte]].toArray))
        } catch {
          case e: Throwable =>
            println(event.data)
            throw e
        }
    }
  }

  def postMessage(message: Message): Unit =
    worker.postMessage(Message.encode(message).toJSArray)

  protected def terminateGenerator(): Unit =
    worker.terminate()

  override protected def crashHandler(): Unit = {
    AlertBox(
      "Fatal Error",
      "The Web Worker crashed. This may be due to Out of Memory issue, when numbers are too big.<br>" +
        "Consider using the desktop application.",
      () => {}
    )
  }

  worker.onerror = (event: dom.ErrorEvent) => println(event.message)

  {
    val url = dom.window.location
    val href = url.href
    val index = href.indexOf("domino-shuffling-implementation.html")
    val finalUrl = if (index != -1) href.substring(0, index) else href
    worker.postMessage(finalUrl)
  }

}
