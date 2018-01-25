package computationcom

import diamond.Diamond
import diamond.DiamondType.DiamondTypeFromString
import graphics.{Canvas2D, DiamondDrawer}
import messages._
import org.scalajs.dom
import org.scalajs.dom.html
import ui._

import scala.scalajs.js.timers.setTimeout
import scala.util.matching.Regex


/** A DiamondGenerator is the Computer that will communicate for generating diamonds. */
trait DiamondGenerator extends Computer {
  import computationcom.DiamondGenerator._

  protected def receiveMessage(message: Message): Unit = {
    receivedMessage = true

    message match {
      case WorkerLoaded() =>
        computationPhase.textContent = "Weights are being computed"
      case message: TilingComputationMessage =>
        dom.console.warn("We are in diamond generator")
        dom.console.warn(message.toString)
        throw new WrongMessageTypeException(message.getClass.toString)
      case TestMessage(msg) =>
        if (scala.scalajs.LinkingInfo.developmentMode) {
          println(msg)
        }
      case ErrorMessage(error) =>
        if (scala.scalajs.LinkingInfo.developmentMode) {
          dom.console.error(error)
        }
      case DiamondMessage(diamondTypeString, time, args, diamondInfo) =>
        diamondComputationStatusBar.setColor(0, 255, 0)
        diamondComputationStatusBar.setValue(100)
        weightGenerationStatusBar.setColor(0, 255, 0)
        weightGenerationStatusBar.setValue(100)
        computationPhase.textContent = "Diamond has been generated."

        val diamondType = diamondTypeString.toDiamondType

        if (scala.scalajs.LinkingInfo.developmentMode) {
          println(s"It took $time ms to generate the diamond of type $diamondType in the web worker.")
        }

        val arg = diamondType.transformArguments(args)

        val t = new java.util.Date().getTime

        drawnDiamond = DiamondDrawer(Diamond(diamondInfo), diamondType.isInDiamond(arg))
        val order = drawnDiamond.get.diamond.order
        if (scala.scalajs.LinkingInfo.developmentMode) {
          println(s"It took ${new java.util.Date().getTime - t} ms to recover the diamond of order $order.")
        }
        setTimeout(500) {
          DrawingOptions.dominoesBorderCheckBox.checked = order <= 50
          drawDrawer()
          generationInfo.textContent = "Diamond drawn!"
        }
        timeTaken.textContent =
          s"It took ${time / 1000.0} s to generate the diamond of type ${diamondType.name} and of total order $order."
        generationInfo.textContent = "Diamond generated! Drawing Diamond..."
        generationInfo.style.color = "black"

        endOfGenerator()
      case WeightComputationStatus(status) =>
        weightGenerationStatusBar.setValue(status)
      case DiamondIsComputed() =>
        computationPhase.textContent = "Diamond is computed, receiving diamond information..."
      case WeightsAreComputed() =>
        weightGenerationStatusBar.setColor(0, 255, 0)
        weightGenerationStatusBar.setValue(100)
        computationPhase.textContent = "Diamond is being generated..."
      case DiamondComputationStatus(status) =>
        diamondComputationStatusBar.setValue(status)
      case GenerationWrongParameterException(msg) =>
        generationInfo.textContent = s"Error while generating: $msg"
        generationInfo.style.color = "red"
        endOfGenerator()
      case _ =>
        dom.console.warn("We are in diamond generator")
        dom.console.warn(s"I don't know that message: `$message`")
        throw new WrongMessageTypeException(message.getClass.toString)
    }
  }

  override protected def end(): Unit =
    endOfGenerator(crashed = true)

}


object DiamondGenerator {
  private var drawnDiamond: Option[DiamondDrawer] = None

  def currentDiamond: Diamond = drawnDiamond.get.diamond

  import ui.DrawingOptions._

  val generationInfo: html.Paragraph = dom.document.getElementById("generationInfo").asInstanceOf[html.Paragraph]

  val memoryOptimizeCheckbox: html.Input = dom.document.getElementById("memoryOptimizeGeneration")
    .asInstanceOf[html.Input]

  val timeTaken: html.Paragraph = dom.document.getElementById("timeTaken").asInstanceOf[html.Paragraph]

  val canvas2D: Canvas2D = new Canvas2D(dom.document.getElementById("aztecDrawing").asInstanceOf[html.Canvas])

  def applyTransformation(rotation: Double, zoomX: Double, zoomY: Double): Unit = {
    if (drawnDiamond.isDefined) {
      canvas2D.clear()
      canvas2D.withTransformationMatrix(
        canvas2D.rotate(0, rotation) * canvas2D.scale(0, zoomX, zoomY)
      ) {
        canvas2D.drawCanvas(drawnDiamond.get.canvas2D.canvas, 0, canvas2D.canvas.width, canvas2D.canvas.height)
      }
    }

    if (!scala.scalajs.LinkingInfo.developmentMode) {
      canvas2D.printWatermark(zoomX, zoomY)
    }
  }

