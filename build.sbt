import scala.util.matching.Regex
import sbtcrossproject.CrossPlugin.autoImport.crossProject
import sbt.Keys._

name := "AztecDiamond"

val fastCompileRenderer = taskKey[File]("Return main file")
val fastCompileWebWorker = taskKey[File]("Return main file")
lazy val fastOptChangeHtml =
  taskKey[Unit]("Compile in fastOptJS and create html files accordingly.")

val fullCompileRenderer = taskKey[File]("Return main file")
val fullCompileWebWorker = taskKey[File]("Return main file")
lazy val fullOptChangeHtml =
  taskKey[Unit]("Compile in fullOptJS and create html files accordingly")

val fastCompileElectronMainProcess = taskKey[File]("Return main file")
val fastCompileElectronMainWindow = taskKey[File]("Return main file")
lazy val fastOptElectronApp =
  taskKey[Unit]("Compile in fastOptJS and create all electron files.")

val fullCompileElectronMainProcess = taskKey[File]("Return main file")
val fullCompileElectronMainWindow = taskKey[File]("Return main file")
lazy val fullOptElectronApp =
  taskKey[Unit]("Compile in fullOptJS and create all electron files.")

val releaseVersion: String = "1.1.0"

val commonSettings = Seq(
  version := releaseVersion,
  scalaVersion := "2.13.10",
  scalacOptions ++= Seq(
    "-deprecation",
    "-feature",
    "-encoding",
    "utf-8",
    "-Xfatal-warnings"
  )
)

def removeHtmlCommentLine(line: String): String =
  """<!--.+-->""".r.replaceAllIn(line, "").trim

def changePackageJSONVersion(line: String): String =
  if (line.contains("version"))
    """\d+\.\d+\.\d+""".r.replaceAllIn(line, releaseVersion)
  else line

fastOptElectronApp := {

  val mainProcessDirectory =
    (`electronMainProcess` / fastCompileElectronMainProcess).value
  val mainWindowDirectory =
    (`electronApp` / fastCompileElectronMainWindow).value

  IO.delete(baseDirectory.value / "electron/mainprocess")
  val concreteMainProcessFileName =
    """[a-z-\.]+$""".r.findFirstIn(mainProcessDirectory.toString).get
  IO.copyFile(
    mainProcessDirectory,
    baseDirectory.value / ("electron/mainprocess/" + concreteMainProcessFileName)
  )

  IO.delete(baseDirectory.value / "electron/mainwindow/js")
  val concreteMainWindowFileName =
    """[a-z-\.]+$""".r.findFirstIn(mainWindowDirectory.toString).get
  IO.copyFile(
    mainWindowDirectory,
    baseDirectory.value / ("electron/mainwindow/js/" + concreteMainWindowFileName)
  )

  val webWorkerDirectory = (webWorkerJS / fastCompileWebWorker).value
  val concreteWebWorkerFileName =
    """[a-z-\.]+$""".r.findFirstIn(webWorkerDirectory.toString).get
  IO.copyFile(
    webWorkerDirectory,
    baseDirectory.value / ("electron/mainwindow/js/" + concreteWebWorkerFileName)
  )

  val sourceHtml = IO.readLines(
    baseDirectory.value / "sourcehtml/domino-shuffling-implementation.html"
  )

  val html =
    baseDirectory.value / "electron/mainwindow/html/domino-shuffling-implementation.html"

  def setAppType(line: String): String =
    """(?<=<!--electron).+(?=electron-->)""".r.findFirstIn(line) match {
      case Some(content) =>
        content
      case None =>
        if ("""(?<=<!--web).+(?=web-->)""".r.findFirstIn(line).isDefined) ""
        else line
    }

  def changeImportScripts(line: String): String =
    if (
      line.contains("importScripts(data + \"./js/webworkerjs-fastopt.js\")")
    ) {
      "\timportScripts(data + \"../js/webworkerjs-fastopt.js\")"
    } else line

  IO.writeLines(
    html,
    sourceHtml
      .map(changeImportScripts)
      .map(setAppType)
      .map(removeHtmlCommentLine)
      .filter(_.nonEmpty)
  )

  val sourcePackageJSON =
    IO.readLines(baseDirectory.value / "sourcehtml/package.json")

  val packageJSON = baseDirectory.value / "electron/package.json"

  IO.writeLines(packageJSON, sourcePackageJSON.map(changePackageJSONVersion))

  IO.copyFile(
    baseDirectory.value / "webworker/.jvm/target/scala-2.12/web-worker.jar",
    baseDirectory.value / "electron/mainwindow/scala/web-worker.jar"
  )

  IO.copyFile(
    baseDirectory.value / "sourcehtml/style.css",
    baseDirectory.value / "electron/mainwindow/html/style.css"
  )
}

