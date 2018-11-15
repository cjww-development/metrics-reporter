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

import javax.inject.{Inject, Singleton}

import com.kenshoo.play.metrics._
import play.api.inject.{ApplicationLifecycle, Module}
import play.api.{Configuration, Environment}

import scala.util.control.NoStackTrace

class MetricsModule extends Module  {
  def bindings(environment: Environment, configuration: Configuration) = {
    if (configuration.getBoolean("metrics.enabled").getOrElse(true)) {
      Seq(
        bind[MetricsFilter].to[MetricsFilterImpl].eagerly,
        bind[Metrics].to[GraphiteMetricsImpl].eagerly,
        bind[GraphiteReporter].to[GraphiteReporterImpl].eagerly()
      )
    } else {
      Seq(
        bind[MetricsFilter].to[DisabledMetricsFilter].eagerly,
        bind[Metrics].to[CustomDisabledMetrics].eagerly,
        bind[GraphiteReporter].to[DisabledGraphiteReporterImpl].eagerly
      )
    }
  }
}

@Singleton
class GraphiteMetricsImpl @Inject()(lifecycle: ApplicationLifecycle, configuration: Configuration) extends MetricsImpl(lifecycle, configuration) {
  override def onStop() = {}
}

class CustomDisabledMetrics extends Metrics {
  override def defaultRegistry  = throw new DisabledMetricsException
  override def toJson           = throw new DisabledMetricsException
}

private class DisabledMetricsException extends NoStackTrace

