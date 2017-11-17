//package diamond
//
//import custommath.{IntegerMethods, QRoot, Rational, WeightLikeNumber}
//import exceptions.WrongOrderException
//import geometry.{Domino, Face}
//
//import scala.reflect.ClassTag
//
///**
// * A WeightMap will behaves like a Map[Domino, Double] but memory optimized, using Arrays and the structure of the
// * Aztec Diamond.
// *
// * The graph of the Aztec Diamond of order 3 is pictured below.
// *
// *
// *       ._.     <- horizontalWeights(2 * n - 1) (length 1)
// *       | |
// *     ._._._.
// *     | | | |
// *   ._._._._._.
// *   | | | | | |
// *   ._._._._._.
// *     | | | |
// *     ._._._.   <- horizontalWeights(1) (length 3 = 2 * 1 + 1)
// *       | |
// *       ._.     <- horizontalWeights(0) (length 1)
// *
// *   \ verticalWeights(0) (length 1)
// *     \ verticalWeights(1) (length 3)
// *
// * The graph is invariant under pi/2 rotations, that's why we register horizontal and vertical weights in a dual way.
// * The way we store the weights is just an implementation detail, all we need is that the apply and update methods are
// * consistent with each other.
// *
// * It is also mutable for the purpose of the algorithm.
// *
// * As always, n is the order of the diamond.
// */
//class WeightMap[WeightType: ClassTag](val n: Int)
//                                     (implicit val num: WeightLikeNumber[WeightType]) extends WeightTrait[WeightType] {
//
//  private val horizontalWeights: Array[Array[WeightType]] = Array.ofDim[Array[WeightType]](2 * n)
//  // Initializing all the empty arrays for the horizontal weights.
//  for (j <- 0 until n) {
//    horizontalWeights(j) = Array.ofDim[WeightType](2 * j + 1)
//    horizontalWeights(2 * n - j - 1) = Array.ofDim[WeightType](2 * j + 1)
//  }
//  private val verticalWeights: Array[Array[WeightType]] = Array.ofDim[Array[WeightType]](2 * n)
//  // Initializing all the empty arrays for the vertical weights.
//  for (j <- 0 until n) {
//    verticalWeights(j) = Array.ofDim[WeightType](2 * j + 1)
//    verticalWeights(2 * n - j - 1) = Array.ofDim[WeightType](2 * j + 1)
//  }
//
//
//  /**
//   * Retrieve the weight corresponding to that domino.
//   *
//   * This method is used when generating Diamonds.
//   */
//  def apply(domino: Domino): WeightType = if (domino.isHorizontal) {
//    val (x, y) = Domino.changeHorizontalCoordinates(domino.p1, n)
//    // the horizontal weights are indexed according to the height.
//    horizontalWeights(y)(x)
//  } else {
//    val (x, y) = Domino.changeVerticalCoordinates(domino.p1, n)
//    // vertical weights are indexed according to the abscissa.
//    verticalWeights(x)(y)
//  }
//
//  /**
//   * Updates the weight corresponding to that domino.
//   *
//   * This method is used when computing the weights.
//   */
//  def update(domino: Domino, weight: WeightType): Unit = if (domino.isHorizontal) {
//    val (x, y) = Domino.changeHorizontalCoordinates(domino.p1, n)
//    // the horizontal weights are indexed according to the height.
//    horizontalWeights(y)(x) = weight
//  } else {
//    val (x, y) = Domino.changeVerticalCoordinates(domino.p1, n)
//    // vertical weights are indexed according to the abscissa.
//    verticalWeights(x)(y) = weight
//  }
//
//
////  /**
////   * Computes the Weights corresponding to the Diamond of order diamondOrder - 1
////   */
////  def subWeights(implicit num: WeightLikeNumber[WeightType]): WeightMap[WeightType] = if (n == 1)
////    throw new WrongOrderException("Can't compute WeightMap of order 0.")
////  else {
////
////    val _0 = num.zero
////
////    val newWeights = new WeightMap[WeightType](n - 1)
////    Face.activeFaces(n).flatMap(face => {
////      val (newPairs, newZeroes) = face.subWeights[WeightType](this)
////
////      for (
////        (d, w) <- newPairs
////        if newWeights.inBoundsDomino(d)
////      ) {
////        newWeights(d) = w
////      }
////
////      newZeroes
////    }).filter(newWeights.inBoundsDomino).foreach(domino => newWeights(domino) = _0)
////
////    newWeights
////
////  }
//
//  /**
//   * Returns a WeightMap with all the weights of this WeightMap multiplied by the coefficient.
//   * This does not change the probability distribution induced over the Aztec diamonds.
//   */
//  def normalize(coefficient: WeightType)(implicit num: WeightLikeNumber[WeightType]): WeightMap[WeightType] = {
//    import scala.math.Fractional.Implicits._
//    import scala.language.implicitConversions
//
//    val newWeights = new WeightMap[WeightType](n)
//    Face.activeFaces(n).flatMap(_.dominoes).toList.foreach(
//      (domino: Domino) => newWeights(domino) = apply(domino) * coefficient
//    )
//    newWeights
//  }
//
//  def normalizeDenominator(implicit ev: WeightType =:= QRoot): WeightMap[QRoot] = {
//    normalize(QRoot(Face.activeFaces(n)
//      .flatMap(_.dominoes)
//      .map(apply)
//      .filter(_.isInstanceOf[Rational])
//      .map(_.asInstanceOf[Rational])
//      .map(_.denominator)
//      .fold(BigInt(0))(IntegerMethods.euclidGCD), 1).asInstanceOf[WeightType]).asInstanceOf[WeightMap[QRoot]]
//  }
//
//  def toList: List[WeightType] = horizontalWeights.flatten.toList ++ verticalWeights.flatten.toList
//
//  override def toString: String = "Horizontal weights:\n" + horizontalWeights.map(_.mkString(", ")).mkString("\n") +
//    "\nVertical weights:\n" + verticalWeights.map(_.mkString(", ")).mkString("\n")
//}
//
//
//object WeightMap {
//
//  /**
//   * Returns a WeightMap for a Diamond of order n with all weights set to 1, which correspond to the uniform probability
//   * distribution on the set of possible tilings.
//   *
//   * This is only for example or test purpose, as we implemented UniformWeightMap for memory optimization.
//   */
//  def uniformDiamondWeights[WeightType: ClassTag](n: Int)
//                                                 (implicit num: WeightLikeNumber[WeightType]): WeightMap[WeightType] = {
//    val _1 = num.one
//
//    val weights = new WeightMap[WeightType](n)
//    Face.activeFaces(n).flatMap(face => List(
//      face.horizontalDominoes._1, face.horizontalDominoes._2,
//      face.verticalDominoes._1, face.verticalDominoes._2
//    )).foreach(weights(_) = _1)
//
//    weights
//  }
//
//  /**
//   * Returns true with probability p, and false with probability 1-p.
//   */
//  def nextBernoulli(p: Double): Boolean = scala.util.Random.nextDouble < p
//
//}