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

package com.cjwwdev.metrics

import java.net.InetSocketAddress
import java.util.concurrent.TimeUnit.{MILLISECONDS, SECONDS}

import com.codahale.metrics.graphite.{Graphite, GraphiteReporter => GR}
import com.codahale.metrics.{MetricFilter, SharedMetricRegistries}
import com.typesafe.config.Config
import javax.inject.Inject
import org.slf4j.{Logger, LoggerFactory}
import play.api.Configuration
import play.api.inject.ApplicationLifecycle

import scala.concurrent.Future

class GraphiteReporterImpl @Inject()(lifecycle: ApplicationLifecycle, configuration: Configuration) extends GraphiteReporter {
  private val metricsConfig: Config     = configuration.underlying.getConfig("metrics")
  override lazy val metricsEnabled: Boolean  = metricsConfig.getBoolean("enabled")
  override lazy val graphiteEnabled: Boolean = metricsConfig.getBoolean("graphite.enabled")
  override lazy val registryName: String     = metricsConfig.getString("name")
  override lazy val graphiteHost: String     = metricsConfig.getString("graphite.host")
  override lazy val graphitePort: Int        = metricsConfig.getInt("graphite.port")
  override lazy val prefix: String           = metricsConfig.getString("graphite.prefix")
  override lazy val interval: Long           = metricsConfig.getLong("graphite.interval")

  bootGraphiteReporter()

  lifecycle.addStopHook {
    () => Future.successful(teardownGraphiteReporter())
  }
}

class DisabledGraphiteReporterImpl extends GraphiteReporter {
  override lazy val metricsEnabled: Boolean  = false
  override lazy val graphiteEnabled: Boolean = false
  override lazy val registryName: String     = "disabled"
  override lazy val graphiteHost: String     = "disabled"
  override lazy val graphitePort: Int        = 1024
  override lazy val prefix: String           = "disabled"
  override lazy val interval: Long           = 1L
}

trait GraphiteReporter {
  val metricsEnabled: Boolean
  val graphiteEnabled: Boolean

  val registryName: String

  val graphiteHost: String
  val graphitePort: Int

  val prefix: String

  val interval: Long

  private lazy val logger: Logger = LoggerFactory.getLogger(getClass)

  private lazy val enabled: Boolean = metricsEnabled && graphiteEnabled

  private lazy  val graphite = new Graphite(new InetSocketAddress(graphiteHost, graphitePort))

  private lazy val reporter = GR
    .forRegistry(SharedMetricRegistries.getOrCreate(registryName))
    .prefixedWith(s"$prefix.${java.net.InetAddress.getLocalHost.getHostName}")
    .convertRatesTo(SECONDS)
    .convertDurationsTo(MILLISECONDS)
    .filter(MetricFilter.ALL)
    .build(graphite)

  def bootGraphiteReporter(): Unit = {
    logger.info("Initialising graphite reporting module")
    reporter.start(interval, SECONDS)
  }

  def teardownGraphiteReporter(): Unit = {
    reporter.stop()
  }
}
