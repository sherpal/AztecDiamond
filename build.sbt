import scala.util.matching.Regex

name := "AztecDiamond"


val fastCompileRenderer = taskKey[File]("Return main file")
val fastCompileWebWorker = taskKey[File]("Return main file")
lazy val fastOptChangeHtml = taskKey[Unit]("Compile in fastOptJS and create html files accordingly.")

val fullCompileRenderer = taskKey[File]("Return main file")
val fullCompileWebWorker = taskKey[File]("Return main file")
lazy val fullOptChangeHtml = taskKey[Unit]("Compile in fullOptJS and create html files accordingly")

val fastCompileElectronMainProcess = taskKey[File]("Return main file")
val fastCompileElectronMainWindow = taskKey[File]("Return main file")
lazy val fastOptElectronApp = taskKey[Unit]("Compile in fastOptJS and create all electron files.")

val fullCompileElectronMainProcess = taskKey[File]("Return main file")
val fullCompileElectronMainWindow = taskKey[File]("Return main file")
lazy val fullOptElectronApp = taskKey[Unit]("Compile in fullOptJS and create all electron files.")

val fastCompileSlides = taskKey[File]("Return main file")
lazy val fastCompileMakeSlides = taskKey[Unit]("Compile the slides in fastOptJS and create the file.")

val fullCompileSlides = taskKey[File]("Return main file")
lazy val fullCompileMakeSlides = taskKey[Unit]("Compile the slides in fullOptJS and create the file.")

val releaseVersion: String = "1.1.0"

val commonSettings = Seq(
  version := releaseVersion,
  scalaVersion := "2.12.1",
  scalacOptions ++= Seq("-deprecation", "-feature", "-encoding", "utf-8")
)


def removeHtmlCommentLine(line: String): String = """<!--.+-->""".r.replaceAllIn(line, "").trim

def changePackageJSONVersion(line: String): String =
  if (line.contains("version")) """\d+\.\d+\.\d+""".r.replaceAllIn(line, releaseVersion)
  else line


