/*
 * Copyright 2016 Google, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License")
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.netflix.spinnaker.fiat.providers

import com.netflix.spinnaker.fiat.model.resources.Account
import com.netflix.spinnaker.fiat.model.resources.Role
import com.netflix.spinnaker.fiat.providers.internal.ClouddriverService
import spock.lang.Specification
import spock.lang.Subject
import spock.lang.Unroll

class DefaultAccountProviderSpec extends Specification {

  @Unroll
  def "should get all accounts based on supplied roles"() {
    setup:
    ClouddriverService clouddriverService = Mock(ClouddriverService) {
      getAccounts() >> [
          new Account().setName("noReqGroups"),
          new Account().setName("reqGroup1").setRequiredGroupMembership(["group1"]),
          new Account().setName("reqGroup1and2").setRequiredGroupMembership(["group1", "group2"]),
      ]
    }
    @Subject
    DefaultAccountProvider accountProvider = new DefaultAccountProvider(
        clouddriverService: clouddriverService
    )

    when:
    def result = accountProvider.getAllRestricted(input.collect {new Role(it)})

    then:
    result*.name.containsAll(values)

    when:
    accountProvider.getAllRestricted(null)

    then:
    thrown IllegalArgumentException

    where:
    input                || values
    []                   || []
    ["group1"]           || ["reqGroup1", "reqGroup1and2"]
    ["group2"]           || ["reqGroup1and2"]
    ["group1", "group2"] || ["reqGroup1", "reqGroup1and2"]
    ["group3"]           || []
    ["group2", "group3"] || ["reqGroup1and2"]
  }
}
