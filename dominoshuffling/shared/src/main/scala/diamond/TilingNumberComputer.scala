package diamond

import custommath.QRoot
import narr.NArray

import scala.annotation.tailrec

final class TilingNumberComputer(
    diamond: Diamond,
    weights: ComputePartitionFunctionWeight,
    statusCallback: Int => Unit,
    subroutineStatusCallback: Int => Unit
) {

  def order: Int = diamond.order

  val totalNumberOfSubDiamonds: Long = diamond.numberOfSubDiamonds

  private var lastSubRoutingSentStatus = -1

  private def sendStatus(currentPath: Path, currentIndex: Int, thisIsANewLoop: Boolean): Unit =
    if thisIsANewLoop && currentPath.indices.nonEmpty then {
      lastSubRoutingSentStatus = -1
      statusCallback((currentPath(0) * 100.0 / totalNumberOfSubDiamonds).toInt)
    } else {
      val newStatus = currentIndex * 100 / order
      if newStatus != lastSubRoutingSentStatus then {
        subroutineStatusCallback(newStatus)
        lastSubRoutingSentStatus = newStatus
      }
    }

  private case class Path(indices: Vector[Int]) {

    inline def apply(idx: Int): Int = indices(idx)

    def increaseOnIndex(indexToIncrease: Int): Option[Path] = Option.when(indexToIncrease >= 0) {
      // noinspection RangeToIndices
      Path(
        indices
          .zip(0 until indices.length)
          .map { (diamondIndex, index) =>
            if index < indexToIncrease then diamondIndex else if index == indexToIncrease then diamondIndex + 1 else 0
          }
      )
    }

    def probabilityFactorForPath: (QRoot, Int) = {
      @tailrec
      def accumulator(
          coefficientAcc: QRoot,
          indexForNextPath: Int,
          currentDiamond: Diamond,
          currentWeights: ComputePartitionFunctionWeight
      ): (QRoot, Int) = if currentDiamond.order == 1 then
        (thisStepProbability(currentDiamond, currentWeights) * coefficientAcc, indexForNextPath)
      else {
        val indexInPath         = order - currentDiamond.order
        val subDiamondIndex     = indices(indexInPath)
        val subDiamond          = currentDiamond.indexedSubDiamond(subDiamondIndex)
        val numberOfSubDiamonds = currentDiamond.numberOfSubDiamonds

        sendStatus(this, indexInPath, thisIsANewLoop = false)

        accumulator(
          coefficientAcc * thisStepProbability(currentDiamond, currentWeights),
          if numberOfSubDiamonds > subDiamondIndex + 1 then indexInPath else indexForNextPath,
          subDiamond,
          currentWeights.subWeights
        )
      }

      accumulator(QRoot.one, -1, diamond, weights)
    }
  }

  private def initialPath: Path = Path(Vector.fill(order - 1)(0))

  private def thisStepProbability(diamond: Diamond, weightTrait: WeightTrait[QRoot]): QRoot =
    diamond.activeFaces
      .filter(_.dominoes.count(diamond.contains) == 2)
      .map { face =>
        val (alpha, beta, gamma, delta) = face.getFaceWeights(weightTrait)
        if diamond.contains(face.horizontalDominoes._1) then alpha * gamma / (alpha * gamma + beta * delta)
        else beta * delta / (alpha * gamma + beta * delta)
      }
      .product

  def probability: QRoot = {
    @tailrec
    def probabilityAcc(maybePath: Option[Path], acc: QRoot): QRoot = maybePath match {
      case None => acc
      case Some(path) =>
        sendStatus(path, 0, thisIsANewLoop = true)
        val (coef, index) = path.probabilityFactorForPath
        probabilityAcc(path.increaseOnIndex(index), acc + coef)
    }

    probabilityAcc(Some(initialPath), QRoot.zero)
  }

}
