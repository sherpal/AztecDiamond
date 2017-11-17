package mainobject

import computationcom.{DiamondGenerationWorker, TilingNumberCountingWorker}
import org.scalajs.dom

import scala.scalajs.js

object WebApp {

  def main(args: Array[String]): Unit = {

    println("This is a web application")

    AztecDiamond

    if (js.Dynamic.global.Worker == null) {
      dom.window.alert("Your Browser does not seem to support Web Workers." +
        "\n" +
        "You should update your browser and get the latest version if you want to use the Aztec Diamond generation." +
        " Sorry.")

    } else {
      DiamondGenerationWorker
      TilingNumberCountingWorker
    }
  }

}
