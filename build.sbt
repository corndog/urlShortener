name := "urlShortener"

version := "1.0"

scalaVersion := "2.10.1"

resolvers ++= Seq(
	"Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/",
	"Sonatype OSS Releases" at "http://oss.sonatype.org/content/repositories/releases/",
	"spray repo" at "http://repo.spray.io"
	)
 
libraryDependencies ++= Seq(
	"com.typesafe.akka" %% "akka-actor" % "2.1.4",
	"io.spray" % "spray-can" % "1.1-M8",
	"io.spray" % "spray-routing" % "1.1-M8",
	"com.typesafe.slick" %% "slick" % "1.0.1",
	"org.slf4j" % "slf4j-nop" % "1.6.4",
	"org.postgresql" % "postgresql" % "9.2-1003-jdbc4"
  )