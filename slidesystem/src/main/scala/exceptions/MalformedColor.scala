package exceptions

import scala.scalajs.js

class MalformedColor(val colors: js.Array[js.Array[Int]]) extends Throwable
