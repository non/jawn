import ReleaseTransformations._

lazy val previousJawnVersion = "1.1.2"

lazy val scala212 = "2.12.14"
lazy val scala213 = "2.13.6"
lazy val scala3 = "3.0.1"
ThisBuild / scalaVersion := scala212
ThisBuild / organization := "org.typelevel"
ThisBuild / licenses += ("MIT", url("http://opensource.org/licenses/MIT"))
ThisBuild / homepage := Some(url("http://github.com/typelevel/jawn"))
ThisBuild / versionScheme := Some("early-semver")
ThisBuild / scmInfo := Some(
  ScmInfo(
    browseUrl = url("https://github.com/typelevel/jawn"),
    connection = "scm:git:git@github.com:typelevel/jawn.git"
  )
)

ThisBuild / developers += Developer(
  name = "Erik Osheim",
  email = "erik@plastic-idolatry.com",
  id = "d_m",
  url = url("http://github.com/non/")
)

lazy val benchmarkVersion =
  scala212

lazy val jawnSettings = Seq(
  crossScalaVersions := Seq(scala212, scala213, scala3),
  mimaPreviousArtifacts := Set(organization.value %% moduleName.value % previousJawnVersion),
  resolvers += Resolver.sonatypeRepo("releases"),
  Test / testOptions += Tests.Argument(TestFrameworks.ScalaCheck, "-verbosity", "1"),
  libraryDependencies += "org.scalacheck" %%% "scalacheck" % "1.15.4" % Test,
  scalacOptions ++=
    "-deprecation" ::
      "-encoding" :: "utf-8" ::
      "-feature" ::
      "-unchecked" ::
      "-Xlint" ::
      "-opt:l:method" ::
      Nil,
  // release stuff
  releaseCrossBuild := true,
  releaseVcsSign := true,
  publishMavenStyle := true,
  Test / publishArtifact := false,
  pomIncludeRepository := Function.const(false),
  publishTo := {
    val nexus = "https://oss.sonatype.org/"
    if (isSnapshot.value)
      Some("Snapshots".at(nexus + "content/repositories/snapshots"))
    else
      Some("Releases".at(nexus + "service/local/staging/deploy/maven2"))
  },
  releaseProcess := Seq[ReleaseStep](
    checkSnapshotDependencies,
    inquireVersions,
    runClean,
    releaseStepCommandAndRemaining("+test"), // formerly runTest
    setReleaseVersion,
    commitReleaseVersion,
    tagRelease,
    releaseStepCommandAndRemaining("+publishSigned"),
    setNextVersion,
    commitNextVersion,
    releaseStepCommandAndRemaining("sonatypeReleaseAll"),
    pushChanges
  )
)

lazy val jawnSettingsJVM = List(Test / fork := true)
lazy val jawnSettingsJS = List(
  scalaJSLinkerConfig ~= {
    _.withSemantics(
      _.withAsInstanceOfs(org.scalajs.linker.interface.CheckedBehavior.Unchecked)
        .withArrayIndexOutOfBounds(org.scalajs.linker.interface.CheckedBehavior.Unchecked)
    )
  },
  mimaPreviousArtifacts := Set()
)

lazy val noPublish = Seq(publish / skip := true, mimaPreviousArtifacts := Set())

lazy val root = project
  .in(file("."))
  .aggregate(all.flatMap(_.componentProjects).map(Project.projectToRef): _*)
  .disablePlugins(JmhPlugin)
  .settings(name := "jawn")
  .settings(jawnSettings: _*)
  .settings(crossScalaVersions := List())
  .settings(noPublish: _*)

lazy val parser = crossProject(JVMPlatform, JSPlatform)
  .crossType(CrossType.Full)
  .in(file("parser"))
  .settings(name := "parser")
  .settings(moduleName := "jawn-parser")
  .settings(jawnSettings: _*)
  .jvmSettings(jawnSettingsJVM: _*)
  .jsSettings(jawnSettingsJS: _*)
  .disablePlugins(JmhPlugin)

lazy val util = crossProject(JVMPlatform, JSPlatform)
  .crossType(CrossType.Full)
  .in(file("util"))
  .dependsOn(parser % "compile->compile;test->test")
  .settings(name := "util")
  .settings(moduleName := "jawn-util")
  .settings(jawnSettings: _*)
  .jvmSettings(jawnSettingsJVM: _*)
  .jsSettings(jawnSettingsJS: _*)
  .disablePlugins(JmhPlugin)

lazy val ast = crossProject(JVMPlatform, JSPlatform)
  .crossType(CrossType.Full)
  .in(file("ast"))
  .dependsOn(parser % "compile->compile;test->test")
  .dependsOn(util % "compile->compile;test->test")
  .settings(name := "ast")
  .settings(moduleName := "jawn-ast")
  .settings(jawnSettings: _*)
  .jvmSettings(jawnSettingsJVM: _*)
  .jsSettings(jawnSettingsJS: _*)
  .disablePlugins(JmhPlugin)

lazy val benchmark = project
  .in(file("benchmark"))
  .dependsOn(all.map(_.jvm).map(Project.classpathDependency[Project]): _*)
  .settings(name := "jawn-benchmark")
  .settings(jawnSettings: _*)
  .settings(scalaVersion := benchmarkVersion)
  .settings(crossScalaVersions := Seq(benchmarkVersion))
  .settings(noPublish: _*)
  .enablePlugins(JmhPlugin)

lazy val all =
  Seq(parser, util, ast)
