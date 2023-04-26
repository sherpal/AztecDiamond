package mainobject.pages.playground

import be.doeraene.webcomponents.ui5.*
import com.raquo.laminar.api.L.*
import diamond.DiamondType

object Playground {

  def apply(): HtmlElement = TabContainer(
    _.tab(
      _.text := "Diamond Generation",
      DiamondGeneration(DiamondType.diamondTypes)
    )
  )

}
