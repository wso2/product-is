/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.is.portal.user.client.api;


import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.is.portal.user.client.api.exception.UserPortalUIException;

/**
 * Represent Recovery management service implementation
 */
@Component(
        name = "org.wso2.is.portal.user.client.api.RecoveryMgtService",
        service = RecoveryMgtService.class,
        immediate = true)
public class RecoveryMgtServiceImpl implements RecoveryMgtService {

    private static final Logger LOGGER = LoggerFactory.getLogger(RecoveryMgtService.class);

    @Activate
    protected void start(final BundleContext bundleContext) {
        LOGGER.info("Registered service implementation" + RecoveryMgtService.class); //todo
    }


    @Override
    public boolean isPasswordRecoveryEnabled() throws UserPortalUIException {
        return true;
    }

    @Override
    public boolean isMultiplePasswordRecoveryEnabled() throws UserPortalUIException {
        return isPasswordRecoveryViaNotificationEnabled() && isPasswordRecoveryWithSecurityQuestionsEnabled();
    }

    @Override
    public boolean isPasswordRecoveryViaNotificationEnabled() throws UserPortalUIException {
        return true;
    }

    @Override
    public boolean isPasswordRecoveryWithSecurityQuestionsEnabled() throws UserPortalUIException {
        return true;
    }
}
