package dominoexports

import io.circe.Encoder
import diamond.Diamond

import io.circe.generic.semiauto.deriveEncoder
import geometry.{Domino, Point}
import diamond.DiamondType
import diamond.DiamondType.DiamondTypeWithArgs

/** We encode [[Diamond]] in a "compressed" way, where each domino is represented by its "lower left" point as an array
  * of [x, y] coordinates.
  *
  * Decoding the diamond can be done by separately reading the horizontal and vertical dominoes.
  *
  * Note that in theory we could compress further more by only returning the horizontal dominoes, as they then fully
  * determine the vertical ones.
  */
object JsonSupport {

  given Encoder[Point] = Encoder[(Int, Int)].contramap { case Point(x, y) =>
    (x, y)
  }

  given Encoder[Domino] = Encoder[Point].contramap(_.lowerLeft)

  given Encoder[DiamondType] = Encoder[String].contramap(_.name)

  private case class DiamondForJson(
      order: Int,
      horizontalDominoes: List[Domino],
      verticalDominoes: List[Domino]
  )

  given Encoder[Diamond] = deriveEncoder[DiamondForJson].contramap((diamond: Diamond) =>
    DiamondForJson(
      diamond.order,
      horizontalDominoes = diamond.dominoes.filter(_.isHorizontal).toList,
      verticalDominoes = diamond.dominoes.filter(_.isVertical).toList
    )
  )

  private case class DiamondWithMetadataForJson(
      diamond: Diamond,
      diamondType: DiamondType,
      args: Vector[Double]
  )

  case class DiamondWithMetadata(
      diamond: Diamond,
      diamondTypeWithArg: DiamondTypeWithArgs
  )

  given Encoder[DiamondWithMetadata] = deriveEncoder[DiamondWithMetadataForJson].contramap[DiamondWithMetadata] {
    diamondWithMetadata =>
      DiamondWithMetadataForJson(
        diamondWithMetadata.diamond,
        diamondWithMetadata.diamondTypeWithArg.diamondType,
        diamondWithMetadata.diamondTypeWithArg.transformArgumentsBack.toVector
      )
  }

}
