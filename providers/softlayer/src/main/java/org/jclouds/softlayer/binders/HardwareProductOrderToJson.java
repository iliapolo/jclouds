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
package org.jclouds.softlayer.binders;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import org.jclouds.http.HttpRequest;
import org.jclouds.json.Json;
import org.jclouds.rest.Binder;
import org.jclouds.softlayer.domain.HardwareProductOrder;
import org.jclouds.softlayer.domain.HardwareServer;
import org.jclouds.softlayer.domain.ProductItemPrice;

import javax.inject.Inject;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Converts a ProductOrder into a json string valid for placing an order via the softlayer api The
 * String is set into the payload of the HttpRequest
 * 
 * @author Jason King
 */
public class HardwareProductOrderToJson implements Binder {

   private Json json;

   @Inject
   public HardwareProductOrderToJson(Json json) {
      this.json = json;
   }

   @Override
   public <R extends HttpRequest> R bindToRequest(R request, Object input) {
      checkNotNull(input, "order");
      HardwareProductOrder order = HardwareProductOrder.class.cast(input);
      request.setPayload(buildJson(order));
      return request;
   }

   /**
    * Builds a Json string suitable for sending to the softlayer api
    * 
    * @param order
    * @return
    */
   String buildJson(HardwareProductOrder order) {

      Iterable<Price> prices = Iterables.transform(order.getPrices(), new Function<ProductItemPrice, Price>() {
         @Override
         public Price apply(ProductItemPrice productItemPrice) {
            return new Price(productItemPrice.getId());
         }
      });

      Iterable<HostnameAndDomain> hosts = Iterables.transform(order.getHardware(),
               new Function<HardwareServer, HostnameAndDomain>() {
                  @Override
                  public HostnameAndDomain apply(HardwareServer virtualGuest) {
                     return new HostnameAndDomain(virtualGuest.getHostname(), virtualGuest.getDomain());
                  }
               });

      OrderData data = new OrderData(order.getPackageId(), order.getLocation(), Sets.newLinkedHashSet(prices), Sets
               .newLinkedHashSet(hosts), order.getQuantity(), order.getUseHourlyPricing());

      return json.toJson(ImmutableMap.of("parameters", ImmutableList.<OrderData> of(data)));
   }

   @SuppressWarnings("unused")
   private static class OrderData {
      private String complexType = "SoftLayer_Container_Product_Order";
      private long packageId = -1;
      private String location;
      private Set<Price> prices;
      private Set<HostnameAndDomain> hardware;
      private long quantity;
      private boolean useHourlyPricing;

      public OrderData(long packageId, String location, Set<Price> prices, Set<HostnameAndDomain> hardware,
               long quantity, boolean useHourlyPricing) {
         this.packageId = packageId;
         this.location = location;
         this.prices = prices;
         this.hardware = hardware;
         this.quantity = quantity;
         this.useHourlyPricing = useHourlyPricing;
      }

   }

   @SuppressWarnings("unused")
   private static class HostnameAndDomain {
      private String hostname;
      private String domain;

      public HostnameAndDomain(String hostname, String domain) {
         this.hostname = hostname;
         this.domain = domain;
      }

   }

   @SuppressWarnings("unused")
   private static class Price {
      private long id;

      public Price(long id) {
         this.id = id;
      }
   }

}
