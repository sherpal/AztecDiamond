package nodejs

import stream.Readable

import scala.scalajs.js
import scala.scalajs.js.annotation.JSImport
import scala.scalajs.js.|

@js.native
trait ChildProcess extends EventEmitter {

  def kill(signal: String = "SIGTERM"): Unit = js.native

  val stderr: Readable = js.native

  val stdin: Readable = js.native

  val stdio: Readable = js.native

  val stdout: Readable = js.native

}

/**
 * https://nodejs.org/api/child_process.html
 */
@js.native
@JSImport("child_process", JSImport.Namespace)
object ChildProcess extends js.Object {

  def spawn(command: String, args: js.Array[String] = js.Array[String]()): ChildProcess = js.native

  def exec(
            command: String,
            callback: js.Function3[js.Any, String | Buffer, String | Buffer, Unit]
          ): ChildProcess = js.native

}
