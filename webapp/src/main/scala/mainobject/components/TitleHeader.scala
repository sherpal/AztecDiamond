package mainobject.components

import be.doeraene.webcomponents.ui5.*
import com.raquo.laminar.api.L.*

object TitleHeader {

  def apply(title: String): HtmlElement = div(
    width           := "100%",
    backgroundColor := "#673ab7",
    padding         := "1em",
    div(padding := "1em", Title.h3(title, color := "white"))
  )

}
