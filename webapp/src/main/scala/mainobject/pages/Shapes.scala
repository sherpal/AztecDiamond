package mainobject.pages

import be.doeraene.webcomponents.ui5.*
import com.raquo.laminar.api.L.*
import be.doeraene.webcomponents.ui5.configkeys.SideContentPosition
import be.doeraene.webcomponents.ui5.configkeys.LinkTarget
import mainobject.components.TitleHeader
import mainobject.components.Footer
import utils.diamondImagesFolder

object Shapes {

  private def doubleAztecAndTacnode = Link(
    _.href   := "https://arxiv.org/abs/1112.5532",
    _.target := LinkTarget._blank,
    "Double Aztec Diamonds and the Tacnode Process"
  )

  def aNoteOnDominoShuffling = Link(
    _.href   := "http://www.combinatorics.org/ojs/index.php/eljc/article/view/v13i1r30",
    _.target := LinkTarget._blank,
    "\"A note on Domino Shuffling\""
  )

  def `Generalized domino-shuffling` = Link(
    _.href   := "https://arxiv.org/abs/math/0111034",
    _.target := LinkTarget._blank,
    "\"Generalized domino-shuffling\""
  )

  private def `Coupling Functions for Domino tilings of Aztec diamonds` = Link(
    _.href   := "https://arxiv.org/abs/1302.0615",
    _.target := LinkTarget._blank,
    "Coupling Functions for Domino tilings of Aztec diamonds"
  )

  private case class RowInfo(elem: HtmlElement, imageSrc: String)

  def apply() = {
    val diamonds = List(
      RowInfo(p("Uniformly distributed diamonds."), diamondImagesFolder ++ "uniform_30.png"),
      RowInfo(p("Uniformly distributed rectangles."), diamondImagesFolder ++ "rectangle40x30.png"),
      RowInfo(
        p(
          "Uniformly distributed Aztec Rings. An Aztec Ring is obtained by removing an Aztec diamond from a bigger Aztec Diamond. In ",
          aNoteOnDominoShuffling,
          ", it was proven that if the difference between the outer and the inner order is either even, or satisfy outer >= inner-1, then the shape is tileable."
        ),
        diamondImagesFolder ++ "aztec-ring_i15_o31.png"
      ),
      RowInfo(
        p(
          "Uniformly distributed Double Aztec Diamonds. Two Aztec Diamonds that slightly overlap. In ",
          doubleAztecAndTacnode,
          ", it was proven that the Tacnode determinantal point process appears when the size of the dominoes go to infinity such that the overlapping growth at a particular rate."
        ),
        diamondImagesFolder ++ "double-aztec_order25_overlap5.png"
      ),
      RowInfo(
        p("Uniformly distributed Aztec houses. A Triangle on top of a Rectangle."),
        diamondImagesFolder ++ "aztec-house_n30_h30.png"
      ),
      RowInfo(
        p(
          "Two periodic Aztec Diamonds. These are Aztec Diamonds with a non uniform probability distribution. The name \"Two periodic\" comes from the way the weights are put on the dominoes. It was introduced in ",
          `Coupling Functions for Domino tilings of Aztec diamonds`,
          " by Chhita and Young. The special feature of Two Periodic Aztec Diamond is the appearance of a new phase beside the solid and liquid phase. This phase appears in the middle, and starts to appear clearly with diamonds of order 250."
        ),
        diamondImagesFolder ++ "two-periodic_200_a1_b3.png"
      ),
      RowInfo(
        p(
          "Two periodic Rectangles. This is similar to Two periodic Aztec Diamonds, except it is the tiling of a rectangle. The weights on the dominoes are put in the same way as the Two Periodic Aztec Diamond."
        ),
        diamondImagesFolder ++ "two_periodic_rectangle.png"
      ),
      RowInfo(
        p(
          "Hexagon. This is a diamond made of equilateral triangles. In the hexagonal tiling, the dominoes can be represented as lozenges. This is automatically enabled when the diamond is a Hexagon, but can also be turned on or off for other diamonds."
        ),
        diamondImagesFolder ++ "hexagon_15x15x15.png"
      )
    )

    val diamondElements = diamonds.zipWithIndex.map { case (RowInfo(text, imgSrc), index) =>
      div(
        margin := "1em",
        cls    := "row",
        DynamicSideContent(
          _.sideContentPosition := SideContentPosition.Start,
          _.slots.sideContent := div(
            display.flex,
            justifyContent.center,
            alignItems.center,
            img(src := imgSrc, alt := "Diamond", maxWidth := "195px", maxHeight := "165px")
          ),
          p(maxWidth := "80%", text)
        )
      )
    }

    div(
      TitleHeader("Available shapes"),
      div(
        padding := "1em",
        p(
          "The software offers to generate various types of shapes. The description of each of them, as well as an example, can be found below.",
          br(),
          "In order to generate the shapes, we always need to generate a bigger enclosing Aztec Diamond. In the online version of the software, a typical computer will generate diamond up to order 100 quite rapidly. The desktop application is up to 10 times faster."
        ),
        h2("Plotting Options"),
        p(
          "In order to have images that best fit your needs, options for plotting are available:"
        ),
        ul(
          li("Change the colours of the dominoes."),
          li(
            "Draw the dominoes with or without a black boundary. If the order of the diamond is bigger than 50, it " +
              "will not draw the border by default."
          ),
          li("See the shape in its enclosing Diamond."),
          li("See the non-intersecting paths the diamonds are in bijection with."),
          li("Draw the dominoes as lozenges. This is activated by default for Hexagon tiling."),
          li("Rotate the Shape and zoom.")
        ),
        sectionTag(h2("Shapes"), diamondElements)
      ),
      Footer()
    )
  }

}