fastOptElectronApp := {

  val mainProcessDirectory = (fastCompileElectronMainProcess in `electronMainProcess`).value
  val mainWindowDirectory = (fastCompileElectronMainWindow in `electronApp`).value

  IO.delete(baseDirectory.value / "electron/mainprocess")
  val concreteMainProcessFileName = """[a-z-\.]+$""".r.findFirstIn(mainProcessDirectory.toString).get
  IO.copyFile(
    mainProcessDirectory,
    baseDirectory.value / ("electron/mainprocess/" + concreteMainProcessFileName)
  )

  IO.delete(baseDirectory.value / "electron/mainwindow/js")
  val concreteMainWindowFileName = """[a-z-\.]+$""".r.findFirstIn(mainWindowDirectory.toString).get
  IO.copyFile(
    mainWindowDirectory,
    baseDirectory.value / ("electron/mainwindow/js/" + concreteMainWindowFileName)
  )

  val webWorkerDirectory = (fastCompileWebWorker in webWorkerJS).value
  val concreteWebWorkerFileName = """[a-z-\.]+$""".r.findFirstIn(webWorkerDirectory.toString).get
  IO.copyFile(
    webWorkerDirectory,
    baseDirectory.value / ("electron/mainwindow/js/" + concreteWebWorkerFileName)
  )


  val sourceHtml = IO.readLines(baseDirectory.value / "sourcehtml/domino-shuffling-implementation.html")

  val html = baseDirectory.value / "electron/mainwindow/html/domino-shuffling-implementation.html"

  def setAppType(line: String): String =
    """(?<=<!--electron).+(?=electron-->)""".r.findFirstIn(line) match {
      case Some(content) =>
        content
      case None =>
        if ("""(?<=<!--web).+(?=web-->)""".r.findFirstIn(line).isDefined) "" else line
    }

  def changeImportScripts(line: String): String =
    if (line.contains("importScripts(data + \"./js/webworkerjs-fastopt.js\")")) {
      "\timportScripts(data + \"../js/webworkerjs-fastopt.js\")"
    } else line

  IO.writeLines(html, sourceHtml.map(changeImportScripts).map(setAppType).map(removeHtmlCommentLine).filter(_.nonEmpty))

  val sourcePackageJSON = IO.readLines(baseDirectory.value / "sourcehtml/package.json")

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

  val mainProcessDirectory = (fullCompileElectronMainProcess in `electronMainProcess`).value
  val mainWindowDirectory = (fullCompileElectronMainWindow in `electronApp`).value

  IO.delete(baseDirectory.value / "electron/mainprocess")
  val concreteMainProcessFileName = """[a-z-\.]+$""".r.findFirstIn(mainProcessDirectory.toString).get
  IO.copyFile(
    mainProcessDirectory,
    baseDirectory.value / ("electron/mainprocess/" + concreteMainProcessFileName)
  )

  IO.delete(baseDirectory.value / "electron/mainwindow/js")
  val concreteMainWindowFileName = """[a-z-\.]+$""".r.findFirstIn(mainWindowDirectory.toString).get
  IO.copyFile(
    mainWindowDirectory,
    baseDirectory.value / ("electron/mainwindow/js/" + concreteMainWindowFileName)
  )

  val webWorkerDirectory = (fullCompileWebWorker in webWorkerJS).value
  val concreteWebWorkerFileName = """[a-z-\.]+$""".r.findFirstIn(webWorkerDirectory.toString).get
  IO.copyFile(
    webWorkerDirectory,
    baseDirectory.value / ("electron/mainwindow/js/" + concreteWebWorkerFileName)
  )

  val sourceHtml = IO.readLines(baseDirectory.value / "sourcehtml/domino-shuffling-implementation.html")

  val html = baseDirectory.value / "electron/mainwindow/html/domino-shuffling-implementation.html"

  def setAppType(line: String): String =
    """(?<=<!--electron).+(?=electron-->)""".r.findFirstIn(line) match {
      case Some(content) =>
        content
      case None =>
        if ("""(?<=<!--web).+(?=web-->)""".r.findFirstIn(line).isDefined) "" else line
    }

  def fastOptToFullOpt(line: String): String = new Regex("fastopt").replaceAllIn(line, "opt")

  def changeImportScripts(line: String): String =
    if (line.contains("importScripts(data + \"./js/webworkerjs-fastopt.js\")")) {
      "\timportScripts(data + \"../js/webworkerjs-fastopt.js\")"
    } else line

  IO.writeLines(
    html, sourceHtml
    .map(changeImportScripts)
    .map(fastOptToFullOpt)
    .map(setAppType)
    .map(removeHtmlCommentLine)
    .filter(_.nonEmpty)
  )

  val sourcePackageJSON = IO.readLines(baseDirectory.value / "sourcehtml/package.json")

  val packageJSON = baseDirectory.value / "electron/package.json"

  IO.writeLines(packageJSON, sourcePackageJSON.map(fastOptToFullOpt).map(changePackageJSONVersion))

  IO.copyFile(
    baseDirectory.value / "webworker/.jvm/target/scala-2.12/web-worker.jar",
    baseDirectory.value / "electron/mainwindow/scala/web-worker.jar"
  )

  IO.copyFile(
    baseDirectory.value / "sourcehtml/style.css",
    baseDirectory.value / "electron/mainwindow/html/style.css"
  )

}


fastCompileMakeSlides := {

  val slideDirectory = (fastCompileSlides in slideSystem).value
  val webWorkerDirectory = (fastCompileWebWorker in webWorkerJS).value

  val sourceHtml = IO.readLines(baseDirectory.value / "slides/aztec-slide.html")

  val html = baseDirectory.value / "slides/compiled-aztec-slide.html"

  IO.writeLines(
    html,
    sourceHtml.flatMap({
      case "//##workercode" => IO.readLines(webWorkerDirectory)
      case "//##slidecode" => IO.readLines(slideDirectory)
      case line => List(line)
    })
  )

}

fullCompileMakeSlides := {

  val slideDirectory = (fullCompileSlides in slideSystem).value
  val webWorkerDirectory = (fullCompileWebWorker in webWorkerJS).value

  val sourceHtml = IO.readLines(baseDirectory.value / "slides/aztec-slide.html")

  val html = baseDirectory.value / "slides/compiled-aztec-slide.html"

  IO.writeLines(
    html,
    sourceHtml.flatMap({
      case "//##workercode" => IO.readLines(webWorkerDirectory)
      case "//##slidecode" => IO.readLines(slideDirectory)
      case line => List(line)
    })
  )

}

