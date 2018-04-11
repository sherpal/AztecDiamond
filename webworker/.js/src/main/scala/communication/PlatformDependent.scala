package communication

import messages.Message
import org.scalajs.dom
import org.scalajs.dom.raw.DedicatedWorkerGlobalScope._

import scala.scalajs.js
import scala.scalajs.js.JSConverters._


private[communication] object PlatformDependent {

  def postMessage(message: Message): Unit =
    self.postMessage(Message.encode(message).toJSArray)

  def startReceiveMessages(args: Array[String]): Unit = {
    self.postMessage("loaded")

    self.onmessage = (event: dom.MessageEvent) => {
      try {
        event.data match {
          case data: String =>
            self.postMessage("test: " + data)
          case data:js.Array[_] if data(0).isInstanceOf[Byte] =>
            val message = Message.decode(data.asInstanceOf[js.Array[Byte]].toArray)

            Communicator.receiveMessage(message)
        }
      } catch {
        case e: Throwable =>
          self.postMessage(js.Array[String]("error", s"error: ${e.toString}"))
      }
    }

  }

}
