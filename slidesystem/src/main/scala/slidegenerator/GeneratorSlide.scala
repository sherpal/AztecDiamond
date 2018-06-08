package slidegenerator

import diamond.{Diamond, DiamondType}
import exceptions._
import geometry.Domino
import graphics.DiamondDrawer
import popups.Alert

import scala.scalajs.js
import scala.scalajs.js.annotation.{JSExport, JSExportTopLevel}

final class GeneratorSlide private (
                                     protected val slideId: String,
                                     protected val _diamondType: String,
                                     protected val modifySlideTitle: Boolean = true,
                                     protected val colors: js.Array[js.Array[Int]] = js.Array()
                                   ) extends GeneratorRequest {

//  private val slide: html.Element = dom.document.getElementById(slideId).asInstanceOf[html.Element]
//
//  if (slide == null) {
//    throw new NoSuchSlideException(slideId)
//  }
//
//  if (!slide.className.split(" ").contains("slide")) {
//    throw new NotASlideElementException(slide.className)
//  }
//
//  if (modifySlideTitle) {
//    try {
//      slide.firstElementChild.asInstanceOf[html.Heading].textContent = diamondType.name + " Generator"
//    } catch {
//      case _: Throwable =>
//        throw new CantSetSlideTitleException
//    }
//  }
//
//  private val defaultColors: List[Option[Color]] = try {
//    colors.toList.map(arr => if (arr.isEmpty) None else Some(Color(arr(0), arr(1), arr(2))))
//  } catch {
//    case e: Throwable =>
//      if (scala.scalajs.LinkingInfo.developmentMode)
//        e.printStackTrace()
//
//      throw new MalformedColor(colors)
//  }
//
//  private val generateParameters: html.Paragraph =
//    dom.document.createElement("p").asInstanceOf[html.Paragraph]
//
//  slide.appendChild(generateParameters)
//
//  private val inputNumberDivs: List[InputNumberDiv] = diamondType.argumentNames.map({
//    case (argName, defaultValue, _) =>
//      new InputNumberDiv(argName, defaultValue)
//  })
//
//  inputNumberDivs.foreach(_.add(generateParameters))


//  private val generateButton: html.Button = dom.document.createElement("button").asInstanceOf[html.Button]
//
//  generateButton.textContent = "Generate"
//  generateButton.addEventListener[dom.MouseEvent]("click", (_: dom.MouseEvent) => {
//    generate()
//  })
//
//  generateParameters.appendChild(generateButton)


//  private val canvasDiv: html.Div = dom.document.createElement("div").asInstanceOf[html.Div]
//  canvasDiv.className = "canvas_container"
//  canvasDiv.style.width = "50vh"
//  canvasDiv.style.height = "50vh"
//
//  slide.appendChild(canvasDiv)
//
//  val canvas2D: Canvas2D = new Canvas2D
//  canvas2D.canvas.style = "width: 50vh; height: 50vh"

//  canvasDiv.appendChild(canvas2D.canvas)


  def drawDiamond(): Unit = {
    if (drawnDiamond.isDefined) {
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
      canvas2D.withTransformationMatrix(
        canvas2D.rotate(0, info.angle)
      ) {
        canvas2D.drawCanvas(drawnDiamond.get.canvas2D.canvas, 0, canvas2D.width, canvas2D.height)
      }
      if (scala.scalajs.LinkingInfo.developmentMode) {
        println(s"It took ${new java.util.Date().getTime - t} ms to draw the diamond.")
      }
    }
  }


  private var drawnDiamond: Option[DiamondDrawer] = None

  def order: Int = drawnDiamond.get.diamond.order

  def setDiamond(diamond: Diamond, isInSubGraph: (Domino) => Boolean): Unit = {
    setDrawnDiamond(DiamondDrawer(diamond, isInSubGraph).get)
  }

  def clear(): Unit = {}

  private def setDrawnDiamond(diamondDrawer: DiamondDrawer): Unit = {
    drawnDiamond = Some(diamondDrawer)
    drawnDiamond.get.canvas2D.setWidth(canvasDiv.offsetWidth.toInt)
    drawnDiamond.get.canvas2D.setHeight(canvasDiv.offsetHeight.toInt)
    canvas2D.setWidth(canvasDiv.offsetWidth.toInt)
    canvas2D.setHeight(canvasDiv.offsetHeight.toInt)
  }

//  private def args: Option[List[Double]] = {
//    try {
//      Some(inputNumberDivs.map(_.value))
//    } catch {
//      case e: Throwable =>
//        if (scala.scalajs.LinkingInfo.developmentMode) {
//          e.printStackTrace()
//        }
//        Alert.showAlert(
//          "Malformed arguments",
//          "Arguments of diamond generation are malformed."
//        )
//        None
//    }
//  }
//
//  private lazy val progressBar: LoadingBar = new LoadingBar(
//    slide, 0, 200
//  )
//
//
//  private def generate(): Unit = {
//
//    try {
//      val arguments = args.get
//
//      generator = Some(
//        new GeneratorWorker(
//          GenerateDiamondMessage(
//            diamondType.toString,
//            arguments.toVector,
//            memoryOptimized = false
//          ),
//          this,
//          progressBar
//        )
//      )
//
//      generateButton.disabled = true
//
//    } catch {
//      case e: Throwable =>
//        if (scala.scalajs.LinkingInfo.developmentMode) {
//          e.printStackTrace()
//        }
//    }
//  }
//
//
//
//
//
//
//  private var generator: Option[GeneratorWorker] = None
//
//  def endOfGenerator(crashed: Boolean): Unit = {
//    if (generator.isDefined) {
//
//      generator.get.kill(crashed)
//      generator = None
//
//      generateButton.disabled = false
//    }
//  }



//
//  private lazy val options: DrawOptions = new DrawOptions(this, defaultColors)
//
//  private val optionButton: html.Button = dom.document.createElement("button").asInstanceOf[html.Button]
//
//  optionButton.onclick = (_: dom.MouseEvent) => options.show()
//  optionButton.textContent = "Drawing Options"
//
//  private val optionButtonPar: html.Paragraph = dom.document.createElement("p").asInstanceOf[html.Paragraph]
//
//  slide.appendChild(optionButtonPar)
//  optionButtonPar.appendChild(optionButton)

}

@JSExportTopLevel("GeneratorSlide")
object GeneratorSlide {

  @JSExport("apply")
  def apply(
             slideId: String,
             _diamondType: String,
             colors: js.Array[js.Array[Int]] = js.Array(),
             modifySlideTitle: Boolean = true
           ): Boolean = {
    try {
      new GeneratorSlide(slideId, _diamondType, modifySlideTitle, if (colors == null) js.Array() else colors)
      true
    } catch {
      case _: NoSuchSlideException =>
        Alert.showAlert(
          "Generator Slide Error",
          s"Your document does not contain an element with id $slideId."
        )
        false
      case e: NotASlideElementException =>
        Alert.showAlert(
          "Generator Slide Error",
          s"The element with id `$slideId` should have class `slide`, `${e.className}` found instead."
        )
        false
      case e: NoSuchDiamondType =>
        Alert.showAlert(
          "Generator Slide Error",
          s"Could not create slide `$slideId`: " +
            s"`${e.diamondType}` is not a valid Diamond type. Allowed choices are " +
          DiamondType.diamondTypes.map(_.toString).mkString(", ") + "."
        )
        false
      case _: CantSetSlideTitleException =>
        Alert.showAlert(
          "Generator Slide Error",
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