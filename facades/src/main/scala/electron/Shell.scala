package electron

import scala.scalajs.js
import scala.scalajs.js.annotation.JSImport


@js.native
@JSImport("electron", "shell")
object Shell extends js.Object {

  def openExternal(link: String): Unit = js.native

}
