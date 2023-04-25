package computationcom

import diamond.Diamond
import diamond.DiamondType.DiamondTypeFromString
import messages.*
import computationcom.DiamondGenerator.*
import com.raquo.laminar.api.A.*

trait AirstreamDiamondGenerator(val initialMessage: Message) extends Computer {

  private val messageBus: EventBus[Message]       = new EventBus
  private val generatorCrashedBus: EventBus[Unit] = new EventBus

  def messageSignal: Signal[Message]            = messageBus.events.startWith(initialMessage)
  def generatorCrashedEvents: EventStream[Unit] = generatorCrashedBus.events

  def weightComputationStatus: Signal[WeightComputationStatusInfo] = messageBus.events
    .collect {
      case WeightsAreComputed()                => WeightComputationStatusInfo.Ended
      case WorkerLoaded()                      => WeightComputationStatusInfo.Started
      case WeightComputationStatus(percentage) => WeightComputationStatusInfo.Ongoing(percentage)
    }
    .startWith(WeightComputationStatusInfo.Waiting)

  protected def receiveMessage(message: Message): Unit = {
    println(message)
    receivedMessage = true
    messageBus.writer.onNext(message)
  }

  protected def end(): Unit =
    generatorCrashedBus.writer.onNext(())

}

object AirstreamDiamondGenerator {

  final class AirstreamDiamondGeneratorWorker(initialMessage: Message)
      extends AirstreamDiamondGenerator(initialMessage)
      with ComputerWorker

}
