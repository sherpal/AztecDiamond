package mainobject.pages.playground

import be.doeraene.webcomponents.ui5.*
import be.doeraene.webcomponents.ui5.configkeys.*
import com.raquo.laminar.api.L.*
import computationcom.{AirstreamDiamondGenerator, BlobMaker}
import diamond.DiamondType
import diamond.diamondtypes.UniformDiamond
import graphics.{Canvas2D, DiamondDrawer, DiamondDrawingOptions, DiamondMovieOptions}
import mainobject.components.{DiamondDrawingOptionsFormWrapper, ExportButton, ExportMovieButton}
import messages.GenerateDiamondMessage
import org.scalajs.dom

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.*
import scala.scalajs.LinkingInfo
import scala.util.matching.Regex
import scala.util.{Failure, Success}

object DiamondGeneration {

  def apply(diamondTypes: List[DiamondType]): HtmlElement = {
    val diamondTypeVar: Var[DiamondType] = Var(UniformDiamond)

    div(
      DiamondTypeSelector(diamondTypes, diamondTypeVar.writer),
      child <-- diamondTypeVar.signal.map { diamondType =>
        val maybeArgumentVar          = Var(Option.empty[diamondType.ArgType])
        val optimizeMemoryArgumentVar = Var(false)
        val startGenerationBus        = new EventBus[Unit]
        val maybeCurrentComputerVar   = Var(Option.empty[AirstreamDiamondGenerator])
        val busySignal                = maybeCurrentComputerVar.signal.map(_.isDefined)

        val cancelBus = new EventBus[Unit]

        div(
          div(
            DiamondForm(diamondType, DiamondForm.generationDefault)(maybeArgumentVar.writer),
            div(
              display.flex,
              alignItems.center,
              CheckBox(
                _.text     := "Optimize Memory usage (with huge performance loss)",
                _.checked <-- optimizeMemoryArgumentVar.signal,
                _.events.onChange.map(_.target.checked) --> optimizeMemoryArgumentVar.writer
              )
            ),
            Bar(
              width := "500px",
              _.design := BarDesign.Footer,
              _.slots.endContent := Button(
                "Generate",
                _.design := ButtonDesign.Emphasized,
                _.disabled <-- maybeArgumentVar.signal
                  .map(_.isEmpty)
                  .combineWithFn(busySignal)(_ || _),
                _.events.onClick.mapTo(()) --> startGenerationBus.writer
              ),
              _.slots.endContent := Button(
                "Cancel",
                _.design    := ButtonDesign.Transparent,
                _.disabled <-- busySignal.map(!_),
                _.events.onClick.mapTo(()) --> cancelBus.writer,
                cancelBus.events.sample(maybeCurrentComputerVar).collect { case Some(worker) =>
                  worker
                } --> AirstreamDiamondGenerator.cancelObserver
              )
            )
          ),
          startGenerationBus.events
            .sample(maybeArgumentVar.signal)
            .setDisplayName("Arguments for generation")
            .debugLog(_ => LinkingInfo.developmentMode)
            .collect { case Some(arg) =>
              arg
            }
            .withCurrentValueOf(optimizeMemoryArgumentVar.signal)
            .flatMap((arg, mem) =>
              EventStream.fromFuture(BlobMaker.fromFetch(utils.basePath ++ "js/gen/main.js")).map((arg, mem, _))
            )
            .map((arg, memoryOptimized, blobMaker) =>
              Some(
                AirstreamDiamondGenerator.airstreamDiamondGeneratorWorker(
                  GenerateDiamondMessage(
                    diamondType.toString,
                    diamondType.transformArgumentsBack(arg).toVector,
                    memoryOptimized = memoryOptimized
                  ),
                  blobMaker
                )
              )
            ) --> maybeCurrentComputerVar.writer,
          maybeCurrentComputerVar.signal.changes
            .collect { case Some(worker) =>
              worker
            }
            .flatMap(_.endedEvents)
            .mapTo(None) --> maybeCurrentComputerVar.writer,
          child.maybe <-- maybeCurrentComputerVar.signal.map(_.map { worker =>
            div(
              onUnmountCallback(_ => worker.cancel()),
              div(
                display.flex,
                alignItems.center,
                padding := "0.5em",
                Label("Weight computation", marginRight := "1em"),
                ProgressIndicator(
                  _.value <-- worker.weightComputationStatus.map(_.percentage),
                  _.valueState <-- worker.weightComputationStatus
                    .map(_.ended)
                    .map(ended => if ended then ValueState.Success else ValueState.None),
                  width := "300px"
                )
              ),
              div(
                display.flex,
                alignItems.center,
                padding := "0.5em",
                Label("Diamond generation", marginRight := "1em"),
                ProgressIndicator(
                  _.value <-- worker.generationComputationStatusSignal.map(_.percentage),
                  _.valueState <-- worker.generationComputationStatusSignal
                    .map(_.ended)
                    .map(ended => if ended then ValueState.Success else ValueState.Information),
                  width := "300px"
                )
              )
            )
          }),
          child <-- maybeCurrentComputerVar.signal.changes
            .collect { case Some(worker) =>
              worker
            }
            .flatMap(_.diamondEvent)
            .map { diamondGenerationInfo =>
              val diamond             = diamondGenerationInfo.diamond
              val diamondType         = diamondGenerationInfo.diamondType
              val diamondOrder        = diamondGenerationInfo.diamond.order
              val timeTaken           = diamondGenerationInfo.timeTaken
              val diamondTypeWithArgs = diamondGenerationInfo.diamondTypeWithArgs
              DiamondDrawer(
                diamondGenerationInfo.diamond,
                diamondGenerationInfo.diamondTypeWithArgs.isInDiamond
              ) match {
                case Some(diamondDrawer) =>
                  val optionsVar = Var(DiamondDrawingOptions.default(diamond, diamondType))

                  val openDrawingOptionsBus = new EventBus[Unit]
                  val downloadSvgBus        = new EventBus[Unit]

                  div(
                    Button(
                      _.design := ButtonDesign.Default,
                      "Change drawing options...",
                      _.icon := IconName.settings,
                      _.events.onClick.mapTo(()) --> openDrawingOptionsBus.writer
                    ),
                    DiamondDrawingOptionsFormWrapper(openDrawingOptionsBus.events, optionsVar.now(), optionsVar.writer),
                    div(
                      margin := "1em",
                      width  := "500px",
                      height := "500px",
                      canvasTag(
                        idAttr     := "canvas-for-the-diamond",
                        widthAttr  := 2000,
                        heightAttr := 2000,
                        width      := "500px",
                        height     := "500px",
                        onMountBind { ctx =>
                          val canvas = Canvas2D(ctx.thisNode.ref)
                          optionsVar.signal --> Observer(diamondDrawer.drawOnCanvas(canvas, _))
                        },
                        border := "2px solid black"
                      ),
                      optionsVar.signal.changes.debugLog(_ => LinkingInfo.developmentMode) --> Observer.empty
                    ),
                    div(
                      displayTiming(diamondType, diamondOrder, timeTaken, true)
                    ),
                    Bar(
                      _.slots.startContent :=
                        ExportButton(
                          () =>
                            utils.downloadSvgFile(
                              "diamond.svg",
                              diamondDrawer
                                .svgCode(diamondOrder, diamondGenerationInfo.diamondTypeWithArgs, optionsVar.now())
                            ),
                          () => {
                            val a = dom.document.createElement("a").asInstanceOf[dom.HTMLAnchorElement]
                            val dataUrl = dom.document
                              .getElementById("canvas-for-the-diamond")
                              .asInstanceOf[dom.HTMLCanvasElement]
                              .toDataURL("image/png")
                            a.href =
                              new Regex("^data:image/[^;]").replaceFirstIn(dataUrl, "data:application/octet-stream")
                            dom.document.body.appendChild(a)
                            a.style.display = "none"
                            a.setAttribute("download", "aztec-diamond.png")
                            a.click()
                            dom.document.body.removeChild(a)
                          },
                          diamond,
                          diamondTypeWithArgs
                        ),
                      _.slots.startContent <-- optionsVar.signal.map(options => ExportMovieButton(diamond, options))
                    )
                  )
                case None =>
                  MessageStrip(
                    _.hideCloseButton := true,
                    _.design          := MessageStripDesign.Negative,
                    "There was an error with creating the diamond drawer. This is not supposed to happen. Please check the console by pressing F12, and contact the author with the error message."
                  )
              }

            }
        )
      }
    )
  }

  def timeDisplay(timeTaken: FiniteDuration): String =
    if timeTaken < 1.second then timeTaken.toMillis.toString ++ "ms"
    else if timeTaken < 1.minute then timeTaken.toSeconds.toString ++ "s"
    else if timeTaken < 1.hour then s"${timeTaken.toMinutes} min ${timeTaken.toSeconds % 60}s"
    else s"${timeTaken.toMinutes} min"

  private def displayTiming(
      diamondType: DiamondType,
      diamondOrder: Int,
      timeTaken: FiniteDuration,
      generationOrWeightComputation: Boolean
  ): String =
    s"It took ${timeDisplay(timeTaken)} to generate a diamond of type $diamondType of total order $diamondOrder"

}
