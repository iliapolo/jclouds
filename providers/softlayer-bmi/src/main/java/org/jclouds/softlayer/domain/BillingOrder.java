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

package org.jclouds.softlayer.domain;

import com.google.common.base.CaseFormat;
import org.jclouds.javax.annotation.Nullable;

import java.beans.ConstructorProperties;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * The SoftLayer_Billing_Order data type contains general information relating to an individual order applied to a SoftLayer customer account or to a new customer.
 * Personal information in this type such as names, addresses, and phone numbers are taken from the account's contact information at the time the order is generated for existing SoftLayer customer.
 *
 * @author Eli Polonsky
 * @see <a href="http://sldn.softlayer.com/reference/datatypes/SoftLayer_Billing_Order"/>
 */
public class BillingOrder {

   /**
    * These statuses come from the hardwareStatus field. i.e.
    * http://sldn.softlayer.com/reference/datatypes/SoftLayer_Hardware_Status
    */
   public static enum Status {
      APPROVED,
      PENDING_APPROVAL,
      UNRECOGNIZED;

      @Override
      public String toString() {
         return CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, name());
      }

      public static Status fromValue(String status) {
         try {
            return valueOf(CaseFormat.UPPER_CAMEL.to(CaseFormat.UPPER_UNDERSCORE, checkNotNull(status, "status")));
         } catch (IllegalArgumentException e) {
            return UNRECOGNIZED;
         }
      }
   }

   public static Builder<?> builder() {
      return new ConcreteBuilder();
   }

   public Builder<?> toBuilder() {
      return new ConcreteBuilder().fromBillingOrderStatus(this);
   }

   public abstract static class Builder<T extends Builder<T>>  {

      protected abstract T self();

      protected int id;
      protected Status status;

      /**
       * @see BillingOrder#getId()
       */
      public T id(int id) {
         this.id = id;
         return self();
      }

      /**
       * @see BillingOrder#getStatus()
       */
      public T status(Status status) {
         this.status = status;
         return self();
      }

      public T fromBillingOrderStatus(BillingOrder in) {
         return this
                 .id(in.getId())
                 .status(in.getStatus());
      }
   }

   private static class ConcreteBuilder extends Builder<ConcreteBuilder> {
      @Override
      protected ConcreteBuilder self() {
         return this;
      }
   }

   private final int id;
   private final Status status;

   @ConstructorProperties({"id", "status"})
   public BillingOrder(int id, @Nullable Status status) {
      this.id = id;
      this.status = status;
   }

   /**
    *
    * @return the order id
    */
   public int getId() {
      return id;
   }

   /**
    *
    * @return the status key name.
    */
   public Status getStatus() {
      return status;
   }
}
