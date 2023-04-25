package mainobject.pages.playground

import be.doeraene.webcomponents.ui5.*
import com.raquo.laminar.api.L.*
import diamond.DiamondType
import diamond.DiamondType.*
import diamond.diamondtypes.UniformDiamond
import computationcom.AirstreamDiamondGenerator
import messages.GenerateDiamondMessage

object DiamondGeneration {

  def apply(diamondTypes: List[DiamondType]): HtmlElement = {
    val diamondTypeVar: Var[DiamondType] = Var(UniformDiamond)

    div(
      DiamondTypeSelector(diamondTypes, diamondTypeVar.writer),
      child <-- diamondTypeVar.signal.map { diamondType =>
        val maybeArgumentVar   = Var(Option.empty[diamondType.ArgType])
        val startGenerationBus = new EventBus[Unit]

        div(
          child.text <-- maybeArgumentVar.signal.map(_.toString),
          DiamondForm(diamondType, DiamondForm.generationDefault)(maybeArgumentVar.writer),
          Button(
            "Click me",
            _.disabled <-- maybeArgumentVar.signal.map(_.isEmpty),
            _.events.onClick.mapTo(()) --> startGenerationBus.writer
          ),
          startGenerationBus.events.sample(maybeArgumentVar.signal).collect { case Some(arg) =>
            arg
          } --> Observer[diamondType.ArgType](arg =>
            AirstreamDiamondGenerator.AirstreamDiamondGeneratorWorker(
              GenerateDiamondMessage(
                diamondType.toString,
                diamondType.transformArgumentsBack(arg).toVector,
                memoryOptimized = false
              )
            )
          )
        )
      }
    )
  }

}
