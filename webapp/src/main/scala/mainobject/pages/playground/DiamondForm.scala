package mainobject.pages.playground

import be.doeraene.webcomponents.ui5.*
import com.raquo.laminar.api.L.*
import diamond.DiamondType
import diamond.DiamondType.ArgumentName
import be.doeraene.webcomponents.ui5.configkeys.MessageStripDesign
import be.doeraene.webcomponents.ui5.configkeys.InputType
import be.doeraene.webcomponents.ui5.configkeys.ValueState
import exceptions.WrongParameterException

object DiamondForm {

  opaque type DefaultArgumentInfo = ArgumentName => Double
  val generationDefault: DefaultArgumentInfo     = _.defaultGenerationValue
  val tilingCountingDefault: DefaultArgumentInfo = _.defaultTilingCountingValue

  def apply(diamondType: DiamondType, infoFromArgument: DefaultArgumentInfo)(
      argumentObserver: Observer[Option[diamondType.ArgType]]
  ): HtmlElement = {
    val rawArgsVar: Var[Seq[Option[Double]]] = Var(diamondType.argumentNames.map(infoFromArgument).map(Some(_)))

    val modifyArgAtIndexObserver = rawArgsVar.updater[(Option[Double], Int)] { case (values, (value, index)) =>
      values.patch(index, List(value), 1)
    }

    val argOrErrorSignal = rawArgsVar.signal
      .map(
        _.foldLeft[Option[Seq[Double]]](Some(Seq.empty))((maybeAcc, maybeValue) =>
          for {
            acc   <- maybeAcc
            value <- maybeValue
          } yield acc :+ value
        )
      )
      .map(_.toRight(new WrongParameterException(s"Wrongly formed argument")).flatMap(diamondType.transformArguments))

    sectionTag(
      padding := "0.5em",
      h4(s"Choose the parameters for the ${diamondType.name}"),
      diamondType.argumentNames.zipWithIndex.map { case (argumentName, index) =>
        val label                   = argumentName.label
        val thisArgumentVar         = Var(infoFromArgument(argumentName).toString)
        val maybeThisArgumentSignal = thisArgumentVar.signal.map(_.toDoubleOption)
        div(
          display.flex,
          alignItems.center,
          marginBottom := "0.5em",
          Label(label, marginRight := "1em"),
          Input(
            _.tpe := InputType.Number,
            _.events.onChange.map(_.target.value) --> thisArgumentVar.writer,
            _.value <-- thisArgumentVar.signal,
            _.valueState <-- maybeThisArgumentSignal.map {
              case None    => ValueState.Error
              case Some(_) => ValueState.None
            },
            _.slots.valueStateMessage <-- maybeThisArgumentSignal.map {
              case None    => span("Malformed number")
              case Some(_) => span()
            }
          ),
          maybeThisArgumentSignal.map(_ -> index) --> modifyArgAtIndexObserver
        )
      },
      child.maybe <-- argOrErrorSignal
        .map(_.swap.toOption.map(_.msg))
        .map(_.map { errorMessage =>
          MessageStrip(
            _.hideCloseButton := true,
            _.design          := MessageStripDesign.Negative,
            errorMessage
          )
        }),
      argOrErrorSignal.map(_.toOption) --> argumentObserver
    )
  }

}
