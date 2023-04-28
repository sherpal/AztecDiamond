package mainobject.components

import be.doeraene.webcomponents.ui5.*
import com.raquo.laminar.api.L.*
import graphics.DiamondDrawingOptions
import be.doeraene.webcomponents.ui5.configkeys.ButtonDesign

object DiamondDrawingOptionsFormWrapper {

  def apply(
      openEvents: EventStream[Unit],
      initialOptions: DiamondDrawingOptions,
      optionsChangedObserver: Observer[DiamondDrawingOptions]
  ): HtmlElement = {
    val optionsVar = Var(initialOptions)

    val closeBus = new EventBus[Boolean]

    val closeSetOptionsEvents = closeBus.events.filter(identity).sample(optionsVar.signal)

    div(
      Dialog(
        _.showFromEvents(openEvents),
        closeSetOptionsEvents --> optionsChangedObserver,
        _.closeFromEvents(closeBus.events.mapTo(())),
        _.slots.header := Bar(_.slots.startContent := Title.h2("Drawing Options")),
        DiamondDrawingOptionForm(initialOptions, optionsVar.writer),
        _.slots.footer := Bar(
          _.slots.endContent := Button(
            _.design := ButtonDesign.Emphasized,
            "Set Options",
            _.events.onClick.mapTo(true) --> closeBus.writer
          ),
          _.slots.endContent := Button(
            "Cancel",
            _.events.onClick.mapTo(false) --> closeBus.writer
          )
        )
      )
    )

  }

}
