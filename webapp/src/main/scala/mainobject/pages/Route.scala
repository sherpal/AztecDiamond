package mainobject.pages

import com.raquo.laminar.api.L.*
import urldsl.language.dummyErrorImpl.*
import urldsl.language.PathSegment
import org.scalajs.dom
import mainobject.pages.playground.Playground

final case class Route(segment: PathSegment[Unit, _], component: () => HtmlElement, title: String) {
  def path: String = "/" ++ segment.createPart()
}

object Route {

  println(utils.rawBasePath)
  val baseRoute = root / utils.rawBasePath.filterNot(_ == '/')

  val homeRoute      = baseRoute / endOfSegments
  val shapesRoute    = baseRoute / "shapes"
  val examplesRoute  = baseRoute / "examples"
  val algorithmRoute = baseRoute / "algorithm"
  val generatorRoute = baseRoute / "generation"

  val routes = List(
    Route(homeRoute, () => Home(), "Home"),
    Route(shapesRoute, () => Shapes(), "Shapes"),
    Route(examplesRoute, () => Examples(), "Examples"),
    Route(algorithmRoute, () => Algorithm(), "Algorithm"),
    Route(generatorRoute, () => Playground(), "Playground")
  )

  private val path                   = dom.document.location.pathname.stripPrefix("/")
  private lazy val maybeCurrentRoute = routes.find(_.segment.matchPath(path).isRight)

  def currentPage = maybeCurrentRoute.map(_.component()).getOrElse(div(h1("404")))

  def currentTitle: String = maybeCurrentRoute.map(_.title).getOrElse("404")

}
