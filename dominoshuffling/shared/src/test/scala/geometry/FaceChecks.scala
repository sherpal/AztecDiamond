package geometry

import org.scalacheck.*
import org.scalacheck.Prop.*

object FaceChecks extends Properties("Face") {

  property("Horizontal dominoes of face are horizontal") = forAll(faceGen) { face =>
    val (h1, h2) = face.horizontalDominoes
    h1.isHorizontal && h2.isHorizontal
  }

  property("Vertical dominoes of case are vertical") = forAll(faceGen) { face =>
    val (v1, v2) = face.verticalDominoes
    v1.isVertical && v2.isVertical
  }

  property("diamondConstructionFaceMapping removes dominoes when there are 2") = forAll(faceGen) { face =>
    face.diamondConstructionFaceMapping(_.isHorizontal, (0, 0, 0, 0), 2).isEmpty &&
    face.diamondConstructionFaceMapping(_.isVertical, (0, 0, 0, 0), 2).isEmpty

  }

}
