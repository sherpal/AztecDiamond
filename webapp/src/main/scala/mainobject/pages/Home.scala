package mainobject.pages

import com.raquo.laminar.api.L.*
import be.doeraene.webcomponents.ui5.*
import be.doeraene.webcomponents.ui5.configkeys.MediaGalleryLayout
import mainobject.components.TitleHeader
import mainobject.components.Footer
import utils.diamondImagesFolder
import mainobject.components.LinkButton
import be.doeraene.webcomponents.ui5.configkeys.LinkTarget

object Home {

  def apply(): HtmlElement = div(
    idAttr := "description",
    TitleHeader("Aztec Diamonds"),
    div(
      textAlign.center,
      cls := "container",
      div(
        cls := "row centered",
        div(cls := "col-lg-2"),
        div(
          cls := "col-lg-8",
          Title.h1(paddingTop := "20px", "Discover Aztec Diamonds"),
          img(
            src      := diamondImagesFolder ++ "aztec_diamond_5_example.png",
            alt      := "Image of diamond of order 5",
            maxWidth := "100px"
          ),
          p(
            "The goal of this website is to offer an easy-to-use tool for generating random tilings of shapes in the plane, as well as counting the number of such tilings."
          ),
          p("Follow the link below to get started, generate and count tilings."),
          div(
            LinkButton("Generate tiling", Route.routes.find(_.title == "Playground").get.path)
          ), {
            def diamondImages = List(
              "uniform_30.png",
              "rectangle40x30.png",
              "uniform_1000.png",
              "hexagon_15x15x15.png"
            ).map(link => MediaGallery.item(img(src := (diamondImagesFolder ++ link))))

            div(
              display.flex,
              justifyContent.center,
              div(
                MediaGallery(
                  _.showAllThumbnails := true,
                  _.layout            := MediaGalleryLayout.Vertical,
                  diamondImages
                )
              )
            )

          },
          p(
            marginBottom := "10px",
            "The code of the project is ",
            Link(
              _.href   := "https://github.com/sherpal/AztecDiamond",
              _.target := LinkTarget._blank,
              "available on the GitHub website"
            ),
            ". Any suggestion or comments are most welcome!"
          )
        )
      )
    ),
    Footer()
  )

}
