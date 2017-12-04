import com.typesafe.config.ConfigFactory
import scala.util.{Failure, Success, Try}

val libraryName = "metrics-reporter"

val bTVersion : String = Try(ConfigFactory.load.getString("version")) match {
  case Success(ver) => ver
  case Failure(_)   => "0.1.0"
}

val dependencies: Seq[ModuleID] = Seq(
  "com.typesafe.play"      % "play_2.11"        % "2.5.16",
  "com.codahale.metrics"   % "metrics-graphite" % "3.0.2",
  "de.threedimensions"    %% "metrics-play"     % "2.5.13"
)

lazy val library = Project(libraryName, file("."))
  .settings(
    version                               :=  bTVersion,
    scalaVersion                          :=  "2.11.12",
    organization                          :=  "com.cjww-dev.libs",
    resolvers                             +=  "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/",
    resolvers                             +=  "breadfan" at "https://dl.bintray.com/breadfan/maven/",
    libraryDependencies                   ++= dependencies,
    bintrayOrganization                   :=  Some("cjww-development"),
    bintrayReleaseOnPublish in ThisBuild  :=  true,
    bintrayRepository                     :=  "releases",
    bintrayOmitLicense                    :=  true
  )
