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

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSet.Builder;
import com.google.common.collect.Iterables;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Module;
import com.google.inject.TypeLiteral;
import org.jclouds.compute.domain.Template;
import org.jclouds.compute.domain.TemplateBuilder;
import org.jclouds.softlayer.SoftLayerClient;
import org.jclouds.softlayer.domain.*;
import org.testng.annotations.Test;

import java.util.Properties;
import java.util.Random;

import static com.google.common.base.Predicates.and;
import static com.google.common.collect.Iterables.find;
import static com.google.common.collect.Iterables.get;
import static org.jclouds.softlayer.predicates.ProductItemPredicates.capacity;
import static org.jclouds.softlayer.predicates.ProductItemPredicates.categoryCode;
import static org.jclouds.softlayer.predicates.ProductPackagePredicates.named;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

/**
 * Tests behavior of {@code VirtualGuestClient}
 *
 * @author Adrian Cole
 */
@Test(groups = "live")
public class VirtualGuestClientLiveTest extends BaseSoftLayerClientLiveTest {

   private static final String TEST_HOSTNAME_PREFIX = "livetest";
   private TemplateBuilder templateBuilder;

   @Test(groups = "live")
   public void testCancelAndPlaceOrder() {
      // TODO: Should also check if there are active transactions before trying to cancel.
      // objectMask: virtualGuests.activeTransaction
      for (VirtualGuest guest : accountApi().listVirtualGuests()) {
         if (guest.getHostname().startsWith(TEST_HOSTNAME_PREFIX)) {
            if (guest.getBillingItemId() != -1) {
               api().cancelService(guest.getBillingItemId());
            }
         }
      }

      int pkgId = Iterables.find(api.getAccountClient().getActivePackages(),
               named(ProductPackageClientLiveTest.CLOUD_SERVER_PACKAGE_NAME)).getId();
      ProductPackage productPackage = api.getProductPackageClient().getProductPackage(pkgId);

      VirtualGuest guest = VirtualGuest.builder().domain("jclouds.org").hostname(
               TEST_HOSTNAME_PREFIX + new Random().nextInt()).build();

      Template template = templateBuilder.build();

      ProductOrder order = ProductOrder.builder()
              .packageId(productPackage.getId())
              .quantity(1)
              .location(template.getLocation().getId())
              .useHourlyPricing(true)
              .prices(getPrices(template, productPackage))
              .virtualGuests(guest).build();

      ProductOrderReceipt receipt = api().orderVirtualGuest(order);
      ProductOrder order2 = receipt.getOrderDetails();
      assertEquals(order.getPrices(), order2.getPrices());
      assertNotNull(receipt);
   }

   private Iterable<ProductItemPrice> defaultPrices;

   @Override
   protected SoftLayerClient create(Properties props, Iterable<Module> modules) {
      Injector injector = newBuilder().modules(modules).overrides(props).buildInjector();
      templateBuilder = injector.getInstance(TemplateBuilder.class);
      defaultPrices = injector.getInstance(Key.get(new TypeLiteral<Iterable<ProductItemPrice>>() {
      }));
      return injector.getInstance(SoftLayerClient.class);
   }

   private VirtualGuestClient api() {
      return api.getVirtualGuestClient();
   }

   private AccountClient accountApi() {
      return api.getAccountClient();
   }

   private Iterable<ProductItemPrice> getPrices(Template template, ProductPackage productPackage) {
      Builder<ProductItemPrice> result = ImmutableSet.builder();

      int imageId = Integer.parseInt(template.getImage().getId());
      result.add(ProductItemPrice.builder().id(imageId).build());

      Iterable<String> hardwareIds = Splitter.on(",").split(template.getHardware().getId());
      for (String hardwareId : hardwareIds) {
         int id = Integer.parseInt(hardwareId);
         result.add(ProductItemPrice.builder().id(id).build());
      }
      float portSpeed = 10f;
      ProductItem uplinkItem = find(productPackage.getItems(),
              and(capacity(portSpeed), categoryCode("port_speed")));
      result.add(get(uplinkItem.getPrices(), 0));
      result.addAll(defaultPrices);
      return result.build();
   }
}
