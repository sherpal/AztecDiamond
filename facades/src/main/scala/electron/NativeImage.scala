package electron

import nodejs.Buffer

import scala.scalajs.js
import scala.scalajs.js.annotation.JSImport


@js.native
@JSImport("electron", "nativeImage")
class NativeImage() extends js.Object {

  def toPNG(): Buffer = js.native

}


@js.native
@JSImport("electron", "nativeImage")
object NativeImage extends js.Object {

  def createFromBuffer(buffer: Buffer): NativeImage = js.native

  def createFromDataURL(dataURL: String): NativeImage = js.native

}
