package org.wso2.carbon.is.migration.service;


import org.apache.commons.lang.StringUtils;
import org.wso2.carbon.is.migration.DataSourceManager;
import org.wso2.carbon.is.migration.MigrationClientException;
import org.wso2.carbon.is.migration.config.Config;
import org.wso2.carbon.is.migration.config.MigratorConfig;
import org.wso2.carbon.is.migration.config.Version;

import javax.sql.DataSource;

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

    public abstract void migrate() throws MigrationClientException;

}