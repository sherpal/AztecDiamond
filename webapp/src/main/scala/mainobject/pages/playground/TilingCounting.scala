package mainobject.pages.playground

import be.doeraene.webcomponents.ui5.*
import com.raquo.laminar.api.L.*
import diamond.DiamondType
import diamond.DiamondType.*
import diamond.diamondtypes.UniformDiamond
import computationcom.AirstreamTilingNumberCounting
import be.doeraene.webcomponents.ui5.configkeys.BarDesign
import be.doeraene.webcomponents.ui5.configkeys.ButtonDesign
import computationcom.BlobMaker

import scala.concurrent.ExecutionContext.Implicits.global
import messages.CountingTilingMessage
import be.doeraene.webcomponents.ui5.configkeys.ValueState
import diamond.Diamond

import scala.scalajs.LinkingInfo

object TilingCounting {

  def apply(diamondTypes: List[DiamondType]): HtmlElement = {
    val diamondTypeVar: Var[DiamondType] = Var(UniformDiamond)

    div(
      DiamondTypeSelector(diamondTypes, diamondTypeVar.writer),
      child <-- diamondTypeVar.signal.map { diamondType =>
        val maybeArgumentVar        = Var(Option.empty[diamondType.ArgType])
        val startTilingBus          = new EventBus[Unit]
        val maybeCurrentComputerVar = Var(Option.empty[AirstreamTilingNumberCounting])
        val busySignal              = maybeCurrentComputerVar.signal.map(_.isDefined)

        val cancelBus = new EventBus[Unit]

        div(
          div(
            DiamondForm(diamondType, DiamondForm.tilingCountingDefault)(maybeArgumentVar.writer),
            child <-- maybeCurrentComputerVar.signal.changes.collect { case Some(worker) => worker }.map { worker =>
              div(
                padding := "1em",
                p(
                  child.text <-- worker.tilingEvents.map(displayTiming)
                ),
                p(
                  wordWrap.breakWord,
                  child.text <-- worker.tilingEvents.map { info =>
                    val partitionOrTiling =
                      if info.diamondType.designedForPartitionFunction then "Partition function"
                      else "number of tilings"
                    val number =
                      if info.isSubGraphPartitionInteger then info.subGraphPartition.toBigInt.toString
                      else info.subGraphPartition.toString
                    s"The $partitionOrTiling for diamonds of type ${info.diamondType.name} with total size ${info.diamondTypeWithArgs.diamondOrder} is $number${info.scientificNotation}."
                  }
                )
              )
            },
            Bar(
              width := "500px",
              _.design := BarDesign.Footer,
              _.slots.endContent := Button(
                "Count tilings",
                _.design    := ButtonDesign.Emphasized,
                _.disabled <-- maybeArgumentVar.signal.map(_.isEmpty).combineWithFn(busySignal)(_ || _),
                _.events.onClick.mapTo(()) --> startTilingBus.writer
              ),
              _.slots.endContent := Button(
                "Cancel",
                _.design    := ButtonDesign.Transparent,
                _.disabled <-- busySignal.map(!_),
                _.events.onClick.mapTo(()) --> cancelBus.writer,
                cancelBus.events.sample(maybeCurrentComputerVar.signal).collect { case Some(worker) =>
                  worker
                } --> AirstreamTilingNumberCounting.cancelObserver
              )
            )
          ),
          startTilingBus.events
            .sample(maybeArgumentVar.signal)
            .collect { case Some(arg) =>
              arg
            }
            .setDisplayName("Arguments for tiling counting")
            .debugLog(_ => LinkingInfo.developmentMode)
            .flatMap(arg =>
              EventStream.fromFuture(BlobMaker.fromFetch(utils.basePath ++ "js/gen/main.js")).map((arg, _))
            )
            .map((arg, blobMaker) =>
              Some(
                AirstreamTilingNumberCounting.airstreamTilingNumberCountingWorker(
                  CountingTilingMessage(
                    diamondType.toString,
                    diamondType.transformArgumentsBack(arg).toVector
                  ),
                  blobMaker
                )
              )
            ) --> maybeCurrentComputerVar.writer,
          maybeCurrentComputerVar.signal.changes
            .collect { case Some(worker) => worker }
            .flatMap(_.endedEvents)
            .mapTo(None) --> maybeCurrentComputerVar.writer,
          child.maybe <-- maybeCurrentComputerVar.signal.map(_.map { worker =>
            div(
              onUnmountCallback(_ => worker.cancel()),
              div(
                display.flex,
                alignItems.center,
                padding := "0.5em",
                Label("Local progress", marginRight := "1em"),
                ProgressIndicator(
                  _.value <-- worker.innerLoopStatusSignal.map(_.percentage),
                  _.valueState <-- worker.innerLoopStatusSignal
                    .map(_.ended)
                    .map(ended => if ended then ValueState.Success else ValueState.None),
                  width := "300px"
                )
              ),
              div(
                display.flex,
                alignItems.center,
                padding := "0.5em",
                Label("Overall progress", marginRight := "1em"),
                ProgressIndicator(
                  _.value <-- worker.outerLoopStatusSignal.map(_.percentage),
                  _.valueState <-- worker.outerLoopStatusSignal
                    .map(_.ended)
                    .map(ended => if ended then ValueState.Success else ValueState.None),
                  width := "300px"
                )
              )
            )
          })
        )
      }
    )
  }

  private def displayTiming(info: Diamond.DiamondCountingInfo): String = {
    val numberOfTilingsOrPartitionFunction =
      if info.diamondType.designedForPartitionFunction then "compute the partition function"
      else "count the number of tilings"
    s"It took ${DiamondGeneration.timeDisplay(info.timeTaken)} $numberOfTilingsOrPartitionFunction of a ${info.diamondType.name}."
  }

}
