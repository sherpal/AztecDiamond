package ui

import org.scalajs.dom
import org.scalajs.dom.html

object DrawingOptions {

  DrawingTransformations
  DominoColorSelector

  val drawOptions: html.Div = dom.document.getElementById("drawOptions").asInstanceOf[html.Div]


  val drawDominoesCheckBox: html.Input = dom.document.getElementById("drawDominoes").asInstanceOf[html.Input]
  drawDominoesCheckBox.checked = true
  val inFullAztecCheckBox: html.Input = dom.document.getElementById("inFullAztec").asInstanceOf[html.Input]
  val drawInLozengesCheckBox: html.Input = dom.document.getElementById("drawInLozenges").asInstanceOf[html.Input]
  val dominoesBorderCheckBox: html.Input = dom.document.getElementById("showDominoesBorder").asInstanceOf[html.Input]
  dominoesBorderCheckBox.checked = true

  val drawPathsCheckBox: html.Input = dom.document.getElementById("drawPaths").asInstanceOf[html.Input]

  val header: html.Head = drawOptions.getElementsByTagName("header")(0).asInstanceOf[html.Head]

  val slidingContent: html.Div = drawOptions.getElementsByClassName("slidingContent")(0).asInstanceOf[html.Div]

  header.onclick = (_: dom.MouseEvent) => {
    if (slidingContent.style.display == "none") {
      drawOptions.style.paddingBottom = "5px"
      slidingContent.style.display = "block"
    } else {
      drawOptions.style.paddingBottom = "0px"
      slidingContent.style.display = "none"
    }
  }

}
