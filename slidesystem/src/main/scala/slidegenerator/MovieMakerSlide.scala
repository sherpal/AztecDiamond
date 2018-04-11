package slidegenerator

import diamond.{Diamond, DiamondType}
import exceptions._
import geometry.Domino
import graphics.DiamondMovieMaker
import org.scalajs.dom
import org.scalajs.dom.html
import popups.Alert

import scala.scalajs.js
import scala.scalajs.js.annotation.{JSExport, JSExportTopLevel}

final class MovieMakerSlide private (
                                      protected val slideId: String,
                                      protected val _diamondType: String,
                                      protected val modifySlideTitle: Boolean = true,
                                      protected val colors: js.Array[js.Array[Int]] = js.Array()
                                    ) extends GeneratorRequest {



  private var diamondMovieMaker: Option[DiamondMovieMaker] = None


  def setDiamond(diamond: Diamond, isInSubGraph: Domino => Boolean): Unit = {
    range.max = diamond.order.toString
    diamondMovieMaker = Some(new DiamondMovieMaker(diamond))
  }

  def order: Int = diamondMovieMaker.get.currentOrder

  def drawDiamond(): Unit = {
    val drawnDiamond = diamondMovieMaker.map(_.currentDrawer)

    if (drawnDiamond.isDefined) {
      rangeDiv.style.display = "block"
      range.value = order.toString

      val t = new java.util.Date().getTime
      drawnDiamond.get.canvas2D.clear()

      val info = drawInfo

      if (info.drawDominoes) {
        if (info.showInFullAztecDiamond && !info.drawDominoesAsLozenge) {
          drawnDiamond.get.draw(
            border = info.showBorderOfDominoes,
            colors = info.dominoColors
          )
        } else if (!info.drawDominoesAsLozenge) {
          drawnDiamond.get.drawSubGraph(
            border = info.showBorderOfDominoes,
            colors = info.dominoColors
          )
        } else if (info.showInFullAztecDiamond) {
          drawnDiamond.get.drawAsLozenges(
            border = info.showBorderOfDominoes,
            colors = info.dominoColors
          )
        } else {
          drawnDiamond.get.drawSubGraphAsLozenges(
            border = info.showBorderOfDominoes,
            colors = info.dominoColors
          )
        }
      }

      if (info.drawNonIntersectingPaths) {
        drawnDiamond.get.drawNonIntersectingPaths(
          subGraph = !info.showInFullAztecDiamond
        )
      }


      canvas2D.clear()
      canvas2D.drawCanvas(drawnDiamond.get.canvas2D.canvas, 0, canvas2D.width, canvas2D.height)
      if (scala.scalajs.LinkingInfo.developmentMode) {
        println(s"It took ${new java.util.Date().getTime - t} ms to draw the diamond.")
      }
    }
  }



  private val rangeDiv: html.Div = dom.document.createElement("div").asInstanceOf[html.Div]
  rangeDiv.style.width = "50vh"
  rangeDiv.style.height = "5vh"
  rangeDiv.style.backgroundColor = "red"
  rangeDiv.style.marginLeft = "auto"
  rangeDiv.style.marginRight = "auto"
  rangeDiv.style.marginTop = "0px"
  rangeDiv.style.marginBottom = "0px"
  rangeDiv.style.display = "none"

  slide.insertBefore(rangeDiv, slide.lastChild)

  private val (playPauseButton, nextButton, prevButton, range): (html.Button, html.Button, html.Button, html.Input) = {

    val pP = dom.document.createElement("button").asInstanceOf[html.Button]
    pP.textContent = "P"

    val next = dom.document.createElement("button").asInstanceOf[html.Button]
    next.textContent = ">"
    val prev = dom.document.createElement("button").asInstanceOf[html.Button]
    prev.textContent = "<"

    List(pP, next, prev).foreach(elem => {
      elem.style.width = "10%"
      elem.style.paddingTop = "0px"
      elem.style.paddingBottom = "0px"
    })

    val range = dom.document.createElement("input").asInstanceOf[html.Input]
    range.`type` = "range"
    range.style.width = "70%"
    range.min = "1"
    range.max = "10"

    List(prev, pP, range, next).foreach(elem => {
      elem.style.height = "100%"
      rangeDiv.appendChild(elem)
    })

    (pP, next, prev, range)
  }

  private def changeDrawer(diamondOrder: Int): Unit = {
    diamondMovieMaker match {
      case Some(movieMaker) =>
        movieMaker.changeDrawer(diamondOrder)
        drawDiamond()
      case None =>
    }
  }

  nextButton.onclick = (_: dom.MouseEvent) => {
    if (!playing) {
      changeDrawer(order + 1)
    }
  }

  prevButton.onclick = (_: dom.MouseEvent) => {
    if (!playing) {
      changeDrawer(order - 1)
    }
  }

  range.onchange = (_: dom.Event) => {
    if (!playing) {
      changeDrawer(range.valueAsNumber)
    }
  }

  private var playing: Boolean = false

  playPauseButton.onclick = (_: dom.MouseEvent) => {
//    playing = !playing
//    animate()
    togglePlaying()
  }

  private def startPlaying(): Unit = {
    playing = true
    nextButton.disabled = true
    range.disabled = true
    prevButton.disabled = true
    animate()
  }

  private def stopPlaying(): Unit = {
    playing = false
    nextButton.disabled = false
    range.disabled = false
    prevButton.disabled = false
  }

  private def togglePlaying(): Unit =
    if (playing) stopPlaying() else startPlaying()

  private def animate(): Unit = {
    if (playing) {

      changeDrawer(order + 1)

      if (order == diamondMovieMaker.get.order) {
        stopPlaying()
      }

      scala.scalajs.js.timers.setTimeout(100) {
        animate()
      }
    }
  }

}


@JSExportTopLevel("MovieMakerSlide")
object MovieMakerSlide {

  @JSExport("apply")
  def apply(
             slideId: String,
             _diamondType: String,
             colors: js.Array[js.Array[Int]] = js.Array(),
             modifySlideTitle: Boolean = true
           ): Boolean = {
    try {
      new MovieMakerSlide(slideId, _diamondType, modifySlideTitle, if (colors == null) js.Array() else colors)
      true
    } catch {
      case _: NoSuchSlideException =>
        Alert.showAlert(
          "Movie Maker Slide Error",
          s"Your document does not contain an element with id $slideId."
        )
        false
      case e: NotASlideElementException =>
        Alert.showAlert(
          "Movie Maker Slide Error",
          s"The element with id `$slideId` should have class `slide`, `${e.className}` found instead."
        )
        false
      case e: NoSuchDiamondType =>
        Alert.showAlert(
          "Movie Maker Slide Error",
          s"Could not create slide `$slideId`: " +
            s"`${e.diamondType}` is not a valid Diamond type. Allowed choices are " +
            DiamondType.diamondTypes.map(_.toString).mkString(", ") + "."
        )
        false
      case _: CantSetSlideTitleException =>
        Alert.showAlert(
          "Movie Maker Slide Error",
          s"Could not create slide `$slideId`: I could not set the name of the title. Perhaps you forgot to " +
            s"put an H1 element."
        )
        false
      case e: MalformedColor =>
        Alert.showAlert(
          "Malformed Color",
          s"The array of colors, given by " +
            s"[${e.colors.map(arr => "[" + arr.mkString(",") + "]").mkString(",")}] is not formed correctly.\n" +
            "It has to be of the form [[r,g,b], [r, g, b], ...] with r, g, b integers from 0 to 255."
        )
        false
      case e: Throwable =>
        e.printStackTrace()
        false
    }
  }

}