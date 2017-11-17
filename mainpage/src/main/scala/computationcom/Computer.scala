package computationcom

import messages.Message
import org.scalajs.dom

import scala.scalajs.js.timers.{clearInterval, setInterval}


/**
 * A Computer is a binding to the communication with an external program that will actually do the computation.
 * In the case of the web application, it will be a Web Worker.
 * In the case of the electron application, it will be a scala jar child process.
 *
 * Every 100s, we check if the computer showed some activity. If not, we conclude that it's dead and we kill it.
 */
trait Computer {

  val initialMessage: Message

  protected def receiveMessage(message: Message): Unit

  protected def end(): Unit

  protected var receivedMessage: Boolean = false

  private val handler = setInterval(100000) {
    if (!receivedMessage) {
      dom.console.error("A Computer has stop sending messages. Probably a crash")
      end()
    } else {
      if (scala.scalajs.LinkingInfo.developmentMode) {
        println("Computer still alive!")
      }
      receivedMessage = false
    }
  }

  protected def crashHandler(): Unit = {}

  protected def terminateGenerator(): Unit

  def kill(): Unit = {
    terminateGenerator()
    clearInterval(handler)
    crashHandler()
  }

  def postMessage(message: Message): Unit

}
