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
package org.jclouds.softlayer;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableSet;
import org.jclouds.providers.ProviderMetadata;
import org.jclouds.providers.internal.BaseProviderMetadata;

import java.net.URI;
import java.util.Properties;

import static org.jclouds.compute.config.ComputeServiceProperties.TEMPLATE;
import static org.jclouds.softlayer.reference.HardwareSoftLayerConstants.*;

/**
 * Implementation of {@link org.jclouds.providers.ProviderMetadata} for SoftLayer.
 * @author Adrian Cole
 */
public class SoftLayerBareMetalInstancesProviderMetadata extends BaseProviderMetadata {

   public static Builder builder() {
      return new Builder();
   }

   @Override
   public Builder toBuilder() {
      return builder().fromProviderMetadata(this);
   }

   public SoftLayerBareMetalInstancesProviderMetadata() {
      super(builder());
   }

   public SoftLayerBareMetalInstancesProviderMetadata(Builder builder) {
      super(builder);
   }

   public static Properties defaultProperties() {
      Properties properties = new Properties();
      properties.setProperty(PROPERTY_SOFTLAYER_BARE_METAL_INSTANCES_LOGIN_DETAILS_DELAY, "" + 60 * 60 * 1000);
      properties.setProperty(PROPERTY_SOFTLAYER_BARE_METAL_INSTANCES_ACTIVE_TRANSACTIONS_DELAY, "" + 10 * 60 * 60 * 1000);
      properties.setProperty(PROPERTY_SOFTLAYER_BARE_METAL_INSTANCES_HARDWARE_ORDER_APPROVED_DELAY, "" + 5 * 60 * 60 * 1000);
      properties.setProperty(PROPERTY_SOFTLAYER_BARE_METAL_INSTANCES_PACKAGE_NAME, "Bare Metal Instance");

      // 10, 100, 1000
      properties.setProperty(PROPERTY_SOFTLAYER_BARE_METAL_INSTANCES_PORT_SPEED, "10");
      ImmutableSet.Builder<String> prices = ImmutableSet.builder();
      prices.add("21"); // 1 IP Address
      prices.add("55"); // Host Ping: categoryCode: monitoring, notification
      prices.add("57"); // Email and Ticket: categoryCode: notification
      prices.add("58"); // Automated Notification: categoryCode: response
      prices.add("1800"); // 0 GB Bandwidth: categoryCode: bandwidth
      prices.add("905"); // Reboot / Remote Console: categoryCode: remote_management
      prices.add("418"); // Nessus Vulnerability Assessment & Reporting: categoryCode:
                         // vulnerability_scanner
      prices.add("420"); // Unlimited SSL VPN Users & 1 PPTP VPN User per account: categoryCode:
                         // vpn_management
      properties.setProperty(PROPERTY_SOFTLAYER_BARE_METAL_INSTANCES_PRICES, Joiner.on(',').join(prices.build()));
      properties.setProperty(TEMPLATE, "osFamily=UBUNTU,osVersionMatches=1[012].[01][04],os64Bit=true,osDescriptionMatches=.*Minimal Install.*");
      return properties;
   }

   public static class Builder extends BaseProviderMetadata.Builder {

      protected Builder() {
         id("softlayer-bmi")
         .name("SoftLayer Bare Metal Support")
         .apiMetadata(new SoftLayerBareMetalInstancesApiMetadata())
         .homepage(URI.create("http://www.softlayer.com"))
         .console(URI.create("https://manage.softlayer.com"))
         .iso3166Codes("SG", "US-CA", "US-TX", "US-VA", "US-WA", "US-TX", "NL", "NSFTW-IL")  // NSFTW-IL is a weird isoCode returned by Softlayer
         .endpoint("https://api.softlayer.com/rest")
         .defaultProperties(SoftLayerBareMetalInstancesProviderMetadata.defaultProperties());
      }

      @Override
      public SoftLayerBareMetalInstancesProviderMetadata build() {
         return new SoftLayerBareMetalInstancesProviderMetadata(this);
      }

      @Override
      public Builder fromProviderMetadata(ProviderMetadata in) {
         super.fromProviderMetadata(in);
         return this;
      }

   }
}
