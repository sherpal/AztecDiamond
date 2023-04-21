package diamond

import diamond.diamondtypes.Rectangle
import exceptions.WrongOrderException
import geometry.{Domino, Face, Point}

import scala.reflect.ClassTag

class CustomGenerationWeight(val n: Int)(implicit val tTag: ClassTag[Double])
    extends GenerationWeight
    with CustomWeightTrait[Double] {

  /** Computes the Weights corresponding to the Diamond of order diamondOrder -
    * 1
    */
  def subWeights: CustomGenerationWeight = if (n == 1)
    throw new WrongOrderException("Can't compute WeightMap of order 0.")
  else {

    val _0 = 0.0

    val newWeights = new CustomGenerationWeight(n - 1)
    Face
      .activeFaces(n)
      .flatMap(face => {
        val (newPairs, newZeroes) = face.doubleSubWeights(this)

        for (
          (d, w) <- newPairs
          if newWeights.inBoundsDomino(d)
        ) {
          newWeights(d) = w
        }

        newZeroes
      })
      .filter(newWeights.inBoundsDomino)
      .foreach(domino => newWeights(domino) = _0)

    newWeights

  }

}

object CustomGenerationWeight {

  /** Returns true with probability p, and false with probability 1-p.
    */
  def nextBernoulli(p: Double): Boolean = scala.util.Random.nextDouble() < p

  /** Computes a GenerationWeight associated with the picture whose information
    * is contained within imageData.
    * @param imageData
    *   width * height * 4 Vector containing the integers of a canvas imageData
    * @param width
    *   width of the canvas
    * @param height
    *   height of the canvas
    * @return
    */
  def fromImageData(
      imageData: Vector[Int],
      width: Int,
      height: Int
  ): CustomGenerationWeight = {
    val order = Rectangle.diamondOrder(width, height)

    val weights = new CustomGenerationWeight(
      Rectangle.diamondOrder(width, height)
    )

    val isPointInRectangle: (Point) => Boolean =
      Rectangle.isPointInDiamond(width, height)

    val isDominoInRectangle: (Domino) => Boolean =
      Rectangle.isInDiamond(width, height)

    val (facesIn, facesOut) =
      Face.activeFaces(order).partition(_.dominoes.forall(isDominoInRectangle))

    facesOut
      .flatMap(_.dominoes)
      .foreach(domino => {
        if (
          isPointInRectangle(domino.p1) && !isPointInRectangle(domino.p2) ||
          (isPointInRectangle(domino.p2) && !isPointInRectangle(domino.p1))
        ) {
          weights(domino) = 0.0
        } else {
          weights(domino) = 1.0
        }
      })

    def clamp(x: Double): Double = math.max(0.01, math.min(0.99, x))

    val greyScale = imageData
      .grouped(4)
      .map(_.take(3).sum / 3)
      .map(_ / 255.0)
      .map(clamp)
      .toVector

    def pointToPixel(point: Point): Double = {
      val imgX = point.x + width / 2 - 1
      val imgY = height - 1 - (point.y + height / 2 - 1)

      greyScale(imgY * width + imgX)
    }

    facesIn.foreach(face => {
      val luminosity = face.points.map(pointToPixel).sum / 4
      // horizontal dominoes will be lighter and vertical ones darker
      face.dominoes.foreach(domino =>
        weights(domino) =
          if (domino.isHorizontal) luminosity else 1 - luminosity
      )
    })

    weights
  }

}
