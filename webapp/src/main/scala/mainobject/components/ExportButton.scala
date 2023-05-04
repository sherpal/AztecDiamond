package mainobject.components

import be.doeraene.webcomponents.ui5.*
import com.raquo.laminar.api.L.*
import be.doeraene.webcomponents.ui5.configkeys.ButtonDesign
import be.doeraene.webcomponents.ui5.configkeys.IconName

import org.scalajs.dom
import diamond.Diamond
import diamond.DiamondType.DiamondTypeWithArgs
import dominoexports.JsonSupport

object ExportButton {

  private type Click = "svg" | "png" | "json"

  def apply(
      svgExport: () => Unit,
      pngExport: () => Unit,
      diamond: Diamond,
      diamondTypeWithArgs: DiamondTypeWithArgs
  ): HtmlElement = {
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
        _.item(_.text := "json"),
        _.events.onItemClick.map(_.detail.text).map {
          case "svg"  => "svg": Click
          case "png"  => "png": Click
          case "json" => "json": Click
        } --> clickBus.writer
      ),
      clickBus.events --> Observer[Click] {
        case "svg" => svgExport()
        case "png" => pngExport()
        case "json" =>
          utils.downloadJson("diamond.json", JsonSupport.DiamondWithMetadata(diamond, diamondTypeWithArgs))
      }
    )
  }

}
