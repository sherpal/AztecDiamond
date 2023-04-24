package communication

import java.io._
import java.net.{InetSocketAddress, Socket}

import messages._


private[communication] object PlatformDependent {

  private var address: String = _
  private var port: Int = _

  private val socket: Socket = new Socket()

  private lazy val output: DataOutputStream = new DataOutputStream(socket.getOutputStream)

  private def disconnect(): Unit = {
    output.close()
    socket.close()

    System.out.println("Task ended.")
  }

  def postMessage(message: Message): Unit = {
    val bytes = Message.encode(message)

    //output.writeInt(bytes.length)
    output.writeInt(bytes.length)
    output.write(bytes)


//    output.writeBytes("#" + bytes.mkString(",") + "#")
    Thread.sleep(1)

    message match {
      case _: DiamondMessage =>
        disconnect()
        //System.out.println("#" + Message.encode(message).mkString(",") + "#")
      case _: TilingMessage =>
        disconnect()
      case _: TilingWrongParameterException =>
        disconnect()
      case _: NotImplementedTilingCounting =>
        disconnect()
      case _: GenerationWrongParameterException =>
        disconnect()
        //System.out.println("#" + Message.encode(message).mkString(",") + "#")
      case _ =>
    }
  }

  def startReceiveMessages(args: Array[String]): Unit = {

    address = "localhost"
    port = args(0).toInt

    socket.connect(new InetSocketAddress(address, port))
    output.writeBytes("loaded")

    val message = Message.decode(args(1).split(",").map(_.toInt).map(_.toByte))

    try {
      Communicator.receiveMessage(message)
    } catch {
      case _: OutOfMemoryError =>
        System.err.println("outofmemory")
      case e: Throwable =>
        throw e
    }

//    System.out.println(message.toString)

//    socket.connect(new InetSocketAddress(address, port))
//
//    val out = new PrintWriter(socket.getOutputStream, true)
//    val in = new BufferedReader(new InputStreamReader(socket.getInputStream))

    //out.println(s"Received: ${Message.decode(in.readLine().toCharArray.map(_.toByte)).toString}")

  }

}
