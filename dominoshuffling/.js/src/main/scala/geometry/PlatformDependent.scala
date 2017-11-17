package geometry

import scala.collection.GenSeq

private[geometry] object PlatformDependent {

  def toPar[A](traversable: Traversable[A]): GenSeq[A] = traversable.toList

}
