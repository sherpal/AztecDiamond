package geometry

import diamond.DiamondConstruction

final class RegressionTests extends munit.FunSuite {

  test("Generation of faces is not broken") {
    def oldActiveFaces(n: Int): Seq[Face] = for {
      j <- 0 to (-n + 1) by -1
      k <- 0 until n
    } yield Face(Point(j + k, -n + 1 - j + k))

    (1 to 100).foreach { order =>
      val old = oldActiveFaces(order).toList
      val ne  = Face.activeFaces(order).toList

      assertEquals(ne, old)
    }
  }

  test("Generation of points is not broken") {
    def oldAllPoints(order: Int): List[Point] = (for {
      y <- 1 to order
      x <- -order + y to order + 1 - y
    } yield Point(x, y)).flatMap { case Point(x, y) =>
      List(Point(x, y), Point(x, -y + 1))
    }.toList

    (1 to 100).foreach { order =>
      val old = oldAllPoints(order)
      val ne  = DiamondConstruction.allPoints(order).toList
      assertEquals(ne, old)
    }
  }

}
