/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.user.profile.internal;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.user.profile.impl.UserAccountAssociationClientServiceImpl;
import org.wso2.carbon.user.profile.impl.UserProfileClientServiceProxyImpl;
import org.wso2.carbon.user.profile.service.UserAccountAssociationClientService;
import org.wso2.carbon.user.profile.service.UserProfileClientService;

/**
 * Activation class for the bundle
 */
public class Activator implements BundleActivator {

    private static final Logger log = LoggerFactory.getLogger(Activator.class);

    @Override
    public void start(BundleContext bundleContext) throws Exception {
        bundleContext.registerService(UserProfileClientService.class.getName(),
                new UserProfileClientServiceProxyImpl(), null);
        bundleContext.registerService(UserAccountAssociationClientService.class.getName(),
                new UserAccountAssociationClientServiceImpl(), null);
        if (log.isDebugEnabled()) {
            log.debug("User profile UUF support Bundle Started.");
        }
    }

    @Override
    public void stop(BundleContext bundleContext) throws Exception {
        if (log.isDebugEnabled()) {
            log.debug("User profile UUF support Bundle Stopped.");
        }
    }
}
