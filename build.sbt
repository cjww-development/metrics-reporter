import com.typesafe.config.ConfigFactory
import scala.util.{Failure, Success, Try}

val bTVersion : String = Try(ConfigFactory.load.getString("version")) match {
  case Success(ver) => ver
  case Failure(_)   => "0.1.0"
}

name          := "metrics-reporter"
version       := bTVersion
scalaVersion  := "2.11.11"
organization  := "com.cjww-dev.libs"

libraryDependencies ++= Seq(
  "com.typesafe.play"      % "play_2.11"        % "2.5.15",
  "com.codahale.metrics"   % "metrics-graphite" % "3.0.2",
  "de.threedimensions"    %% "metrics-play"     % "2.5.13"
)

resolvers += "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/"
resolvers += "breadfan" at "https://dl.bintray.com/breadfan/maven/"

bintrayOrganization                   := Some("cjww-development")
bintrayReleaseOnPublish in ThisBuild  := true
bintrayRepository                     := "releases"
bintrayOmitLicense                    := true