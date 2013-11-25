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
package org.jclouds.softlayer.compute.functions;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import org.jclouds.compute.domain.Hardware;
import org.jclouds.compute.domain.HardwareBuilder;
import org.jclouds.softlayer.domain.ProductItem;
import org.jclouds.softlayer.domain.ProductItemPrice;

import javax.inject.Singleton;
import java.util.List;

import static com.google.common.collect.Iterables.filter;
import static com.google.common.collect.Iterables.get;
import static org.jclouds.softlayer.predicates.ProductItemPredicates.categoryCode;

/**
 * Converts a set of ProductItems to Hardware. All cores have a speed of 2.0Ghz The Hardware Id will
 * be a comma separated list containing the prices ids: cpus,ram,volume
 * 
 * @author Jason King
 */
@Singleton
public class HardwareProductItemsToHardware implements Function<Iterable<ProductItem>, Hardware> {

   @Override
   public Hardware apply(Iterable<ProductItem> items) {

      ProductItem ramAndCoresItem = get(filter(items, categoryCode("server_core")), 0);
      ProductItem volumeItem = get(filter(items, categoryCode("disk0")), 0);

      String hardwareId = hardwareId().apply(ImmutableList.of(ramAndCoresItem, volumeItem));

      return new HardwareBuilder().ids(hardwareId).build();
   }

   /**
    * Generates a hardwareId based on the priceId's of the items in the list
    *
    * @return comma separated list of priceid's
    */
   public static Function<List<ProductItem>, String> hardwareId() {
      return new Function<List<ProductItem>, String>() {
         @Override
         public String apply(List<ProductItem> productItems) {
            StringBuilder builder = new StringBuilder();
            for (ProductItem item : productItems) {
               ProductItemPrice price = ProductItems.price().apply(item);
               builder.append(price.getId()).append(",");
            }
            return builder.toString().substring(0, builder.lastIndexOf(","));
         }
      };
   }
}
