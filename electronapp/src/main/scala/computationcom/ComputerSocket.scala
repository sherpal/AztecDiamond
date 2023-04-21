package computationcom

import electron.{ElectronGlobals, IPCRenderer}
import messages.{DiamondMessage, Message, TilingMessage, WorkerLoaded}
import nodejs.{Buffer, ChildProcess, Path}
import nodejs.net.{Net, TCPServer, TCPSocket}
import org.scalajs.dom
import ui.AlertBox

import scala.scalajs.js
import scala.scalajs.js.typedarray.{ArrayBuffer, Int8Array}

trait ComputerSocket extends Computer {
  private val server: TCPServer = Net.createServer()

  private var active: Boolean = true

  private var childProcess: Option[ChildProcess] = None

//  private var received: String = ""

  private var bytesToRead: Int = 0
  private val emptyBuffer: ArrayBuffer = new ArrayBuffer(0)
  private var bufferingBytes: Int8Array = new Int8Array(emptyBuffer)

  private def concat(a: Int8Array, b: Int8Array): Int8Array = {
    val c = new Int8Array(a.length + b.length)
    c.set(a)
    c.set(b, a.length)

    c
  }

  def deserializeMessage(data: ArrayBuffer = emptyBuffer): Unit = {

    bufferingBytes = concat(bufferingBytes, new Int8Array(data))

    if (bytesToRead == 0 && bufferingBytes.length >= 4) {
      bytesToRead = Buffer.from(bufferingBytes.buffer.slice(0, 4)).readInt32BE()
      bufferingBytes = new Int8Array(bufferingBytes.buffer.slice(4))
    }

    if (bufferingBytes.length >= bytesToRead && bytesToRead > 0) {
      receiveMessage(Message.decode(bufferingBytes.take(bytesToRead).toArray))
      bufferingBytes = new Int8Array(bufferingBytes.buffer.slice(bytesToRead))
      bytesToRead = 0

      if (bufferingBytes.nonEmpty) {
        deserializeMessage()
      }
    }

  }

//  def deserializeMessage(data: String): Unit = {
//    val regex = """#[^#]+#""".r
//
//    received += data
//
//    val messages = regex.findAllIn(received)
//      .map(_.drop(1).dropRight(1))
//      .map(_.split(",").map(_.toByte))
//      .map(Message.decode)
//      .toList
//
//    received = regex.replaceAllIn(received, "")
//
//    if (messages.exists(_.isInstanceOf[DiamondMessage]) || messages.exists(_.isInstanceOf[TilingMessage])) {
//      IPCRenderer.send("flash-window")
//    }
//
//    messages.foreach(receiveMessage)
//  }

  private def connectionCallback(socket: TCPSocket): Unit = {
    println("A socket connected")

    socket.on(
      "data",
      (data: Buffer) => {
        if (data.toString() == "loaded") {
          if (scala.scalajs.LinkingInfo.developmentMode) {
            println(data.toString())
          }
          receiveMessage(WorkerLoaded())
        } else if (active) {
          deserializeMessage(data.buffer)
        }
      }
    )
  }

  server.on("connection", (socket: TCPSocket) => connectionCallback(socket))

  server.listen(
    0,
    "localhost",
    () => {

      if (scala.scalajs.LinkingInfo.developmentMode) {
        println(
          s"server is bound to ${server.address().address}:${server.address().port} (${server.address().family})"
        )
      }

      val directory =
        Path.join(ElectronGlobals.__dirname, "../scala/web-worker.jar")

      childProcess = Some(
        ChildProcess.spawn(
          "java",
          js.Array[String](
            "-jar",
            directory,
            server.address().port.toString,
            Message.encode(initialMessage).mkString(",")
          )
        )
      )

      childProcess.get.stderr.on(
        "data",
        (data: Any) => {
          if (scala.scalajs.LinkingInfo.developmentMode) {
            println("stderr -> " + data)
          }
          if (active) {
            end()
            if (data.toString.toLowerCase.contains("outofmemory")) {
              AlertBox(
                "Out Of Memory",
                "The program assigned to compute apparently ran into an Out Of Memory exception. This is probabibly " +
                  "due to asking a too greedy computation.<br>" +
                  "You can try with smaller parameters" +
                  (if (this.isInstanceOf[DiamondGenerationSocket])
                     " or check the Optimize Memory button"
                   else "") +
                  ".",
                () => {}
              )
            } else {
              AlertBox(
                "Fatal Error",
                "Wow... Something went really wrong. The program that was assigned to compute crashed.<br>" +
                  "See below the error message. Don't hesitate to ask for help.<br>" +
                  s"Error message:<br>$data",
                () => {
                  dom.console.error("Something went wrong in computer")
                }
              )
            }
          }

          active = false
        }
      )

      childProcess.get.stdout.on(
        "data",
        (data: Any) => {
          if (scala.scalajs.LinkingInfo.developmentMode) {
            println("stdout -> " + data)
          }
        }
      )

      childProcess.get.on(
        "exit",
        (code: Int) => {
          if (scala.scalajs.LinkingInfo.developmentMode) {
            println(s"Child exitted with code $code.")
          }
        }
      )

    }
  )

  if (scala.scalajs.LinkingInfo.developmentMode) {
    server.on(
      "close",
      (_: Any) => {
        println("server closed")
      }
    )
  }

  def terminateGenerator(): Unit = {
    server.close()

    active = false

    if (childProcess.isDefined) {
      childProcess.get.kill()
    }
  }

  def postMessage(
      message: Message
  ): Unit = {} // nothing to do here, we pass the message in the exec

}
