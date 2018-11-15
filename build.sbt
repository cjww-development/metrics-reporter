/*
 * Copyright 2018 CJWW Development
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import com.typesafe.config.ConfigFactory
import scala.util.{Failure, Success, Try}

val libraryName = "metrics-reporter"

val btVersion : String = Try(ConfigFactory.load.getString("version")) match {
  case Success(ver) => ver
  case Failure(_)   => "0.1.0"
}

val dependencies: Seq[ModuleID] = Seq(
  "com.typesafe.play"      % "play_2.12"        % "2.6.20",
  "com.codahale.metrics"   % "metrics-graphite" % "3.0.2",
  "com.kenshoo"           %% "metrics-play"     % "2.6.19_0.7.0"
)

val resolversAll = Seq(
  "scalaz-bintray"      at "http://dl.bintray.com/scalaz/releases",
  "cjww-dev"            at "http://dl.bintray.com/cjww-development/releases",
  "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/",
  "breadfan"            at "https://dl.bintray.com/breadfan/maven/"
)

lazy val library = Project(libraryName, file("."))
  .settings(
    version                               :=  btVersion,
    scalaVersion                          :=  "2.12.7",
    organization                          :=  "com.cjww-dev.libs",
    resolvers                             ++= resolversAll,
    libraryDependencies                   ++= dependencies,
    bintrayOrganization                   :=  Some("cjww-development"),
    bintrayReleaseOnPublish in ThisBuild  :=  true,
    bintrayRepository                     :=  "releases",
    bintrayOmitLicense                    :=  true
  )
