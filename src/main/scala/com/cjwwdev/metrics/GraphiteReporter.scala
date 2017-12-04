// Copyright (C) 2016-2017 the original author or authors.
// See the LICENCE.txt file distributed with this work for additional
// information regarding copyright ownership.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.cjwwdev.metrics

import java.net.InetSocketAddress
import java.util.concurrent.TimeUnit.{MILLISECONDS, SECONDS}
import javax.inject.{Inject, Singleton}

import com.codahale.metrics.graphite.{Graphite, GraphiteReporter => GR}
import com.codahale.metrics.{MetricFilter, SharedMetricRegistries}
import com.typesafe.config.Config
import play.api.{Application, Configuration, Logger}

private class MissingMetricsConfigException extends Exception

@Singleton
class GraphiteReporterImpl @Inject()(app: Application, configuration: Configuration) extends GraphiteReporter {
  private val metricsConfig: Config    = configuration.underlying.getConfig("metrics")
  private val metricsEnabled: Boolean  = metricsConfig.getBoolean("enabled")
  private val graphiteEnabled: Boolean = metricsConfig.getBoolean("graphite.enabled")
  private val enabled: Boolean         = metricsEnabled && graphiteEnabled

  private val registryName: String     = metricsConfig.getString("name")


  private def initialiseGraphiteReporter(app: Application): Unit = {
    Logger.info("Initialising graphite reporting module")

    val graphite = new Graphite(new InetSocketAddress(metricsConfig.getString("graphite.host"), metricsConfig.getInt("graphite.port")))
    val prefix = metricsConfig.getString("graphite.prefix")

    val reporter = GR.forRegistry(SharedMetricRegistries.getOrCreate(registryName))
      .prefixedWith(s"$prefix.${java.net.InetAddress.getLocalHost.getHostName}")
      .convertRatesTo(SECONDS)
      .convertDurationsTo(MILLISECONDS)
      .filter(MetricFilter.ALL)
      .build(graphite)

    reporter.start(metricsConfig.getLong("graphite.interval"), SECONDS)
  }

  initialiseGraphiteReporter(app)
}

class DisabledGraphiteReporterImpl extends GraphiteReporter

trait GraphiteReporter
