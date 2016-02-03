import sbt.Keys._

val scalaV = "2.11.7"
val catsV = "0.4.0"
val scalazV = "7.2.0"
val raptureV = "2.0.+"

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
  "org.scalaz" %% "scalaz-concurrent" % scalazV,
  "org.typelevel" %% "cats" % catsV,
  "org.scala-lang" % "scala-compiler" % scalaV,
  "com.propensive" %% "rapture-core" % raptureV,
  "com.propensive" %% "rapture-core-scalaz" % raptureV
)

scalacOptions in (Compile, console) ++= Seq(
  "-i", "myrepl.init"
)

tutSettings

tutTargetDirectory := file(".")
