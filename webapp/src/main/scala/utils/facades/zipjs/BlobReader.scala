package utils.facades.zipjs

import org.scalajs.dom

import scala.scalajs.js
import scala.scalajs.js.annotation.JSImport

@js.native
@JSImport("@zip.js/zip.js", "BlobReader")
class BlobReader(blob: dom.Blob) extends Reader[dom.Blob] {

  def getData(): js.Promise[dom.Blob] = js.native

}
