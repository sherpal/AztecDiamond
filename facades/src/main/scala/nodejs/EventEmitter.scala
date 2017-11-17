package nodejs

import scala.scalajs.js
import scala.scalajs.js.annotation.JSGlobal


@js.native
@JSGlobal
class EventEmitter extends js.Object {
  def on(eventName: String, listener: js.Function): Unit = js.native

  def once(eventName: String, listener: js.Function): Unit = js.native

  /**
   * Synchronously calls each of the listeners registered for the event named eventName, in the order they were
   * registered, passing the supplied arguments to each.
   *
   * Returns true if the event had listeners, false otherwise.
   */
  def emit(eventName: String, args: Any*): Boolean = js.native

  def addListener(eventType: String, listener: js.Function): Unit = js.native

  def removeAllListeners(eventType: String): Unit = js.native

}

