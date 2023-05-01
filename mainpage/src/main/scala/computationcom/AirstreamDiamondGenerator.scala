package computationcom

import diamond.Diamond
import diamond.DiamondType.DiamondTypeFromString
import messages.*
import computationcom.DiamondGenerator.*
import com.raquo.laminar.api.A.*

import scala.concurrent.duration.*

trait AirstreamDiamondGenerator(val initialMessage: Message) extends Computer {

  private var ended = false

  private val messageBus: EventBus[Message]       = new EventBus
  private val generatorCrashedBus: EventBus[Unit] = new EventBus
  private val generatorCancelBus: EventBus[Unit]  = new EventBus

  def messageSignal: Signal[Message]            = messageBus.events.startWith(initialMessage)
  def generatorCrashedEvents: EventStream[Unit] = generatorCrashedBus.events
  def generatorCancelEvents: EventStream[Unit]  = generatorCancelBus.events

  def weightComputationStatus: Signal[WeightComputationStatusInfo] = messageBus.events
    .collect {
      case WeightsAreComputed()                => WeightComputationStatusInfo.Ended
      case WorkerLoaded()                      => WeightComputationStatusInfo.Started
      case WeightComputationStatus(percentage) => WeightComputationStatusInfo.Ongoing(percentage)
    }
    .startWith(WeightComputationStatusInfo.Waiting)

  def generationComputationStatusSignal: Signal[GenerationComputationStatusInfo] = messageBus.events
    .collect {
      case _: DiamondMessage                    => GenerationComputationStatusInfo.Ended
      case DiamondComputationStatus(percentage) => GenerationComputationStatusInfo.Ongoing(percentage)
    }
    .startWith(GenerationComputationStatusInfo.Waiting)

  def diamondEvent: EventStream[Diamond.DiamondGenerationInfo] = messageSignal.changes.collect {
    case DiamondMessage(diamondTypeStr, timeTaken, rawArgs, diamondInfo) =>
      val diamondType = diamondTypeStr.toDiamondType
      val args        = diamondType.unsafeTransformArguments(rawArgs)
      val diamond     = Diamond(diamondInfo)
      Diamond.DiamondGenerationInfo(diamondType)(diamond, timeTaken.millis, args)
  }

  def endedEvents = EventStream
    .merge(
      generatorCrashedEvents.mapTo(true),
      messageSignal.changes.map {
        case DiamondMessage(diamondType, timeTaken, args, diamondInfo) => true
        case _                                                         => false
      },
      generatorCancelEvents.mapTo(true)
    )
    .filter(identity)
    .mapTo(())

  protected def receiveMessage(message: Message): Unit = {
    receivedMessage = true
    if !ended then {
      messageBus.writer.onNext(message)

      message match {
        case _: DiamondMessage => ended = true
        case _                 =>
      }
    }
  }

  protected def end(): Unit = {
    val wasEnded = ended
    ended = true

    if !wasEnded then generatorCrashedBus.writer.onNext(())
  }
  def cancel(): Unit = {
    val wasEnded = ended
    ended = true

    if !wasEnded then {
      kill(crashed = false)
      generatorCancelBus.writer.onNext(())
    }
  }

  def cancelObserver: Observer[Unit] = Observer(_ => cancel())

}

object AirstreamDiamondGenerator {

  private class AirstreamDiamondGeneratorWorker(initialMessage: Message, val blobMaker: BlobMaker)
      extends AirstreamDiamondGenerator(initialMessage)
      with ComputerWorker

  def airstreamDiamondGeneratorWorker(initialMessage: Message, blobMaker: BlobMaker): AirstreamDiamondGenerator =
    AirstreamDiamondGeneratorWorker(initialMessage, blobMaker)

  def cancelObserver: Observer[AirstreamDiamondGenerator] = Observer(_.cancel())

}