fullOptElectronApp := {

  val mainProcessDirectory =
    (`electronMainProcess` / fullCompileElectronMainProcess).value
  val mainWindowDirectory =
    (`electronApp` / fullCompileElectronMainWindow).value

  IO.delete(baseDirectory.value / "electron/mainprocess")
  val concreteMainProcessFileName =
    """[a-z-\.]+$""".r.findFirstIn(mainProcessDirectory.toString).get
  IO.copyFile(
    mainProcessDirectory,
    baseDirectory.value / ("electron/mainprocess/" + concreteMainProcessFileName)
  )

  IO.delete(baseDirectory.value / "electron/mainwindow/js")
  val concreteMainWindowFileName =
    """[a-z-\.]+$""".r.findFirstIn(mainWindowDirectory.toString).get
  IO.copyFile(
    mainWindowDirectory,
    baseDirectory.value / ("electron/mainwindow/js/" + concreteMainWindowFileName)
  )

  val webWorkerDirectory = (webWorkerJS / fullCompileWebWorker).value
  val concreteWebWorkerFileName =
    """[a-z-\.]+$""".r.findFirstIn(webWorkerDirectory.toString).get
  IO.copyFile(
    webWorkerDirectory,
    baseDirectory.value / ("electron/mainwindow/js/" + concreteWebWorkerFileName)
  )

  val sourceHtml = IO.readLines(
    baseDirectory.value / "sourcehtml/domino-shuffling-implementation.html"
  )

  val html =
    baseDirectory.value / "electron/mainwindow/html/domino-shuffling-implementation.html"

  def setAppType(line: String): String =
    """(?<=<!--electron).+(?=electron-->)""".r.findFirstIn(line) match {
      case Some(content) =>
        content
      case None =>
        if ("""(?<=<!--web).+(?=web-->)""".r.findFirstIn(line).isDefined) ""
        else line
    }

  def fastOptToFullOpt(line: String): String =
    new Regex("fastopt").replaceAllIn(line, "opt")

  def changeImportScripts(line: String): String =
    if (
      line.contains("importScripts(data + \"./js/webworkerjs-fastopt.js\")")
    ) {
      "\timportScripts(data + \"../js/webworkerjs-fastopt.js\")"
    } else line

  IO.writeLines(
    html,
    sourceHtml
      .map(changeImportScripts)
      .map(fastOptToFullOpt)
      .map(setAppType)
      .map(removeHtmlCommentLine)
      .filter(_.nonEmpty)
  )

  val sourcePackageJSON =
    IO.readLines(baseDirectory.value / "sourcehtml/package.json")

  val packageJSON = baseDirectory.value / "electron/package.json"

  IO.writeLines(
    packageJSON,
    sourcePackageJSON.map(fastOptToFullOpt).map(changePackageJSONVersion)
  )

  IO.copyFile(
    baseDirectory.value / "webworker/.jvm/target/scala-2.12/web-worker.jar",
    baseDirectory.value / "electron/mainwindow/scala/web-worker.jar"
  )

  IO.copyFile(
    baseDirectory.value / "sourcehtml/style.css",
    baseDirectory.value / "electron/mainwindow/html/style.css"
  )

}

