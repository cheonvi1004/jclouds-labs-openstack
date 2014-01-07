/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jclouds.openstack.swift.v1.internal;

import static com.google.common.base.Preconditions.checkState;

import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.jclouds.apis.BaseApiLiveTest;
import org.jclouds.location.reference.LocationConstants;
import org.jclouds.openstack.keystone.v2_0.config.KeystoneProperties;
import org.jclouds.openstack.swift.v1.SwiftApi;
import org.jclouds.openstack.swift.v1.domain.BulkDeleteResponse;
import org.jclouds.openstack.swift.v1.domain.ObjectList;
import org.jclouds.openstack.swift.v1.domain.SwiftObject;
import org.jclouds.openstack.swift.v1.options.ListContainerOptions;
import org.testng.annotations.BeforeClass;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;

public class BaseSwiftApiLiveTest extends BaseApiLiveTest<SwiftApi> {

   protected Set<String> regions;
   
   public BaseSwiftApiLiveTest() {
      provider = "openstack-swift";
   }
   
   @Override
   @BeforeClass(groups = "live")
   public void setup() {
      super.setup();
      String providedRegion = System.getProperty("test." + LocationConstants.PROPERTY_REGION);
      if (providedRegion != null) {
        regions = ImmutableSet.of(providedRegion);
      } else {
        regions = api.configuredRegions();
      }
   }
   
   @Override
   protected Properties setupProperties() {
      Properties props = super.setupProperties();
      setIfTestSystemPropertyPresent(props, KeystoneProperties.CREDENTIAL_TYPE);
      setIfTestSystemPropertyPresent(props, LocationConstants.PROPERTY_REGION);
      return props;
   }

   protected void deleteAllObjectsInContainer(String regionId, final String containerName) {
      ObjectList objects = api.objectApiInRegionForContainer(regionId, containerName).list(new ListContainerOptions());
      if (objects == null) {
         return;
      }
      List<String> pathsToDelete = Lists.transform(objects, new Function<SwiftObject, String>() {
         public String apply(SwiftObject input) {
            return containerName + "/" + input.name();
         }
      });
      if (!pathsToDelete.isEmpty()) {
         BulkDeleteResponse response = api.bulkApiInRegion(regionId).bulkDelete(pathsToDelete);
         checkState(response.errors().isEmpty(), "Errors deleting paths %s: %s", pathsToDelete, response);
      }
   }
}
