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
package org.wso2.carbon.is.migration.internal;

import org.wso2.carbon.base.api.ServerConfigurationService;
import org.wso2.carbon.identity.claim.metadata.mgt.ClaimMetadataManagementService;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.registry.core.service.TenantRegistryLoader;
import org.wso2.carbon.user.core.service.RealmService;

/**
 * Migration Service Data Holder
 */
public class ISMigrationServiceDataHolder {
    //Registry Service which is used to get registry data.
    private static RegistryService registryService;
    private static ClaimMetadataManagementService claimMetadataManagementService;

    //Realm Service which is used to get tenant data.
    private static RealmService realmService;

    private static ServerConfigurationService serverConfigurationService;

    //Tenant registry loader which is used to load tenant registry
    private static TenantRegistryLoader tenantRegLoader;

    private static String identityOracleUser;
    private static String umOracleUser;

    /**
     * Method to get RegistryService.
     *
     * @return registryService.
     */
    public static RegistryService getRegistryService() {
        return registryService;
    }

    /**
     * Method to set registry RegistryService.
     *
     * @param service registryService.
     */
    public static void setRegistryService(RegistryService service) {
        registryService = service;
    }

    /**
     * This method used to get RealmService.
     *
     * @return RealmService.
     */
    public static RealmService getRealmService() {
        return realmService;
    }

    /**
     * Method to set registry RealmService.
     *
     * @param service RealmService.
     */
    public static void setRealmService(RealmService service) {
        realmService = service;
    }

    /**
     * This method used to get TenantRegistryLoader
     *
     * @return tenantRegLoader  Tenant registry loader for load tenant registry
     */
    public static TenantRegistryLoader getTenantRegLoader() {
        return tenantRegLoader;
    }

    /**
     * This method used to set TenantRegistryLoader
     *
     * @param service Tenant registry loader for load tenant registry
     */
    public static void setTenantRegLoader(TenantRegistryLoader service) {
        tenantRegLoader = service;
    }

    /**
     * This method is used to get the user when the database is oracle
     *
     * @return oracleUser user of the oracle database
     */
    public static String getIdentityOracleUser() {
        return identityOracleUser;
    }

    /**
     * This method is used to set the user when the user when the database is oracle
     *
     * @param identityOracleUser
     */
    public static void setIdentityOracleUser(String identityOracleUser) {
        ISMigrationServiceDataHolder.identityOracleUser = identityOracleUser;
    }

    public static String getUmOracleUser() {
        return umOracleUser;
    }

    public static void setUmOracleUser(String umOracleUser) {
        ISMigrationServiceDataHolder.umOracleUser = umOracleUser;
    }

    public static ClaimMetadataManagementService getClaimMetadataManagementService() {
        return claimMetadataManagementService;
    }

    public static void setClaimMetadataManagementService(
            ClaimMetadataManagementService claimMetadataManagementService) {
        ISMigrationServiceDataHolder.claimMetadataManagementService = claimMetadataManagementService;
    }

    public static ServerConfigurationService getServerConfigurationService() {

        return serverConfigurationService;
    }

    public static void setServerConfigurationService(ServerConfigurationService serverConfigurationService) {

        ISMigrationServiceDataHolder.serverConfigurationService = serverConfigurationService;
    }
}
