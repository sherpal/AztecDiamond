package mainobject

import diamond.DiamondType._
import diamond._
import ui.{ColorPicker, CountingTilingForm, DrawingOptions, GenerateDiamondForm}

/**
 * Main Object
 *
 * The algorithms implemented in this project are taken from
 * [1] E. Janvresse, T. de la Rue and Y. Velen, "A Note on Domino Shuffling", Electronic Journal of Combinatorics, 13
 * (2006).
 */
object AztecDiamond {

  /**
   * We're currently doing testing so the main method is garbage.
   */
    println("Welcome to AztecDiamond drawing!")

    ColorPicker
    DrawingOptions
    GenerateDiamondForm.switchForm(UniformDiamond)
    CountingTilingForm.switchForm(UniformDiamond)
    TabManager



//    for {
//      h <- List(2)
//      w <- 2 to 30 by 2
//    } {
//      val weight = WeightTrait.aztecHouseWeights(w,h)
//
//      println(s"n=${w/2}, h=${h-1}")
//      val containerContent = dom.document.createElement("h1").asInstanceOf[html.Head]
//      containerContent.textContent = s"n=${w/2}, h=${h-1}, ${(QRootIsWeightLikeNumber.one /
//        Diamond.handCraftedAztecHouse(w, h)
//          .probability[QRoot](WeightTrait.aztecHouseWeights[QRoot](w, h))).toBigInt}"
//      dom.document.body.appendChild(containerContent)
//
//      val container = dom.document.createElement("div").asInstanceOf[html.Div]
//      container.style.display = "flex"
//      container.style.asInstanceOf[js.Dynamic].`flex-direction` = "row"
//
//      dom.document.body.appendChild(container)
//
//      (1 until weight.n).foldLeft(List[WeightMap[QRoot]](weight))({ case (current, _) =>
//        current.head.subWeights.normalize(QRoot(2,1)) +: current
//      })
//        .reverse
//        .map(new WeightDrawer(_))
//        .map(drawer => {
//          drawer.draw()
//          drawer
//        })
//        .map(_.canvas2D.canvas)
//        .foreach(canvas => {
//          container.appendChild(canvas)
//        })
//    }


//    val weight = WeightTrait.diamondRingPartition(2, 6)
//    val container = dom.document.createElement("div").asInstanceOf[html.Div]
//    container.style.display = "flex"
//    container.style.asInstanceOf[js.Dynamic].`flex-direction` = "row"
//    dom.document.body.appendChild(container)
//    (1 until weight.n).foldLeft(List[CustomComputePartitionFunctionWeight](weight))({ case (current, _) =>
//      current.head.subWeights.normalize(QRoot(2,1)).normalizeDenominator +: current
//    })
//      .reverse
//      .map(new WeightDrawer(_))
//      .map(drawer => {
//        drawer.draw()
//        drawer
//      })
//      .map(_.canvas2D.canvas)
//      .foreach(canvas => {
//        container.appendChild(canvas)
//      })





    val width = 3
    val height = 6
    //val descendants = Rectangle.countingTilingDiamond((width, height)).allSubDiamonds.groupBy(_.order)
    //for (j <- 1 to WeightTrait.rectangleOrder(width, height)) println(descendants.getOrElse(j, Nil).size)



    def probability(diamondType: DiamondType)(arg: diamondType.ArgType): Unit = {
      println(diamondType.countingTilingDiamond(arg).probability(diamondType.makeComputationWeight(arg)))
    }



//    def drawOrder(order: Int): Unit = {
//      println(order)
//      val diamonds = descendants(order).distinct
//
//      val div = dom.document.createElement("div").asInstanceOf[html.Div]
//      dom.document.body.appendChild(div)
//      div.style.display = "flex"
//      div.style.asInstanceOf[js.Dynamic].`flex-direction` = "row"
//
//
//      diamonds.zipWithIndex.foreach {
//        case (diamond, j) =>
//          scala.scalajs.js.timers.setTimeout(j * 1000) {
//            val diamondDrawer = DiamondDrawer(diamond)
//            val canvas2D: Canvas2D = new Canvas2D()
//            canvas2D.setSize(500, 500)
//            div.appendChild(canvas2D.canvas)
//            diamondDrawer.get.draw(border = true)
//            canvas2D.drawCanvas(diamondDrawer.get.canvas2D.canvas, 0, 500, 500)
//          }
//      }
//    }

    //for (j <- 1 to WeightTrait.rectangleOrder(width, height)) drawOrder(j)

//    val div = dom.document.createElement("div").asInstanceOf[html.Div]
//    dom.document.body.appendChild(div)
//    div.style.display = "flex"
//    div.style.asInstanceOf[js.Dynamic].`flex-direction` = "row"
//
//    val diamond = Rectangle.countingTilingDiamond((width, height))
//
//    scala.scalajs.js.timers.setTimeout(1000) {
//      val diamondDrawer = DiamondDrawer(diamond)
//      val canvas2D: Canvas2D = new Canvas2D()
//      canvas2D.setSize(500, 500)
//      div.appendChild(canvas2D.canvas)
//      diamondDrawer.get.draw(border = true)
//      canvas2D.drawCanvas(diamondDrawer.get.canvas2D.canvas, 0, 500, 500)
//    }




//    scala.scalajs.js.timers.setInterval(1000) {
//      val fileInput = dom.document.getElementById("file-input").asInstanceOf[html.Input]
//
//      if (fileInput.files.length > 0) {
//        val fr = new FileReader()
//        fr.readAsText(fileInput.files(0))
//        fr.onload = (event: dom.Event) => println(event.target.asInstanceOf[js.Dynamic].result)
//      }
//    }




}
