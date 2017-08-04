/*
 * Copyright 2017 HM Revenue & Customs
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
package metrics

import java.net.InetSocketAddress
import java.util.concurrent.TimeUnit.{MILLISECONDS, SECONDS}
import javax.inject.{Inject, Singleton}

import com.codahale.metrics.graphite.{Graphite, GraphiteReporter => GR}
import com.codahale.metrics.{MetricFilter, SharedMetricRegistries}
import play.api.{Application, Configuration, Logger}

private class MissingMetricsConfigException extends Exception

@Singleton
class GraphiteReporterImpl @Inject()(app: Application, configuration: Configuration) extends GraphiteReporter {
  def microserviceMetricsConfig(implicit app: Application): Option[Configuration] = app.configuration.getConfig(s"metrics")
  private def enabled(app: Application) : Boolean = metricsPluginEnabled(app) && graphitePublisherEnabled(app)
  private def metricsPluginEnabled(app: Application) : Boolean = microserviceMetricsConfig(app).flatMap(_.getBoolean("metrics.enabled")).getOrElse(false)
  private def graphitePublisherEnabled(app: Application) : Boolean = microserviceMetricsConfig(app).flatMap(_.getBoolean("graphite.enabled")).getOrElse(false)
  private def registryName(app: Application) = app.configuration.getString("metrics.name").getOrElse("default")

  private def initialiseGraphiteReporter(app: Application): Unit = {
    Logger.info("Initialising graphite reporting module")

    val metricsConfig = microserviceMetricsConfig(app).getOrElse(throw new MissingMetricsConfigException)

    val graphite = new Graphite(new InetSocketAddress(
      metricsConfig.getString("graphite.host").getOrElse("graphite"),
      metricsConfig.getInt("graphite.port").getOrElse(2003)))

    val prefix = metricsConfig.getString("graphite.prefix").getOrElse(s"tax.${app.configuration.getString("appName")}")

    val reporter = GR.forRegistry(
      SharedMetricRegistries.getOrCreate(registryName(app)))
      .prefixedWith(s"$prefix.${java.net.InetAddress.getLocalHost.getHostName}")
      .convertRatesTo(SECONDS)
      .convertDurationsTo(MILLISECONDS)
      .filter(MetricFilter.ALL)
      .build(graphite)

    reporter.start(metricsConfig.getLong("graphite.interval").getOrElse(10L), SECONDS)
  }

  initialiseGraphiteReporter(app)
}

class DisabledGraphiteReporterImpl extends GraphiteReporter

trait GraphiteReporter
