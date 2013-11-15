package org.jclouds.softlayer.features;

import com.google.common.collect.ImmutableSet;
import org.jclouds.softlayer.domain.HardwareProductOrder;
import org.jclouds.softlayer.domain.HardwareProductOrderReceipt;
import org.jclouds.softlayer.domain.HardwareServer;
import org.jclouds.softlayer.domain.ProductItemPrice;
import org.testng.annotations.Test;

import java.util.Set;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * Tests behavior of {@code AccountWithBareMetalInstancesClient}
 *
 * @author Eli Polonsky
 */
@Test(groups = "live")
public class AccountWithBareMetalInstancesClientLiveTest extends BaseSoftLayerBareMetalInstancesClientLiveTest {

   @Test
   public void testListHardwareServers() throws Exception {

      Set<HardwareServer> response = api.getAccountWithBareMetalInstancesClient().listHardwareServers();
      assert null != response;
      assertTrue(response.size() >= 0);
      for (HardwareServer hs : response) {
         HardwareServer newDetails = api.getHardwareServerClient().getHardwareServer(hs.getId());
         assertEquals(hs.getId(), newDetails.getId());
         checkHardwareServer(hs);
      }
   }

   @Test
   public void testCancelService() throws Exception {
      api.getHardwareServerClient().cancelService(16582809);
   }

   @Test
   public void testOrderHardwareServer() {
      HardwareProductOrderReceipt hardwareProductOrderReceipt = api.getHardwareServerClient().orderHardwareServer(createHardwareProductOrder());
      System.out.println(hardwareProductOrderReceipt);

   }

   private void checkHardwareServer(HardwareServer vg) {
      if (vg.getBillingItemId() == -1)
         return;// Quotes and shutting down guests

      assert vg.getAccountId() > 0 : vg;
      assert vg.getDomain() != null : vg;
      assert vg.getFullyQualifiedDomainName() != null : vg;
      assert vg.getHostname() != null : vg;
      assert vg.getId() > 0 : vg;
      assert vg.getPrimaryBackendIpAddress() != null : vg;
      assert vg.getPrimaryIpAddress() != null : vg;
   }

   private HardwareProductOrder createHardwareProductOrder() {

      HardwareServer server = HardwareServer.builder()
              .domain("elitest.org").hostname("elip-test-jclouds-bare-metal")
              .build();

      // default stuff
      ImmutableSet.Builder<ProductItemPrice> prices = ImmutableSet.builder();
      prices.add(ProductItemPrice.builder().id(21).build()); // 1 IP Address
      prices.add(ProductItemPrice.builder().id(55).build()); // Host Ping: categoryCode: monitoring, notification
      prices.add(ProductItemPrice.builder().id(57).build()); // Email and Ticket: categoryCode: notification
      prices.add(ProductItemPrice.builder().id(58).build()); // Automated Notification: categoryCode: response
      prices.add(ProductItemPrice.builder().id(1800).build()); // 0 GB Bandwidth: categoryCode: bandwidth
      prices.add(ProductItemPrice.builder().id(905).build()); // Reboot / Remote Console: categoryCode: remote_management
      prices.add(ProductItemPrice.builder().id(418).build()); // Nessus Vulnerability Assessment & Reporting: categoryCode:
      // vulnerability_scanner
      prices.add(ProductItemPrice.builder().id(420).build()); // Unlimited SSL VPN Users & 1 PPTP VPN User per account: categoryCode:

      ProductItemPrice cpuAndRam = ProductItemPrice.builder()
              .id(1922).build();

      ProductItemPrice disk = ProductItemPrice.builder()
              .id(19).build();

      ProductItemPrice image = ProductItemPrice.builder()
              .id(13963).build();

      ProductItemPrice uplink = ProductItemPrice.builder()
              .id(272).build();

      prices.add(cpuAndRam);
      prices.add(disk);
      prices.add(image);
      prices.add(uplink);

      return HardwareProductOrder.builder()
              .hardware(server)
              .packageId(50)
              .location("AMSTERDAM")
              .quantity(1)
              .useHourlyPricing(true)
              .prices(prices.build())
              .build();
   }
}
