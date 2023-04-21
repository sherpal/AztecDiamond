package computationcom

import custommath.QRoot
import diamond.DiamondType.DiamondTypeFromString
import messages._
import org.scalajs.dom
import org.scalajs.dom.html
import ui.{CountingTilingForm, StatusBar}

/** TilingCounting is the Computer that will communicate for counting tilings.
  */
trait TilingCounting extends Computer {
  import TilingCounting._

  protected def end(): Unit =
    endOfGenerator()

  protected def receiveMessage(message: Message): Unit = {
    receivedMessage = true

    message match {
      case WorkerLoaded() =>
        computePartitionInfo.textContent =
          "Worker loaded, starting computation..."
      case _: DiamondGenerationMessage =>
        dom.console.warn("We are in tiling counting")
        dom.console.warn(message.toString)
        throw new WrongMessageTypeException(message.getClass.toString)
      case TestMessage(msg) =>
        if (scala.scalajs.LinkingInfo.developmentMode) {
          println(msg)
        }
      case TilingMessage(
            diamondTypeString,
            time,
            args,
            weightInfo,
            probabilityInfo
          ) =>
        val diamondType = diamondTypeString.toDiamondType
        val arguments = diamondType.transformArguments(args)

        val probability = probabilityInfo.toQRoot
        val weight = weightInfo.toQRoot

        val partitionFunction = weight / probability

        val subGraphPartition = diamondType.totalPartitionFunctionToSubGraph(
          arguments,
          partitionFunction
        )

        val isInteger =
          subGraphPartition equals QRoot.fromBigInt(subGraphPartition.toBigInt)

        val scientificNotation: String =
          if (!isInteger) ""
          else {
            val stringNbr = subGraphPartition.toBigInt.toString
            if (stringNbr.length < 6) ""
            else {
              " (" ++ (stringNbr(0).toString ++ "." ++ stringNbr.slice(
                1,
                4
              ) ++ "e+" ++ (stringNbr.length - 1).toString) + ")"
            }

          }

        computePartitionInfo.style.color = "black"
        computePartitionInfo.innerHTML =
          (s"Computation completed. It took ${time / 1000.0} s to complete.<br>" +
            s"The ${if (diamondType.designedForPartitionFunction) "Partition function"
              else "number of tilings"}" +
            s" for diamonds of type ${diamondType.name} is ${if (isInteger) subGraphPartition.toBigInt
              else subGraphPartition}" + scientificNotation + ".").stripMargin
        outerLoopStatusBar.setValue(100)
        outerLoopStatusBar.setColor(0, 255, 0)
        innerLoopStatusBar.setValue(100)
        innerLoopStatusBar.setColor(0, 255, 0)

        if (scala.scalajs.LinkingInfo.developmentMode) {
          println(
            s"It took $time ms to compute the number of tilings (args: $args)."
          )
        }

        endOfGenerator()
      case TilingWrongParameterException(errorMessage) =>
        if (scala.scalajs.LinkingInfo.developmentMode) {
          dom.console.warn(errorMessage)
        }
        computePartitionInfo.textContent = errorMessage
        computePartitionInfo.style.color = "red"
        endOfGenerator()
      case NotImplementedTilingCounting(msg) =>
        if (scala.scalajs.LinkingInfo.developmentMode) {
          dom.console.warn(msg)
        }
        computePartitionInfo.textContent =
          msg.toDiamondType.name + " type of tiling is not yet implemented. Be Patient."
        computePartitionInfo.style.color = "rgb(176,112,0)"
        endOfGenerator()
      case CountingComputationStatus(status) =>
        if (scala.scalajs.LinkingInfo.developmentMode) {
          println("outer-loop")
        }
        outerLoopStatusBar.setValue(status)
      case CountingComputationStatusSubroutine(status) =>
        if (scala.scalajs.LinkingInfo.developmentMode) {
          println("inner-loop")
        }
        innerLoopStatusBar.setValue(status)
      case ErrorMessage(msg) =>
        dom.console.error(msg)
        dom.window.alert(msg)
      case _ =>
        dom.console.warn(message.toString)
    }
  }

}

object TilingCounting {

  private var workingComputer: Option[TilingCounting] = None

  val startCountingButton: html.Input = dom.document
    .getElementById("computePartitionStart")
    .asInstanceOf[html.Input]
  val cancelButton: html.Input = dom.document
    .getElementById("computePartitionCancel")
    .asInstanceOf[html.Input]
  cancelButton.disabled = true

  val computePartitionInfo: html.Paragraph =
    dom.document
      .getElementById("computePartitionInfo")
      .asInstanceOf[html.Paragraph]

  private def endOfGenerator(): Unit = {
    if (workingComputer.isDefined) {
      workingComputer.get.kill()
      workingComputer = None
      cancelButton.disabled = true
      startCountingButton.disabled = false
    }
  }

  cancelButton.onclick = (_: dom.MouseEvent) => {
    endOfGenerator()
  }

  val outerLoopStatusBar: StatusBar = StatusBar(0, 100, 200, 20)
  outerLoopStatusBar.setWithText(enabled = true)
  outerLoopStatusBar.setColor(255, 69, 0)
  outerLoopStatusBar.setParent(
    dom.document
      .getElementById("countingStatusBarContainer")
      .asInstanceOf[html.Element]
  )

  val innerLoopStatusBar: StatusBar = StatusBar(0, 100, 200, 20)
  innerLoopStatusBar.setWithText(enabled = true)
  innerLoopStatusBar.setColor(255, 69, 0)
  innerLoopStatusBar.setParent(
    dom.document
      .getElementById("countingStatusBarContainer")
      .asInstanceOf[html.Element]
  )

  private[computationcom] def compute(
      computer: (Message) => TilingCounting
  ): Unit = {
    CountingTilingForm.args match {
      case Some(arguments) =>
        startCountingButton.disabled = true
        cancelButton.disabled = false

        workingComputer = Some(
          computer(
            CountingTilingMessage(
              CountingTilingForm.diamondType.toString,
              arguments.toVector
            )
          )
        )

        computePartitionInfo.textContent = "Loading worker..."
        computePartitionInfo.style.color = "black"

        outerLoopStatusBar.setValue(0)
        outerLoopStatusBar.setColor(255, 69, 0)
        innerLoopStatusBar.setValue(0)
        innerLoopStatusBar.setColor(255, 69, 0)
      case None =>
        computePartitionInfo.textContent = "Malformed number arguments."
        computePartitionInfo.style.color = "red"
        if (scala.scalajs.LinkingInfo.developmentMode) {
          dom.console.error("Malformed number arguments.")
        }
    }

  }

}
