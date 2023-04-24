package mainobject.pages

import com.raquo.laminar.api.L.*
import be.doeraene.webcomponents.ui5.*
import be.doeraene.webcomponents.ui5.configkeys.MediaGalleryLayout

object Home {

  private def diamondImagesFolder = "/images/diamonds/"

  def apply(): HtmlElement = div(
    idAttr := "description",
    div(
      cls := "container",
      div(
        cls := "row centered",
        div(cls := "col-lg-2"),
        div(
          cls := "col-lg-8",
          h4(paddingTop := "20px", "Discover Aztec Diamonds"),
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
            a(href := "domino-shuffling-implementation.html", cls := "btn btn-theme", "Generate tiling")
          ),
          p(
            "The software is available online, within your browser, but a fully optimized desktop application is also available."
          ),
          div(
            cls := "row",
            a(href := "https://github.com/sherpal/AztecDiamond/releases", cls := "btn btn-theme", "Download")
          ), {
            def diamondImages = List(
              "uniform_30.png",
              "rectangle40x30.png",
              "uniform_1000.png",
              "hexagon_15x15x15.png"
            ).map(link => MediaGallery.item(img(src := (diamondImagesFolder ++ link))))

            div(
              MediaGallery(
                height := "600px",
                _.showAllThumbnails := true,
                _.layout            := MediaGalleryLayout.Vertical,
                diamondImages
              )
            )

          },
          p(
            fontSize := "14px",
            "By clicking on download, you will be redirected to the GitHub website, where Windows, Mac and Linux version of the software are available for download (",
            a(cls := "link", href := "https://www.java.com/fr/download/", "Java required"),
            ")."
          ),
          p(
            marginBottom := "10px",
            "The code of the project is ",
            a(cls := "link", href := "https://github.com/sherpal/AztecDiamond", "available on the GitHub website"),
            ". Any suggestion or comments are most welcome!"
          )
        )
      )
    )
  )

}
