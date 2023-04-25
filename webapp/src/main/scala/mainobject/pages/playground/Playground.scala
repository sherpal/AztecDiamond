package mainobject.pages.playground

import com.raquo.laminar.api.L.*
import diamond.DiamondType

object Playground {

  def apply(): HtmlElement = div(
    DiamondGeneration(DiamondType.diamondTypes)
  )

}
