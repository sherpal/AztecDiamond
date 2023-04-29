package mainobject.components

import be.doeraene.webcomponents.ui5.*
import com.raquo.laminar.api.L.*
import graphics.DiamondDrawingOptions

object DiamondDrawingOptionForm {

  def apply(initialOptions: DiamondDrawingOptions, optionsObserver: Observer[DiamondDrawingOptions]): HtmlElement = {
    val optionsVar = Var(initialOptions)

    val optionsSignal = optionsVar.signal

    def accessSignal[T](f: DiamondDrawingOptions => T)                      = optionsSignal.map(f)
    def modifier[T](f: (DiamondDrawingOptions, T) => DiamondDrawingOptions) = optionsVar.updater(f)

    div(
      minHeight := "300px",
      div(
        CheckBox(
          _.text     := "Draw dominoes",
          _.checked <-- accessSignal(_.drawDominoes),
          _.events.onChange.map(_.target.checked) --> modifier[Boolean]((options, checked) =>
            options.copy(drawDominoes = checked)
          )
        ),
        CheckBox(
          _.text     := "Show in full Aztec Diamond",
          _.checked <-- accessSignal(_.showInFullAztecDiamond),
          _.events.onChange.map(_.target.checked) --> modifier[Boolean]((options, checked) =>
            options.copy(showInFullAztecDiamond = checked)
          )
        ),
        CheckBox(
          _.text     := "Draw dominoes as lozenges",
          _.checked <-- accessSignal(_.drawDominoesAsLozenges),
          _.events.onChange.map(_.target.checked) --> modifier[Boolean]((options, checked) =>
            options.copy(drawDominoesAsLozenges = checked)
          )
        ),
        CheckBox(
          _.text     := "Show border of dominoes",
          _.checked <-- accessSignal(_.showBorderOfDominoes),
          _.events.onChange.map(_.target.checked) --> modifier[Boolean]((options, checked) =>
            options.copy(showBorderOfDominoes = checked)
          )
        ),
        CheckBox(
          _.text     := "Draw non-intersecting paths",
          _.checked <-- accessSignal(_.drawNonIntersectingPaths),
          _.events.onChange.map(_.target.checked) --> modifier[Boolean]((options, checked) =>
            options.copy(drawNonIntersectingPaths = checked)
          )
        )
      ),
      DominoColoursForm(
        initialOptions.colors,
        modifier((options, colors) => options.copy(colors = colors))
      ),
      TransformationForm(
        initialOptions.transformations,
        modifier((options, transformations) => options.copy(transformations = transformations))
      ),
      optionsSignal --> optionsObserver
    )
  }

}
