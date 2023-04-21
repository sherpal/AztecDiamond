package geometry

private[geometry] object PlatformDependent {

  def toPar[A](traversable: Iterable[A]): Seq[A] = traversable.toList

}
