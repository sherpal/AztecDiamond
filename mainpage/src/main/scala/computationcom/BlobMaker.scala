package computationcom

import org.scalajs.dom
import org.scalajs.dom.{html, Blob}
import org.scalajs.dom.BlobPropertyBag

import scala.scalajs.js
import scala.concurrent.Future
import scala.concurrent.ExecutionContext

trait BlobMaker {

  def blob: Blob

}

object BlobMaker {

  def apply(blob0: Blob): BlobMaker = new BlobMaker {
    def blob: Blob = blob0
  }

  def fromJSTextContent(jsText: String): BlobMaker = apply(
    new Blob(
      js.Array(jsText),
      new BlobPropertyBag {
        `type` = "text/javascript"
      }
    )
  )

  def fromFetch(address: String)(using ExecutionContext): Future[BlobMaker] = for {
    call <- dom.fetch(address).toFuture
    text <- call.text().toFuture
  } yield fromJSTextContent(text)

  def fromScriptElement(scriptElementId: String) = fromJSTextContent(
    dom.document
      .getElementById(scriptElementId)
      .asInstanceOf[html.Script]
      .textContent
  )

}
