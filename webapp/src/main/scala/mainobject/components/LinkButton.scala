package mainobject.components

import be.doeraene.webcomponents.ui5.*
import com.raquo.laminar.api.L.*
import be.doeraene.webcomponents.ui5.configkeys.ButtonDesign

object LinkButton {

  def apply(text: String, hrefString: String): Mod[HtmlElement] = {
    val clickBus = new EventBus[Unit]
    List[Mod[HtmlElement]](
      Button(text, _.events.onClick.mapTo(()) --> clickBus.writer, _.design := ButtonDesign.Emphasized),
      a(
        href := hrefString,
        inContext(el => clickBus.events --> Observer[Unit](_ => el.ref.click()))
      )
    )
  }

}
