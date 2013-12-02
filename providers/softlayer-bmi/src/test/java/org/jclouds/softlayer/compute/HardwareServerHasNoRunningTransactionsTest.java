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

package org.jclouds.softlayer.compute;

import com.google.common.base.Predicate;
import org.jclouds.softlayer.SoftLayerBareMetalInstancesClient;
import org.jclouds.softlayer.compute.strategy.SoftLayerBareMetalInstancesComputeServiceAdapter;
import org.jclouds.softlayer.domain.HardwareProductOrder;
import org.jclouds.softlayer.domain.HardwareProductOrderReceipt;
import org.jclouds.softlayer.domain.HardwareServer;
import org.jclouds.softlayer.domain.Transaction;
import org.jclouds.softlayer.features.*;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.Random;

import static org.jclouds.util.Predicates2.retry;

/**
 * Test functionality of {@code HardwareServerHasNoRunningTransactions}
 *
 * @author Eli Polonsky
 */

@Test(groups = "unit")
public class HardwareServerHasNoRunningTransactionsTest {

   Predicate<HardwareServer> tester;

   @BeforeMethod
   public void setup() {
      SoftLayerBareMetalInstancesComputeServiceAdapter.HardwareServerHasNoRunningTransactions
              hardwareServerHasNoRunningTransactions = new SoftLayerBareMetalInstancesComputeServiceAdapter
              .HardwareServerHasNoRunningTransactions(new McokSoftLayerBareMetalInstancesClient());
      tester = retry(hardwareServerHasNoRunningTransactions , 1000 * 60);
   }

   @Test(enabled = false)
   public void testApply() {

      HardwareServer server = HardwareServer.builder().id(123).build();
      tester.apply(server);
   }


   public class MockHardwareServerClient implements HardwareServerClient {

      private int count = 0;

      private int MAX_TRANSACTION = 10;

      public MockHardwareServerClient(int transactionNumber) {
         this.count = transactionNumber;
      }

      @Override
      public HardwareServer getHardwareServer(long id) {
         throw new UnsupportedOperationException();
      }

      @Override
      public boolean cancelService(long id) {
         throw new UnsupportedOperationException();
      }

      @Override
      public HardwareProductOrderReceipt orderHardwareServer(HardwareProductOrder order) {
         throw new UnsupportedOperationException();
      }

      @Override
      public Transaction getActiveTransaction(long id) {
         if (count == 10) {
            return null;
         }
         return Transaction.builder()
                 .name("TEST_TRANSACTION" + count)
                 .id(count)
                 .averageDuration(new Random().nextDouble())
                 .elapsedSeconds(new Random().nextInt())
                 .build();

      }

      @Override
      public Transaction getLastTransaction(long id) {
         throw new UnsupportedOperationException();
      }
   }

   public class McokSoftLayerBareMetalInstancesClient implements SoftLayerBareMetalInstancesClient {

      private int currentTransactionNumber = 0;

      @Override
      public AccountWithBareMetalInstancesClient getAccountWithBareMetalInstancesClient() {
         throw new UnsupportedOperationException();
      }

      @Override
      public HardwareServerClient getHardwareServerClient() {
         MockHardwareServerClient mockHardwareServerClient = new MockHardwareServerClient(currentTransactionNumber);
         currentTransactionNumber++;
         return mockHardwareServerClient;
      }

      @Override
      public VirtualGuestClient getVirtualGuestClient() {
         throw new UnsupportedOperationException();
      }

      @Override
      public DatacenterClient getDatacenterClient() {
         throw new UnsupportedOperationException();
      }

      @Override
      public ProductPackageClient getProductPackageClient() {
         throw new UnsupportedOperationException();
      }

      @Override
      public AccountClient getAccountClient() {
         throw new UnsupportedOperationException();
      }

      @Override
      public void close() throws IOException {
         throw new UnsupportedOperationException();
      }
   }

}
