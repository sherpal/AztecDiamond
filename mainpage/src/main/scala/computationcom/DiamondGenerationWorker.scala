package computationcom

import messages.Message
import org.scalajs.dom
import org.scalajs.dom.raw.{BlobPropertyBag, URL}
import org.scalajs.dom.webworkers.Worker
import org.scalajs.dom.{Blob, html}

import scala.scalajs.js
import scala.scalajs.js.JSConverters._


final class DiamondGenerationWorker(val initialMessage: Message) extends DiamondGenerator with ComputerWorker {
//  private val worker = new Worker(URL.createObjectURL(DiamondGenerationWorker.blob))
//
//  worker.onmessage = (event: dom.MessageEvent) => {
//    event.data match {
//      case s: String =>
//        if (scala.scalajs.LinkingInfo.developmentMode) {
//          println(s"received $s")
//        }
//        postMessage(initialMessage)
//      case _ =>
//        receiveMessage(Message.decode(event.data.asInstanceOf[scala.scalajs.js.Array[Byte]].toArray))
//    }
//  }
//
//  def postMessage(message: Message): Unit =
//    worker.postMessage(Message.encode(message).toJSArray)
//
//  protected def terminateGenerator(): Unit =
//    worker.terminate()
//
//  worker.onerror = (event: dom.ErrorEvent) => println(event.message)
//
//  {
//    val url = dom.window.location
//    val href = url.href
//    val index = href.indexOf("index.html")
//    val finalUrl = if (index != -1) href.substring(0, index) else href
//    println(finalUrl)
//    worker.postMessage(finalUrl)
//  }
//

}


object DiamondGenerationWorker {
  DiamondGenerator

  val blob = new Blob(
    js.Array(
      dom.document.getElementById("scriptWorker").asInstanceOf[html.Script].textContent
    ),
    BlobPropertyBag("text/javascript")
  )

  dom.document.getElementById("generateForm").asInstanceOf[html.Form].onsubmit = (event: dom.Event) => {
    event.preventDefault()

    DiamondGenerator.generate((message: Message) => new DiamondGenerationWorker(message))

    false
  }
}
