package slidegenerator

import diamond.{Diamond, DiamondType}
import exceptions.{CantSetSlideTitleException, MalformedColor, NoSuchSlideException, NotASlideElementException}
import geometry.Domino
import graphics.Canvas2D
import messages.GenerateDiamondMessage
import org.scalajs.dom
import org.scalajs.dom.ext.Color
import org.scalajs.dom.html
import popups.Alert

import scala.scalajs.js

trait GeneratorRequest {

  protected val _diamondType: String

  val diamondType: DiamondType = _diamondType

  protected val slideId: String

  protected val modifySlideTitle: Boolean

  protected val colors: js.Array[js.Array[Int]]

  protected val slide: html.Element = dom.document.getElementById(slideId).asInstanceOf[html.Element]

  if (slide == null) {
    throw new NoSuchSlideException(slideId)
  }

  if (!slide.className.split(" ").contains("slide")) {
    throw new NotASlideElementException(slide.className)
  }

    try {
      if (modifySlideTitle) {
        slide.firstElementChild.asInstanceOf[html.Heading].textContent = diamondType.name + " Generator"
      }
      slide.firstElementChild.asInstanceOf[html.Heading].style.marginBottom = "2vh"
    } catch {
      case _: Throwable =>
        throw new CantSetSlideTitleException
    }



  private val defaultColors: List[Option[Color]] = try {
    colors.toList.map(arr => if (arr.isEmpty) None else Some(Color(arr(0), arr(1), arr(2))))
  } catch {
    case e: Throwable =>
      if (scala.scalajs.LinkingInfo.developmentMode)
        e.printStackTrace()

      throw new MalformedColor(colors)
  }

  protected val generateParameters: html.Paragraph =
    dom.document.createElement("p").asInstanceOf[html.Paragraph]

  slide.appendChild(generateParameters)

  private val inputNumberDivs: List[InputNumberDiv] = diamondType.argumentNames.map({
    case (argName, defaultValue, _) =>
      new InputNumberDiv(argName, defaultValue)
  })

  inputNumberDivs.foreach(_.add(generateParameters))

  private val generateButton: html.Button = dom.document.createElement("button").asInstanceOf[html.Button]

  generateButton.textContent = "Generate"
  generateButton.addEventListener[dom.MouseEvent]("click", (_: dom.MouseEvent) => {
    generate()
  })

  generateParameters.appendChild(generateButton)

  protected val canvasDiv: html.Div = dom.document.createElement("div").asInstanceOf[html.Div]
  canvasDiv.className = "canvas_container"
  canvasDiv.style.width = "50vh"
  canvasDiv.style.height = "50vh"

  slide.appendChild(canvasDiv)

  val canvas2D: Canvas2D = new Canvas2D
  canvas2D.canvas.style = "width: 50vh; height: 50vh"
  canvasDiv.appendChild(canvas2D.canvas)



  private def args: Option[List[Double]] = {
    try {
      Some(inputNumberDivs.map(_.value))
    } catch {
      case e: Throwable =>
        if (scala.scalajs.LinkingInfo.developmentMode) {
          e.printStackTrace()
        }
        Alert.showAlert(
          "Malformed arguments",
          "Arguments of diamond generation are malformed."
        )
        None
    }
  }

  private lazy val progressBar: LoadingBar = new LoadingBar(
    slide, 0, 200
  )


  private def generate(): Unit = {

    try {
      val arguments = args.get

      generator = Some(
        new GeneratorWorker(
          GenerateDiamondMessage(
            diamondType.toString,
            arguments.toVector,
            memoryOptimized = false
          ),
          this,
          progressBar
        )
      )

      generateButton.disabled = true

    } catch {
      case e: Throwable =>
        if (scala.scalajs.LinkingInfo.developmentMode) {
          e.printStackTrace()
        }
    }
  }






  private var generator: Option[GeneratorWorker] = None

  def endOfGenerator(crashed: Boolean): Unit = {
    if (generator.isDefined) {

      generator.get.kill(crashed)
      generator = None

      generateButton.disabled = false
    }
  }




  private lazy val options: DrawOptions = new DrawOptions(this, defaultColors)

  protected def drawInfo: DrawOptions.DrawInfo = options.drawInfo

  private val optionButton: html.Button = dom.document.createElement("button").asInstanceOf[html.Button]

  optionButton.onclick = (_: dom.MouseEvent) => options.show()
  optionButton.textContent = "Drawing Options"

  private val optionButtonPar: html.Paragraph = dom.document.createElement("p").asInstanceOf[html.Paragraph]

  slide.appendChild(optionButtonPar)
  optionButtonPar.appendChild(optionButton)



  def drawDiamond(): Unit

  def order: Int

  def setDiamond(diamond: Diamond, isInSubGraph: (Domino) => Boolean): Unit


}
