lazy val V = _root_.scalafix.sbt.BuildInfo

lazy val rulesCrossVersions = Seq(V.scala213, V.scala212)
lazy val scala3Version = "3.2.1"

inThisBuild(
  List(
    organization := "no.arktekk",
    organizationName := "Arktekk",
    tlBaseVersion := "0.1",
    homepage := Some(url("https://github.com/arktekk/scala3-scalafix")),
    licenses := List(License.Apache2),
    tlBaseVersion := "0.1",
    developers := List(
      Developer(
        "ingarabr",
        "Ingar Abrahamsen",
        "oss@abrahams1.com",
        url("https://github.com/ingarabr")
      ),
      Developer(
        "hamnis",
        "Erlend Hammnaberg",
        "erlend@hamnaberg.net",
        url("https://github.com/hamnis")
      )
    ),
    tlSonatypeUseLegacyHost := true,
    semanticdbEnabled := true,
    semanticdbVersion := scalafixSemanticdb.revision
  )
)

val circeDeps = List(
  "io.circe" %% "circe-generic",
  "io.circe" %% "circe-core"
).map(_ % "0.14.2")

val doobie = "org.tpolecat" %% "doobie-core" % "1.0.0-RC2"

lazy val `scala3-scalafix-root` = (project in file("."))
  .enablePlugins(NoPublishPlugin)
  .aggregate(
    rules.projectRefs ++
      input.projectRefs ++
      output.projectRefs ++
      tests.projectRefs: _*
  )

lazy val rules = projectMatrix
  .settings(
    name := "scala3-scalafix",
    moduleName := name.value,
    libraryDependencies += "ch.epfl.scala" %% "scalafix-core" % V.scalafixVersion
  )
  .defaultAxes(VirtualAxis.jvm)
  .jvmPlatform(V.scala213 :: V.scala212 :: Nil)

lazy val input = projectMatrix
  .enablePlugins(NoPublishPlugin)
  .settings(
    libraryDependencies ++= circeDeps,
    libraryDependencies += doobie,
    SettingKey[Boolean]("ide-skip-project") := true,
    headerSources / excludeFilter := AllPassFilter,
    tlFatalWarnings := false
  )
  .defaultAxes(VirtualAxis.jvm)
  .jvmPlatform(scalaVersions = rulesCrossVersions :+ scala3Version)

lazy val output = projectMatrix
  .enablePlugins(NoPublishPlugin)
  .settings(
    libraryDependencies ++= circeDeps,
    libraryDependencies += doobie,
    SettingKey[Boolean]("ide-skip-project") := true,
    headerSources / excludeFilter := AllPassFilter,
    tlFatalWarnings := false
  )
  .defaultAxes(VirtualAxis.jvm)
  .jvmPlatform(scalaVersions = rulesCrossVersions :+ scala3Version)

lazy val testsAggregate = Project("tests", file("target/testsAggregate"))
  .enablePlugins(NoPublishPlugin)
  .aggregate(tests.projectRefs: _*)

lazy val tests = projectMatrix
  .enablePlugins(NoPublishPlugin)
  .settings(
    scalafixTestkitOutputSourceDirectories :=
      TargetAxis
        .resolve(output, Compile / unmanagedSourceDirectories)
        .value,
    scalafixTestkitInputSourceDirectories :=
      TargetAxis
        .resolve(input, Compile / unmanagedSourceDirectories)
        .value,
    scalafixTestkitInputClasspath :=
      TargetAxis.resolve(input, Compile / fullClasspath).value,
    scalafixTestkitInputScalacOptions :=
      TargetAxis.resolve(input, Compile / scalacOptions).value,
    scalafixTestkitInputScalaVersion :=
      TargetAxis.resolve(input, Compile / scalaVersion).value
  )
  .defaultAxes(
    rulesCrossVersions.map(VirtualAxis.scalaABIVersion) :+ VirtualAxis.jvm: _*
  )
  .jvmPlatform(
    scalaVersions = Seq(V.scala213),
    axisValues = Seq(TargetAxis(scala3Version)),
    settings = Seq()
  )
  .dependsOn(rules)
  .enablePlugins(ScalafixTestkitPlugin)
