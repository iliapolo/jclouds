package org.jclouds.softlayer.reference;

/**
 * Configuration properties and constants used in SoftLayer connections to hardware API.
 *
 * @author Eli Polonsky
 */
public interface HardwareSoftLayerConstants {

   /**
    * Name of the product package corresponding to bare metal instances
    */
   public static final String PROPERTY_SOFTLAYER_BARE_METAL_INSTANCES_PACKAGE_NAME = "jclouds.softlayer.baremetal.package-name";

   /**
    * Uplink port speed for new servers (10, 100, 1000)
    */
   public static final String PROPERTY_SOFTLAYER_BARE_METAL_INSTANCES_PORT_SPEED = "jclouds.softlayer.baremetal.port-speed";

   /**
    * number of milliseconds to wait for an order to arrive on the api.
    */
   public static final String PROPERTY_SOFTLAYER_BARE_METAL_INSTANCES_LOGIN_DETAILS_DELAY = "jclouds.softlayer.virtualguest.order-delay";

   /**
    * standard prices for all new servers..
    */
   public static final String PROPERTY_SOFTLAYER_BARE_METAL_INSTANCES_PRICES = "jclouds.softlayer.baremetal.prices";




}
