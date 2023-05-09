package geometry

import org.scalacheck.*
import org.scalacheck.Prop.*

object PointChecks extends Properties("Point") {

  property("A point is adjacent to its adjacent points") = forAll(pointGen) { point =>
    Prop.all(point.adjacentPoints.map(adj => Prop(adj.adjacentPoints.contains(point))).toList: _*)
  }

}
