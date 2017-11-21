package mainobject

import computationcom.{DiamondGenerationSocket, DiamondGenerationWorker, TilingNumberCountingSocket, TilingNumberCountingWorker}
import electron.Shell
import globalvariables.{AppVersion, DataStorage}
import nodejs.ChildProcess
import nodejs.https.{HTTPS, ServerResponse}
import org.scalajs.dom
import org.scalajs.dom.html
import ui.{AlertBox, ConfirmBox}

import scala.scalajs.js


object ElectronApp {

  private case class JavaVersion(major: Int, minor: Int) extends Ordered[JavaVersion] {

    override def compare(that: JavaVersion): Int = if (this.major != that.major) this.major - that.major
    else this.minor - that.minor

    override def toString: String = major + "." + minor

  }

  def main(args: Array[String]): Unit = {

    println("This is an electron application")

    if (scala.scalajs.LinkingInfo.developmentMode) {
      println("App version: " + DataStorage.retrieveGlobalValue("appVersion"))
    }

    AztecDiamond

    TilingNumberCountingSocket
    DiamondGenerationSocket

    def openJavaDownloadPage(): Unit = Shell.openExternal("https://java.com/en/download/")


    /**
     * Checks if Java is installed on the computer by using the java -version command.
     * Also checks if the version is at least 1.8.
     */
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
            () => openJavaDownloadPage()
          )
          TilingNumberCountingWorker
          DiamondGenerationWorker
        } else {
          if (scala.scalajs.LinkingInfo.developmentMode) {
            println(stderr)
          }


          try {
            val versionNumbers = """\d+\.\d+""".r.findFirstIn(stderr.toString).get.split("""\.""")
            val version = JavaVersion(versionNumbers(0).toInt, versionNumbers(1).toInt)

            val requiredVersion = JavaVersion(1, 8)

            if (version < requiredVersion) {
              ConfirmBox(
                "Old Java version",
                s"The Java version installed on your machine is $version. The app requires at least version " +
                  s"$requiredVersion to work.<br>" +
                  "We will use the online technology instead. Restart the app when Java is installed.<br>" +
                  s"You would like us to open the Java download page in your browser?",
                (answer: Boolean) => if (answer) {
                  openJavaDownloadPage()
                }
              )
              TilingNumberCountingWorker
              DiamondGenerationWorker
            }
          } catch {
            case e: Throwable =>
              if (scala.scalajs.LinkingInfo.developmentMode) {
                e.printStackTrace()
              }
              AlertBox(
                "Java not identified",
                "We were not able to identify the version of Java installed on your machine. The app might not be " +
                  "working.",
                () => {}
              )
          }
        }
      }
    )


    /**
     * Change the href of anchors by Shell.openExternal.
     */
    val linkList = dom.document.getElementsByClassName("link")
    val linkListElements = (for (j <- 0 until linkList.length) yield linkList(j).asInstanceOf[html.Anchor]).toList

    linkListElements.foreach(anchor => {
      val href = anchor.href
      if (href != "") {
        anchor.href = ""
        anchor.onclick = (event: dom.MouseEvent) => {
          event.preventDefault()

          Shell.openExternal(href)

          false
        }
      }
    })


    /**
     * Checks whether this version of the application is the latest one.
     */
    try {
      HTTPS.get("https://sites.uclouvain.be/aztecdiamond/version.txt", (response: ServerResponse) => {
        var received: String = ""

        response.on("data", (data: js.Any) => received += data)
        response.on("end", () => {
          val officialVersion = AppVersion.fromString(
            """version: #\d+\.\d+\.\d+#""".r.findFirstIn(received).get.drop("version: #".length).dropRight(1)
          )

          val appVersion = DataStorage.retrieveGlobalValue("appVersion").asInstanceOf[AppVersion]

          if (appVersion < officialVersion) {
            ConfirmBox(
              "New version",
              "A new version of the program is available. Would you like to be redirected to the GitHub website?",
              (answer: Boolean) => if (answer) {
                Shell.openExternal("https://github.com/sherpal/AztecDiamond/releases")
              }
            )
          }
        })
      })
    } catch {
      case e: Throwable =>
        if (scala.scalajs.LinkingInfo.developmentMode) {
          println("Could not verify app version.")
          e.printStackTrace()
        }
    }


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
