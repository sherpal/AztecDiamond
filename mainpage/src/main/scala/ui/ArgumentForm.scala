package ui

import diamond.DiamondType
import org.scalajs.dom.html

trait ArgumentForm {

  val diamondType: DiamondType

  protected val header: html.Head

  protected val parametersDiv: html.Div

  private lazy val name: html.Heading = header.firstChild.asInstanceOf[html.Heading]
  // name.textContent = diamondType.name

  private lazy val inputNumberDivs: List[InputNumberDiv] = diamondType.argumentNames.map {
    case DiamondType.ArgumentName(label, generationValue, countingValue) =>
      this match {
        case _: CountingTilingForm  => new InputNumberDiv(label, countingValue)
        case _: GenerateDiamondForm => new InputNumberDiv(label, generationValue)
      }
  }

  def hide(): Unit = {
    // parametersDiv.removeChild(name)
    name.textContent = ""
    inputNumberDivs.foreach(_.remove(parametersDiv))
  }

  def show(): Unit = {
    // parametersDiv.appendChild(name)
    name.textContent = diamondType.name
    inputNumberDivs.foreach(_.add(parametersDiv))
  }

  def args: Option[List[Double]] =
    try
      Some(inputNumberDivs.map(_.value))
    catch {
      case e: Throwable =>
        if (scala.scalajs.LinkingInfo.developmentMode) {
          e.printStackTrace()
        }
        None
    }

}