fastOptChangeHtml := {

  import scala.collection.JavaConversions._

  val rendererDirectory = (fastCompileRenderer in `webApp`).value
  val webWorkerDirectory = (fastCompileWebWorker in webWorkerJS).value

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

  val sourceHtml = IO.readLines(baseDirectory.value / "sourcehtml/domino-shuffling-implementation.html")

  val html = baseDirectory.value / "compiled/domino-shuffling-implementation.html"

  def replaceBaseDirectory(line: String): String =
    new Regex("baseDirectory").replaceAllIn(line, baseDirectory.value.toPath.iterator().toList.mkString("/"))

  def setAppType(line: String): String =
    """(?<=<!--web).+(?=web-->)""".r.findFirstIn(line) match {
      case Some(content) =>
        content
      case None =>
        if ("""(?<=<!--electron).+(?=electron-->)""".r.findFirstIn(line).isDefined) "" else line
    }

  IO.writeLines(
    html, sourceHtml.map(replaceBaseDirectory).map(setAppType).map(removeHtmlCommentLine).filter(_.nonEmpty)
  )

  val sourceIndex = IO.readLines(baseDirectory.value / "sourcehtml/webapp-index.html")

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

fullOptChangeHtml := {

  import scala.collection.JavaConversions._

  val rendererDirectory = (fullCompileRenderer in `webApp`).value
  val webWorkerDirectory = (fullCompileWebWorker in webWorkerJS).value

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

  val sourceHtml = IO.readLines(baseDirectory.value / "sourcehtml/domino-shuffling-implementation.html")

  val html = baseDirectory.value / "compiled/domino-shuffling-implementation.html"

  def replaceBaseDirectory(line: String): String =
    new Regex("baseDirectory").replaceAllIn(line, baseDirectory.value.toPath.iterator().toList.mkString("/"))

  def fastOptToFullOpt(line: String): String = new Regex("fastopt").replaceAllIn(line, "opt")

  def setAppType(line: String): String =
    """(?<=<!--web).+(?=web-->)""".r.findFirstIn(line) match {
      case Some(content) =>
        content
      case None =>
        if ("""(?<=<!--electron).+(?=electron-->)""".r.findFirstIn(line).isDefined) "" else line
    }


  IO.writeLines(
    html, sourceHtml.map(replaceBaseDirectory).map(fastOptToFullOpt).map(setAppType).map(removeHtmlCommentLine)
      .filter(_.nonEmpty)
  )

  val sourceIndex = IO.readLines(baseDirectory.value / "sourcehtml/webapp-index.html")

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

lazy val `renderer` = project.in(file("./mainpage"))
  .enablePlugins(JSDependenciesPlugin)
  .enablePlugins(ScalaJSPlugin)
  .settings(commonSettings)
  .settings(
    libraryDependencies ++= Seq(
      "org.scala-js" %%% "scalajs-dom" % "0.9.3"
    )
  )
  .dependsOn(dominoShufflingAlgorithmJS)
  .disablePlugins(sbtassembly.AssemblyPlugin)

lazy val `electronFacade` = project.in(file("./facades"))
  .enablePlugins(ScalaJSPlugin)
  .settings(commonSettings)
  .settings(
    libraryDependencies ++= Seq(
      "org.scala-js" %%% "scalajs-dom" % "0.9.3"
    )
  )
  .disablePlugins(sbtassembly.AssemblyPlugin)

lazy val `electronShared` = project.in(file("electronshared"))
  .enablePlugins(ScalaJSPlugin)
  .settings(commonSettings)
  .settings(
    libraryDependencies ++= Seq(
      "org.scala-js" %%% "scalajs-dom" % "0.9.3",
      "io.suzaku" %%% "boopickle" % "1.2.7-SNAPSHOT"
    )
  )
  .dependsOn(electronFacade)
  .disablePlugins(sbtassembly.AssemblyPlugin)

lazy val `electronMainProcess` = project.in(file("electronmainprocess"))
  .enablePlugins(ScalaJSPlugin)
  .settings(commonSettings)
  .settings(
    libraryDependencies ++= Seq(
      "org.scala-js" %%% "scalajs-dom" % "0.9.3",
      "io.suzaku" %%% "boopickle" % "1.2.7-SNAPSHOT"
    ),
    scalaJSUseMainModuleInitializer := true,
    scalaJSLinkerConfig ~= { _.withModuleKind(ModuleKind.CommonJSModule) },
    fastCompileElectronMainProcess := {
      (fastOptJS in Compile).value.data
    },
    fullCompileElectronMainProcess := {
      (fullOptJS in Compile).value.data
    }
  )
  .dependsOn(electronShared)
  .disablePlugins(sbtassembly.AssemblyPlugin)

lazy val `electronApp` = project.in(file("./electronapp"))
  .enablePlugins(ScalaJSPlugin)
  .settings(commonSettings)
  .settings(
    libraryDependencies ++= Seq(
      "org.scala-js" %%% "scalajs-dom" % "0.9.3"
    ),
    scalaJSUseMainModuleInitializer := true,
    scalaJSLinkerConfig ~= { _.withModuleKind(ModuleKind.CommonJSModule) },
    fastCompileElectronMainWindow := {
      (fastOptJS in Compile).value.data
    },
    fullCompileElectronMainWindow := {
      (fullOptJS in Compile).value.data
    }
  )
  .dependsOn(renderer)
  .dependsOn(electronShared)
  .disablePlugins(sbtassembly.AssemblyPlugin)

lazy val `webApp` = project.in(file("./webapp"))
  .enablePlugins(JSDependenciesPlugin)
  .enablePlugins(ScalaJSPlugin)
  .settings(commonSettings)
  .settings(
    libraryDependencies ++= Seq(
      "org.scala-js" %%% "scalajs-dom" % "0.9.3"//,
      //"org.scalatest" %%% "scalatest" % "3.0.1" % "test"
    ),
    fastCompileRenderer := {
      (fastOptJS in Compile).value.data
    },
    fullCompileRenderer := {
      (fullOptJS in Compile).value.data
    },
    scalaJSUseMainModuleInitializer := true
  )
  .dependsOn(renderer)
  .disablePlugins(sbtassembly.AssemblyPlugin)


/**
 * This project is intended to be used with Justin Dekeyser's Html/CSS Slide system.
 * It can also work with plain Html/CSS environment, but you might need to add a little CSS for it to be nice looking.
 */
lazy val `slideSystem` = project.in(file("./slidesystem"))
  .enablePlugins(JSDependenciesPlugin)
  .enablePlugins(ScalaJSPlugin)
  .settings(commonSettings)
  .settings(
    libraryDependencies ++= Seq(
      "org.scala-js" %%% "scalajs-dom" % "0.9.3"//,
      //"org.scalatest" %%% "scalatest" % "3.0.1" % "test"
    ),
    scalaJSUseMainModuleInitializer := true,
    fastCompileSlides := {
      (fastOptJS in Compile).value.data
    },
    fullCompileSlides := {
      (fullOptJS in Compile).value.data
    }
  )
  .dependsOn(renderer)
  .disablePlugins(sbtassembly.AssemblyPlugin)

lazy val `webWorker` = crossProject(JSPlatform, JVMPlatform).crossType(CrossType.Pure).in(file("./webworker"))
  .settings(commonSettings)
    .jsSettings(
      libraryDependencies ++= Seq(
        "org.scala-js" %%% "scalajs-dom" % "0.9.3"
      ),
      fastCompileWebWorker := {
        (fastOptJS in Compile).value.data
      },
      fullCompileWebWorker := {
        (fullOptJS in Compile).value.data
      },
      scalaJSUseMainModuleInitializer := true
    )
    .jvmSettings(
      test in assembly := {},
      assemblyJarName in assembly := "web-worker.jar",
      javaOptions += "-Xmx4G",
      fork in run := true
    )
  .settings(

  )
  .dependsOn(dominoShufflingAlgorithm)

lazy val webWorkerJS = `webWorker`.js.settings(name := "webWorkerJS")
lazy val webWorkerJVM = `webWorker`.jvm.settings(name := "webWorkerJVM")

lazy val `dominoShufflingAlgorithm` = crossProject(JSPlatform, JVMPlatform).crossType(CrossType.Pure)
  .in(file("./dominoshuffling"))
  .settings(commonSettings)
  .jsConfigure(_.enablePlugins(JSDependenciesPlugin))
  .settings(
    name := "shared",
    libraryDependencies ++= Seq(
      "io.suzaku" %%% "boopickle" % "1.2.7-SNAPSHOT"
    )
  )
  .disablePlugins(sbtassembly.AssemblyPlugin)



lazy val dominoShufflingAlgorithmJS = dominoShufflingAlgorithm.js.settings(name := "sharedJS")
lazy val dominoShufflingAlgorithmJVM = dominoShufflingAlgorithm.jvm.settings(name := "sharedJVM")
//lazy val dominoShufflingAlgorithmJS = `dominoShufflingAlgorithm`

