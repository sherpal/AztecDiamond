package mainobject.components

import be.doeraene.webcomponents.ui5.*
import com.raquo.laminar.api.L.*

object Footer {

  private val logosFolder = utils.basePath ++ "images/"

  def apply(): HtmlElement = {
    val footerContainerCls = "footer-container"
    val leftMarginCls      = "left-margin"
    val rightMarginCls     = "right-margin"
    val midCls             = "middle"

    div(
      backgroundColor := "#341d5b",
      padding         := "1em",
      color           := "white",
      display.grid,
      className := footerContainerCls,
      styleTag(s"""
                |.$footerContainerCls {
                |  grid-template-columns: [left] 1fr [mid] 4fr [right] 1fr;
                |  grid-template-rows: 1fr;
                |}
                |
                |.$leftMarginCls {
                |  grid-column-start: left;
                |  grid-column-end: left;
                |  grid-row-start: 1;
                |  grid-row-end: 1;
                |}
                |
                |.$rightMarginCls {
                |  grid-column-start: right;
                |  grid-column-end: right;
                |  grid-row-start: 1;
                |  grid-row-end: 1;
                |}
                |
                |.$midCls {
                |  grid-column-start: mid;
                |  grid-column-end: mid;
                |  grid-row-start: 1;
                |  grid-row-end: 1;
                |}
                |
                |
                |""".stripMargin),
      div(className := leftMarginCls, display.flex),
      div(className := rightMarginCls, display.flex), {
        val containerCls   = "content-container"
        val linkHeadCls    = "link-head"
        val contactHeadCls = "contact-head"
        val uclLinkCls     = "ucl-link"
        val irmpLinkCls    = "irmp-link"
        val contactLinkCls = "contact-link"

        def makeCell(cls: String, row: String | Int, column: String | Int): String =
          s"""|.$cls {
              |  grid-column-start: $column;
              |  grid-column-end: $column;
              |  grid-row-start: $row;
              |  grid-row-end: $row;
              |}""".stripMargin
        div(
          className := midCls,
          display.flex,
          display.grid,
          className := containerCls,
          styleTag(
            s"""|
              |.$containerCls {
              |  grid-template-columns: [linksfirst] 1fr [linkssecond] 1fr [linksthird] 1fr [contact] 1fr;
              |  grid-template-rows: [head] 30px [body] 1fr;
              |}
              |
              |${makeCell(linkHeadCls, "head", "linksfirst")}
              |${makeCell(contactHeadCls, "head", "contact")}
              |${makeCell(uclLinkCls, "body", "linksfirst")}
              |${makeCell(irmpLinkCls, "body", "linkssecond")}
              |${makeCell(contactLinkCls, "body", "contact")}
              |""".stripMargin
          ),
          div(className := linkHeadCls, display.flex, Title.h4("LINKS", color := "white")),
          div(className := contactHeadCls, display.flex, Title.h4("CONTACT", color := "white")),
          div(
            display.flex,
            className := uclLinkCls,
            a(
              href   := "https://uclouvain.be/en/index.html",
              target := "_blank",
              img(alt := "ucl", src := logosFolder ++ "ucl_logo.png")
            )
          ),
          div(
            display.flex,
            className := irmpLinkCls,
            a(
              href   := "https://uclouvain.be/fr/instituts-recherche/irmp",
              target := "_blank",
              img(alt := "irmp", src := logosFolder ++ "irmp.png")
            )
          ),
          div(
            display.flex,
            alignItems.center,
            className := contactLinkCls,
            a(
              href   := "https://github.com/sherpal",
              target := "_blank",
              img(alt := "github", src := logosFolder ++ "github-logo.png"),
              marginRight := "0.5em"
            ),
            "sherpal"
          )
        )
      }
    )
  }

}
