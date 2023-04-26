package mainobject.pages.playground

import be.doeraene.webcomponents.ui5.*
import com.raquo.laminar.api.L.*
import diamond.DiamondType
import diamond.diamondtypes.UniformDiamond

object DiamondTypeSelector {

  def apply(diamondTypes: List[DiamondType], selectedTypeObserver: Observer[DiamondType]): HtmlElement = {
    val typesFromName = diamondTypes.map(tpe => tpe.name -> tpe).toMap

    val selectedTypeVar: Var[DiamondType] = Var(UniformDiamond)

    div(
      display.flex,
      alignItems.center,
      padding := "0.5em",
      Label(marginRight := "1em", "Select type of diamond"),
      Select(
        selectedTypeVar.signal --> selectedTypeObserver,
        diamondTypes.map(diamondType => Select.option(diamondType.name, dataAttr("name") := diamondType.name)),
        _.events.onChange
          .map(_.detail.selectedOption.dataset.get("name").get)
          .map(typesFromName(_)) --> selectedTypeVar.writer
      )
    )
  }

}
