package ui

import computationcom.{DiamondGenerationWorker, DiamondGenerator}
import org.scalajs.dom
import org.scalajs.dom.html

object DrawingTransformations {

  val rotationBox: html.Input = dom.document.getElementById("rotationBox").asInstanceOf[html.Input]

  private val zoomBox: html.Input = dom.document.getElementById("zoomBox").asInstanceOf[html.Input]

  private def rotation: Option[Double] = try {
    Some(rotationBox.value.toInt / 360.0 * 2 * math.Pi)
  } catch {
    case _: Throwable =>
      None
  }

  private def zoom: Option[Double] = try {
    Some(zoomBox.value.toDouble)
  } catch {
    case _: Throwable =>
      None
  }

  def transformationSettings: (Double, Double) = (rotation.getOrElse(0), zoom.getOrElse(1))


  dom.document.getElementById("applyTransformationSettings").asInstanceOf[html.Input].onclick = (_: dom.MouseEvent) => {
    val (r, z) = transformationSettings
    DiamondGenerator.applyTransformation(r, z, z)
  }


}
