package mainobject

import computationcom.{DiamondGenerationWorker, TilingNumberCountingWorker}
import org.scalajs.dom
import org.scalajs.dom.html
import com.raquo.laminar.api.L.*
import be.doeraene.webcomponents.ui5.*
import mainobject.pages.*
import mainobject.components.*

import scala.scalajs.js
import scala.scalajs.js.timers.setTimeout

object WebApp {

  def main(args: Array[String]): Unit = {

    println("This is a web application")

    // AztecDiamond

    setTimeout(1000) {
      if (js.Dynamic.global.Worker == null) {
        dom.window.alert(
          "Your Browser does not seem to support Web Workers." +
            "\n" +
            "You should update your browser and get the latest version if you want to use the Aztec Diamond generation." +
            " Sorry."
        )

      } else {
        TilingNumberCountingWorker
      }
    }

    setTimeout(2000) {
      if isMobile then {
        dom.document.getElementById("mobileAlert").asInstanceOf[html.Element].style.display = "block"
      }
    }

    def isMobile: Boolean = List[String](
      "Android",
      "webOS",
      "iPhone",
      "iPad",
      "iPod",
      "BlackBerry",
      "Windows Phone"
    ).exists(dom.window.navigator.userAgent.contains)

    render(
      dom.document.getElementById("root"),
      div(
        className := (if isMobile then "" else "ui5-content-density-compact"),
        Header(Route.currentTitle, Route.routes),
        div(
          marginTop := "76px",
          Route.currentPage
        )
      )
    )

  }

}
