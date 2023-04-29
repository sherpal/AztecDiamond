package mainobject.components

import be.doeraene.webcomponents.ui5.*
import com.raquo.laminar.api.L.*
import graphics.DiamondDrawingOptions.*
import be.doeraene.webcomponents.ui5.configkeys.InputType

object TransformationForm {

  def apply(initialTransformation: Transformations, transformationObserver: Observer[Transformations]): HtmlElement = {
    val transformationsVar = Var(initialTransformation)
    div(
      Title.h4("Transform the drawing"),
      div(
        display.flex,
        alignItems.start,
        flexDirection := "column",
        Label("Rotation (in degrees)", marginRight := "1em"),
        Slider(
          _.value       := initialTransformation.rotationInDegrees,
          _.min         := 0,
          _.max         := 360,
          _.showTooltip := true,
          _.events.onChange.map(_.target.value) --> transformationsVar.updater[Double]((transformations, rotation) =>
            transformations.copy(rotationInDegrees = rotation)
          ),
          _.events.onInput.map(_.target.value) --> transformationsVar.updater[Double]((transformations, rotation) =>
            transformations.copy(rotationInDegrees = rotation)
          ),
          width := "400px"
        )
      ),
      div(
        display.flex,
        alignItems.center,
        Label("Zoom", marginRight := "1em"),
        Input(
          _.tpe   := InputType.Number,
          _.value := initialTransformation.zoom.toString,
          _.events.onChange.map(_.target.value).map(_.toDoubleOption.getOrElse(0.0)) --> transformationsVar
            .updater[Double]((transformations, zoom) => transformations.copy(zoom = zoom))
        )
      ),
      transformationsVar.signal --> transformationObserver
    )
  }

}
