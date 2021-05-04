credentials in ThisBuild += Credentials(Path.userHome / ".sbt" / ".credentials")

addCommandAlias("f", ";scalafmtAll;scalafixAll")

def scalafixRunExplicitly: Def.Initialize[Task[Boolean]] =
  Def.task {
    executionRoots.value.exists { root =>
      Seq(
        scalafix.key,
        scalafixAll.key
      ).contains(root.key)
    }
  }

lazy val commonSettings = Seq(
  organization := "com.bcf",
  licenses ++= Seq(("MPL-2.0", url("https://mozilla.org/MPL/2.0"))),
  homepage := Some(url("https://github.com/BluechipFinancial/flink-statefun4s")),
  developers := List(
    Developer(
      "tdbgamer",
      "Timothy Bess",
      "tim.b@bluechipfinancial.com",
      url("https://github.com/tdbgamer")
    )
  ),
  scalaVersion := "2.13.3",
  version := "1.3.4",
  Compile / scalacOptions ++= Seq(
    "-Ymacro-annotations"
  ),
  Compile / console / scalacOptions --= Seq("-Xfatal-warnings", "-Ywarn-unused:imports"),
  Compile / doc / scalacOptions --= Seq("-Xfatal-warnings"),
  Compile / doc / scalacOptions ++= Seq(
    "-groups",
    "-sourcepath",
    (baseDirectory in LocalRootProject).value.getAbsolutePath
  ),
  scalafixDependencies in ThisBuild += "com.github.liancheng" %% "organize-imports" % "0.3.1-RC3",
  addCompilerPlugin("org.typelevel" % "kind-projector" % "0.11.0" cross CrossVersion.full),
  addCompilerPlugin("com.olegpy" %% "better-monadic-for" % "0.3.1"),
  publishTo := {
    val base = "https://bluechipfinancial.jfrog.io/artifactory/sbt-release-local"
    if (isSnapshot.value)
      Some("Artifactory Realm" at base + ";build.timestamp=" + new java.util.Date().getTime)
    else Some("Artifactory Realm" at base)
  },
  scalacOptions --= {
    if (!scalafixRunExplicitly.value) Seq() else Seq("-Xfatal-warnings")
  },
  semanticdbEnabled := true, // enable SemanticDB
  semanticdbVersion := scalafixSemanticdb.revision, // use Scalafix compatible version
)

lazy val root = project
  .in(file("."))
  .settings(commonSettings)
  .settings(
    name := "flink-statefun4s-root",
    crossScalaVersions := Nil,
    publish / skip := true
  )
  .dependsOn(core, example, docs)
  .aggregate(core, example, docs)

lazy val circeVersion = "0.13.0"

lazy val core = project
  .in(file("core"))
  .settings(commonSettings)
  .settings(
    name := "flink-statefun4s",
    description := "Statefun SDK for Scala",
    PB.targets in Compile := Seq(
      scalapb.gen() -> (sourceManaged in Compile).value / "scalapb"
    ),
    libraryDependencies ++= Seq(
      "com.olegpy" %% "meow-mtl-effects" % "0.4.0",
      "org.typelevel" %% "cats-core" % "2.0.0",
      "org.typelevel" %% "cats-mtl" % "1.0.0",
      "org.typelevel" %% "cats-effect" % "2.1.3",
      "org.http4s" %% "http4s-dsl" % "0.21.4",
      "org.http4s" %% "http4s-blaze-server" % "0.21.4",
      "org.typelevel" %% "simulacrum" % "1.0.0",
      "io.circe" %% "circe-core" % circeVersion,
      "io.circe" %% "circe-parser" % circeVersion,
      "com.thesamet.scalapb" %% "scalapb-runtime" % scalapb.compiler.Version.scalapbVersion % "protobuf",
    )
  )

lazy val example = project
  .in(file("example"))
  .settings(commonSettings)
  .settings(
    name := "flink-statefun4s-example",
    mainClass := Some("com.bcf.statefun4s.Example"),
    description := "Statefun SDK example",
    publish / skip := true,
    PB.targets in Compile := Seq(
      scalapb.gen() -> (sourceManaged in Compile).value / "scalapb"
    ),
    libraryDependencies ++= Seq(
      "com.olegpy" %% "meow-mtl-effects" % "0.4.0",
      "org.typelevel" %% "cats-core" % "2.0.0",
      "org.typelevel" %% "cats-mtl" % "1.0.0",
      "org.typelevel" %% "cats-effect" % "2.1.3",
      "org.http4s" %% "http4s-dsl" % "0.21.12",
      "org.http4s" %% "http4s-blaze-server" % "0.21.4",
      "org.typelevel" %% "simulacrum" % "1.0.0",
      "com.thesamet.scalapb" %% "scalapb-runtime" % scalapb.compiler.Version.scalapbVersion % "protobuf",
      // Logging deps
      "org.slf4j" % "slf4j-api" % "1.7.30",
      "org.apache.logging.log4j" % "log4j-slf4j-impl" % "2.13.3",
      "org.apache.logging.log4j" % "log4j-core" % "2.13.3",
      "com.fasterxml.jackson.dataformat" % "jackson-dataformat-yaml" % "2.10.4",
      "com.fasterxml.jackson.core" % "jackson-databind" % "2.11.0",
      "io.chrisdavenport" %% "log4cats-slf4j" % "1.1.1",
      "com.github.mlangc" %% "zio-interop-log4j2" % "1.0.0-RC21"
    ),
    assemblyJarName in assembly := "statefun-greeter-example.jar",
    assemblyMergeStrategy in assembly := {
      case PathList("META-INF", "MANIFEST.MF") => MergeStrategy.discard
      case x                                   => MergeStrategy.first
    },
  )
  .dependsOn(core)

lazy val micrositeSettings: Seq[Def.Setting[_]] = Seq(
  micrositeName := "flink-statefun4s",
  micrositeDescription := "Scala SDK for Flink Statefun",
  micrositeBaseUrl := "flink-statefun4s",
  micrositeDocumentationUrl := "/flink-statefun4s/docs",
  micrositeGithubOwner := "BlueChipFinancial",
  micrositeGithubRepo := "flink-statefun4s",
  micrositeGithubToken := sys.env.get("GITHUB_TOKEN"),
  micrositePushSiteWith := GitHub4s,
  micrositeHighlightTheme := "atom-one-light",
  micrositeHighlightLanguages ++= Seq("protobuf"),
  includeFilter in makeSite := "*.html" | "*.css" | "*.png" | "*.jpg" | "*.gif" | "*.js" | "*.swf" | "*.md" | "*.svg"
)

lazy val docs = project
  .in(file("project-docs"))
  .dependsOn(core)
  .settings(commonSettings)
  .settings(micrositeSettings)
  .settings(
    publish / skip := true,
    libraryDependencies ++= Seq(
      "org.http4s" %% "http4s-client" % "0.21.4",
      "org.http4s" %% "http4s-blaze-client" % "0.21.4",
    )
  )
  .enablePlugins(MicrositesPlugin)
  .enablePlugins(MdocPlugin)