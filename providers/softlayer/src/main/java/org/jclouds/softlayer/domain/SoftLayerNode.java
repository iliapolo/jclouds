package org.jclouds.softlayer.domain;

/**
 * Interface describing softlayer compute nodes. (CCI, BMI, Dedicated)
 *
 * @author Eli Polonsky
 */
public interface SoftLayerNode {

   /**
    *
    * @return the billing item for this node.
    */
   int getBillingItemId();

   /**
    * @return Unique ID for a node instance.
    */
   int getId();
}
