package ui

import computationcom.TilingCounting
import diamond.DiamondType
import diamond.DiamondType._
import org.scalajs.dom
import org.scalajs.dom.html


class CountingTilingForm(val diamondType: DiamondType) extends ArgumentForm {

  protected val header: html.Head = dom.document.getElementById("computePartitionFormHeader").asInstanceOf[html.Head]

  protected val parametersDiv: html.Div =
    dom.document.getElementById("computePartitionParameters").asInstanceOf[html.Div]

}

object CountingTilingForm {

  val forms: Map[DiamondType, CountingTilingForm] = DiamondType.diamondTypes
    .map(diamondType => diamondType -> new CountingTilingForm(diamondType))
    .toMap

  private var _activeForm: CountingTilingForm = forms(UniformDiamond)
  _activeForm.show()

  def diamondType: DiamondType = _activeForm.diamondType
  def args: Option[List[Double]] = _activeForm.args

  def switchForm(diamondType: DiamondType): Unit = {
    TilingCounting.computePartitionInfo.textContent = ""
    _activeForm.hide()
    _activeForm = forms(diamondType)
    _activeForm.show()
  }

  private val formSelect: html.Select = dom.document.getElementById("countingFormChooser").asInstanceOf[html.Select]


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
