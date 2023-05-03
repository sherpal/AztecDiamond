package communication

import custommath.{NotRational, QRoot, Rational}
import diamond.{ComputePartitionFunctionWeight, CustomGenerationWeight, Diamond, GenerationWeight}
import diamond.DiamondType.DiamondTypeFromString
import dominoshuffingextension.DiamondGeneration
import exceptions.WrongParameterException
import messages.*

object Communicator {

  def apply(args: Array[String]): Unit =
    PlatformDependent.startReceiveMessages(args)

  def postMessage(message: Message): Unit =
    PlatformDependent.postMessage(message)

  def receiveMessage(message: Message): Unit = message match {
    case GenerateDiamondMessage(diamondTypeString, arg, memoryOptimized) =>
      postMessage(TestMessage(diamondTypeString))

      val diamondType = diamondTypeString.toDiamondType

      val t = new java.util.Date().getTime
      try {
        val diamondArguments = diamondType.transformArguments(arg).toTry.get

        def sendDiamond(diamond: Diamond): Unit = {
          postMessage(DiamondIsComputed())
          postMessage(
            DiamondMessage(
              diamondType.toString,
              new java.util.Date().getTime - t,
              arg,
              diamond.toArray.toArray
            )
          )
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

        val arg: diamondType.ArgType = diamondType.transformArguments(arguments).toTry.get

        val weights: ComputePartitionFunctionWeight = diamondType.makeComputationWeight(arg)

        val diamond: Diamond = diamondType.countingTilingDiamond(arg)

        val probability = diamond.probability(
          weights,
          status =>
            Communicator.postMessage(
              CountingComputationStatus(
                status
              )
            )
        )

        val diamondWeight = diamond.weightQRoot(weights)

        def qRootToInfo(q: QRoot): QRootMessage = q match {
          case q: Rational =>
            QRootMessage("Rational", q.numerator.toString, q.denominator.toString)
          case q: NotRational =>
            QRootMessage("NotRational", q.numerators.mkString(","), q.denominators.mkString(","))
        }

        val probabilityInfo   = qRootToInfo(probability)
        val diamondWeightInfo = qRootToInfo(diamondWeight)

        postMessage(
          TilingMessage(
            diamondTypeString,
            new java.util.Date().getTime - t,
            arguments,
            diamondWeightInfo,
            probabilityInfo
          )
        )

      } catch {
        case wrongParams: WrongParameterException =>
          postMessage(TilingWrongParameterException(wrongParams.msg))
        case _: NotImplementedError =>
          postMessage(NotImplementedTilingCounting(diamondTypeString))
        case e: Throwable =>
          throw e
      }
    case GenerateImageMessage(imageData, width, height) =>
      if (imageData.length != width * height * 4) {
        postMessage(
          WrongImageData(
            s"Length of image data (${imageData.length}) must be " +
              s"4 * w * h (${4 * width * height})"
          )
        )
      } else {
        val startTime = new java.util.Date().getTime

        val heightCutImageData = if (height % 2 == 1) imageData.dropRight(width) else imageData
        val transformedImageData =
          if (width % 2 == 0) heightCutImageData
          else {
            val idxToRemove = (0 until height).map(width * _)

            imageData.zipWithIndex.filter(elem => idxToRemove.contains(elem._2 / 4)).unzip._1
          }
        val weight = CustomGenerationWeight.fromImageData(
          transformedImageData,
          width,
          height
        )

        val allWeights = DiamondGeneration.computeAllWeights(weight)

        postMessage(WeightsAreComputed())

        val diamond = DiamondGeneration.generateDiamond(allWeights)

        postMessage(ImageDiamondMessage(diamond.toArray.toList, new java.util.Date().getTime - startTime))
      }
    case _ =>
      println(message.toString)

  }

}
