/*
 *
 *  * Licensed to the Apache Software Foundation (ASF) under one or more
 *  * contributor license agreements.  See the NOTICE file distributed with
 *  * this work for additional information regarding copyright ownership.
 *  * The ASF licenses this file to You under the Apache License, Version 2.0
 *  * (the "License"); you may not use this file except in compliance with
 *  * the License.  You may obtain a copy of the License at
 *  *
 *  *     http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */

package org.jclouds.softlayer.features;

import com.google.common.collect.ImmutableList;
import com.google.common.reflect.Invokable;
import org.jclouds.Fallbacks;
import org.jclouds.http.functions.ParseJson;
import org.jclouds.rest.internal.GeneratedHttpRequest;
import org.jclouds.softlayer.domain.HardwareProductOrder;
import org.testng.annotations.Test;

import java.io.IOException;

import static org.jclouds.reflect.Reflection2.method;

/**
 * Tests annotation parsing of {@code HardwareServerAsyncClient}
 *
 * @author Eli Polonsky
 */
@Test(groups = "unit")
public class HardwareServerAsyncClientTest extends BaseSoftLayerBareMetalInstancesAsyncClientTest<HardwareServerAsyncClient> {


   public void testGetHardwareServer() throws SecurityException, NoSuchMethodException, IOException {
      Invokable<?, ?> method = method(HardwareServerAsyncClient.class, "getHardwareServer", long.class);
      GeneratedHttpRequest httpRequest = processor.createRequest(method, ImmutableList.<Object> of(1234));

      assertRequestLineEquals(
              httpRequest,
              "GET https://api.softlayer.com/rest/v3/SoftLayer_Hardware_Server/1234.json?objectMask=hardwareStatus%3BoperatingSystem.passwords%3Bdatacenter%3BbillingItem HTTP/1.1");
      assertNonPayloadHeadersEqual(httpRequest, "Accept: application/json\n");
      assertPayloadEquals(httpRequest, null, null, false);

      assertResponseParserClassEquals(method, httpRequest, ParseJson.class);
      assertSaxResponseParserClassEquals(method, null);
      assertFallbackClassEquals(method, Fallbacks.NullOnNotFoundOr404.class);

      checkFilters(httpRequest);

   }

   @Test(enabled = false)
   public void testCancelService() throws SecurityException, NoSuchMethodException, IOException {

      Invokable<?, ?> method = method(HardwareServerAsyncClient.class, "cancelService", long.class);
      GeneratedHttpRequest httpRequest = processor.createRequest(method, ImmutableList.<Object> of(1234));

      assertRequestLineEquals(
              httpRequest,
              "GET https://api.softlayer.com/rest/v3/SoftLayer_Billing_Item/1234/cancelService.json HTTP/1.1");
      assertNonPayloadHeadersEqual(httpRequest, "Accept: application/json\n");
      assertPayloadEquals(httpRequest, null, null, false);

      assertResponseParserClassEquals(method, httpRequest, ParseJson.class);
      assertSaxResponseParserClassEquals(method, null);
      assertFallbackClassEquals(method, Fallbacks.FalseOnNotFoundOr404.class);

      checkFilters(httpRequest);

   }

   public void testOrderHardwareServer() throws SecurityException, NoSuchMethodException, IOException {

      Invokable<?, ?> method = method(HardwareServerAsyncClient.class, "orderHardwareServer", HardwareProductOrder.class);

      HardwareProductOrder hardwareProductOrder = HardwareProductOrder.builder().location("").build();

      GeneratedHttpRequest httpRequest = processor.createRequest(method, ImmutableList.<Object> of(hardwareProductOrder));

      assertRequestLineEquals(
              httpRequest,
              "POST https://api.softlayer.com/rest/v3/SoftLayer_Product_Order/placeOrder.json HTTP/1.1");
      assertNonPayloadHeadersEqual(httpRequest, "Accept: application/json\n");

      assertResponseParserClassEquals(method, httpRequest, ParseJson.class);
      assertSaxResponseParserClassEquals(method, null);
      assertFallbackClassEquals(method, Fallbacks.NullOnNotFoundOr404.class);

      checkFilters(httpRequest);
   }



}
