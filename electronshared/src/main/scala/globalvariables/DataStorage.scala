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
      .addConcreteType[RGBData]

      .addConcreteType[DrawLine]
      .addConcreteType[DrawCircle]
      .addConcreteType[DrawEllipse]
      .addConcreteType[DrawFillCircle]
      .addConcreteType[DrawFillEllipse]
      .addConcreteType[DrawRectangle]
      .addConcreteType[DrawPolygon]

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
    Unpickle[Data](dataStoragePickler).fromBytes(ByteBuffer.wrap(buffer.toArray))

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
final case class RGBData(red: Int, green: Int, blue: Int) extends Data

abstract sealed class DrawMode extends Data
case class DrawLine() extends DrawMode
case class DrawCircle() extends DrawMode
case class DrawEllipse() extends DrawMode
case class DrawRectangle() extends DrawMode
case class DrawFillEllipse() extends DrawMode
case class DrawFillCircle() extends DrawMode
case class DrawPolygon() extends DrawMode