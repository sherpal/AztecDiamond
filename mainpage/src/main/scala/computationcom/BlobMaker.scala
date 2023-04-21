package computationcom

import org.scalajs.dom
import org.scalajs.dom.{Blob, html}
import org.scalajs.dom.BlobPropertyBag

import scala.scalajs.js

object BlobMaker {

  val blob = new Blob(
    js.Array(
      dom.document
        .getElementById("scriptWorker")
        .asInstanceOf[html.Script]
        .textContent
    ),
    new BlobPropertyBag {
      `type` = "text/javascript"
    }
  )

}
