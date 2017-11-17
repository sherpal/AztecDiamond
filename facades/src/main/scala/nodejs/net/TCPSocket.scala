package nodejs.net

import nodejs.EventEmitter

import scala.scalajs.js

@js.native
trait TCPSocket extends EventEmitter {

  def write(data: String, encoding: String = "utf8"): Unit = js.native

}
