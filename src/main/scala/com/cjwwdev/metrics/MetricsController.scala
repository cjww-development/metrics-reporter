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
package com.cjwwdev.metrics

import javax.inject.{Inject, Singleton}

import com.kenshoo.play.metrics.{Metrics, MetricsDisabledException}
import play.api.mvc.{Action, Controller}

@Singleton
class MetricsController @Inject()(metrics: Metrics) extends Controller {
  def metricsToJson = Action {
    try {
      Ok(metrics.toJson).as("application/json").withHeaders("Cache-Control" -> "must-revalidate,no-cache,no-store")
    } catch {
      case _: MetricsDisabledException => InternalServerError("metrics plugin not enabled")
    }
  }
}