  def drawDrawer(): Unit = {
    if (drawnDiamond.isDefined) {
      val t = new java.util.Date().getTime
      drawnDiamond.get.canvas2D.clear()

      if (drawDominoesCheckBox.checked) {
        if (inFullAztecCheckBox.checked && !drawInLozengesCheckBox.checked) {
          drawnDiamond.get.draw(
            border = dominoesBorderCheckBox.checked,
            colors = DominoColorSelector.dominoColorsDouble
          )
        } else if (!drawInLozengesCheckBox.checked) {
          drawnDiamond.get.drawSubGraph(
            border = dominoesBorderCheckBox.checked,
            colors = DominoColorSelector.dominoColorsDouble
          )
        } else if (inFullAztecCheckBox.checked) {
          drawnDiamond.get.drawAsLozenges(
            border = dominoesBorderCheckBox.checked,
            colors = DominoColorSelector.dominoColorsDouble
          )
        } else {
          drawnDiamond.get.drawSubGraphAsLozenges(
            border = dominoesBorderCheckBox.checked,
            colors = DominoColorSelector.dominoColorsDouble
          )
        }
      }

      if (drawPathsCheckBox.checked) {
        drawnDiamond.get.drawNonIntersectingPaths(subGraph = !inFullAztecCheckBox.checked)
      }

      val (rotation, zoom) = DrawingTransformations.transformationSettings
      applyTransformation(rotation, zoom, zoom)
      if (scala.scalajs.LinkingInfo.developmentMode) {
        println(s"It took ${new java.util.Date().getTime - t} ms to draw the diamond.")
      }
    }
  }

  val weightGenerationStatusBar: StatusBar = StatusBar(0, 100, 200, 20)
  weightGenerationStatusBar.setParent(dom.document.getElementById("statusBarContainer").asInstanceOf[html.Div])
  weightGenerationStatusBar.setColor(255,69,0)
  weightGenerationStatusBar.setWithText(enabled = true)

  val diamondComputationStatusBar: StatusBar = StatusBar(0, 100, 200, 20)
  diamondComputationStatusBar.setParent(dom.document.getElementById("statusBarContainer").asInstanceOf[html.Div])
  diamondComputationStatusBar.setColor(255,69,0)
  diamondComputationStatusBar.setWithText(enabled = true)

  val computationPhase: html.Paragraph = dom.document.getElementById("computationPhase").asInstanceOf[html.Paragraph]

  private var workingGenerator: Option[DiamondGenerator] = None

  val generateButton: html.Input = dom.document.getElementById("diamondGenerationStart").asInstanceOf[html.Input]
  val cancelButton: html.Input = dom.document.getElementById("diamondGenerationCancel").asInstanceOf[html.Input]

  cancelButton.disabled = true

  private def endOfGenerator(crashed: Boolean = false): Unit = {
    if (workingGenerator.isDefined) {
      workingGenerator.get.kill(crashed)
      workingGenerator = None
      cancelButton.disabled = true
      generateButton.disabled = false
    }
  }

  cancelButton.onclick = (_: dom.Event) => {
    endOfGenerator()
  }

  private[computationcom] def generate(generator: (Message) => DiamondGenerator): Unit = {
    GenerateDiamondForm.args match {
      case Some(arguments) =>
        generateButton.disabled = true
        cancelButton.disabled = false

        workingGenerator = Some(generator(
          GenerateDiamondMessage(
            GenerateDiamondForm.diamondType.toString,
            arguments.toVector,
            memoryOptimized = memoryOptimizeCheckbox.checked
          )
        ))

        generationInfo.textContent = "Starting diamond generation..."
        generationInfo.style.color = "black"

        weightGenerationStatusBar.setValue(0)
        weightGenerationStatusBar.setColor(255,69,0)
        diamondComputationStatusBar.setValue(0)
        diamondComputationStatusBar.setColor(255,69,0)
        computationPhase.textContent = "Loading worker..."
      case None =>
        generationInfo.textContent = "Malformed number arguments."
        generationInfo.style.color = "red"
        dom.console.error("Malformed number arguments.")
    }
  }

  List(
    drawDominoesCheckBox, inFullAztecCheckBox, drawInLozengesCheckBox, dominoesBorderCheckBox, drawPathsCheckBox
  ).foreach(_.onchange = (_: dom.Event) => {
    setTimeout(5) {
      drawDrawer()
    }
  })


  def saveAsPNG(element: html.Anchor): Unit = {
    val dataURL = canvas2D.canvas.toDataURL("image/png")
    element.href = new Regex("^data:image/[^;]").replaceFirstIn(dataURL, "data:application/octet-stream")
  }

  private val a: html.Anchor = dom.document.getElementById("savePng").asInstanceOf[html.Anchor]
  a.addEventListener("click", (_: dom.MouseEvent) => {
    saveAsPNG(a)
  })

}