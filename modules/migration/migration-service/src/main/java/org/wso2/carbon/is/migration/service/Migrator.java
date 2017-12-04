/*
* Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.is.migration.service;

import org.apache.commons.lang.StringUtils;
import org.wso2.carbon.identity.core.migrate.MigrationClientException;
import org.wso2.carbon.is.migration.DataSourceManager;
import org.wso2.carbon.is.migration.config.Config;
import org.wso2.carbon.is.migration.config.MigratorConfig;
import org.wso2.carbon.is.migration.config.Version;

import javax.sql.DataSource;

/**
 * Abstract class for Migrator contract. All migration implementation should be implemented from this class.
 */
public abstract class Migrator {

    public static final String SCHEMA = "schema" ;
    public static final String CONTINUE_ON_ERROR = "continueOnError" ;
    public static final String BATCH_UPDATE = "batchUpdate" ;
    public static final String IGNORE_FOR_INACTIVE_TENANTS = "ignoreForInactiveTenants" ;


    private MigratorConfig migratorConfig ;
    private Version versionConfig ;

    public void setMigratorConfig(MigratorConfig migratorConfig) {
        this.migratorConfig = migratorConfig;
    }

    public MigratorConfig getMigratorConfig() {
        return this.migratorConfig;
    }

    public DataSource getDataSource(String schema) throws MigrationClientException {
        DataSource dataSource = DataSourceManager.getInstance().getDataSource(schema);
        return dataSource;
    }

    public DataSource getDataSource() throws MigrationClientException {
        DataSource dataSource = DataSourceManager.getInstance().getDataSource(getSchema());
        return dataSource;
    }

    public boolean isContinueOnError() {
        String continueOnError = getMigratorConfig().getParameterValue(CONTINUE_ON_ERROR);
        if(StringUtils.isBlank(continueOnError)){
            return Config.getInstance().isContinueOnError();
        }
        return Boolean.parseBoolean(continueOnError);
    }

    public boolean isBatchUpdate() {
        String batchUpdate = getMigratorConfig().getParameterValue(BATCH_UPDATE);
        if(StringUtils.isBlank(batchUpdate)){
            return Config.getInstance().isBatchUpdate();
        }
        return Boolean.parseBoolean(batchUpdate);
    }

    public boolean isIgnoreForInactiveTenants() {
        String ignoreForInactiveTenants = getMigratorConfig().getParameterValue(IGNORE_FOR_INACTIVE_TENANTS);
        if(StringUtils.isBlank(ignoreForInactiveTenants)){
            return Config.getInstance().isIgnoreForInactiveTenants();
        }
        return Boolean.parseBoolean(ignoreForInactiveTenants);
    }

    public String getSchema() {
        return getMigratorConfig().getParameterValue(SCHEMA);
    }

    public Version getVersionConfig() {
        return versionConfig;
    }

    public void setVersionConfig(Version versionConfig) {
        this.versionConfig = versionConfig;
    }

    /**
     * Migrator specific implementation.
     *
     * @throws MigrationClientException
     */
    public abstract void migrate() throws MigrationClientException;

}