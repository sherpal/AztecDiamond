package messages

import java.nio.ByteBuffer

import boopickle.CompositePickler
import boopickle.Default._
import custommath.QRoot

object Message {
  implicit val messagePickler: CompositePickler[Message] = compositePickler[Message]
    .addConcreteType[TestMessage]
    .addConcreteType[ErrorMessage]
    .addConcreteType[WorkerLoaded]
    .addConcreteType[GenerateDiamondMessage]
    .addConcreteType[DiamondMessage]
    .addConcreteType[WeightComputationStatus]
    .addConcreteType[WeightsAreComputed]
    .addConcreteType[DiamondIsComputed]
    .addConcreteType[DiamondComputationStatus]
    .addConcreteType[GenerationWrongParameterException]
    .addConcreteType[CountingTilingMessage]
    .addConcreteType[QRootMessage]
    .addConcreteType[TilingMessage]
    .addConcreteType[TilingWrongParameterException]
    .addConcreteType[NotImplementedTilingCounting]
    .addConcreteType[CountingComputationStatus]
    .addConcreteType[CountingComputationStatusSubroutine]
    .addConcreteType[GenerateImageMessage]
    .addConcreteType[ImageDiamondMessage]

  def decode(buffer: Array[Byte]): Message =
    Unpickle[Message](messagePickler).fromBytes(ByteBuffer.wrap(buffer))

  def encode(message: Message): Array[Byte] = {
    val byteBuffer = Pickle.intoBytes(message)
    val array      = new Array[Byte](byteBuffer.remaining())
    byteBuffer.get(array)
    array
  }

}

sealed trait Message

final case class TestMessage(msg: String) extends Message

final case class ErrorMessage(error: String) extends Message

final case class WorkerLoaded() extends Message

sealed trait DiamondGenerationMessage extends Message

final case class GenerateDiamondMessage(
    diamondType: String,
    args: Vector[Double],
    memoryOptimized: Boolean
) extends DiamondGenerationMessage

final case class DiamondMessage(
    diamondType: String,
    timeTaken: Long,
    args: Vector[Double],
    diamondInfo: Array[Int]
) extends DiamondGenerationMessage

final case class WeightComputationStatus(percentage: Int) extends DiamondGenerationMessage

final case class WeightsAreComputed() extends DiamondGenerationMessage

final case class DiamondIsComputed() extends DiamondGenerationMessage

final case class DiamondComputationStatus(percentage: Int) extends DiamondGenerationMessage

final case class GenerationWrongParameterException(errorMessage: String) extends DiamondGenerationMessage

sealed trait TilingComputationMessage extends Message

final case class CountingTilingMessage(
    diamondType: String,
    args: Vector[Double]
) extends TilingComputationMessage

final case class QRootMessage(className: String, num: String, den: String) extends TilingComputationMessage {
  def toQRoot: QRoot = className match {
    case "Rational" =>
      QRoot(BigInt(num), BigInt(den))
    case "NotRational" =>
      throw new NotImplementedError
  }
}

final case class TilingMessage(
    diamondType: String,
    timeTaken: Long,
    args: Vector[Double],
    weightInfo: QRootMessage,
    probabilityInfo: QRootMessage
) extends TilingComputationMessage

final case class TilingWrongParameterException(errorMessage: String) extends TilingComputationMessage

final case class NotImplementedTilingCounting(message: String) extends TilingComputationMessage

final case class CountingComputationStatus(percentage: Int) extends TilingComputationMessage {
  def ended: Boolean = percentage >= 100
}

final case class CountingComputationStatusSubroutine(percentage: Int) extends TilingComputationMessage {
  def ended: Boolean = percentage >= 100
}

sealed trait ImageDiamond extends Message

final case class GenerateImageMessage(imageData: Vector[Int], width: Int, height: Int) extends ImageDiamond

final case class ImageDiamondMessage(diamondInfo: List[Int], timeTaken: Long) extends ImageDiamond

final case class WrongImageData(msg: String) extends ImageDiamond
