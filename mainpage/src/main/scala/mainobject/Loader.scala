package mainobject

object Loader {

  def load(items: Iterable[() => Any], rate: Long = 500): Unit = {
    items.head.apply()

    if (items.tail.nonEmpty) {
      scala.scalajs.js.timers.setTimeout(rate.toDouble) {
        load(items.tail, rate)
      }
    }
  }

}
