package computationcom

import messages.Message
import org.scalajs.dom
import org.scalajs.dom.html
import org.scalajs.dom.raw.URL
import org.scalajs.dom.webworkers.Worker

import scala.scalajs.js
import scala.scalajs.js.JSConverters._

class TilingNumberCountingWorker(val initialMessage: Message) extends TilingCounting  with ComputerWorker {

//  private val worker = new Worker(URL.createObjectURL(DiamondGenerationWorker.blob))
//
//  def terminateGenerator(): Unit =
//    worker.terminate()
//
//  def postMessage(message: Message): Unit =
//    worker.postMessage(Message.encode(message).toJSArray)
//
//  worker.onmessage = (event: dom.MessageEvent) => {
//    receivedMessage = true
//
//    event.data match {
//      case data: String if data == "loaded" =>
//        if (scala.scalajs.LinkingInfo.developmentMode) {
//          println("web worker loaded")
//        }
//        //worker.postMessage("test web worker")
//        postMessage(initialMessage)
//      case data: String =>
//        println(data)
//      case data: js.Array[_] if data(0).isInstanceOf[Byte] =>
//        receiveMessage(Message.decode(data.asInstanceOf[js.Array[Byte]].toArray))
//    }
//  }
//
//  worker.onerror = (event: dom.ErrorEvent) => println(event)
//
//  {
//    val url = dom.window.location
//    val href = url.href
//    val index = href.indexOf("index.html")
//    val finalUrl = if (index != -1) href.substring(0, index) else href
//    worker.postMessage(finalUrl)
//  }
//
//

}

object TilingNumberCountingWorker {

  dom.document.getElementById("computePartitionForm").asInstanceOf[html.Form].onsubmit = (event: dom.Event) => {
    event.preventDefault()

    TilingCounting.compute((message: Message) => new TilingNumberCountingWorker(message))

    false
  }

}