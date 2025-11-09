import org.scalajs.linker.interface.ESVersion

import scala.util.matching.Regex
import sbtcrossproject.CrossPlugin.autoImport.crossProject
import sbt.Keys.*

import java.nio.charset.StandardCharsets
import scala.sys.process.Process

name := "AztecDiamond"

val laminarVersion = "15.0.0"

val releaseVersion: String = "1.1.0"

val isWindows = System.getProperty("os.name").toLowerCase().contains("windows")
val npm       = if (isWindows) "npm.cmd" else "npm"

Global / onLoad := {
  val scalaVersionValue = (`webApp` / scalaVersion).value
  val outputFile        = baseDirectory.value / "webapp" / "scala-metadata.js"
  IO.writeLines(
    outputFile,
    s"""
       |const scalaVersion = "$scalaVersionValue"
       |
       |exports.scalaMetadata = {
       |  scalaVersion: scalaVersion
       |}
       |""".stripMargin.split("\n").toList,
    StandardCharsets.UTF_8
  )

  println("""
            |  ___      _             ______ _                                 _
            | / _ \    | |            |  _  (_)                               | |
            |/ /_\ \___| |_ ___  ___  | | | |_  __ _ _ __ ___   ___  _ __   __| |___
            ||  _  |_  / __/ _ \/ __| | | | | |/ _` | '_ ` _ \ / _ \| '_ \ / _` / __|
            || | | |/ /| ||  __/ (__  | |/ /| | (_| | | | | | | (_) | | | | (_| \__ \
            |\_| |_/___|\__\___|\___| |___/ |_|\__,_|_| |_| |_|\___/|_| |_|\__,_|___/
            |
            |""".stripMargin)

  (Global / onLoad).value
}

val commonSettings = Seq(
  version      := releaseVersion,
  scalaVersion := "3.7.0",
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

def circe = {
  val circeVersion = "0.14.1"

  Def.settings(
    libraryDependencies ++= Seq(
      "io.circe" %%% "circe-core",
      "io.circe" %%% "circe-generic",
      "io.circe" %%% "circe-parser"
    ).map(_ % circeVersion)
  )
}

def testDeps = Def.settings {
  libraryDependencies ++= List(
    "org.scalameta"  %%% "munit"      % "0.7.29",
    "org.scalacheck" %%% "scalacheck" % "1.17.0"
  ).map(_ % Test)
}

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
    ),
    circe
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
    ),
    testDeps
  )
  .jvmSettings(
    libraryDependencies += "org.scala-lang.modules" %% "scala-parallel-collections" % "1.0.3"
  )
  .jsSettings(
    Test / jsEnv := new org.scalajs.jsenv.nodejs.NodeJSEnv(
      org.scalajs.jsenv.nodejs.NodeJSEnv.Config().withSourceMap(true)
    )
  )
  .disablePlugins(sbtassembly.AssemblyPlugin)

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
