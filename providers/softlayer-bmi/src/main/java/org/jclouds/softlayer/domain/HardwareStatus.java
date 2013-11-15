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

import com.google.common.base.Objects;
import org.jclouds.javax.annotation.Nullable;

import java.beans.ConstructorProperties;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * SoftLayer_Hardware_Status models the inventory state of any piece of hardware in SoftLayer's inventory.
 * Most of these statuses are used by SoftLayer while a server is not provisioned or undergoing provisioning.
 *
 * @author Eli Polonsky
 * @see <a href= "http://sldn.softlayer.com/reference/datatypes/SoftLayer_Hardware_Status"
 */
public class HardwareStatus {

   public static Builder<?> builder() {
      return new ConcreteBuilder();
   }

   public Builder<?> toBuilder() {
      return new ConcreteBuilder().fromHardwareStatus(this);
   }

   public abstract static class Builder<T extends Builder<T>>  {
      protected abstract T self();

      protected HardwareServer.Status status;

      /**
       * @see HardwareStatus#getStatus() ()
       */
      public T status(HardwareServer.Status status) {
         this.status = status;
         return self();
      }

      public HardwareStatus build() {
         return new HardwareStatus(status);
      }

      public T fromHardwareStatus(HardwareStatus in) {
         return this
                 .status(in.getStatus());
      }
   }

   private static class ConcreteBuilder extends Builder<ConcreteBuilder> {
      @Override
      protected ConcreteBuilder self() {
         return this;
      }
   }

   private final HardwareServer.Status status;

   @ConstructorProperties("status")
   public HardwareStatus(HardwareServer.Status status) {
      this.status = checkNotNull(status,"status cannot be null or empty:"+ status);
   }

   /**
    * Maps onto {@code VirtualGuest.State}
    *
    * @return The key name of a power state.
    */
   @Nullable
   public HardwareServer.Status getStatus() {
      return this.status;
   }

   @Override
   public int hashCode() {
      return Objects.hashCode(status);
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) return true;
      if (obj == null || getClass() != obj.getClass()) return false;
      HardwareStatus that = HardwareStatus.class.cast(obj);
      return Objects.equal(this.status, that.status);
   }

   protected Objects.ToStringHelper string() {
      return Objects.toStringHelper(this)
              .add("status", status);
   }

   @Override
   public String toString() {
      return string().toString();
   }

}
