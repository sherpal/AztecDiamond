package mainobject.components

import be.doeraene.webcomponents.ui5.*
import be.doeraene.webcomponents.ui5.configkeys.{ButtonDesign, IconName, InputType}
import com.raquo.laminar.api.L.*
import diamond.Diamond
import graphics.{Canvas2D, DiamondDrawer, DiamondDrawingOptions, DiamondMovieOptions}
import org.scalajs.dom
import org.scalajs.dom.CanvasRenderingContext2D
import utils.facades.zipjs.*
import utils.{toBlob, TimerLogger}

import scala.concurrent.duration.{DurationInt, FiniteDuration}
import scala.concurrent.{ExecutionContext, Future}

object ExportMovieButton {

  def apply(
      diamond: Diamond,
      options: DiamondDrawingOptions
  )(using executionContext: ExecutionContext): HtmlElement = {
    val currentlyDrawingDiamondOfOrderSignal = Var(Option.empty[Int])

    val imagesAreBeingGenerated = currentlyDrawingDiamondOfOrderSignal.signal.map(_.isDefined)

    val openDialogBus = new EventBus[Boolean]()

    val diamondMovieOptionsVar = Var(
      DiamondMovieOptions(
        resolution = 1600,
        diamondSizeTier = diamond.order,
        drawDominoBorderUntil = if options.showBorderOfDominoes then diamond.order else 0,
        fullSizedDiamondStartingFromOrder = diamond.order
      )
    )
    val resolutionUpdater =
      diamondMovieOptionsVar.updater[Int]((current, newResolution) => current.copy(resolution = newResolution))
    val sizeTierUpdater =
      diamondMovieOptionsVar.updater[Int]((current, tier) => current.copy(diamondSizeTier = tier))
    val drawDominoBorderUntilUpdater =
      diamondMovieOptionsVar.updater[Int]((current, limit) => current.copy(drawDominoBorderUntil = limit))
    val fullSizedDiamondStartingFromOrderUpdater =
      diamondMovieOptionsVar.updater[Int]((current, cutoff) => current.copy(fullSizedDiamondStartingFromOrder = cutoff))

    val startDownloadBus = new EventBus[Unit]()

    val cancelledVar = Var(false)

    val downloadEvents = startDownloadBus.events
      .sample(diamondMovieOptionsVar.signal)
      .flatMap(movieOptions =>
        cancelledVar.set(false)
        EventStream.fromFuture(
          generateMovieImages(
            diamond,
            options,
            movieOptions,
            currentlyDrawingDiamondOfOrderSignal.writer.contramap[Int](Some(_)),
            () => cancelledVar.now()
          )
        )
      )

    def formField(mods: Modifier[HtmlElement]*): HtmlElement = div(
      display.flex,
      alignItems.center,
      justifyContent.spaceBetween,
      mods
    )

    span(
      Button(
        _.design := ButtonDesign.Emphasized,
        "Export All Diamond Generation...",
        _.icon := IconName.download,
        _.events.onClick.mapTo(true) --> openDialogBus.writer
      ),
      Dialog(
        _.showFromEvents(openDialogBus.events.collect { case true => () }),
        _.closeFromEvents(openDialogBus.events.collect { case false => () }),
        _.slots.header := Title.h3("Download movie images"),
        div(
          formField(
            Label("Resolution (in px)"),
            Input(
              _.value <-- diamondMovieOptionsVar.signal.map(_.resolution.toString),
              _.tpe    := InputType.Number,
              _.events.onChange.map(_.target.value.toInt) --> resolutionUpdater
            )
          ),
          formField(
            Label("Keep domino size for how many orders"),
            Input(
              _.value <-- diamondMovieOptionsVar.signal.map(_.diamondSizeTier.toString),
              _.tpe    := InputType.Number,
              _.events.onChange.map(_.target.value.toInt) --> sizeTierUpdater
            )
          ),
          formField(
            Label("Draw domino border until order"),
            Input(
              _.value <-- diamondMovieOptionsVar.signal.map(_.drawDominoBorderUntil.toString),
              _.tpe    := InputType.Number,
              _.events.onChange.map(_.target.value.toInt) --> drawDominoBorderUntilUpdater
            )
          ),
          formField(
            Label("Full Size starting at order"),
            Input(
              _.value <-- diamondMovieOptionsVar.signal.map(_.fullSizedDiamondStartingFromOrder.toString),
              _.tpe    := InputType.Number,
              _.events.onChange.map(_.target.value.toInt) --> fullSizedDiamondStartingFromOrderUpdater
            )
          ),
          div(
            Button(
              "Download images...",
              _.events.onClick.mapToUnit --> startDownloadBus.writer,
              _.disabled <-- imagesAreBeingGenerated
            )
          ),
          div(
            display <-- imagesAreBeingGenerated.map(if _ then "block" else "none"),
            ProgressIndicator(
              _.value <-- currentlyDrawingDiamondOfOrderSignal.signal
                .map(_.getOrElse(diamond.order))
                .map(currentDiamondOrder =>
                  (100 * (diamond.order - currentDiamondOrder).toDouble / diamond.order).toInt
                )
            ),
            child.text <-- currentlyDrawingDiamondOfOrderSignal.signal
              .map(_.getOrElse(diamond.order))
              .map(currentOrder => s"Currently drawing order $currentOrder...")
          )
        ),
        _.slots.footer := Bar(
          _.slots.endContent <-- imagesAreBeingGenerated.map(generating =>
            if generating then Button("Cancel", _.events.onClick.mapTo(true) --> cancelledVar.writer)
            else Button("Close", _.events.onClick.mapTo(false) --> openDialogBus.writer)
          )
        )
      ),
      downloadEvents --> Observer[Unit](_ => currentlyDrawingDiamondOfOrderSignal.set(None))
    )
  }

