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
package org.jclouds.softlayer.compute.strategy;

import com.google.common.base.Predicate;
import com.google.common.base.Splitter;
import com.google.common.base.Supplier;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSet.Builder;
import com.google.common.collect.Maps;
import org.jclouds.collect.Memoized;
import org.jclouds.compute.ComputeServiceAdapter;
import org.jclouds.compute.domain.Template;
import org.jclouds.compute.reference.ComputeServiceConstants;
import org.jclouds.domain.LoginCredentials;
import org.jclouds.javax.annotation.Nullable;
import org.jclouds.logging.Logger;
import org.jclouds.softlayer.SoftLayerBareMetalInstancesClient;
import org.jclouds.softlayer.compute.functions.ProductItemToImage;
import org.jclouds.softlayer.compute.options.SoftLayerTemplateOptions;
import org.jclouds.softlayer.domain.*;
import org.jclouds.softlayer.reference.HardwareSoftLayerConstants;

import javax.annotation.Resource;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static com.google.common.base.Preconditions.*;
import static com.google.common.base.Predicates.and;
import static com.google.common.collect.Iterables.*;
import static org.jclouds.softlayer.predicates.ProductItemPredicates.capacity;
import static org.jclouds.softlayer.predicates.ProductItemPredicates.categoryCode;
import static org.jclouds.softlayer.reference.HardwareSoftLayerConstants.PROPERTY_SOFTLAYER_BARE_METAL_INSTANCES_LOGIN_DETAILS_DELAY;
import static org.jclouds.softlayer.reference.HardwareSoftLayerConstants.PROPERTY_SOFTLAYER_BARE_METAL_INSTANCES_PORT_SPEED;
import static org.jclouds.softlayer.reference.HardwareSoftLayerConstants.PROPERTY_SOFTLAYER_BARE_METAL_INSTANCES_ACTIVE_TRANSACTIONS_DELAY;
import static org.jclouds.util.Predicates2.retry;

/**
 * defines the connection between the {@link org.jclouds.softlayer.SoftLayerClient} implementation and
 * the jclouds {@link org.jclouds.compute.ComputeService}
 * 
 */
