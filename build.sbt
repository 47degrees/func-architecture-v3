import sbt.Keys._

val scalaV = "2.11.7"

scalacOptions ++= Seq("-feature", "-language:higherKinds")

scalaVersion := scalaV

resolvers ++= Seq(
    Resolver.mavenLocal,
    DefaultMavenRepository,
    "jcenter" at "http://jcenter.bintray.com",
    "47 Degrees Bintray Repo" at "http://dl.bintray.com/47deg/maven",
    Resolver.typesafeRepo("releases"),
    Resolver.typesafeRepo("snapshots"),
    Resolver.typesafeIvyRepo("snapshots"),
    Resolver.sonatypeRepo("releases"),
    Resolver.sonatypeRepo("snapshots"),
    Resolver.defaultLocal,
    "Scalaz Bintray Repo" at "http://dl.bintray.com/scalaz/releases"
  ) 

libraryDependencies ++= Seq(
  "org.scalaz" %% "scalaz-concurrent" % "7.2.0",
  "org.spire-math" %% "cats" % "0.4.0-SNAPSHOT",
  "org.scala-lang" % "scala-compiler" % scalaV,
  "com.propensive" %% "rapture-core" % "2.0.+",
  "com.propensive" %% "rapture-core-scalaz" % "2.0.+"
)

scalacOptions in (Compile, console) ++= Seq(
  "-i", "myrepl.init"
)

tutSettings

tutTargetDirectory := file(".")
