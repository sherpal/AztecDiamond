package ui

import org.scalajs.dom
import org.scalajs.dom.{File, html}
import org.scalajs.dom.raw.URL

import scala.scalajs.js.timers.{SetIntervalHandle, setInterval, clearInterval}

object DragAndDrop {

  println("Loading Drag and Drop")

  private val form = dom.document.getElementsByClassName("box")(0).asInstanceOf[html.Form]

  private val fileInput = dom.document.getElementById("file").asInstanceOf[html.Input]

  private var droppedFile: Option[File] = None

  private def fileChanged(file: File): Unit = {
    userImg.src = URL.createObjectURL(file)
    userImg.style.display = "block"

    droppedFile = Some(file)
  }

  private val userImg = dom.document.getElementById("userInputImg").asInstanceOf[html.Image]

  form.addClass("has-advanced-upload")

  form.on(
    "drag dragstart dragend dragover dragenter dragleave drop".split(" "),
    (event: dom.Event) => {
      event.preventDefault()
      event.stopPropagation()
    }
  )

  form.on(
    "dragover dragenter".split(" "),
    (_: dom.Event) => {
      form.addClass("is-dragover")
    }
  )

  form.on(
    "dragleave dragend drop".split(" "),
    (_: dom.Event) => {
      form.removeClass("is-dragover")
    }
  )

  form.addEventListener("drop", (event: dom.DragEvent) => {
    val fileList = event.dataTransfer.files

    val length: Int = fileList.length

    println(s"length: $length")

    for (j <- 0 until length) {
      println(fileList(j).name)
    }

    if (length > 0) {
      fileChanged(fileList(0))
    }
  })

  scala.scalajs.js.timers.setInterval(1000) {
    println(dom.document.getElementById("file").asInstanceOf[html.Input].value)
  }

  private var timeoutHandle: Option[SetIntervalHandle] = None

  fileInput.onclick = (_: dom.Event) => {
    timeoutHandle match {
      case Some(h) =>
        clearInterval(h)
      case None =>
    }
    timeoutHandle = Some(setInterval(100) {
      val files = fileInput.files

      if (files.length > 0) {
        fileChanged(files(0))
        clearInterval(timeoutHandle.get)
        timeoutHandle = None
      }
    })
  }

  implicit private class ElementClassManagement(elem: html.Element) {

    def addClass(className: String): Unit = elem.className += " " + className

    def removeClass(className: String): Unit = elem.className = (" " + className).r.replaceAllIn(elem.className, "")

    def on(eventNames: Traversable[String], handler: (dom.Event) => Unit): html.Element = {
      eventNames.foreach(
        elem.addEventListener(_, handler)
      )
      elem
    }

  }

}
