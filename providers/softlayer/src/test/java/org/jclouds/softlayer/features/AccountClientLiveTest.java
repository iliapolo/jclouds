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
package org.jclouds.softlayer.features;

import org.jclouds.softlayer.domain.ProductItem;
import org.jclouds.softlayer.domain.ProductPackage;
import org.jclouds.softlayer.domain.VirtualGuest;
import org.testng.annotations.Test;

import java.util.Set;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * Tests behavior of {@code AccountClient}
 * 
 * @author Jason King
 */
@Test(groups = "live")
public class AccountClientLiveTest extends BaseSoftLayerClientLiveTest {

   @Test
   public void testGetActivePackages() {
      Set<ProductPackage> response = api.getAccountClient().getActivePackages();
      assert null != response;

      assertTrue(response.size() >= 0);
      for (ProductPackage p : response) {
          ProductPackage productPackage = api.getProductPackageClient().getProductPackage(p.getId());
          assert productPackage.getId() > 0 : response;
          assert productPackage.getName() != null : response;

          for (ProductItem item : productPackage.getItems()) {
              checkProductItem(item);
          }
      }
   }

   @Test
   public void testListVirtualGuests() throws Exception {
      Set<VirtualGuest> response = api.getAccountClient().listVirtualGuests();
      assert null != response;
      assertTrue(response.size() >= 0);
      for (VirtualGuest vg : response) {
         VirtualGuest newDetails = api.getVirtualGuestClient().getVirtualGuest(vg.getId());
         assertEquals(vg.getId(), newDetails.getId());
         checkVirtualGuest(vg);
      }
   }

   private void checkVirtualGuest(VirtualGuest vg) {
      if (vg.getBillingItemId() == -1)
         return;// Quotes and shutting down guests

      assert vg.getAccountId() > 0 : vg;
      assert vg.getCreateDate() != null : vg;
      assert vg.getDomain() != null : vg;
      assert vg.getFullyQualifiedDomainName() != null : vg;
      assert vg.getHostname() != null : vg;
      assert vg.getId() > 0 : vg;
      assert vg.getMaxCpu() > 0 : vg;
      assert vg.getMaxCpuUnits() != null : vg;
      assert vg.getMaxMemory() > 0 : vg;
      assert vg.getStartCpus() > 0 : vg;
      assert vg.getStatusId() >= 0 : vg;
      assert vg.getUuid() != null : vg;
      assert vg.getPrimaryBackendIpAddress() != null : vg;
      assert vg.getPrimaryIpAddress() != null : vg;
   }


}
