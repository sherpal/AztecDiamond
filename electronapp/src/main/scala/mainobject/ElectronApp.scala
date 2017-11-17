package mainobject

import computationcom.{DiamondGenerationSocket, DiamondGenerationWorker, TilingNumberCountingSocket, TilingNumberCountingWorker}
import electron.Shell
import nodejs.ChildProcess
import org.scalajs.dom
import org.scalajs.dom.html
import ui.AlertBox

import scala.scalajs.js


object ElectronApp {

  def main(args: Array[String]): Unit = {

    println("This is an electron application")

    AztecDiamond

    TilingNumberCountingSocket
    DiamondGenerationSocket

    ChildProcess.exec(
      "java -version",
      (error, _, stderr) => {
        if (error != null || js.isUndefined(error)) {
          AlertBox(
            "Java not installed",
            "You do not seem to have Java installed on your machine. You need to install it " +
            "in order to use Aztec Diamond desktop app. We will try to open a web Browser for you at the page " +
            "where you can download it. It may take a little while...<br>" +
            "We will use the online technology instead. Restart the app when Java is installed.<br>" +
            s"",//Error:<br>$error",
            () => {
              Shell.openExternal("https://java.com/en/download/")
            }
          )
          TilingNumberCountingWorker
          DiamondGenerationWorker
        } else if (scala.scalajs.LinkingInfo.developmentMode) {
          println(stderr)
        }
      }
    )

    val linkList = dom.document.getElementsByClassName("link")
    val linkListElements = (for (j <- 0 until linkList.length) yield linkList(j).asInstanceOf[html.Anchor]).toList

    linkListElements.foreach(anchor => {
      val href = anchor.href
      anchor.href = ""
      anchor.onclick = (event: dom.MouseEvent) => {
        event.preventDefault()

        Shell.openExternal(href)

        false
      }
    })


//
//    val message = DiamondMessage("UniformDiamond", 10, Vector(5.0), List(1,4,2))
//
//    println(Message.encode(message).mkString(","))
//    println(new String(Message.encode(message), StandardCharsets.ISO_8859_1))
//    println(new String(Message.encode(message), StandardCharsets.ISO_8859_1).getBytes(StandardCharsets.ISO_8859_1).mkString(","))
//
//
//    println(Message.encode(message).zip(
//      new String(Message.encode(message), StandardCharsets.ISO_8859_1).getBytes(StandardCharsets.ISO_8859_1)
//    ).forall(elem => elem._1 == elem._2))
  }

}
