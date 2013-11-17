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
import org.jclouds.javax.annotation.Nullable;

import java.beans.ConstructorProperties;

/**
 * Class ProductOrderReceipt
 *
 * @author Jason King
 * @see <a href= "http://sldn.softlayer.com/reference/datatypes/SoftLayer_Container_Product_Order_Receipt"
/>
 */
public class HardwareProductOrderReceipt {

   public static Builder<?> builder() {
      return new ConcreteBuilder();
   }

   public Builder<?> toBuilder() {
      return new ConcreteBuilder().fromProductOrderReceipt(this);
   }

   public abstract static class Builder<T extends Builder<T>>  {
      protected abstract T self();

      protected int orderId;
      protected HardwareProductOrder orderDetails;

      /**
       * @see org.jclouds.softlayer.domain.HardwareProductOrderReceipt#getOrderId()
       */
      public T orderId(int orderId) {
         this.orderId = orderId;
         return self();
      }

      /**
       * @see org.jclouds.softlayer.domain.HardwareProductOrderReceipt#getOrderDetails()
       */
      public T orderDetails(HardwareProductOrder orderDetails) {
         this.orderDetails = orderDetails;
         return self();
      }

      public HardwareProductOrderReceipt build() {
         return new HardwareProductOrderReceipt(orderId, orderDetails);
      }

      public T fromProductOrderReceipt(HardwareProductOrderReceipt in) {
         return this
               .orderId(in.getOrderId())
               .orderDetails(in.getOrderDetails());
      }
   }

   private static class ConcreteBuilder extends Builder<ConcreteBuilder> {
      @Override
      protected ConcreteBuilder self() {
         return this;
      }
   }

   private final int orderId;
   private final HardwareProductOrder orderDetails;

   @ConstructorProperties({
         "orderId", "orderDetails"
   })
   protected HardwareProductOrderReceipt(int orderId, @Nullable HardwareProductOrder orderDetails) {
      this.orderId = orderId;
      this.orderDetails = orderDetails;
   }

   /**
    * @return unique identifier for the order.
    */
   public int getOrderId() {
      return this.orderId;
   }

   /**
    * This is a copy of the SoftLayer_Container_Product_Order
    * which holds all the data related to an order.
    * This will only return when an order is processed successfully.
    * It will contain all the items in an order as well as the order totals.
    */
   @Nullable
   public HardwareProductOrder getOrderDetails() {
      return this.orderDetails;
   }

   @Override
   public int hashCode() {
      return Objects.hashCode(orderId, orderDetails);
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) return true;
      if (obj == null || getClass() != obj.getClass()) return false;
      HardwareProductOrderReceipt that = HardwareProductOrderReceipt.class.cast(obj);
      return Objects.equal(this.orderId, that.orderId)
            && Objects.equal(this.orderDetails, that.orderDetails);
   }

   protected ToStringHelper string() {
      return Objects.toStringHelper(this)
            .add("orderId", orderId).add("orderDetails", orderDetails);
   }

   @Override
   public String toString() {
      return string().toString();
   }

}
