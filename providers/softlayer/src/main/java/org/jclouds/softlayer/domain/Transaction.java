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

/**
 * Class Transaction represents an active/expired transaction performed on a {@code HardwareServer}
 * or on a {@code VirtualGuest}
 *
 * @see <a href="http://sldn.softlayer.com/reference/datatypes/SoftLayer_Provisioning_Version1_Transaction"/>
 * @author Eli Polonsky
 */
public class Transaction {

   public static Builder<?> builder() {
      return new ConcreteBuilder();
   }

   public Builder<?> toBuilder() {
      return new ConcreteBuilder().fromTransaction(this);
   }

   public abstract static class Builder<T extends Builder<T>>  {

      protected abstract T self();

      protected int id;
      protected int elapsedSeconds;
      protected String name;
      protected double averageDuration;

      /**
       * @see org.jclouds.softlayer.domain.Transaction#getId()
       */
      public T id(int id) {
         this.id = id;
         return self();
      }

      /**
       * @see org.jclouds.softlayer.domain.Transaction#getElapsedSeconds()
       */
      public T elapsedSeconds(int elapsedSeconds) {
         this.elapsedSeconds = elapsedSeconds;
         return self();
      }

      /**
       * @see org.jclouds.softlayer.domain.Transaction#getName() ()
       */
      public T name(String name) {
         this.name = name;
         return self();
      }

      /**
       * @see org.jclouds.softlayer.domain.Transaction#getAverageDuration() ()
       */
      public T averageDuration(double averageDuration) {
         this.averageDuration = averageDuration;
         return self();
      }

      public Transaction build() {
         TransactionStatus transactionStatus = new TransactionStatus(name, averageDuration);
         return new Transaction(id, elapsedSeconds, transactionStatus);
      }

      public T fromTransaction(Transaction in) {
         return this
                 .id(in.getId())
                 .elapsedSeconds(in.getElapsedSeconds())
                 .name(in.getName())
                 .averageDuration(in.getAverageDuration());
      }
   }

   public static class TransactionStatus {

      private final String name;
      private final double averageDuration;

      @ConstructorProperties({"name", "averageDuration"})
      public TransactionStatus(String name, double averageDuration) {
         this.name = name;
         this.averageDuration = averageDuration;
      }

      @Override
      public String toString() {
         return Objects.toStringHelper(this)
                 .add("name", name)
                 .add("averageDuration", averageDuration).toString();
      }
   }

   private static class ConcreteBuilder extends Builder<ConcreteBuilder> {

      @Override
      protected ConcreteBuilder self() {
         return this;
      }
   }

   private final int id;
   private int elapsedSeconds;
   private double averageDuration;
   private String name;


   @ConstructorProperties({"id", "elapsedSeconds", "transactionStatus"})
   protected Transaction(int id, int elapsedSeconds, @Nullable TransactionStatus status) {
      this.id = id;
      this.elapsedSeconds = elapsedSeconds;
      this.name = status == null ? "" : status.name;
      this.averageDuration = status == null ? 0 : status.averageDuration;
   }

   /**
    * @return The unique identifier of a specific transaction.
    */
   public int getId() {
      return this.id;
   }

   /**
    * @return The number of seconds passed since this transaction started.
    */
   public int getElapsedSeconds() {
      return elapsedSeconds;
   }

   /**
    * @return The average duration in minutes for this kind of transaction.
    */
   @Nullable
   public double getAverageDuration() {
      return averageDuration;
   }

   /**
    * @return The transaction name.
    */
   @Nullable
   public String getName() {
      return name;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) return true;
      if (obj == null || getClass() != obj.getClass()) return false;
      Transaction that = Transaction.class.cast(obj);
      return Objects.equal(this.id, that.id);
   }

   @Override
   public String toString() {
      return Objects.toStringHelper(this)
              .add("id", id)
              .add("name", name)
              .add("averageDuration", averageDuration)
              .add("elapsedSeconds", elapsedSeconds).toString();
   }
}
