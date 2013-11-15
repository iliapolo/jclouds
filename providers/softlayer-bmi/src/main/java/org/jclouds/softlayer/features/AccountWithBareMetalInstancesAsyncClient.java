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
package org.jclouds.softlayer.features;

import com.google.common.util.concurrent.ListenableFuture;
import org.jclouds.Fallbacks;
import org.jclouds.http.filters.BasicAuthentication;
import org.jclouds.rest.annotations.Fallback;
import org.jclouds.rest.annotations.QueryParams;
import org.jclouds.rest.annotations.RequestFilters;
import org.jclouds.softlayer.domain.HardwareServer;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import java.util.Set;

/**
 * Provides asynchronous access to Account via their REST API.
 * <p/>
 *
 * @see AccountWithBareMetalInstancesClient
 * @see <a href="http://sldn.softlayer.com/article/REST" />
 * @author Jason King
 * @deprecated Async interfaces will be removed in 1.7.0.
 */
@Deprecated
@RequestFilters(BasicAuthentication.class)
@Path("/v{jclouds.api-version}")
public interface AccountWithBareMetalInstancesAsyncClient extends AccountAsyncClient {

   public static String LIST_HARDWARE_MASK = "hardware.hardwareStatus;hardware.operatingSystem.passwords;hardware.datacenter;hardware.billingItem";

   /**
    * @see AccountWithBareMetalInstancesClient#listVirtualGuests
    */
   @GET
   @Path("/SoftLayer_Account/getHardware")
   @QueryParams(keys = "objectMask", values = LIST_HARDWARE_MASK)
   @Consumes(MediaType.APPLICATION_JSON)
   @Fallback(Fallbacks.EmptySetOnNotFoundOr404.class)
   ListenableFuture<Set<HardwareServer>> listHardwareServers();

}
