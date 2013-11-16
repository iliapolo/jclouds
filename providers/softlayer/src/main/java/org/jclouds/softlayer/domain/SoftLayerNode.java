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

   /**
    *
    * @return a node's host name.
    */
   String getHostname();

   /**
    *
    * @return the node's data center.
    */
   Datacenter getDatacenter();

   /**
    *
    * @return the node's public ip.
    */
   String getPrimaryIpAddress();

   /**
    *
    * @return the node's private ip.
    */
   String getPrimaryBackendIpAddress();


}