  private def generateMovieImages(
      diamond: Diamond,
      options: DiamondDrawingOptions,
      movieOptions: DiamondMovieOptions,
      currentlyDrawingDiamondOfOrder: Observer[Int],
      cancelled: () => Boolean
  )(using executionContext: ExecutionContext): Future[Unit] = {
    given timerLogger: TimerLogger = if scala.scalajs.LinkingInfo.developmentMode then
      (label: String, time: FiniteDuration) => dom.console.log(s"Computing <$label>: ${time.toMillis}ms")
    else TimerLogger.noOp

    val canvas = dom.document.createElement("canvas").asInstanceOf[dom.HTMLCanvasElement]

    canvas.style.width = s"${movieOptions.resolution}px"
    canvas.style.height = canvas.style.width
    canvas.width = movieOptions.resolution
    canvas.height = movieOptions.resolution
    val canvasContext = canvas.getContext("2d").asInstanceOf[CanvasRenderingContext2D]

    def clearCanvas(): Unit =
      canvasContext.clearRect(0, 0, canvas.width, canvas.height)

    def drawStep(diamondToDraw: Diamond): Future[Unit] = {
      clearCanvas()
      DiamondDrawer(
        diamondToDraw,
        drawWithWatermark = false
      ) match {
        case Some(drawer) =>
          val zoomModifier =
            if diamondToDraw.order >= movieOptions.fullSizedDiamondStartingFromOrder then 1.0
            else {
              val diamondOrderForDominoSize =
                math.ceil(diamondToDraw.order / movieOptions.diamondSizeTier.toDouble) * movieOptions.diamondSizeTier
              diamondToDraw.order / diamondOrderForDominoSize
            }
          val zoom = options.transformations.zoom * zoomModifier
          currentlyDrawingDiamondOfOrder.onNext(diamondToDraw.order)
          timerLogger.time("draw diamond")(
            drawer.drawOnCanvas(
              Canvas2D(canvas, canvasContext),
              options
                .withZoom(zoom)
                .copy(showBorderOfDominoes = diamondToDraw.order <= movieOptions.drawDominoBorderUntil)
            )
          )
          utils.sleep(10.millis)
        case None =>
          throw IllegalStateException(s"Diamond was empty which should not have happened 😱")
      }
    }

    val zipWriter = ZipWriter(BlobWriter("application/zip"))

    def addDiamondToZip(diamondToDraw: Diamond): Future[Unit] = for {
      _          <- drawStep(diamondToDraw)
      canvasBlob <- canvas.toBlob
      filename = s"the-diamond-${String.format(s"%0${diamond.order.toString.length}d", diamondToDraw.order)}.png"
      _ <- zipWriter.add(filename, BlobReader(canvasBlob)).toFuture
    } yield ()

    def addAllDiamondsToZip(startingDiamond: Diamond): Future[Boolean] =
      addDiamondToZip(startingDiamond).flatMap { _ =>
        startingDiamond.randomSubDiamond match
          case None                             => Future.successful(true)
          case Some(subDiamond) if !cancelled() => addAllDiamondsToZip(subDiamond)
          case Some(_)                          => Future.successful(false)
      }

    val a = dom.document.createElement("a").asInstanceOf[dom.HTMLAnchorElement]

    for {
      generated <- addAllDiamondsToZip(diamond)
      zipBlob   <- zipWriter.close().toFuture
    } yield
      if generated then
        a.href = dom.URL.createObjectURL(zipBlob)
        dom.document.body.appendChild(a)
        a.style.display = "none"
        a.setAttribute("download", "aztec-diamond.zip")
        a.click()
        dom.document.body.removeChild(a)

  }

}
