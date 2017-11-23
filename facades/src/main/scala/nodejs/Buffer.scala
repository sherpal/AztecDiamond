package nodejs

import scala.scalajs.js
import scala.scalajs.js.annotation.JSGlobal
import scala.scalajs.js.typedarray.ArrayBuffer

@js.native
@JSGlobal
abstract class Buffer extends js.typedarray.Uint8Array(0) {
  def toString(encoding: String = "utf8", start: Int = 0, end: Int = this.length): String = js.native

  def readInt32BE(): Int = js.native

  def slice(start: Int = 0, end: Int = length): Buffer = js.native

}


@js.native
@JSGlobal
object Buffer extends js.Object {
  def from(s: String, encoding: String = "utf8"): Buffer = js.native

  def from(arrayBuffer: ArrayBuffer): Buffer = js.native

  def concat(list: js.Array[Buffer]): Buffer = js.native
}
