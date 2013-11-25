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

   /**
    * number of milliseconds to wait for an order to be empty of transactions.
    */
   public static final String PROPERTY_SOFTLAYER_BARE_METAL_INSTANCES_ACTIVE_TRANSACTIONS_DELAY = "jclouds.softlayer.baremetal.active-transactions-delay";


   /**
    * Transaction logger name for logging active transactions of the hardware server.
    */
   public static final String TRANSACTION_LOGGER = "jclouds.softlayer-bmi.transaction";
}
