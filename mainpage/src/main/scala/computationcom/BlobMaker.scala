package computationcom

import org.scalajs.dom
import org.scalajs.dom.{Blob, html}
import org.scalajs.dom.raw.BlobPropertyBag

import scala.scalajs.js

object BlobMaker {

  val blob = new Blob(
    js.Array(
      dom.document.getElementById("scriptWorker").asInstanceOf[html.Script].textContent
    ),
    BlobPropertyBag("text/javascript")
  )

}
