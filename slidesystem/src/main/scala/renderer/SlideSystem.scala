package renderer

import slidegenerator.{GeneratorSlide, MovieMakerSlide}
import ui.ColorPicker

import scala.scalajs.js

object SlideSystem {

  def main(args: Array[String]): Unit = {

    ColorPicker

    if (scala.scalajs.LinkingInfo.developmentMode) {
      println("coucou")

      GeneratorSlide(
        "test", "Hexagon", colors = js.Array(js.Array(255,0,0), js.Array(0,255,255), js.Array(0,255,255))
      )
      MovieMakerSlide(
        "test2", "Rectangle"
      )
    }

  }

}
