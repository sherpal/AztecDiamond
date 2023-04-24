import scala.util.matching.Regex
import sbtcrossproject.CrossPlugin.autoImport.crossProject
import sbt.Keys._

name := "AztecDiamond"

val laminarVersion = "15.0.0"

val releaseVersion: String = "1.1.0"

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
      "com.raquo"   %%% "laminar"            % laminarVersion,
      "be.doeraene" %%% "web-components-ui5" % "1.10.0",
      "be.doeraene" %%% "url-dsl"            % "0.6.0"
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
    scalaJSUseMainModuleInitializer := true
    // Compile / fastLinkJS / scalaJSLinkerOutputDirectory := file("./webapp") / "public" / "js" / "gen"
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

lazy val webWorkerJS  = `webWorker`.js.settings(name := "webWorkerJS")
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
