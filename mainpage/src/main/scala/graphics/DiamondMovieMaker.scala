package graphics

import diamond.Diamond
import org.scalajs.dom
import org.scalajs.dom.html
import org.scalajs.dom.CanvasRenderingContext2D

final class DiamondMovieMaker(diamond: Diamond) {

  val order: Int = diamond.order

//  private val backupNumber: Int = math.max(1, order / 5)

  private val backupOrders: List[Int] = (order to 1 by -1).toList.reverse
  // (order to 1 by -math.ceil(order.toDouble / backupNumber).toInt).toList.reverse

  private val startInitializationTime: Long = new java.util.Date().getTime

  private val backupDiamonds: Map[Int, Diamond] = {
    (diamond +: (order to 2 by -1)
      .foldLeft((diamond, List[Diamond]()))({
        case ((previousDiamond, backups), _) =>
          val newDiamond = previousDiamond.randomSubDiamond.get
          (
            newDiamond,
            if (backupOrders.contains(newDiamond.order)) newDiamond +: backups
            else backups
          )
      })
      ._2).map(d => (d.order, d)).toMap
  }

  if (scala.scalajs.LinkingInfo.developmentMode) {
    println(
      s"It took ${new java.util.Date().getTime - startInitializationTime}ms to compute the " +
        s"${backupDiamonds.size} backups."
    )
  }

  private def computeDiamond(diamondOrder: Int): Diamond = {
    val startingDiamond = backupDiamonds(
      backupOrders.find(_ >= math.max(1, math.min(order, diamondOrder))).get
    )

    (1 to startingDiamond.order - diamondOrder)
      .foldLeft(startingDiamond)((prevDiamond, _) =>
        prevDiamond.randomSubDiamond.get
      )
  }

  private var _currentDrawer: DiamondDrawer = DiamondDrawer(
    computeDiamond(1)
  ).get

  def currentDrawer: DiamondDrawer = _currentDrawer

  def canvas2d: Canvas2D = _currentDrawer.canvas2D

  private val canvas: html.Canvas =
    dom.document.createElement("canvas").asInstanceOf[html.Canvas]
  private val ctx: CanvasRenderingContext2D =
    canvas.getContext("2d").asInstanceOf[CanvasRenderingContext2D]

  def changeDrawer(diamondOrder: Int): DiamondDrawer = {
    val actualOrder = math.max(1, math.min(order, diamondOrder))
    if (_currentDrawer.diamond.order != actualOrder) {
      if (scala.scalajs.LinkingInfo.developmentMode) {
        println(actualOrder)
      }
      _currentDrawer = DiamondDrawer(
        computeDiamond(actualOrder),
        canvas = Some((canvas, ctx))
      ).get
    }

    _currentDrawer
  }

  def currentOrder: Int = _currentDrawer.diamond.order

}
