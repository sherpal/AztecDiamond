package mainobject.pages.playground

import be.doeraene.webcomponents.ui5.*
import com.raquo.laminar.api.L.*
import diamond.DiamondType
import mainobject.components.TitleHeader

object Playground {

  def apply(): HtmlElement = div(
    TitleHeader("Playground"),
    TabContainer(
      _.tab(
        _.text := "Diamond Generation",
        DiamondGeneration(DiamondType.diamondTypes)
      ),
      _.tab(
        _.text := "Tiling Counting",
        TilingCounting(DiamondType.diamondTypes)
      )
    )
  )

}
