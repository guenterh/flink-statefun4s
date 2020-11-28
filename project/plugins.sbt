addSbtPlugin("com.typesafe.sbt" % "sbt-native-packager" % "1.7.3")
addSbtPlugin("org.scalameta" % "sbt-scalafmt" % "2.4.0")
addSbtPlugin("ch.epfl.scala" % "sbt-scalafix" % "0.9.18-1")
addSbtPlugin("com.eed3si9n" % "sbt-assembly" % "0.15.0")
addSbtPlugin("com.47deg"  % "sbt-microsites" % "1.2.1")
addSbtPlugin("org.scalameta" % "sbt-mdoc" % "2.2.3" )
addSbtPlugin("io.github.davidgregory084" % "sbt-tpolecat" % "0.1.13")
addSbtPlugin("com.thesamet" % "sbt-protoc" % "1.0.0-RC4")

libraryDependencies += "com.thesamet.scalapb" %% "compilerplugin" % "0.10.9"