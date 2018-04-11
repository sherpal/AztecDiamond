package slidegenerator

import computationcom.{ComputerWorker, WrongMessageTypeException}
import diamond.Diamond
import diamond.DiamondType.DiamondTypeFromString
import graphics.DiamondDrawer
import messages._
import org.scalajs.dom
import popups.Alert

final class GeneratorWorker(
                             val initialMessage: Message,
                             generatorRequest: GeneratorRequest,
                             loadingBar: LoadingBar
                           ) extends ComputerWorker {

  loadingBar.setValue(0)
  loadingBar.setColor(255, 69, 0)

  protected def receiveMessage(message: Message): Unit = {
    receivedMessage = true

    message match {
      case WorkerLoaded() =>
        loadingBar.show()
      case message: TilingComputationMessage =>
        dom.console.warn("We are in diamond generator")
        dom.console.warn(message.toString)
        throw new WrongMessageTypeException(message.getClass.toString)
      case TestMessage(msg) =>
        if (scala.scalajs.LinkingInfo.developmentMode) {
          println(msg)
        }
      case ErrorMessage(error) =>
        if (scala.scalajs.LinkingInfo.developmentMode) {
          dom.console.error(error)
        }
      case DiamondMessage(diamondTypeString, time, args, diamondInfo) =>
        loadingBar.setValue(200)
        loadingBar.setColor(0, 255, 0)

        if (scala.scalajs.LinkingInfo.developmentMode) {
          println("Diamond has been computed.")
        }

        val diamondType = diamondTypeString.toDiamondType

        if (scala.scalajs.LinkingInfo.developmentMode) {
          println(s"It took $time ms to generate the diamond of type $diamondType in the web worker.")
        }

        val arg = diamondType.transformArguments(args)

        //val drawnDiamond = DiamondDrawer(Diamond(diamondInfo), diamondType.isInDiamond(arg))

        generatorRequest.setDiamond(Diamond(diamondInfo), diamondType.isInDiamond(arg))

        generatorRequest.drawDiamond()

        loadingBar.hide()

        endOfGenerator()
      case WeightComputationStatus(status) =>
        loadingBar.setValue(status)
      case DiamondIsComputed() =>
        if (scala.scalajs.LinkingInfo.developmentMode) {
          println("Diamond is computed.")
        }
      case WeightsAreComputed() =>
        loadingBar.setValue(math.max(loadingBar.value, 100))
      case DiamondComputationStatus(status) =>
        loadingBar.setValue(100 + status)
      case GenerationWrongParameterException(msg) =>
        Alert.showAlert(
          "Wrong parameters",
          msg
        )
        endOfGenerator()
      case _ =>
        dom.console.warn("We are in diamond generator")
        dom.console.warn(s"I don't know that message: `$message`")
        throw new WrongMessageTypeException(message.getClass.toString)
    }
  }

  protected def end(): Unit =
    endOfGenerator(crashed = true)


  private def endOfGenerator(crashed: Boolean = false): Unit = generatorRequest.endOfGenerator(crashed)
}