fastOptChangeHtml := {

  import scala.jdk.CollectionConverters._

  IO.delete(baseDirectory.value / "compiled/jekyll")
  IO.copyDirectory(
    baseDirectory.value / "sourcehtml/jekyll",
    baseDirectory.value / "compiled/jekyll",
    overwrite = true
  )

  val rendererDirectory = (webApp / fastCompileRenderer).value
  val webWorkerDirectory = (webWorkerJS / fastCompileWebWorker).value

  // IO.delete(baseDirectory.value / "compiled/js")
  IO.copyDirectory(
    rendererDirectory.getParentFile,
    baseDirectory.value / "compiled/jekyll/js",
    overwrite = true
  )
  IO.copyDirectory(
    webWorkerDirectory.getParentFile,
    baseDirectory.value / "compiled/jekyll/js",
    overwrite = true
  )

  val sourceHtml = IO.readLines(
    baseDirectory.value / "sourcehtml/domino-shuffling-implementation.html"
  )

  val html =
    baseDirectory.value / "compiled/jekyll/domino-shuffling-implementation.html"

  def replaceBaseDirectory(line: String): String =
    new Regex("baseDirectory").replaceAllIn(
      line,
      baseDirectory.value.toPath.iterator().asScala.toList.mkString("/")
    )

  def setAppType(line: String): String =
    """(?<=<!--web).+(?=web-->)""".r.findFirstIn(line) match {
      case Some(content) =>
        content
      case None =>
        if (
          """(?<=<!--electron).+(?=electron-->)""".r.findFirstIn(line).isDefined
        ) ""
        else line
    }

  IO.writeLines(
    html,
    sourceHtml
      .map(replaceBaseDirectory)
      .map(setAppType)
      .map(removeHtmlCommentLine)
      .filter(_.nonEmpty)
  )

//  val sourceIndex = IO.readLines(baseDirectory.value / "sourcehtml/webapp-index.html")
//
//  val index = baseDirectory.value / "compiled/index.html"
//
//  IO.writeLines(
//    index,
//    sourceIndex.map(removeHtmlCommentLine)
//  )

  IO.copyFile(
    baseDirectory.value / "sourcehtml/style.css",
    baseDirectory.value / "compiled/jekyll/style.css"
  )
}

fullOptChangeHtml := {

  import scala.jdk.CollectionConverters._

  val rendererDirectory = (`webApp` / fullCompileRenderer).value
  val webWorkerDirectory = (webWorkerJS / fullCompileWebWorker).value

  IO.delete(baseDirectory.value / "compiled/js")
  IO.copyDirectory(
    rendererDirectory.getParentFile,
    baseDirectory.value / "compiled/js",
    overwrite = true
  )
  IO.copyDirectory(
    webWorkerDirectory.getParentFile,
    baseDirectory.value / "compiled/js",
    overwrite = true
  )

  val sourceHtml = IO.readLines(
    baseDirectory.value / "sourcehtml/domino-shuffling-implementation.html"
  )

  val html =
    baseDirectory.value / "compiled/domino-shuffling-implementation.html"

  def replaceBaseDirectory(line: String): String =
    new Regex("baseDirectory").replaceAllIn(
      line,
      baseDirectory.value.toPath.iterator().asScala.toList.mkString("/")
    )

  def fastOptToFullOpt(line: String): String =
    new Regex("fastopt").replaceAllIn(line, "opt")

  def setAppType(line: String): String =
    """(?<=<!--web).+(?=web-->)""".r.findFirstIn(line) match {
      case Some(content) =>
        content
      case None =>
        if (
          """(?<=<!--electron).+(?=electron-->)""".r.findFirstIn(line).isDefined
        ) ""
        else line
    }

  IO.writeLines(
    html,
    sourceHtml
      .map(replaceBaseDirectory)
      .map(fastOptToFullOpt)
      .map(setAppType)
      .map(removeHtmlCommentLine)
      .filter(_.nonEmpty)
  )

  val sourceIndex =
    IO.readLines(baseDirectory.value / "sourcehtml/webapp-index.html")

  val index = baseDirectory.value / "compiled/index.html"

  IO.writeLines(
    index,
    sourceIndex.map(removeHtmlCommentLine)
  )

  IO.copyFile(
    baseDirectory.value / "sourcehtml/style.css",
    baseDirectory.value / "compiled/style.css"
  )
}

lazy val `renderer` = project
  .in(file("./mainpage"))
  .enablePlugins(ScalaJSPlugin)
  .settings(commonSettings)
  .settings(
    libraryDependencies ++= Seq(
      "org.scala-js" %%% "scalajs-dom" % "2.4.0"
    )
  )
  .dependsOn(dominoShufflingAlgorithm.js)
  .disablePlugins(sbtassembly.AssemblyPlugin)

lazy val `electronFacade` = project
  .in(file("./facades"))
  .enablePlugins(ScalaJSPlugin)
  .settings(commonSettings)
  .settings(
    libraryDependencies ++= Seq(
      "org.scala-js" %%% "scalajs-dom" % "2.4.0"
    )
  )
  .disablePlugins(sbtassembly.AssemblyPlugin)