@Singleton
public class SoftLayerBareMetalInstancesComputeServiceAdapter implements
      ComputeServiceAdapter<HardwareServer, Iterable<ProductItem>, ProductItem, Datacenter> {

   @Resource
   @Named(ComputeServiceConstants.COMPUTE_LOGGER)
   protected Logger logger = Logger.NULL;

   private final SoftLayerBareMetalInstancesClient client;
   private final Supplier<ProductPackage> productPackageSupplier;
   private final Predicate<HardwareServer> loginDetailsTester;
   private final Predicate<HardwareServer> activeTransactionsTester;
   private final long serverLoginDelay;
   private final float portSpeed;
   private final Iterable<ProductItemPrice> prices;

   @Inject
   public SoftLayerBareMetalInstancesComputeServiceAdapter(SoftLayerBareMetalInstancesClient client,
                                                           HardwareServerHasLoginDetailsPresent serverHasLoginDetailsPresent,
                                                           HardwareServerHasNoRunningTransactions activeTransactionsTester,
                                                           @Memoized Supplier<ProductPackage> productPackageSupplier,
                                                           Iterable<ProductItemPrice> prices,
                                                           @Named(PROPERTY_SOFTLAYER_BARE_METAL_INSTANCES_PORT_SPEED) float portSpeed,
                                                           @Named(PROPERTY_SOFTLAYER_BARE_METAL_INSTANCES_LOGIN_DETAILS_DELAY) long serverLoginDelay,
                                                           @Named(PROPERTY_SOFTLAYER_BARE_METAL_INSTANCES_ACTIVE_TRANSACTIONS_DELAY) long activeTransactionsDelay) {
      this.client = checkNotNull(client, "client");
      this.serverLoginDelay = serverLoginDelay;
      this.productPackageSupplier = checkNotNull(productPackageSupplier, "productPackageSupplier");
      checkArgument(serverLoginDelay > 500, "guestOrderDelay must be in milliseconds and greater than 500");
      this.loginDetailsTester = retry(serverHasLoginDetailsPresent, serverLoginDelay);
      this.prices = checkNotNull(prices, "prices");
      this.portSpeed = portSpeed;
      checkArgument(portSpeed > 0, "portSpeed must be greater than zero, often 10, 100, 1000, 10000");
      this.activeTransactionsTester = retry(serverHasLoginDetailsPresent, serverLoginDelay);
   }

   @Override
   public NodeAndInitialCredentials<HardwareServer> createNodeWithGroupEncodedIntoName(String group, String name,
         Template template) {
      checkNotNull(template, "template was null");
      checkNotNull(template.getOptions(), "template options was null");
      checkArgument(template.getOptions().getClass().isAssignableFrom(SoftLayerTemplateOptions.class),
            "options class %s should have been assignable from SoftLayerTemplateOptions", template.getOptions()
                  .getClass());

      String domainName = template.getOptions().as(SoftLayerTemplateOptions.class).getDomainName();

      HardwareServer newServer = HardwareServer.builder().domain(domainName).hostname(name).build();

      HardwareProductOrder order = HardwareProductOrder.builder().packageId(productPackageSupplier.get().getId())
            .location(template.getLocation().getId()).quantity(1).useHourlyPricing(true).prices(getPrices(template))
            .hardware(newServer).build();

      logger.debug(">> ordering new hardwareServer domain(%s) hostname(%s)", domainName, name);
      HardwareProductOrderReceipt hardwareProductOrderReceipt = client.getHardwareServerClient().orderHardwareServer(order);
      HardwareServer result = get(hardwareProductOrderReceipt.getOrderDetails().getHardware(), 0);
      logger.trace("<< virtualGuest(%s)", result.getId());

      logger.debug(">> awaiting login details for hardwareServer(%s)", result.getId());
      boolean orderInSystem = loginDetailsTester.apply(result);
      logger.trace("<< virtualGuest(%s) complete(%s)", result.getId(), orderInSystem);

      checkState(orderInSystem, "order for server %s doesn't have login details within %sms", result,
            Long.toString(serverLoginDelay));
      result = client.getHardwareServerClient().getHardwareServer(result.getId());

      Password pw = get(result.getOperatingSystem().getPasswords(), 0);
      return new NodeAndInitialCredentials<HardwareServer>(result, result.getId() + "", LoginCredentials.builder()
              .user(pw.getUsername())
              .privateKey(pw.getPassword())
              .build());
   }

   private Iterable<ProductItemPrice> getPrices(Template template) {
      Builder<ProductItemPrice> result = ImmutableSet.builder();

      int imageId = Integer.parseInt(template.getImage().getId());
      result.add(ProductItemPrice.builder().id(imageId).build());

      Iterable<String> hardwareIds = Splitter.on(",").split(template.getHardware().getId());
      for (String hardwareId : hardwareIds) {
         int id = Integer.parseInt(hardwareId);
         result.add(ProductItemPrice.builder().id(id).build());
      }
      ProductItem uplinkItem = find(productPackageSupplier.get().getItems(),
            and(capacity(portSpeed), categoryCode("port_speed")));
      result.add(get(uplinkItem.getPrices(), 0));
      result.addAll(prices);
      return result.build();
   }

   @Override
   public Iterable<Iterable<ProductItem>> listHardwareProfiles() {
      ProductPackage productPackage = productPackageSupplier.get();
      Set<ProductItem> items = productPackage.getItems();
      Builder<Iterable<ProductItem>> result = ImmutableSet.builder();
      for (ProductItem cpuAndRamItem : filter(items, categoryCode("server_core"))) {
         for (ProductItem firsDiskItem : filter(items, categoryCode("disk0"))) {
            result.add(ImmutableSet.of(cpuAndRamItem, firsDiskItem));
         }
      }
      return result.build();
   }

   @Override
   public Iterable<ProductItem> listImages() {
      return filter(productPackageSupplier.get().getItems(), categoryCode("os"));
   }
   
   // cheat until we have a getProductItem command
   @Override
   public ProductItem getImage(final String id) {
      return find(listImages(), new Predicate<ProductItem>(){

         @Override
         public boolean apply(ProductItem input) {
            return ProductItemToImage.imageId().apply(input).equals(id);
         }
         
      }, null);
   }
   
   @Override
   public Iterable<HardwareServer> listNodes() {
      return filter(client.getAccountWithBareMetalInstancesClient().listHardwareServers(), new Predicate<HardwareServer>() {

         @Override
         public boolean apply(HardwareServer arg0) {
            boolean hasBillingItem = arg0.getBillingItemId() != -1;
            if (hasBillingItem)
               return true;
            logger.trace("guest invalid, as it has no billing item %s", arg0);
            return false;
         }

      });
   }

   @Override
   public Iterable<HardwareServer> listNodesByIds(final Iterable<String> ids) {
      return filter(listNodes(), new Predicate<HardwareServer>() {

         @Override
         public boolean apply(HardwareServer server) {
            return contains(ids, server.getId());
         }
      });
   }

   @Override
   public Iterable<Datacenter> listLocations() {
      return productPackageSupplier.get().getDatacenters();
   }

   @Override
   public HardwareServer getNode(String id) {
      long serverId = Long.parseLong(id);
      return client.getHardwareServerClient().getHardwareServer(serverId);
   }

   @Override
   public void destroyNode(String id) {
      HardwareServer guest = getNode(id);
      if (guest == null)
         return;

      if (guest.getBillingItemId() == -1)
         throw new IllegalStateException(String.format("no billing item for guest(%s) so we cannot cancel the order",
               id));

      logger.debug(">> canceling service for guest(%s) billingItem(%s)", id, guest.getBillingItemId());
      client.getHardwareServerClient().cancelService(guest.getBillingItemId());
   }

   @Override
   public void rebootNode(String id) {
      throw new UnsupportedOperationException("rebooting is not supported for bare metal instances");
   }

   @Override
   public void resumeNode(String id) {
      throw new UnsupportedOperationException("resuming is not supported for bare metal instances");
   }

   @Override
   public void suspendNode(String id) {
      throw new UnsupportedOperationException("suspending is not supported for bare metal instances");
   }

   public static class HardwareServerHasLoginDetailsPresent implements Predicate<HardwareServer> {

      private final SoftLayerBareMetalInstancesClient client;


      @Inject
      public HardwareServerHasLoginDetailsPresent(SoftLayerBareMetalInstancesClient client) {
         this.client = checkNotNull(client, "client was null");
      }

      @Override
      public boolean apply(HardwareServer server) {
         checkNotNull(server, "virtual guest was null");

         HardwareServer newGuest = client.getHardwareServerClient().getHardwareServer(server.getId());
         boolean hasBackendIp = newGuest.getPrimaryBackendIpAddress() != null;
         boolean hasPrimaryIp = newGuest.getPrimaryIpAddress() != null;
         boolean hasPasswords = newGuest.getOperatingSystem() != null
               && newGuest.getOperatingSystem().getPasswords().size() > 0;

         return hasBackendIp && hasPrimaryIp && hasPasswords;
      }
   }

   public static class HardwareServerHasNoRunningTransactions implements Predicate<HardwareServer> {

      private Map<Integer, Transaction> transactionPerServer = Maps.newHashMap();

      private final SoftLayerBareMetalInstancesClient client;

      @Resource
      @Named(HardwareSoftLayerConstants.TRANSACTION_LOGGER)
      protected Logger logger = Logger.NULL;

      @Inject
      public HardwareServerHasNoRunningTransactions(SoftLayerBareMetalInstancesClient client) {
         this.client = checkNotNull(client, "client was null");
      }

      @Override
      public boolean apply(@Nullable HardwareServer server) {

         Transaction activeTransaction = client.getHardwareServerClient().getActiveTransaction(server.getId());
         if (activeTransaction == null && transactionPerServer.get(server.getId()) != null) {
            // no current transaction, but a previous transaction was present.
            // this means the server is ready.
            transactionPerServer.remove(server.getId());
            return true;
         } else {
            if (activeTransaction == null) {
               // no current transaction, and no previous transaction.
               // this means the server has not yet started its transaction process.
               return false;
            }
            Transaction transaction = transactionPerServer.get(server.getId());
            if (transaction.getId() != activeTransaction.getId()) {
               // server has moved to a new transaction. save and print
               Transaction previous = transactionPerServer.get(server.getId());
               transactionPerServer.put(server.getId(), activeTransaction);
               logger.info("Successfully completed transaction " + previous.getName() + " in "
                       + TimeUnit.SECONDS.toMinutes(previous.getElapsedSeconds()) + " Minutes.");
               logger.info("Current transaction : " + activeTransaction.getName()
                       + ". Average completion time is " + activeTransaction.getAverageDuration() + " Minutes.");
            }
            return false;
         }
      }
   }
}
