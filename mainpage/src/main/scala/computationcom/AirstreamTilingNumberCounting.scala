package computationcom

import messages.Message
import com.raquo.laminar.api.A.*
import messages.TilingMessage
import diamond.Diamond
import diamond.DiamondType.*

import scala.concurrent.duration.*
import messages.NotImplementedTilingCounting
import messages.TilingWrongParameterException
import messages.ErrorMessage
import org.scalajs.dom
import messages.CountingComputationStatus
import messages.CountingComputationStatusSubroutine

trait AirstreamTilingNumberCounting(val initialMessage: Message) extends Computer {

  private var ended = false

  private val messageBus       = new EventBus[Message]
  private val workerCrashedBus = new EventBus[Unit]
  private val workerCancelBus  = new EventBus[Unit]
  private val naturalEndBus    = new EventBus[Unit]

  def messageSignal: Signal[Message]         = messageBus.events.startWith(initialMessage)
  def workerCrashedEvents: EventStream[Unit] = workerCrashedBus.events
  def workerCancelEvents: EventStream[Unit]  = workerCancelBus.events

  def tilingEvents: EventStream[Diamond.DiamondCountingInfo] = messageBus.events.collect {
    case TilingMessage(diamondTypeStr, timeTaken, args, weightInfo, probabilityInfo) =>
      val diamondType = diamondTypeStr.toDiamondType
      Diamond.DiamondCountingInfo(diamondType)(
        timeTaken.millis,
        diamondType.unsafeTransformArguments(args),
        weightInfo.toQRoot,
        probabilityInfo.toQRoot
      )
  }

  def outerLoopStatusSignal: Signal[CountingComputationStatus] =
    messageBus.events
      .collect { case message: CountingComputationStatus =>
        message
      }
      .startWith(CountingComputationStatus(0))

  def innerLoopStatusSignal: Signal[CountingComputationStatusSubroutine] =
    messageBus.events
      .collect { case message: CountingComputationStatusSubroutine =>
        message
      }
      .startWith(CountingComputationStatusSubroutine(0))

  def endedEvents = EventStream
    .merge(
      workerCrashedEvents.mapTo(true),
      naturalEndBus.events.mapTo(true),
      workerCancelEvents.mapTo(true)
    )
    .filter(identity)
    .mapTo(())

  protected def receiveMessage(message: Message): Unit = {
    receivedMessage = true
    if !ended then {
      messageBus.writer.onNext(message)

      def endFromMessage(): Unit = {
        ended = true
        naturalEndBus.writer.onNext(())
      }
      message match {
        case _: TilingMessage                 => endFromMessage()
        case _: NotImplementedTilingCounting  => endFromMessage()
        case _: TilingWrongParameterException => endFromMessage()
        case ErrorMessage(msg) =>
          dom.console.error(msg)
          endFromMessage()
        case _ =>
      }
    }
  }

  protected def end(): Unit = {
    val wasEnded = ended
    ended = true

    if !wasEnded then workerCrashedBus.writer.onNext(())
  }
  def cancel(): Unit = {
    val wasEnded = ended
    ended = true

    if !wasEnded then {
      kill(crashed = false)
      workerCancelBus.writer.onNext(())
    }
  }

  def cancelObserver: Observer[Unit] = Observer(_ => cancel())

}

object AirstreamTilingNumberCounting {

  private class AirstreamTilingNumberCountingWorker(initialMessage: Message, val blobMaker: BlobMaker)
      extends AirstreamTilingNumberCounting(initialMessage)
      with ComputerWorker

  def airstreamTilingNumberCountingWorker(
      initialMessage: Message,
      blobMaker: BlobMaker
  ): AirstreamTilingNumberCounting =
    AirstreamTilingNumberCountingWorker(initialMessage, blobMaker)

  def cancelObserver: Observer[AirstreamTilingNumberCounting] = Observer(_.cancel())

}
