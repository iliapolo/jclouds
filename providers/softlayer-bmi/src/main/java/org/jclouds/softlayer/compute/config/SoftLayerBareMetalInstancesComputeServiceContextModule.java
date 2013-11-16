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
package org.jclouds.softlayer.compute.config;

import com.google.common.base.Function;
import com.google.common.base.Objects;
import com.google.common.base.Splitter;
import com.google.common.base.Supplier;
import com.google.common.collect.Iterables;
import com.google.inject.Provides;
import com.google.inject.TypeLiteral;
import org.jclouds.collect.Memoized;
import org.jclouds.compute.ComputeServiceAdapter;
import org.jclouds.compute.config.ComputeServiceAdapterContextModule;
import org.jclouds.compute.domain.NodeMetadata;
import org.jclouds.compute.options.TemplateOptions;
import org.jclouds.domain.Location;
import org.jclouds.rest.AuthorizationException;
import org.jclouds.rest.suppliers.MemoizedRetryOnTimeOutButNotOnAuthorizationExceptionSupplier;
import org.jclouds.softlayer.SoftLayerBareMetalInstancesClient;
import org.jclouds.softlayer.SoftLayerClient;
import org.jclouds.softlayer.compute.functions.*;
import org.jclouds.softlayer.compute.options.SoftLayerTemplateOptions;
import org.jclouds.softlayer.compute.strategy.SoftLayerBareMetalInstancesComputeServiceAdapter;
import org.jclouds.softlayer.compute.strategy.SoftLayerBareMetalInstancesComputeServiceAdapter;
import org.jclouds.softlayer.domain.*;
import org.jclouds.softlayer.features.AccountClient;
import org.jclouds.softlayer.features.ProductPackageClient;

import javax.inject.Named;
import javax.inject.Singleton;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Iterables.find;
import static org.jclouds.Constants.PROPERTY_SESSION_INTERVAL;
import static org.jclouds.softlayer.predicates.ProductPackagePredicates.named;
import static org.jclouds.softlayer.reference.HardwareSoftLayerConstants.*;

/**
 * 
 * @author Eli Polonsky
 */
public class SoftLayerBareMetalInstancesComputeServiceContextModule extends
         ComputeServiceAdapterContextModule<HardwareServer, Iterable<ProductItem>, ProductItem, Datacenter> {

   @Override
   protected void configure() {
      super.configure();
      bind(new TypeLiteral<ComputeServiceAdapter<HardwareServer, Iterable<ProductItem>, ProductItem, Datacenter>>() {
      }).to(SoftLayerBareMetalInstancesComputeServiceAdapter.class);
      bind(new TypeLiteral<Function<HardwareServer, NodeMetadata>>() {
      }).to(HardwareServerToNodeMetaData.class);
      bind(new TypeLiteral<Function<ProductItem, org.jclouds.compute.domain.Image>>() {
      }).to(ProductItemToImage.class);
      bind(new TypeLiteral<Function<Iterable<ProductItem>, org.jclouds.compute.domain.Hardware>>() {
      }).to(ProductItemsToHardware.class);
      bind(new TypeLiteral<Function<Datacenter, Location>>() {
      }).to(DatacenterToLocation.class);
      bind(TemplateOptions.class).to(SoftLayerTemplateOptions.class);
      // to have the compute service adapter override default locations
      install(new LocationsFromComputeServiceAdapterModule<HardwareServer, Iterable<ProductItem>, ProductItem, Datacenter>(){});
   }

   /**
    * Many requests need the same productPackage, which is in this case the package for virtual
    * guests. We may at some point need to make an annotation qualifying it as such. ex. @VirtualGuest
    */
   @Provides
   @Singleton
   @Memoized
   public Supplier<ProductPackage> getProductPackage(AtomicReference<AuthorizationException> authException,
            @Named(PROPERTY_SESSION_INTERVAL) long seconds, final SoftLayerBareMetalInstancesClient client,
            @Named(PROPERTY_SOFTLAYER_BARE_METAL_INSTANCES_PACKAGE_NAME) final String hardwareServerPackageName) {
      return MemoizedRetryOnTimeOutButNotOnAuthorizationExceptionSupplier.create(authException,
               new Supplier<ProductPackage>() {
                  @Override
                  public ProductPackage get() {
                     AccountClient accountClient = client.getAccountClient();
                     ProductPackageClient productPackageClient = client.getProductPackageClient();
                     ProductPackage p = find(accountClient.getReducedActivePackages(), named(hardwareServerPackageName));
                     return productPackageClient.getProductPackage(p.getId());
                  }
                  
                  @Override
                  public String toString() {
                     return Objects.toStringHelper(client).add("method", "accountClient.getActivePackages")
                                                          .add("method", "productPackageClient.getProductPackage").toString();
                  }
               }, seconds, TimeUnit.SECONDS);
   }

   // TODO: check the prices really do exist
   @Provides
   @Singleton
   public Iterable<ProductItemPrice> prices(@Named(PROPERTY_SOFTLAYER_BARE_METAL_INSTANCES_PRICES) String prices) {
      return Iterables.transform(Splitter.on(',').split(checkNotNull(prices, "prices")),
               new Function<String, ProductItemPrice>() {
                  @Override
                  public ProductItemPrice apply(String arg0) {
                     return ProductItemPrice.builder().id(Integer.parseInt(arg0)).build();
                  }
               });
   }

}
