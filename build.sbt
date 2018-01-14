import sbt.Keys._

import scala.language.postfixOps

val akkaVersion = "2.5.8"
val akkaHttpVersion = "10.0.11"
val circeVersion = "0.9.0"

lazy val root = (project in file("."))
  .enablePlugins(BuildInfoPlugin)
  .settings(

    organization := "prowse.github.cosm1c",

    name := "http-importer",

    version := "1.0",

    scalaVersion := "2.12.4",

    libraryDependencies ++= Seq(
      "com.typesafe.akka" %% "akka-typed" % akkaVersion,
      "com.typesafe.akka" %% "akka-stream" % akkaVersion,
      "com.typesafe.akka" %% "akka-http-core" % akkaHttpVersion,
      "com.typesafe.akka" %% "akka-http" % akkaHttpVersion,
      "com.typesafe.akka" %% "akka-slf4j" % akkaVersion,
      "com.github.swagger-akka-http" %% "swagger-akka-http" % "0.11.0",

      "de.heikoseeberger" %% "akka-http-circe" % "1.19.0",
      "io.circe" %% "circe-core" % circeVersion,
      "io.circe" %% "circe-generic" % circeVersion,
      "io.circe" %% "circe-parser" % circeVersion,
      "io.circe" % "circe-java8_2.12" % circeVersion,

      "com.lightbend.akka" %% "akka-stream-alpakka-xml" % "0.15.1",
      "javax.xml.bind" % "jaxb-api" % "2.3.0",

      "com.typesafe.scala-logging" %% "scala-logging" % "3.7.2",
      "ch.qos.logback" % "logback-classic" % "1.2.3" % Runtime,
      "org.slf4j" % "jul-to-slf4j" % "1.7.25",

      "com.typesafe.akka" %% "akka-testkit" % akkaVersion % Test,
      "com.typesafe.akka" %% "akka-stream-testkit" % akkaVersion % Test,
      "org.scalatest" %% "scalatest" % "3.0.1" % Test,
      "org.mockito" % "mockito-all" % "1.10.19" % Test
    ),

    scalacOptions ++= Seq(
      "-target:jvm-1.8",
      "-deprecation",
      "-encoding", "UTF-8", // yes, this is 2 args
      "-feature",
      "-language:existentials",
      "-language:higherKinds",
      "-language:implicitConversions",
      "-unchecked",
      "-Xfatal-warnings",
      "-Xlint",
      "-Yno-adapted-args",
      "-Ywarn-dead-code", // N.B. doesn't work well with the ??? hole
      "-Ywarn-numeric-widen",
      "-Ywarn-value-discard",
      "-Xfuture",
      "-Ywarn-unused-import" // 2.11 only, seems to cause issues with generated sources?
    ),

    javacOptions ++= Seq(
      "-source", "1.8",
      "-target", "1.8"
    ),

    // Build Info
    buildInfoOptions += BuildInfoOption.ToJson,
    buildInfoPackage := "prowse",
    buildInfoKeys := Seq[BuildInfoKey](
      name,
      version,
      scalaVersion,
      sbtVersion,
      BuildInfoKey.action("buildInstant") {
        java.time.format.DateTimeFormatter.ISO_INSTANT.format(java.time.Instant.now())
      },
      "gitChecksum" -> {
        git.gitHeadCommit
      })
  )

//Warts.unsafe only to avoid false positives
wartremoverErrors ++= List(
  // TODO: use Wart.Any when switched to typed Akka
  //  Wart.Any,
  Wart.AsInstanceOf,
  //  Wart.DefaultArguments,
  Wart.EitherProjectionPartial,
  Wart.IsInstanceOf,
  Wart.TraversableOps,
  //  Wart.NonUnitStatements,
  Wart.Null,
  Wart.OptionPartial,
  Wart.Product,
  Wart.Return,
  Wart.Serializable,
  Wart.StringPlusAny,
  Wart.Throw,
  Wart.TryPartial,
  Wart.Var
)

wartremoverExcluded += sourceManaged.value / "main" / "sbt-buildinfo" / "BuildInfo.scala"
//wartremoverExcluded ++= ((sourceManaged.value / "main") ** "*.scala").get

//scalacOptions ++= scalafixScalacOptions.value
