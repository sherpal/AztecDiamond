package ui

import diamond.DiamondType
import diamond.DiamondType._
import diamond.diamondtypes.UniformDiamond
import org.scalajs.dom
import org.scalajs.dom.html
import org.scalajs.dom.html.Head


class GenerateDiamondForm(val diamondType: DiamondType) extends ArgumentForm {

  protected val header: Head = dom.document.getElementById("generateFormHeader").asInstanceOf[html.Head]

  protected val parametersDiv: html.Div = dom.document.getElementById("generateParameters").asInstanceOf[html.Div]

}


object GenerateDiamondForm {

  val forms: Map[DiamondType, GenerateDiamondForm] = DiamondType.diamondTypes
    .map(diamondType => diamondType -> new GenerateDiamondForm(diamondType))
    .toMap

  private var _activeForm: GenerateDiamondForm = forms(UniformDiamond)
  _activeForm.show()

  def diamondType: DiamondType = _activeForm.diamondType
  def args: Option[List[Double]] = _activeForm.args

  def switchForm(diamondType: DiamondType): Unit = {
    _activeForm.hide()
    _activeForm = forms(diamondType)
    _activeForm.show()

    DrawingOptions.drawInLozengesCheckBox.checked = diamondType.lozengeTiling
    DrawingTransformations.rotationBox.value = diamondType.defaultRotation.toString
  }

  private val formSelect: html.Select = dom.document.getElementById("formChooser").asInstanceOf[html.Select]


  forms.keys.map(_.toString).toList.sorted.foreach(diamondType => {
    val option = dom.document.createElement("option").asInstanceOf[html.Option]
    formSelect.appendChild(option)
    option.textContent = diamondType.name
    option.value = diamondType
    if (diamondType == UniformDiamond.toString) {
      option.selected = true
    }
  })

  formSelect.addEventListener("change", (_: dom.Event) => {
    switchForm(formSelect.value)
  })

}