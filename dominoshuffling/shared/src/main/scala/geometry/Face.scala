package geometry

import custommath.{NotRational, QRoot}
import diamond._
import exceptions.{ImpossibleDiamondException, NotTileableException, ShouldNotBeThereException}
import narr.NArray

import scala.language.implicitConversions
import custommath.WeightLikeNumber

/** A Face is 4 points that form a square, and is represented by its bottom left point on the lattice.
  *
  * Faces are at the core of the domino shuffling algorithm, as every step is decomposed around them. Among all faces,
  * the "active faces" play a prominent role, hence the Face object has a way to generate active faces of an aztec
  * diamond of any order.
  *
  * Faces operates with Weights on Dominoes to create diamond of the next order, or to find pre-images of a diamond.
  *
  * Note that there is no "diamond-order" dependence in a Face.
  */
case class Face(bottomLeft: Point) {

  def points: NArray[Point] = NArray(
    bottomLeft,
    bottomLeft + Point(1, 0),
    bottomLeft + Point(0, 1),
    bottomLeft + Point(1, 1)
  )

  val horizontalDominoes: (Domino, Domino) =
    (
      Domino(bottomLeft, bottomLeft + Point(1, 0)),
      Domino(bottomLeft + Point(0, 1), bottomLeft + Point(1, 1))
    )

  val verticalDominoes: (Domino, Domino) =
    (
      Domino(bottomLeft, bottomLeft + Point(0, 1)),
      Domino(bottomLeft + Point(1, 0), bottomLeft + Point(1, 1))
    )

  def dominoes: NArray[Domino] = NArray(
    horizontalDominoes._1,
    horizontalDominoes._2,
    verticalDominoes._1,
    verticalDominoes._2
  )

  /** Returns the four weights associated with the Face.
    */
  def getFaceWeights[WeightType](
      weights: WeightTrait[WeightType]
  ): (WeightType, WeightType, WeightType, WeightType) = {
    val (h1, h2) = horizontalDominoes
    val (v1, v2) = verticalDominoes
    val alpha    = weights(h2)
    val beta     = weights(v2)
    val gamma    = weights(h1)
    val delta    = weights(v1)
    (alpha, beta, gamma, delta)
  }

  /** Returns two dominoes with probability distribution given by the weights.
    */
  def randomSquare(weights: GenerationWeight): NArray[Domino] = {

    val (h1, h2) = horizontalDominoes
    val (v1, v2) = verticalDominoes

    val (alpha, beta, gamma, delta) = getFaceWeights[Double](weights)

    val topBottom    = alpha * gamma
    val leftRight    = beta * delta
    val crossProduct = topBottom + leftRight

    if (crossProduct.toDouble == 0.0) {
      println((alpha, beta, gamma, delta))
      println("Cross product is 0")
      println("weights of order " ++ weights.n.toString)
      throw new NotTileableException
    } else {
      if (alpha.toDouble == 0.0 || gamma.toDouble == 0.0) NArray(v1, v2)
      else if (beta.toDouble == 0.0 || delta.toDouble == 0.0) NArray(h1, h2)
      else if (CustomGenerationWeight.nextBernoulli(topBottom / crossProduct))
        NArray(h1, h2)
      else NArray(v1, v2)
    }
  }

  /** Returns the possibilities of the sub dominoes that can generate this domino, at that face.
    *
    * This the "inverse" operation of the one in nextDiamondConstruction below. It the case of empty faces, two pre-
    * images are generated. When computing probabilities, this step should be avoided in order to gain huge speed up and
    * memory uses. Note that there is no randomness in the deconstruction algorithm.
    */
  def previousDiamondConstruction(
      diamond: Diamond
  ): NArray[NArray[Domino]] = {
    val (h1, h2) = horizontalDominoes
    val (v1, v2) = verticalDominoes

    (
      diamond.contains(h1),
      diamond.contains(h2),
      diamond.contains(v1),
      diamond.contains(v2)
    ) match {
      case (true, false, false, false)  => NArray(NArray(h2))
      case (false, true, false, false)  => NArray(NArray(h1))
      case (false, false, true, false)  => NArray(NArray(v2))
      case (false, false, false, true)  => NArray(NArray(v1))
      case (true, true, false, false)   => NArray(NArray.empty[Domino])
      case (false, false, true, true)   => NArray(NArray.empty[Domino])
      case (false, false, false, false) => NArray(NArray(h1, h2), NArray(v1, v2))
      case _ =>
        throw new ImpossibleDiamondException(
          s"The diamond was of order ${diamond.order}"
        )
    }
  }

  /** We put the dominoes for the next diamond, given the dominoes of the sub diamond and the weights.
    *
    * The rule is as follows: ._. . . . . ---> ._. (with all symmetric cases)
    *
    * ._. . . . . ._. or | | ---> . .
    *
    * . . ._. . . . . ---> randomly chosen between ._. or | |
    */
  def nextDiamondConstruction(
      subDiamond: Diamond,
      weights: GenerationWeight
  ): NArray[Domino] = {
    val (h1, h2) = horizontalDominoes
    val (v1, v2) = verticalDominoes

    (
      subDiamond.contains(h1),
      subDiamond.contains(h2),
      subDiamond.contains(v1),
      subDiamond.contains(v2)
    ) match {
      case (true, false, false, false)  => NArray(h2)
      case (false, true, false, false)  => NArray(h1)
      case (false, false, true, false)  => NArray(v2)
      case (false, false, false, true)  => NArray(v1)
      case (true, true, false, false)   => NArray.empty[Domino]
      case (false, false, true, true)   => NArray.empty[Domino]
      case (false, false, false, false) => randomSquare(weights)
      case _ =>
        throw new ImpossibleDiamondException(
          s"The diamond was of order ${subDiamond.order}"
        )
    }
  }

  /** Computes the weights for the diamond of one order less by applying Step 1 & 2 described in [1].
    * @param weights
    *   the weights for this order n Diamond
    * @return
    *   the weights of this Face for the order n - 1 Diamond, and possible other zeroes to add.
    */
  def doubleSubWeights(
      weights: CustomGenerationWeight
  ): (NArray[(Domino, Double)], NArray[Domino]) = {
    val (h1, h2) = horizontalDominoes
    val (v1, v2) = verticalDominoes

    val (alpha, beta, gamma, delta) = getFaceWeights[Double](weights)

    val crossProduct = alpha * gamma + delta * beta

    (
      (alpha, beta, gamma, delta) match {
        case (_, _, _, _) if crossProduct != 0 =>
          NArray(
            h1 -> alpha / crossProduct,
            h2 -> gamma / crossProduct,
            v1 -> beta / crossProduct,
            v2 -> delta / crossProduct
          )
        case (0, 0, 0, 0) =>
          val _1overV2 = 1 / math.sqrt(2)
          NArray(
            h1 -> _1overV2,
            h2 -> _1overV2,
            v1 -> _1overV2,
            v2 -> _1overV2
          )
        case (_, _, 0, 0) =>
          NArray(
            h1 -> alpha,
            h2 -> 1 / (alpha + beta),
            v1 -> beta,
            v2 -> 1 / (alpha + beta)
          )
        case (0, _, _, 0) =>
          NArray(
            h1 -> 1 / (beta + gamma),
            h2 -> gamma,
            v1 -> beta,
            v2 -> 1 / (beta + gamma)
          )
        case (0, 0, _, _) =>
          NArray(
            h1 -> 1 / (delta + gamma),
            h2 -> gamma,
            v1 -> 1 / (delta + gamma),
            v2 -> delta
          )
        case (_, 0, 0, _) =>
          NArray(
            h1 -> alpha,
            h2 -> 1 / (alpha + delta),
            v1 -> 1 / (alpha + delta),
            v2 -> delta
          )
        case (_, _, _, _) =>
          // this will never happen as all cases are dealt above.
          throw new ShouldNotBeThereException
      },
      NArray[NArray[Domino]](
        if (alpha == 0 && beta == 0)
          NArray(h2.horizontalMove(1), v2.verticalMove(1))
        else NArray.empty[Domino],
        if (beta == 0 && gamma == 0)
          NArray(h1.horizontalMove(1), v2.verticalMove(-1))
        else NArray.empty[Domino],
        if (gamma == 0 && delta == 0)
          NArray(h1.horizontalMove(-1), v1.verticalMove(-1))
        else NArray.empty[Domino],
        if (delta == 0 && alpha == 0)
          NArray(h2.horizontalMove(-1), v1.verticalMove(1))
        else NArray.empty[Domino]
      ).flatten
    )
  }

  private val _1overV2 = summon[WeightLikeNumber[QRoot]].oneOverRoot2

  /** Computes the weights for the diamond of one order less by applying Step 1 & 2 described in [1].
    * @param weights
    *   the weights for this order n Diamond
    * @return
    *   the weights of this Face for the order n - 1 Diamond, and possible other zeroes to add.
    */
  def qRootSubWeights(
      weights: CustomComputePartitionFunctionWeight
  ): (NArray[(Domino, QRoot)], NArray[Domino]) = {
    val (h1, h2) = horizontalDominoes
    val (v1, v2) = verticalDominoes

    val (alpha, beta, gamma, delta) = getFaceWeights[QRoot](weights)

    val crossProduct = alpha * gamma + delta * beta

    val _0 = QRoot(0, 1)

    (
      (alpha, beta, gamma, delta) match {
        case (_, _, _, _) if crossProduct != _0 =>
          NArray(
            h1 -> alpha / crossProduct,
            h2 -> gamma / crossProduct,
            v1 -> beta / crossProduct,
            v2 -> delta / crossProduct
          )
        case (a, b, c, d) if NArray(a, b, c, d).forall(_ == _0) =>
          NArray(
            h1 -> _1overV2,
            h2 -> _1overV2,
            v1 -> _1overV2,
            v2 -> _1overV2
          )
        case (_, _, c, d) if c == _0 && d == _0 =>
          NArray(
            h1 -> alpha,
            h2 -> 1 / (alpha + beta),
            v1 -> beta,
            v2 -> 1 / (alpha + beta)
          )
        case (a, _, _, d) if a == _0 && d == _0 =>
          NArray(
            h1 -> 1 / (beta + gamma),
            h2 -> gamma,
            v1 -> beta,
            v2 -> 1 / (beta + gamma)
          )
        case (a, b, _, _) if a == _0 && b == _0 =>
          NArray(
            h1 -> 1 / (delta + gamma),
            h2 -> gamma,
            v1 -> 1 / (delta + gamma),
            v2 -> delta
          )
        case (_, b, c, _) if b == _0 && c == _0 =>
          NArray(
            h1 -> alpha,
            h2 -> 1 / (alpha + delta),
            v1 -> 1 / (alpha + delta),
            v2 -> delta
          )
        case (_, _, _, _) =>
          // this will never happen as all cases are dealt above.
          throw new ShouldNotBeThereException
      },
      NArray[NArray[Domino]](
        if (alpha == _0 && beta == _0)
          NArray(h2.horizontalMove(1), v2.verticalMove(1))
        else NArray.empty[Domino],
        if (beta == _0 && gamma == _0)
          NArray(h1.horizontalMove(1), v2.verticalMove(-1))
        else NArray.empty[Domino],
        if (gamma == _0 && delta == _0)
          NArray(h1.horizontalMove(-1), v1.verticalMove(-1))
        else NArray.empty[Domino],
        if (delta == _0 && alpha == _0)
          NArray(h2.horizontalMove(-1), v1.verticalMove(1))
        else NArray.empty[Domino]
      ).flatten
    )
  }

}

object Face {

  /** Returns a Iterable of all the ActiveFaces of a diamond of order n.
    *
    * Since we are using scala.js, we can't parallelise this Iterable. However, all operations on faces are independent
    * are were we to use scala with the JVM, we could speed up the algorithm by using toParArray.
    */
  def activeFaces(n: Int): NArray[Face] = {
    val js           = 0 to (-n + 1) by -1
    val ks           = 0 until n
    val faces        = NArray.ofSize[Face](n * n)
    var currentIndex = 0
    for {
      j <- js
      k <- ks
    } {
      faces(currentIndex) = Face(Point(j + k, -n + 1 - j + k))
      currentIndex += 1
    }
    faces
  }

  private def toParIfPossible[A](iterable: Iterable[A]): Seq[A] =
    PlatformDependent.toPar(iterable)

}
