package utils.facades.zipjs

import org.scalajs.dom

import scala.scalajs.js
import scala.scalajs.js.annotation.JSImport

@js.native
@JSImport("@zip.js/zip.js", "ZipWriter")
class ZipWriter(writer: BlobWriter) extends js.Object {

  def add(filename: String, reader: Reader[?]): js.Promise[js.Any] = js.native

  def close(): js.Promise[dom.Blob] = js.native

}
