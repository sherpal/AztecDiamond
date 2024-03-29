package mainobject.components

import com.raquo.laminar.api.L.*
import mainobject.pages.Route

object Header {

  def apply(selectedPage: String, routes: List[Route]): HtmlElement = div(
    position.fixed,
    right           := "0",
    left            := "0",
    top             := "0",
    zIndex          := 1030,
    backgroundColor := "#341d5b",
    display.flex,
    justifyContent.spaceBetween,
    alignItems.center,
    color   := "white",
    padding := "0.5em",
    height  := "65px",
    a(className := "navbar-brand", "Aztec Diamond", href := routes.find(_.title == "Home").get.path),
    div(
      routes.filterNot(_.title == "Home").map { route =>
        val title = route.title
        a(
          className := (if title == selectedPage then "active" else ""),
          className := "navbar-brand",
          title.toUpperCase(),
          fontSize := "14px",
          href     := route.path
        )
      }
    )
  )

}
