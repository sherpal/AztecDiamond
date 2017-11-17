package diamond

import geometry.Domino

import scala.reflect.ClassTag


/**
 * A CustomWeightTrait will behaves like a Map[Domino, Double/QRoot] but memory optimized, using Arrays and the
 * structure of the Aztec Diamond.
 *
 * The graph of the Aztec Diamond of order 3 is pictured below.
 *
 *
 *       ._.     <- horizontalWeights(2 * n - 1) (length 1)
 *       | |
 *     ._._._.
 *     | | | |
 *   ._._._._._.
 *   | | | | | |
 *   ._._._._._.
 *     | | | |
 *     ._._._.   <- horizontalWeights(1) (length 3 = 2 * 1 + 1)
 *       | |
 *       ._.     <- horizontalWeights(0) (length 1)
 *
 *   \ verticalWeights(0) (length 1)
 *     \ verticalWeights(1) (length 3)
 *
 * The graph is invariant under pi/2 rotations, that's why we register horizontal and vertical weights in a dual way.
 * The way we store the weights is just an implementation detail, all we need is that the apply and update methods are
 * consistent with each other.
 *
 * It is also mutable for the purpose of the algorithm.
 *
 * As always, n is the order of the diamond.
 */
trait CustomWeightTrait[WeightType] extends WeightTrait[WeightType] {

  implicit val tTag: ClassTag[WeightType]

  private val horizontalWeights: Array[Array[WeightType]] = Array.ofDim[Array[WeightType]](2 * n)
  // Initializing all the empty arrays for the horizontal weights.
  for (j <- 0 until n) {
    horizontalWeights(j) = Array.ofDim[WeightType](2 * j + 1)
    horizontalWeights(2 * n - j - 1) = Array.ofDim[WeightType](2 * j + 1)
  }
  private val verticalWeights: Array[Array[WeightType]] = Array.ofDim[Array[WeightType]](2 * n)
  // Initializing all the empty arrays for the vertical weights.
  for (j <- 0 until n) {
    verticalWeights(j) = Array.ofDim[WeightType](2 * j + 1)
    verticalWeights(2 * n - j - 1) = Array.ofDim[WeightType](2 * j + 1)
  }


  /**
   * Retrieve the weight corresponding to that domino.
   *
   * This method is used when generating Diamonds.
   */
  def apply(domino: Domino): WeightType = if (domino.isHorizontal) {
    val (x, y) = Domino.changeHorizontalCoordinates(domino.p1, n)
    // the horizontal weights are indexed according to the height.
    horizontalWeights(y)(x)
  } else {
    val (x, y) = Domino.changeVerticalCoordinates(domino.p1, n)
    // vertical weights are indexed according to the abscissa.
    verticalWeights(x)(y)
  }

  /**
   * Updates the weight corresponding to that domino.
   *
   * This method is used when computing the weights.
   */
  def update(domino: Domino, weight: WeightType): Unit = if (domino.isHorizontal) {
    val (x, y) = Domino.changeHorizontalCoordinates(domino.p1, n)
    // the horizontal weights are indexed according to the height.
    horizontalWeights(y)(x) = weight
  } else {
    val (x, y) = Domino.changeVerticalCoordinates(domino.p1, n)
    // vertical weights are indexed according to the abscissa.
    verticalWeights(x)(y) = weight
  }



}
