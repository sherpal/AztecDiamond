package geometry

import org.scalacheck.*
import org.scalacheck.Prop.*

val pointGen = for {
  x <- Gen.choose(-1000, 1000)
  y <- Gen.choose(-1000, 1000)
} yield Point(x, y)

val faceGen = pointGen.map(Face(_))
