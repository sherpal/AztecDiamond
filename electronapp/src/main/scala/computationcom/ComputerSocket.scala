package computationcom

import electron.{ElectronGlobals, IPCRenderer}
import messages.{DiamondMessage, Message, TilingMessage, WorkerLoaded}
import nodejs.{ChildProcess, Path}
import nodejs.net.{Net, TCPServer, TCPSocket}
import org.scalajs.dom
import ui.AlertBox

import scala.scalajs.js

trait ComputerSocket extends Computer {
  private val server: TCPServer = Net.createServer()

  private var active: Boolean = true

  private var childProcess: Option[ChildProcess] = None

  private var received: String = ""

  def deserializeMessage(data: String): List[Message] = {
    val regex = """#[^#]+#""".r

    received += data

    val messages = regex.findAllIn(received)
      .map(_.drop(1).dropRight(1))
      .map(_.split(",").map(_.toByte))
      .map(Message.decode)
      .toList

    received = regex.replaceAllIn(received, "")

    if (messages.exists(_.isInstanceOf[DiamondMessage]) || messages.exists(_.isInstanceOf[TilingMessage])) {
      IPCRenderer.send("flash-window")
    }

    messages
  }


  private def connectionCallback(socket: TCPSocket): Unit = {
    println("A socket connected")

    socket.on("data", (data: Any) => {
      if (data.toString == "loaded") {
        if (scala.scalajs.LinkingInfo.developmentMode) {
          println(data)
        }
        receiveMessage(WorkerLoaded())
      } else if (active) {
        deserializeMessage(data.toString).foreach(receiveMessage)
      }
    })
  }

  server.on("connection", (socket: TCPSocket) => connectionCallback(socket))

  server.listen(0, "localhost", () => {

    if (scala.scalajs.LinkingInfo.developmentMode) {
      println(s"server is bound to ${server.address().address}:${server.address().port} (${server.address().family})")
    }

    val directory = Path.join(ElectronGlobals.__dirname, "../scala/web-worker.jar")

    childProcess = Some(ChildProcess.spawn(
      "java", js.Array[String](
        "-jar", directory, server.address().port.toString, Message.encode(initialMessage).mkString(",")
      )
    ))

    childProcess.get.stderr.on("data", (data: Any) => {
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
              (if (this.isInstanceOf[DiamondGenerationSocket]) " or check the Optimize Memory button" else "") +
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
    })

    childProcess.get.stdout.on("data", (data: Any) => {
      if (scala.scalajs.LinkingInfo.developmentMode) {
        println("stdout -> " + data)
      }
    })

    childProcess.get.on("exit", (code: Int) => {
      if (scala.scalajs.LinkingInfo.developmentMode) {
        println(s"Child exitted with code $code.")
      }
    })

//    childProcess = Some(ChildProcess.exec(
//      s"java -jar ${'"'}$directory${'"'} ${server.address().port} " +
//        s"${Message.encode(initialMessage).mkString(",")}",
//      (error, stdout, stderr) => {
//        if (scala.scalajs.LinkingInfo.developmentMode) {
//          println("stdout -> " + stdout)
//          println("stderr -> " + stderr)
//        }
//
//         if (active && (error != null || !js.isUndefined(error))) {
//           end()
//           if (error.toString.toLowerCase.contains("outofmemoryerror")) {
//             AlertBox(
//               "Out Of Memory",
//               "The program assigned to compute apparently ran into an Out Of Memory exception. This is probabibly " +
//                 "due to asking a too greedy computation.<br>" +
//                 "You can try with smaller parameters" +
//                 (if (this.isInstanceOf[DiamondGenerationSocket]) " or check the Optimize Memory button" else "") +
//                 ".",
//               () => {}
//             )
//           } else {
//             AlertBox(
//               "Fatal Error",
//               "Wow... Something went really wrong. The program that was assigned to compute crashed.<br>" +
//                 "See below the error message. Don't hesitate to ask for help.<br>" +
//                 s"Error message:<br>$error",
//               () => {
//                 dom.console.error("Something went wrong in computer")
//               }
//             )
//           }
//        }
//
//        active = false
//
//      }
//    ))
  })


  if (scala.scalajs.LinkingInfo.developmentMode) {
    server.on("close", (_: Any) => {
      println("server closed")
    })
  }


  def terminateGenerator(): Unit = {
    server.close()

    active = false

    if (childProcess.isDefined) {
      childProcess.get.kill()
    }
  }

  def postMessage(message: Message): Unit = {} // nothing to do here, we pass the message in the exec

}
