package globalvariables



import java.nio.ByteBuffer

import boopickle.CompositePickler
import boopickle.Default._
import electron.IPCRenderer

import scala.scalajs.js
import scala.scalajs.js.JSConverters._


abstract sealed class Data

/**
 * The DataStorage object allows to store [[Data]] to the main process.
 * This is much more type safe than VariableStorage, which will probably be deleted in the future.
 */
object DataStorage {

  private implicit val dataStoragePickler: CompositePickler[Data] = {
    compositePickler[Data]
      .addConcreteType[BaseDirectory]
      .addConcreteType[WindowId]
      .addConcreteType[AppVersion]

  }

  def storeValue(key: String, data: Data): Unit = {
    IPCRenderer.sendSync("store-value", key, encode(data))
  }


  def retrieveValue(key: String): Data = {
    decode(IPCRenderer.sendSync("retrieve-value", key).asInstanceOf[js.Array[Byte]])
  }

  def retrieveGlobalValue(key: String): Data = {
    decode(IPCRenderer.sendSync("retrieve-global-value", key).asInstanceOf[js.Array[Byte]])
  }

  def storeGlobalValue(key: String, data: Data): Unit = {
    IPCRenderer.sendSync("store-global-value", key, encode(data))
  }

  def decode(buffer: scala.scalajs.js.Array[Byte]): Data =
    Unpickle[Data](using dataStoragePickler).fromBytes(ByteBuffer.wrap(buffer.toArray))

  def encode(message: Data): scala.scalajs.js.Array[Byte] = {
    val byteBuffer = Pickle.intoBytes(message)
    val array = new Array[Byte](byteBuffer.remaining())
    byteBuffer.get(array)
    array.toJSArray
  }

  def unStoreValue(key: String): Unit = {
    IPCRenderer.sendSync("unStore-value", key)

  }

}


final case class BaseDirectory(directory: String) extends Data
final case class WindowId(id: Int) extends Data
final case class AppVersion(major: Int, minor: Int, update: Int) extends Data with Ordered[AppVersion] {
  override def compare(that: AppVersion): Int =
    if (that.major != this.major) this.major - that.major
    else if (that.minor != this.minor) this.minor - this.minor
    else this.update - that.update
}
object AppVersion {
  def fromString(string: String): AppVersion = {
    val numbers = """\d+""".r.findAllIn(string).toArray
    AppVersion(numbers(0).toInt, numbers(1).toInt, numbers(2).toInt)
  }
}