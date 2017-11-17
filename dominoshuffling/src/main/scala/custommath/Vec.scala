package custommath

import scala.language.implicitConversions

trait Vec {
  val dim: Int

  def toVec2: Vec2
  def toVec3: Vec3
  def toVec4: Vec4
}


case class Vec2(x1: Double, x2: Double) extends Vec {
  val dim: Int = 2

  def toVec2: Vec2 = this
  def toVec3: Vec3 = Vec3(x1, x2, 1.0)
  def toVec4: Vec4 = Vec4(x1, x2, 1.0, 1.0)
}

case class Vec3(x1: Double, x2: Double, x3: Double) extends Vec {
  val dim: Int = 3

  def toVec2: Vec2 = Vec2(x1, x2)
  def toVec3: Vec3 = this
  def toVec4: Vec4 = Vec4(x1, x2, x3, 1.0)
}

object Vec3 {
  implicit def fromTuple(v: (Double, Double, Double)): Vec3 = Vec3(v._1, v._2, v._3)
}

case class Vec4(x1: Double, x2: Double, x3: Double, x4: Double) extends Vec {
  val dim: Int = 4

  def toVec2: Vec2 = Vec2(x1, x2)
  def toVec3: Vec3 = Vec3(x1, x2, x3)
  def toVec4: Vec4 = this

  def *(that: Vec4): Vec4 = Vec4(this.x1 * that.x1, this.x2 * that.x2, this.x3 * that.x3, this.x4 * that.x4)

  def +(that: Vec4): Vec4 = Vec4(this.x1 + that.x1, this.x2 + that.x2, this.x3 + that.x3, this.x4 + that.x4)

  def toList: List[Double] = List(x1, x2, x3, x4)

  def toCSSColor: String = s"rgba(${(x1 * 255).toInt},${(x2 * 255).toInt},${(x3 * 255).toInt},$x4)"

  def map(f: (Double) => Double): Vec4 = Vec4(f(x1), f(x2), f(x3), f(x4))
}


object Vec4 {
  implicit def listEmbedding2(list: List[Vec2]): List[Vec4] = list.map(_.toVec4)
  implicit def listEmbedding3(list: List[Vec3]): List[Vec4] = list.map(_.toVec4)

  implicit def fromVec3(v: Vec3): Vec4 = v.toVec4
  implicit def fromVec2(v: Vec2): Vec4 = v.toVec4

  implicit def fromTuple(v: (Double, Double, Double, Double)): Vec4 = Vec4(v._1, v._2, v._3, v._4)
}