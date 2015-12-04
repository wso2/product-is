/*
* Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.is.migration.client.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.identity.core.util.IdentityCoreInitializedEvent;
import org.wso2.carbon.is.migration.client.MigrateFrom5to510;
import org.wso2.carbon.is.migration.util.Constants;
import org.wso2.carbon.user.core.service.RealmService;


/**
 * @scr.component name="org.wso2.carbon.is.migration.client"
 * immediate="true"
 * @scr.reference name="realm.service"
 * interface="org.wso2.carbon.user.core.service.RealmService" cardinality="1..1"
 * policy="dynamic" bind="setRealmService" unbind="unsetRealmService"
 */

@SuppressWarnings("unused")
public class ISMigrationServiceComponent {

    private static final Log log = LogFactory.getLog(ISMigrationServiceComponent.class);

    /**
     * Method to activate bundle.
     *
     * @param context OSGi component context.
     */
    protected void activate(ComponentContext context) {
        try {
            ISMigrationServiceDataHolder.setIdentityOracleUser(System.getProperty("identityOracleUser"));
            ISMigrationServiceDataHolder.setUmOracleUser(System.getProperty("umOracleUser"));
            if(log.isDebugEnabled()) {
                log.debug("WSO2 IS migration bundle is activated");
            }
        } catch (Throwable e) {
            log.error("Error while initiating Migration component", e);
        }

    }

    /**
     * Method to deactivate bundle.
     *
     * @param context OSGi component context.
     */
    protected void deactivate(ComponentContext context) {
        if(log.isDebugEnabled()) {
            log.debug("WSO2 IS migration bundle is deactivated");
        }
    }


    /**
     * Method to set realm service.
     *
     * @param realmService service to get tenant data.
     */
    protected void setRealmService(RealmService realmService) {
        if(log.isDebugEnabled()) {
            log.debug("Setting RealmService to WSO2 IS Migration component");
        }
        ISMigrationServiceDataHolder.setRealmService(realmService);
    }

    /**
     * Method to unset realm service.
     *
     * @param realmService service to get tenant data.
     */
    protected void unsetRealmService(RealmService realmService) {
        if (log.isDebugEnabled()) {
            log.debug("Unsetting RealmService from WSO2 IS Migration component");
        }
        ISMigrationServiceDataHolder.setRealmService(null);
    }

    protected void unsetIdentityCoreInitializedEventService(IdentityCoreInitializedEvent identityCoreInitializedEvent) {
        /* reference IdentityCoreInitializedEvent service to guarantee that this component will wait until identity core
         is started */
    }

    protected void setIdentityCoreInitializedEventService(IdentityCoreInitializedEvent identityCoreInitializedEvent) {
        /* reference IdentityCoreInitializedEvent service to guarantee that this component will wait until identity core
         is started */
    }

}
