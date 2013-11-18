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
import org.jclouds.rest.annotations.BinderParam;
import org.jclouds.rest.annotations.Fallback;
import org.jclouds.rest.annotations.QueryParams;
import org.jclouds.rest.annotations.RequestFilters;
import org.jclouds.softlayer.binders.HardwareProductOrderToJson;
import org.jclouds.softlayer.domain.HardwareProductOrder;
import org.jclouds.softlayer.domain.HardwareProductOrderReceipt;
import org.jclouds.softlayer.domain.HardwareServer;
import org.jclouds.softlayer.domain.Transaction;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

/**
 * Provides asynchronous access to HardwareServer via their REST API.
 * <p/>
 * 
 * @see org.jclouds.softlayer.features.HardwareServerClient
 * @see <a href="http://sldn.softlayer.com/article/REST" />
 * @author Eli Polonsky
 * @deprecated Async interfaces will be removed in 1.7.0
 */
@Deprecated
@RequestFilters(BasicAuthentication.class)
@Path("/v{jclouds.api-version}")
public interface HardwareServerAsyncClient {

   public static String SERVER_MASK = "hardwareStatus;operatingSystem.passwords;datacenter;billingItem";


   /**
    * @see HardwareServerClient#getHardwareServer
    */
   @GET
   @Path("/SoftLayer_Hardware_Server/{id}.json")
   @QueryParams(keys = "objectMask", values = SERVER_MASK)
   @Consumes(MediaType.APPLICATION_JSON)
   @Fallback(Fallbacks.NullOnNotFoundOr404.class)
   ListenableFuture<HardwareServer> getHardwareServer(@PathParam("id") long id);

   /**
    * @see HardwareServerClient#cancelService
    */
   @GET
   @Path("/SoftLayer_Billing_Item/{id}/cancelService.json")
   @Consumes(MediaType.APPLICATION_JSON)
   @Fallback(Fallbacks.FalseOnNotFoundOr404.class)
   ListenableFuture<Boolean> cancelService(@PathParam("id") long id);

   /**
    * @see org.jclouds.softlayer.features.HardwareServerClient#orderHardwareServer
    */
   @POST
   @Path("/SoftLayer_Product_Order/placeOrder.json")
   @Consumes(MediaType.APPLICATION_JSON)
   @Fallback(Fallbacks.NullOnNotFoundOr404.class)
   ListenableFuture<HardwareProductOrderReceipt> orderHardwareServer(@BinderParam(HardwareProductOrderToJson.class)HardwareProductOrder order);

   /**
    * @see org.jclouds.softlayer.features.HardwareServerClient#getActiveTransaction
    */
   @GET
   @Path("/SoftLayer_Hardware_Server/{id}/getActiveTransaction.json")
   @Consumes(MediaType.APPLICATION_JSON)
   @Fallback(Fallbacks.NullOnNotFoundOr404.class)
   ListenableFuture<Transaction> getActiveTransaction(@PathParam("id") long id);
}
