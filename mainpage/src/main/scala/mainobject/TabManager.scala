package mainobject

import org.scalajs.dom
import org.scalajs.dom.html

object TabManager {

  private def openTab(button: html.Button): Unit = {

    val div = dom.document.getElementById(button.id.dropRight(3) + "Div").asInstanceOf[html.Div]
    val wasOpen = div.style.display == "block"

    val tabContents = dom.document.getElementsByClassName("tabContent")
    for (j <- 0 until tabContents.length) {
      tabContents(j).asInstanceOf[html.Element].style.display = "none"
    }

    val tabLinks = dom.document.getElementsByClassName("tabLinks")
    for (j <- 0 until tabLinks.length) {
      tabLinks(j).asInstanceOf[html.Element].className =
        """ active""".r.replaceAllIn(tabLinks(j).asInstanceOf[html.Element].className, "")
    }

    if (!wasOpen) {
      div.style.display = "block"
      button.className += " active"
    }
  }

  private val tabButtons = dom.document.getElementsByClassName("tab")(0).asInstanceOf[html.Div].children

  (for (j <- 0 until tabButtons.length) yield tabButtons(j).asInstanceOf[html.Button])
    .foreach(button => button.onclick = (_: dom.MouseEvent) => openTab(button))


}
