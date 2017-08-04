/*
 * Copyright 2017 Netflix, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.netflix.spinnaker.orca.clouddriver.config

import com.netflix.spinnaker.orca.clouddriver.KatoService
import com.netflix.spinnaker.orca.clouddriver.MortService
import com.netflix.spinnaker.orca.clouddriver.OortService
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll;

class SelectableServiceSpec extends Specification {
  @Shared
  def mortService = Mock(MortService)

  @Shared
  def oortService = Mock(OortService)

  @Shared
  def katoService = Mock(KatoService)

  @Unroll
  def "should lookup service by application or executionType"() {
    given:
    def selectableService = new SelectableService(
      [
        new ByApplicationServiceSelector(mortService, 10, ["applicationPattern": ".*spindemo.*"]),
        new ByExecutionTypeServiceSelector(oortService, 5, ["executionTypes": [0: "orchestration"]]),
        new DefaultServiceSelector(katoService, 1, [:])
      ]
    )

    when:
    def service = selectableService.getService(criteria)

    then:
    service == expectedService

    where:
    criteria                                                        || expectedService
    new SelectableService.Criteria(null, null)                      || katoService      // the default service selector
    new SelectableService.Criteria("spindemo", "orchestration")     || mortService
    new SelectableService.Criteria("1-spindemo-1", "orchestration") || mortService
    new SelectableService.Criteria("spintest", "orchestration")     || oortService
    new SelectableService.Criteria("spintest", "pipeline")          || katoService
  }
}