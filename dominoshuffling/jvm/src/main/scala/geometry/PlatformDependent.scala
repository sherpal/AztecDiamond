package geometry

import scala.collection.parallel.CollectionConverters._

private[geometry] object PlatformDependent {

  def toPar[A](traversable: Iterable[A]): Seq[A] = traversable.toList

}
