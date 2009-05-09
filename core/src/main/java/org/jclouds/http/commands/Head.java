/**
 *
 * Copyright (C) 2009 Adrian Cole <adrian@jclouds.org>
 *
 * ====================================================================
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 * ====================================================================
 */
package org.jclouds.http.commands;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import org.jclouds.http.HttpFutureCommand;
import org.jclouds.http.commands.callables.ReturnTrueIf2xx;

/**
 * // TODO: Adrian: Document this!
 *
 * @author Adrian Cole
 */
public class Head extends HttpFutureCommand<Boolean> {

    @Inject
    public Head(ReturnTrueIf2xx callable, @Assisted String uri) {
        super("HEAD", uri, callable);
    }
}