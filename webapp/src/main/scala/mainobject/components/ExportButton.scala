package mainobject.components

import be.doeraene.webcomponents.ui5.*
import com.raquo.laminar.api.L.*
import be.doeraene.webcomponents.ui5.configkeys.ButtonDesign
import be.doeraene.webcomponents.ui5.configkeys.IconName

import org.scalajs.dom

object ExportButton {

  private type Click = "svg" | "png"

  def apply(svgExport: () => Unit, pngExport: () => Unit): HtmlElement = {
    val openMenuBus = new EventBus[dom.HTMLElement]
    val clickBus    = new EventBus[Click]

    span(
      Button(
        _.design := ButtonDesign.Emphasized,
        "Export...",
        _.events.onClick.map(_.target) --> openMenuBus.writer,
        _.icon := IconName.download
      ),
      Menu(
        inContext(el => openMenuBus.events.map(el.ref -> _) --> Observer[(Menu.Ref, dom.HTMLElement)](_ showAt _)),
        _.item(_.text := "svg"),
        _.item(_.text := "png"),
        _.events.onItemClick.map(_.detail.text).map {
          case "svg" => "svg": Click
          case "png" => "png": Click
        } --> clickBus.writer
      ),
      clickBus.events --> Observer[Click] {
        case "svg" => svgExport()
        case "png" => pngExport()
      }
    )
  }

}
