package communication

import custommath.{NotRational, QRoot, Rational}
import diamond.{ComputePartitionFunctionWeight, Diamond, GenerationWeight}
import diamond.DiamondType.DiamondTypeFromString
import dominoshuffingextension.{DiamondGeneration, TilingNumberCounting}
import exceptions.WrongParameterException
import messages._

object Communicator {

  def apply(args: Array[String]): Unit = {
    PlatformDependent.startReceiveMessages(args)
  }

  def postMessage(message: Message): Unit = {
    PlatformDependent.postMessage(message)
  }

  def receiveMessage(message: Message): Unit = message match {
    case GenerateDiamondMessage(diamondTypeString, arg, memoryOptimized) =>
      val diamondType = diamondTypeString.toDiamondType

      val t = new java.util.Date().getTime
      try {
        val diamondArguments = diamondType.transformArguments(arg)

        def sendDiamond(diamond: Diamond): Unit = {
          postMessage(DiamondIsComputed())
          postMessage(DiamondMessage(
            diamondType.toString, new java.util.Date().getTime - t, arg, diamond.toArray.toList
          ))
        }

        if (memoryOptimized) {
          val diamond = DiamondGeneration.generateDiamondMemoryOptimized(
            diamondType.makeGenerationWeight(diamondArguments)
          )
          sendDiamond(diamond)
        } else {
          val weights: GenerationWeight = diamondType.makeGenerationWeight(diamondArguments)

          val allWeights = DiamondGeneration.computeAllWeights(weights)

          postMessage(WeightsAreComputed())

          sendDiamond(DiamondGeneration.generateDiamond(allWeights))
        }
      } catch {
        case wrongParams: WrongParameterException =>
          postMessage(GenerationWrongParameterException(wrongParams.msg))
        case e: Throwable =>
          throw e
      }
    case CountingTilingMessage(diamondTypeString, arguments) =>
      val t = new java.util.Date().getTime
      try {
        val diamondType = diamondTypeString.toDiamondType

        val arg: diamondType.ArgType = diamondType.transformArguments(arguments)

        val weights: ComputePartitionFunctionWeight = diamondType.makeComputationWeight(arg)

        val diamond: Diamond = diamondType.countingTilingDiamond(arg)

        val probability = TilingNumberCounting.probability(diamond, weights)

        val diamondWeight = diamond.weightQRoot(weights)

        def qRootToInfo(q: QRoot): QRootMessage = q match {
          case q: Rational =>
            QRootMessage("Rational", q.numerator.toString, q.denominator.toString)
          case q: NotRational =>
            QRootMessage("NotRational", q.numerators.mkString(","), q.denominators.mkString(","))
        }

        val probabilityInfo = qRootToInfo(probability)
        val diamondWeightInfo = qRootToInfo(diamondWeight)

        postMessage(TilingMessage(
          diamondTypeString, new java.util.Date().getTime - t, arguments, diamondWeightInfo, probabilityInfo
        ))

      } catch {
        case wrongParams: WrongParameterException =>
          postMessage(TilingWrongParameterException(wrongParams.msg))
        case _: NotImplementedError =>
          postMessage(NotImplementedTilingCounting(diamondTypeString))
        case e: Throwable =>
          throw e
      }
    case _ =>
      println(message.toString)

  }

}
