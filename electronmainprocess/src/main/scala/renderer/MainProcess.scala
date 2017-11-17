package renderer

import electron._
import globalvariables.WindowId
import nodejs.Path
import org.scalajs.dom.Event
import storage.Storage

import scala.collection.mutable
import scala.scalajs.js.UndefOr

object MainProcess {

  val windows: mutable.Set[BrowserWindowMainProcess] = mutable.Set()

  private var mainWindow: BrowserWindowMainProcess = _

  def main(args: Array[String]): Unit = {

    Storage

    def createWindow(): Unit = {

      val win = new BrowserWindowMainProcess(new BrowserWindowOptions {
        override val width: UndefOr[Int] = if (scala.scalajs.LinkingInfo.developmentMode) 1200 else 400
        override val height: UndefOr[Int] = 800

        override val show: UndefOr[Boolean] = false

        //        override val icon: UndefOr[String] = Path.join(
        //          Path.dirname(js.Dynamic.global.myGlobalDirname.asInstanceOf[String]), "/assets/icon/pentagonBulletsIcon.ico"
        //        )

      })

      if (!scala.scalajs.LinkingInfo.developmentMode) {
        win.maximize()
      }

      win.loadURL(
        "file://" + Path.join(
          Path.dirname(ElectronGlobals.__dirname), "/mainwindow/html/domino-shuffling-implementation.html"
        )
      )

      if (scala.scalajs.LinkingInfo.developmentMode)
        win.webContents.openDevTools()

      win.webContents.on("did-finish-load", () => {
        Storage.storeVariable(win.webContents, "windowId", WindowId(win.id))
      })

      win.once("ready-to-show", () => {
        win.show()
        mainWindow = win
      })

      win.setMenu(null)

    }

  App.on("ready", () => createWindow())

  App.on("window-all-closed", () => App.quit())


  App.on("browser-window-created", (_: Event, window: BrowserWindowMainProcess) => {
    windows += window
    window.once("closed", () => {
      if (window == mainWindow) {
        App.quit()
      }
      windows -= window
    })
  })

  IPCMain.on("testing", (_: IPCMainEvent, a: Any) => {println(a)})

  IPCMain.on("flash-window", (event: IPCMainEvent) => {
    val senderWindow = BrowserWindowMainProcess.fromWebContents(event.sender)
    if (senderWindow != BrowserWindowMainProcess.getFocusedWindow()) {
      BrowserWindowMainProcess.fromWebContents(event.sender).flashFrame(true)
    }
  })

  }



}
