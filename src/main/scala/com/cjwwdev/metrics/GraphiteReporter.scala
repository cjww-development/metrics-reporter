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
  override val metricsEnabled: Boolean  = metricsConfig.getBoolean("enabled")
  override val graphiteEnabled: Boolean = metricsConfig.getBoolean("graphite.enabled")
  override val registryName: String     = metricsConfig.getString("name")
  override val graphiteHost: String     = metricsConfig.getString("graphite.host")
  override val graphitePort: Int        = metricsConfig.getInt("graphite.port")
  override val prefix: String           = metricsConfig.getString("graphite.prefix")
  override val interval: Long           = metricsConfig.getLong("graphite.interval")

  bootGraphiteReporter()

  lifecycle.addStopHook {
    () => Future.successful(teardownGraphiteReporter())
  }
}

class DisabledGraphiteReporterImpl extends GraphiteReporter {
  override val metricsEnabled: Boolean  = false
  override val graphiteEnabled: Boolean = false
  override val registryName: String     = "disabled"
  override val graphiteHost: String     = "disabled"
  override val graphitePort: Int        = 1024
  override val prefix: String           = "disabled"
  override val interval: Long           = 1L
}

trait GraphiteReporter {
  val metricsEnabled: Boolean
  val graphiteEnabled: Boolean

  val registryName: String

  val graphiteHost: String
  val graphitePort: Int

  val prefix: String

  val interval: Long

  private val logger: Logger = LoggerFactory.getLogger(getClass)

  private val enabled: Boolean = metricsEnabled && graphiteEnabled

  private val graphite = new Graphite(new InetSocketAddress(graphiteHost, graphitePort))

  private val reporter = GR
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
