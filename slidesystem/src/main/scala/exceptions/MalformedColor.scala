package exceptions

import scala.scalajs.js

final class MalformedColor(val colors: js.Array[js.Array[Int]]) extends Throwable