lazy val `electronShared` = project
  .in(file("electronshared"))
  .enablePlugins(ScalaJSPlugin)
  .settings(commonSettings)
  .settings(
    libraryDependencies ++= Seq(
      "org.scala-js" %%% "scalajs-dom" % "2.4.0",
      "io.suzaku" %%% "boopickle" % "1.4.0"
    )
  )
  .dependsOn(electronFacade)
  .disablePlugins(sbtassembly.AssemblyPlugin)

lazy val `electronMainProcess` = project
  .in(file("electronmainprocess"))
  .enablePlugins(ScalaJSPlugin)
  .settings(commonSettings)
  .settings(
    libraryDependencies ++= Seq(
      "org.scala-js" %%% "scalajs-dom" % "2.4.0",
      "io.suzaku" %%% "boopickle" % "1.4.0"
    ),
    scalaJSUseMainModuleInitializer := true,
    scalaJSLinkerConfig ~= { _.withModuleKind(ModuleKind.CommonJSModule) },
    fastCompileElectronMainProcess := {
      (Compile / fastOptJS).value.data
    },
    fullCompileElectronMainProcess := {
      (Compile / fullOptJS).value.data
    }
  )
  .dependsOn(electronShared)
  .disablePlugins(sbtassembly.AssemblyPlugin)

lazy val `electronApp` = project
  .in(file("./electronapp"))
  .enablePlugins(ScalaJSPlugin)
  .settings(commonSettings)
  .settings(
    libraryDependencies ++= Seq(
      "org.scala-js" %%% "scalajs-dom" % "2.4.0"
    ),
    scalaJSUseMainModuleInitializer := true,
    scalaJSLinkerConfig ~= { _.withModuleKind(ModuleKind.CommonJSModule) },
    fastCompileElectronMainWindow := {
      (Compile / fastOptJS).value.data
    },
    fullCompileElectronMainWindow := {
      (Compile / fullOptJS).value.data
    }
  )
  .dependsOn(renderer)
  .dependsOn(electronShared)
  .disablePlugins(sbtassembly.AssemblyPlugin)

lazy val `webApp` = project
  .in(file("./webapp"))
  .enablePlugins(ScalaJSPlugin)
  .settings(commonSettings)
  .settings(
    libraryDependencies ++= Seq(
      "org.scala-js" %%% "scalajs-dom" % "2.4.0" // ,
      // "org.scalatest" %%% "scalatest" % "3.0.1" % "test"
    ),
    fastCompileRenderer := {
      (Compile / fastOptJS).value.data
    },
    fullCompileRenderer := {
      (Compile / fullOptJS).value.data
    },
    scalaJSUseMainModuleInitializer := true
  )
  .dependsOn(renderer)
  .disablePlugins(sbtassembly.AssemblyPlugin)

lazy val `webWorker` = crossProject(JSPlatform, JVMPlatform)
  .in(file("./webworker"))
  .settings(commonSettings)
  .jsSettings(
    libraryDependencies ++= Seq(
      "org.scala-js" %%% "scalajs-dom" % "2.4.0"
    ),
    fastCompileWebWorker := {
      (Compile / fastOptJS).value.data
    },
    fullCompileWebWorker := {
      (Compile / fullOptJS).value.data
    },
    scalaJSUseMainModuleInitializer := true
  )
  .jvmSettings(
    assembly / test := {},
    assembly / assemblyJarName := "web-worker.jar",
    javaOptions += "-Xmx4G",
    run / fork := true
  )
  .settings(
  )
  .dependsOn(dominoShufflingAlgorithm)

lazy val webWorkerJS = `webWorker`.js.settings(name := "webWorkerJS")
lazy val webWorkerJVM = `webWorker`.jvm.settings(name := "webWorkerJVM")

lazy val `dominoShufflingAlgorithm` = crossProject(JSPlatform, JVMPlatform)
  .in(file("./dominoshuffling"))
  .settings(commonSettings)
  .settings(
    name := "shared",
    libraryDependencies ++= Seq(
      "io.suzaku" %%% "boopickle" % "1.4.0"
    )
  )
  .jvmSettings(
    libraryDependencies += "org.scala-lang.modules" %% "scala-parallel-collections" % "1.0.3"
  )
  .disablePlugins(sbtassembly.AssemblyPlugin)

lazy val dominoShufflingAlgorithmJS =
  dominoShufflingAlgorithm.js.settings(name := "sharedJS")
lazy val dominoShufflingAlgorithmJVM =
  dominoShufflingAlgorithm.jvm.settings(name := "sharedJVM")
//lazy val dominoShufflingAlgorithmJS = `dominoShufflingAlgorithm`
