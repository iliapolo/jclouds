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
package org.jclouds.softlayer.domain;

import com.google.common.base.Objects;
import com.google.common.base.Objects.ToStringHelper;
import com.google.common.collect.ImmutableSet;
import org.jclouds.javax.annotation.Nullable;

import java.beans.ConstructorProperties;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Class HardwareProductOrder
 *
 * @author Eli Polonsky
 * @see <a href= "http://sldn.softlayer.com/reference/datatypes/SoftLayer_Container_Product_Order"
/>
 */
public class HardwareProductOrder {

   public static Builder<?> builder() {
      return new ConcreteBuilder();
   }

   public Builder<?> toBuilder() {
      return new ConcreteBuilder().fromProductOrder(this);
   }

   public abstract static class Builder<T extends Builder<T>>  {
      protected abstract T self();

      protected int packageId;
      protected String location;
      protected Set<ProductItemPrice> prices = ImmutableSet.of();
      protected Set<HardwareServer> hardware = ImmutableSet.of();
      protected int quantity;
      protected boolean useHourlyPricing;

      /**
       * @see HardwareProductOrder#getPackageId()
       */
      public T packageId(int packageId) {
         this.packageId = packageId;
         return self();
      }

      /**
       * @see HardwareProductOrder#getLocation()
       */
      public T location(String location) {
         this.location = location;
         return self();
      }

      /**
       * @see HardwareProductOrder#getPrices()
       */
      public T prices(Iterable<ProductItemPrice> prices) {
         this.prices = ImmutableSet.copyOf(checkNotNull(prices, "prices"));
         return self();
      }

      public T prices(ProductItemPrice... in) {
         return prices(ImmutableSet.copyOf(in));
      }

      /**
       * @see HardwareProductOrder#getHardware() ()
       */
      public T hardware(Set<HardwareServer> hardwareServers) {
         this.hardware = ImmutableSet.copyOf(checkNotNull(hardwareServers, "virtualGuests"));
         return self();
      }

      public T hardware(HardwareServer... in) {
         return hardware(ImmutableSet.copyOf(in));
      }

      /**
       * @see HardwareProductOrder#getQuantity()
       */
      public T quantity(int quantity) {
         this.quantity = quantity;
         return self();
      }

      /**
       * @see HardwareProductOrder#getUseHourlyPricing()
       */
      public T useHourlyPricing(boolean useHourlyPricing) {
         this.useHourlyPricing = useHourlyPricing;
         return self();
      }

      public HardwareProductOrder build() {
         return new HardwareProductOrder(packageId, location, prices, hardware, quantity, useHourlyPricing);
      }

      public T fromProductOrder(HardwareProductOrder in) {
         return this
               .packageId(in.getPackageId())
               .location(in.getLocation())
               .prices(in.getPrices())
               .hardware(in.getHardware())
               .quantity(in.getQuantity())
               .useHourlyPricing(in.getUseHourlyPricing());
      }
   }

   private static class ConcreteBuilder extends Builder<ConcreteBuilder> {
      @Override
      protected ConcreteBuilder self() {
         return this;
      }
   }

   private final int packageId;
   private final String location;
   private final Set<ProductItemPrice> prices;
   private final Set<HardwareServer> hardware;
   private final int quantity;
   private final boolean useHourlyPricing;

   @ConstructorProperties({
      "packageId", "location", "prices", "hardware", "quantity", "useHourlyPricing"
   })
   protected HardwareProductOrder(int packageId, @Nullable String location, @Nullable Set<ProductItemPrice> prices, @Nullable Set<HardwareServer> hardware, int quantity, boolean useHourlyPricing) {
      this.packageId = packageId;
      this.location = location;
      this.prices = prices == null ? ImmutableSet.<ProductItemPrice>of() : ImmutableSet.copyOf(prices);
      this.hardware = hardware == null ? ImmutableSet.<HardwareServer>of() : ImmutableSet.copyOf(hardware);
      this.quantity = quantity;
      this.useHourlyPricing = useHourlyPricing;
   }

   /**
    * @return The package id of an order. This is required.
    */
   public int getPackageId() {
      return this.packageId;
   }

   /**
    * @return The region keyname or specific location keyname where the order should be provisioned.
    */
   @Nullable
   public String getLocation() {
      return this.location;
   }

   /**
    * Gets the item prices in this order.
    * All that is required to be present is the prices ID
    *
    * @return the prices.
    */
   public Set<ProductItemPrice> getPrices() {
      return this.prices;
   }

   /**
    * Gets the virtual guests in this order.
    *
    * @return the the virtual guests.
    */
   public Set<HardwareServer> getHardware() {
      return this.hardware;
   }

   public int getQuantity() {
      return this.quantity;
   }

   public boolean getUseHourlyPricing() {
      return this.useHourlyPricing;
   }

   @Override
   public int hashCode() {
      return Objects.hashCode(packageId, location, prices, hardware, quantity, useHourlyPricing);
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) return true;
      if (obj == null || getClass() != obj.getClass()) return false;
      HardwareProductOrder that = HardwareProductOrder.class.cast(obj);
      return Objects.equal(this.packageId, that.packageId)
            && Objects.equal(this.location, that.location)
            && Objects.equal(this.prices, that.prices)
            && Objects.equal(this.hardware, that.hardware)
            && Objects.equal(this.quantity, that.quantity)
            && Objects.equal(this.useHourlyPricing, that.useHourlyPricing);
   }

   protected ToStringHelper string() {
      return Objects.toStringHelper(this)
            .add("packageId", packageId).add("location", location).add("prices", prices).add("hardware", hardware).add("quantity", quantity).add("useHourlyPricing", useHourlyPricing);
   }

   @Override
   public String toString() {
      return string().toString();
   }

}
