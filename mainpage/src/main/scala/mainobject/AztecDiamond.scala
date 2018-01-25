package mainobject

import diamond.diamondtypes.UniformDiamond
import ui._

/**
 * Main Object
 *
 * The algorithms implemented in this project are taken from
 * [1] E. Janvresse, T. de la Rue and Y. Velenik, "A Note on Domino Shuffling", Electronic Journal of Combinatorics, 13
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
    //DragAndDrop



//    val drawer1 = DiamondDrawer(Diamond.uniformDiamond(3)).get
//
//    val p1 = dom.document.createElement("p").asInstanceOf[html.Paragraph]
//    dom.document.body.appendChild(p1)
//    p1.textContent = drawer1.tikzCode() + drawer1.nonIntersectingPathTikzCode() + DiamondDrawer.emptyDiamondTikzCode(3)


//    val drawer2 = DiamondDrawer(
//        Diamond.generateDiamond(WeightTrait.computeAllWeights[Double, CustomGenerationWeight](
//            AztecRing.makeGenerationWeight(10, 20)
//        )),
//        AztecRing.isInDiamond(10,20)
//    ).get
//
////  val drawer2 = DiamondDrawer(
////    AztecRing.countingTilingDiamond(2, 6), AztecRing.isInDiamond(2, 6)
////  ).get
//
//    val p2 = dom.document.createElement("p").asInstanceOf[html.Paragraph]
//    dom.document.body.appendChild(p2)
//    p2.textContent = drawer2.tikzCode() + drawer2.nonIntersectingPathTikzCode(subGraph = true)


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





//    val (a, b, c) = (1, 1, 1)
//    val descendants: Map[Int, List[Diamond]] =
//        Hexagon.countingTilingDiamond(a, b, c).allSubDiamonds.groupBy(_.order)
//
//    println(descendants(Hexagon.diamondOrder(a, b, c)).head.probability(Hexagon.makeComputationWeight(a, b, c)))
//
//
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
//
//  for (j <- 1 to descendants.keys.max) drawOrder(j)

    //for (j <- 1 to WeightTrait.rectangleOrder(width, height)) drawOrder(j)

//    val div: html.Div = dom.document.createElement("div").asInstanceOf[html.Div]
//    dom.document.body.appendChild(div)
//    div.style.display = "flex"
//    div.style.asInstanceOf[js.Dynamic].`flex-direction` = "row"
//
//    val diamond: Diamond = Hexagon.countingTilingDiamond(a, b, c)
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


//    val canvas: html.Canvas = dom.document.createElement("canvas").asInstanceOf[html.Canvas]
//    canvas.width = 200 * 720 / 960
//    canvas.height = 200
//    val ctx: CanvasRenderingContext2D = canvas.getContext("2d").asInstanceOf[CanvasRenderingContext2D]
//
//    val image: html.Image = dom.document.createElement("img").asInstanceOf[html.Image]
//    image.src = "./nico.jpg"
//
//    image.setAttribute("crossOrigin", "anonymous")
//
//    image.onload = (_: dom.Event) => {
//        val canvas2 = dom.document.createElement("canvas").asInstanceOf[html.Canvas]
//        canvas2.width = canvas.width
//        canvas2.height = canvas.height
//        val ctx2 = canvas2.getContext("2d").asInstanceOf[CanvasRenderingContext2D]
//
//        ctx.drawImage(image, 0, 0, canvas.width, canvas.height)
//
//        val imageData = ctx.getImageData(0, 0, canvas.width, canvas.height)
//        val data = imageData.data
//
//        data
//          .grouped(4)
//          .flatMap(arr => {
//              val mean = arr.take(3).sum / 3
//              js.Array[Int](mean, mean, mean, arr(3))
//          })
//          .zipWithIndex
//          .foreach({ case (pixel, idx) => data(idx) = pixel })
//
//
//        ctx2.putImageData(imageData, 0, 0)
//
//        dom.document.body.appendChild(canvas2)
//
//        println("coucou")
//
//        js.timers.setTimeout(500) {
//            val diamond = Diamond.generateDiamond(
//                WeightTrait.computeAllWeights[Double, CustomGenerationWeight](
//                    CustomGenerationWeight.fromImageData(
//                        ctx.getImageData(0, 0, canvas.width, canvas.height).data.toVector,
//                        canvas.width, canvas.height
//                    )
//                )
//            )
//            println("diamond computed")
//
//            val drawer = DiamondDrawer(diamond).get
//            drawer.draw(
//                colors = (domino: Domino) => if (domino.isHorizontal) (1,1,1) else (0,0,0)
//            )
//
//            val diamondCanvas = dom.document.createElement("canvas").asInstanceOf[html.Canvas]
//            diamondCanvas.width = 3 * canvas.width
//            diamondCanvas.height = 3 * canvas.height
//            diamondCanvas.getContext("2d").asInstanceOf[CanvasRenderingContext2D]
//              .drawImage(drawer.canvas2D.canvas, 0, 0, diamondCanvas.width, diamondCanvas.height)
//
//            dom.document.body.appendChild(diamondCanvas)
//        }
//
//
//    }
//
//    dom.document.body.appendChild(canvas)


}
