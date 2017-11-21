package nodejs.https

import scala.scalajs.js
import scala.scalajs.js.annotation.JSImport
import scala.scalajs.js.|


@js.native
@JSImport("https", JSImport.Namespace)
object HTTPS extends js.Object {

  def get(options: HTTPSRequestOptions | String, callback: js.Function1[ServerResponse, Unit]): ClientRequest = js.native


}
