package utils

import org.scalajs.dom

import scala.scalajs.js
import scala.scalajs.js.URIUtils.encodeURIComponent
import scala.scalajs.LinkingInfo
import io.circe.Encoder

private def downloadContent(filename: String, contents: String, contentType: String): Unit = {
  val element = dom.document.createElement("a").asInstanceOf[dom.HTMLElement]

  element.setAttribute("href", s"data:$contentType," ++ encodeURIComponent(contents))
  element.setAttribute("download", filename)
  element.style.display = "none"
  dom.document.body.appendChild(element)
  element.click()
  dom.document.body.removeChild(element)
}

def downloadText(filename: String, contents: String): Unit =
  downloadContent(filename, contents, "application/octet-stream")

def downloadSvgFile(filename: String, svg: String): Unit = downloadContent(filename, svg, "image/svg+xml")

def downloadJson[T](filename: String, contents: T)(using encoder: Encoder[T]): Unit =
  downloadContent(filename, encoder(contents).noSpaces, "application/octet-stream")

def rawBasePath         = js.Dynamic.global.basePath.asInstanceOf[String]
def basePath            = if LinkingInfo.developmentMode then "/" else rawBasePath
def diamondImagesFolder = basePath ++ "images/diamonds/"
