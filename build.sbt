import org.scalajs.linker.interface.ESVersion
import scala.util.matching.Regex
import sbtcrossproject.CrossPlugin.autoImport.crossProject
import sbt.Keys._
import scala.sys.process.Process

name := "AztecDiamond"

val laminarVersion = "15.0.0"

val releaseVersion: String = "1.1.0"

val isWindows = System.getProperty("os.name").toLowerCase().contains("windows")
val npm       = if (isWindows) "npm.cmd" else "npm"

val commonSettings = Seq(
  version      := releaseVersion,
  scalaVersion := "3.2.2",
  scalacOptions ++= Seq(
    "-encoding",
    "utf8",
    "-Xfatal-warnings",
    "-deprecation",
    "-unchecked",
    "-language:higherKinds",
    "-feature",
    "-language:implicitConversions"
  )
)

def removeHtmlCommentLine(line: String): String =
  """<!--.+-->""".r.replaceAllIn(line, "").trim

def changePackageJSONVersion(line: String): String =
  if (line.contains("version"))
    """\d+\.\d+\.\d+""".r.replaceAllIn(line, releaseVersion)
  else line

lazy val `renderer` = project
  .in(file("./mainpage"))
  .enablePlugins(ScalaJSPlugin)
  .settings(commonSettings)
  .settings(
    libraryDependencies ++= Seq(
      "com.raquo"   %%% "laminar"            % laminarVersion,
      "be.doeraene" %%% "web-components-ui5" % "1.10.0",
      "be.doeraene" %%% "url-dsl"            % "0.6.0"
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
      "io.suzaku"    %%% "boopickle"   % "1.4.0"
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
      "io.suzaku"    %%% "boopickle"   % "1.4.0"
    ),
    scalaJSUseMainModuleInitializer := true,
    scalaJSLinkerConfig ~= { _.withModuleKind(ModuleKind.CommonJSModule) }
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
    scalaJSLinkerConfig ~= { _.withModuleKind(ModuleKind.CommonJSModule) }
  )
  .dependsOn(renderer)
  .dependsOn(electronShared)
  .disablePlugins(sbtassembly.AssemblyPlugin)

lazy val `webApp` = project
  .in(file("./webapp"))
  .enablePlugins(ScalaJSPlugin)
  .settings(commonSettings)
  .settings(
    libraryDependencies ++= List(
    ),
    scalaJSLinkerConfig ~= {
      _.withModuleKind(ModuleKind.ESModule)
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
    scalaJSUseMainModuleInitializer                     := true,
    Compile / fastLinkJS / scalaJSLinkerOutputDirectory := file("webapp") / "public" / "js" / "gen",
    Compile / fullLinkJS / scalaJSLinkerOutputDirectory := file("webapp") / "public" / "js" / "gen"
  )
  .jvmSettings(
    assembly / test            := {},
    assembly / assemblyJarName := "web-worker.jar",
    javaOptions += "-Xmx4G",
    run / fork := true
  )
  .settings(
  )
  .dependsOn(dominoShufflingAlgorithm)

lazy val `dominoShufflingAlgorithm` = crossProject(JSPlatform, JVMPlatform)
  .in(file("./dominoshuffling"))
  .settings(commonSettings)
  .settings(
    name := "shared",
    libraryDependencies ++= Seq(
      "io.suzaku"    %%% "boopickle" % "1.4.0",
      "ai.dragonfly" %%% "narr"      % "0.101"
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

val buildWebApp = taskKey[Unit]("Builds the web application, ready to be deployed.")

buildWebApp := {
  val buildResult = Process(npm :: "run" :: "rawBuild" :: Nil, baseDirectory.value / "webapp").!
  if (buildResult != 0) {
    throw new RuntimeException("Failure when building vite")
  }

  val options = CopyOptions().withOverwrite(true)

  val folders = List("shapes", "examples", "generation", "algorithm")

  folders.foreach { folder =>
    IO.copyFile(
      baseDirectory.value / "webapp" / "dist" / "index.html",
      baseDirectory.value / "webapp" / "dist" / folder / "index.html",
      options
    )
  }
}

buildWebApp := buildWebApp.dependsOn(webApp / Compile / fullLinkJS).value
buildWebApp := buildWebApp.dependsOn(webWorker.js / Compile / fullLinkJS).value
