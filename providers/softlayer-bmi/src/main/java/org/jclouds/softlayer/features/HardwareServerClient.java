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

package org.jclouds.softlayer.features;

import org.jclouds.softlayer.domain.HardwareProductOrder;
import org.jclouds.softlayer.domain.HardwareProductOrderReceipt;
import org.jclouds.softlayer.domain.HardwareServer;

import java.io.Closeable;

/**
 * Provides synchronous access to HardwareServer.
 * <p/>
 *
 * @see HardwareServerAsyncClient
 * @see <a href="http://sldn.softlayer.com/article/REST" />
 * @author Eli Polonsky
 * @deprecated This will be renamed to HardwareServerApi in 1.7.0.
 *
 */
public interface HardwareServerClient {

   /**
    *
    * @param id
    *           id of the hardware server
    * @return hardware server or null if not found
    */
   HardwareServer getHardwareServer(long id);

   /**
    * Cancel the resource or service for a billing Item
    *
    * @param id
    *            The id of the billing item to cancel
    * @return true or false
    */
   boolean cancelService(long id);

   /**
    * Use this method for placing server orders and additional services orders.
    * @param order
    *             Details required to order.
    * @return A receipt for the order
    * @see <a href="http://sldn.softlayer.com/reference/services/SoftLayer_Product_Order/placeOrder" />
    */
   HardwareProductOrderReceipt orderHardwareServer(HardwareProductOrder order);
}
