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
import org.jclouds.softlayer.compute.functions.HardwareProductItemToImage;
import org.jclouds.softlayer.compute.options.SoftLayerTemplateOptions;
import org.jclouds.softlayer.domain.*;
import org.jclouds.softlayer.reference.SoftLayerConstants;

import javax.annotation.Resource;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.util.Map;
import java.util.Set;

import static com.google.common.base.Preconditions.*;
import static com.google.common.collect.Iterables.*;
import static org.jclouds.softlayer.predicates.ProductItemPredicates.categoryCode;
import static org.jclouds.softlayer.reference.HardwareSoftLayerConstants.*;
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

   private final Predicate<HardwareServer> serverHasNoActiveTransactionsTester;
   private final Predicate<HardwareServer> serverHasActiveTransactionsTester;
   private final Predicate<HardwareProductOrderReceipt> orderApprovedAndServerIsDiscoveredTester;
   private final long activeTransactionsStartedDelay;
   private final long orderApprovedAndServerIsDiscoveredDelay;
   private final long serverLoginDelay;
   private final long serverTransactionsDelay;
   private final Iterable<ProductItemPrice> prices;

   @Inject
   public SoftLayerBareMetalInstancesComputeServiceAdapter(SoftLayerBareMetalInstancesClient client,
                                                           HardwareServerHasLoginDetailsPresent serverHasLoginDetailsPresent,
                                                           HardwareServerHasNoRunningTransactions serverHasNoActiveTransactionsTester,
                                                           HardwareServerStartedTransactions serverHasActiveTransactionsTester,
                                                           HardwareProductOrderApprovedAndServerIsPresent hardwareProductOrderApprovedAndServerIsPresent,
                                                           @Memoized Supplier<ProductPackage> productPackageSupplier,
                                                           Iterable<ProductItemPrice> prices,
                                                           @Named(PROPERTY_SOFTLAYER_BARE_METAL_INSTANCES_LOGIN_DETAILS_DELAY) long serverLoginDelay,
                                                           @Named(PROPERTY_SOFTLAYER_BARE_METAL_INSTANCES_ACTIVE_TRANSACTIONS_ENDED_DELAY) long activeTransactionsEndedDelay,
                                                           @Named(PROPERTY_SOFTLAYER_BARE_METAL_INSTANCES_ACTIVE_TRANSACTIONS_STARTED_DELAY) long activeTransactionsStartedDelay,
                                                           @Named(PROPERTY_SOFTLAYER_BARE_METAL_INSTANCES_HARDWARE_ORDER_APPROVED_DELAY) long hardwareApprovedDelay) {
      this.client = checkNotNull(client, "client");
      this.serverLoginDelay = serverLoginDelay;
      this.serverTransactionsDelay = activeTransactionsEndedDelay;
      this.productPackageSupplier = checkNotNull(productPackageSupplier, "productPackageSupplier");
      this.orderApprovedAndServerIsDiscoveredTester = retry(hardwareProductOrderApprovedAndServerIsPresent, hardwareApprovedDelay, 5000, 10000);
      this.orderApprovedAndServerIsDiscoveredDelay = hardwareApprovedDelay;
      this.activeTransactionsStartedDelay = activeTransactionsStartedDelay;
      this.serverHasActiveTransactionsTester = retry(serverHasActiveTransactionsTester, activeTransactionsStartedDelay, 5000, 10000);
      checkArgument(serverLoginDelay > 500, "guestOrderDelay must be in milliseconds and greater than 500");
      this.loginDetailsTester = retry(serverHasLoginDetailsPresent, serverLoginDelay);
      this.prices = checkNotNull(prices, "prices");
      this.serverHasNoActiveTransactionsTester = retry(serverHasNoActiveTransactionsTester, activeTransactionsEndedDelay, 5000, 10000);
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

      logger.debug(">> awaiting order approval for hardwareServer(%s)", name);
      logger.info("Waiting for server(%s) order approval", name);
      boolean orderApproved = orderApprovedAndServerIsDiscoveredTester.apply(hardwareProductOrderReceipt);
      logger.debug(">> hardwareServer(%s) order approval result(%s)", name, orderApproved);

      checkState(orderApproved, "order for server %s did not finish its transactions within %sms", name,
              Long.toString(orderApprovedAndServerIsDiscoveredDelay));

      HardwareServer result = find(client.getAccountWithBareMetalInstancesClient().listHardwareServers(),
              SoftLayerBareMetalInstancesComputeServiceAdapter.hostNamePredicate(newServer.getHostname()));

      logger.trace("<< hardwareServer(%s)", result.getId());

      logger.info("Waiting for server(%s) transactions to complete", name);

      logger.debug(">> waiting for server(%s) transactions to start", result.getHostname());
      boolean serverHasActiveTransactions = serverHasActiveTransactionsTester.apply(result);
      logger.debug(">> server has active transactions result(%s)", serverHasActiveTransactions);

      checkState(serverHasActiveTransactions, "order for server %s did not start its transactions within %sms", result,
              Long.toString(activeTransactionsStartedDelay));

      logger.debug(">> awaiting transactions for hardwareServer(%s)", result.getHostname());
      boolean noMoreTransactions = serverHasNoActiveTransactionsTester.apply(result);
      logger.debug(">> hardwareServer(%s) complete(%s)", result.getId(), noMoreTransactions);

      checkState(noMoreTransactions, "order for server %s did not finish its transactions within %sms", result,
              Long.toString(serverTransactionsDelay));

      checkNotNull(result, "result server is null");

      logger.debug(">> awaiting login details for hardwareServer(%s)", result.getId());
      boolean orderInSystem = loginDetailsTester.apply(result);
      logger.trace("<< hardwareServer(%s) complete(%s)", result.getId(), orderInSystem);

      checkState(orderInSystem, "order for server %s doesn't have login details within %sms", result,
            Long.toString(serverLoginDelay));
      result = client.getHardwareServerClient().getHardwareServer(result.getId());

      Password pw = get(result.getOperatingSystem().getPasswords(), 0);
      return new NodeAndInitialCredentials<HardwareServer>(result, result.getId() + "", LoginCredentials.builder()
              .user(pw.getUsername())
              .password(pw.getPassword())
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
            for (ProductItem uplinkItem : filter(items, categoryCode("port_speed"))) {
               result.add(ImmutableSet.of(cpuAndRamItem, firsDiskItem, uplinkItem));
            }
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
            return HardwareProductItemToImage.imageId().apply(input).equals(id);
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

      HardwareServer server = getNode(id);
      if (server == null)
         return;

      if (server.getBillingItemId() == -1)
         throw new IllegalStateException(String.format("no billing item for server(%s) so we cannot cancel the order",
               id));

      logger.debug(">> canceling service for guest(%s) billingItem(%s)", id, server.getBillingItemId());
      client.getHardwareServerClient().cancelService(server.getBillingItemId());

      logger.debug(">> waiting for server(%s) transactions to start", server.getHostname());
      boolean serverHasActiveTransactions = serverHasActiveTransactionsTester.apply(server);
      logger.debug(">> server has active transactions result(%s)", serverHasActiveTransactions);

      checkState(serverHasActiveTransactions, "order for server %s did not start its transactions within %sms", server,
              Long.toString(activeTransactionsStartedDelay));

      logger.debug(">> awaiting transactions for hardwareServer(%s) to complete", server.getId());
      boolean noMoreTransactions = serverHasNoActiveTransactionsTester.apply(server);
      logger.debug(">> hardwareServer(%s) complete(%s)", server.getId(), noMoreTransactions);

      checkState(noMoreTransactions, "order for server %s did not finish its transactions within %sms", id,
              Long.toString(serverTransactionsDelay));
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
         checkNotNull(server, "server guest was null");

         HardwareServer newServer = client.getHardwareServerClient().getHardwareServer(server.getId());
         boolean hasBackendIp = newServer.getPrimaryBackendIpAddress() != null;
         boolean hasPrimaryIp = newServer.getPrimaryIpAddress() != null;
         boolean hasPasswords = newServer.getOperatingSystem() != null
               && newServer.getOperatingSystem().getPasswords().size() > 0;

         return hasBackendIp && hasPrimaryIp && hasPasswords;
      }
   }

   public static class HardwareServerHasNoRunningTransactions implements Predicate<HardwareServer> {

      private Map<HardwareServer, Transaction> lastTransactionPerServer = Maps.newConcurrentMap();

      private final SoftLayerBareMetalInstancesClient client;

      @Resource
      @Named(SoftLayerConstants.TRANSACTION_LOGGER)
      protected Logger logger = Logger.NULL;

      @Inject
      public HardwareServerHasNoRunningTransactions(SoftLayerBareMetalInstancesClient client) {
         this.client = checkNotNull(client, "client was null");
      }

      @Override
      public boolean apply(@Nullable HardwareServer server) {
         Transaction activeTransaction = client.getHardwareServerClient().getActiveTransaction(server.getId());
         if (activeTransaction != null) {
            Transaction previous = lastTransactionPerServer.get(server);
            if (previous != null && !previous.getName().equals(activeTransaction.getName())) {
               logger.info("Successfully completed transaction %s in %s seconds.", previous.getName(), previous.getElapsedSeconds());
               logger.info("Current transaction is %s. Average completion time is %s minutes.",
                       activeTransaction.getName(), activeTransaction.getAverageDuration());
            }

            lastTransactionPerServer.put(server, activeTransaction);
            return false;
         }
         logger.info("Successfully completed all transactions for server %s", server.getHostname());
         lastTransactionPerServer.remove(server);
         return true;
      }
   }

   public static class HardwareProductOrderApprovedAndServerIsPresent implements Predicate<HardwareProductOrderReceipt> {

      private final SoftLayerBareMetalInstancesClient client;

      @Inject
      public HardwareProductOrderApprovedAndServerIsPresent(SoftLayerBareMetalInstancesClient client) {
         this.client = checkNotNull(client, "client was null");
      }

      @Override
      public boolean apply(@Nullable HardwareProductOrderReceipt input) {

         BillingOrder orderStatus = client.getAccountWithBareMetalInstancesClient()
                 .getBillingOrder(input.getOrderId());
         boolean orderApproved = BillingOrder.Status.APPROVED.equals(orderStatus.getStatus());

         boolean serverPresent = tryFind(client.getAccountWithBareMetalInstancesClient().listHardwareServers(),
                 SoftLayerBareMetalInstancesComputeServiceAdapter
                         .hostNamePredicate(input.getOrderDetails()
                                 .getHardware().iterator().next().getHostname())).isPresent();

         return orderApproved && serverPresent;
      }
   }

   public static class HardwareServerStartedTransactions implements Predicate<HardwareServer> {

      private final SoftLayerBareMetalInstancesClient client;

      @Resource
      @Named(SoftLayerConstants.TRANSACTION_LOGGER)
      protected Logger logger = Logger.NULL;

      @Inject
      public HardwareServerStartedTransactions(SoftLayerBareMetalInstancesClient client) {
         this.client = checkNotNull(client, "client was null");
      }

      @Override
      public boolean apply(@Nullable HardwareServer server) {
         boolean result = client.getHardwareServerClient().getActiveTransaction(server.getId()) != null;
         if (!result) {
            logger.debug(">> server(%s) has not started any transactions yet", server.getHostname());
         }
         return result;
      }
   }

   static Predicate<HardwareServer> hostNamePredicate(final String hostName) {

      return new Predicate<HardwareServer>() {
         @Override
         public boolean apply(@Nullable HardwareServer input) {
            return input.getHostname().equals(hostName);
         }
      };

   }
}
