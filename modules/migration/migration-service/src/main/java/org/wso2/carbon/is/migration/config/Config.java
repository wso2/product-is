package org.wso2.carbon.is.migration.config;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.is.migration.MigrationClientException;
import org.wso2.carbon.is.migration.util.Constant;
import org.wso2.carbon.is.migration.util.Utility;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Config holder for the migration service.
 *
 */
public class Config {

    private static final Log log = LogFactory.getLog(Config.class);

    private boolean migrationEnable ;
    private String currentVersion ;
    private String migrateVersion ;

    private boolean continueOnError;
    private boolean batchUpdate;
    private boolean ignoreForInactiveTenants;

    private List<Version> versions = new ArrayList<>();

    private static Config config = null;

    private Config(){

    }

    /**
     * Loading configs.
     *
     * @return
     */
    public static Config getInstance()  {
        if(config == null){
            String migrationConfigFileName = Utility.getMigrationResourceDirectoryPath() + File.separator +
                                             Constant.MIGRATION_CONFIG_FILE_NAME ;
            log.info(Constant.MIGRATION_LOG + "Loading Migration Configs, PATH:" + migrationConfigFileName);
            try {
                config = Utility.loadMigrationConfig(migrationConfigFileName);
            } catch (MigrationClientException e) {
                log.error("Error while loading migration configs.", e);
            }
            log.info(Constant.MIGRATION_LOG + "Successfully loaded the config file.");
        }

        return Config.config;
    }

    public boolean isMigrationEnable() {
        return migrationEnable;
    }

    public void setMigrationEnable(boolean migrationEnable) {
        this.migrationEnable = migrationEnable;
    }

    public List<Version> getVersions() {
        return versions;
    }

    public void setVersions(List<Version> versions) {
        this.versions = versions;
    }

    public String getCurrentVersion() {
        return currentVersion;
    }

    public void setCurrentVersion(String currentVersion) {
        this.currentVersion = currentVersion;
    }

    public String getMigrateVersion() {
        return migrateVersion;
    }

    public void setMigrateVersion(String migrateVersion) {
        this.migrateVersion = migrateVersion;
    }

    public static Config getConfig() {
        return config;
    }

    public static void setConfig(Config config) {
        Config.config = config;
    }

    public Version getMigrateVersion(String version){
        for (Version migrateVersion : versions) {
            if(migrateVersion.getVersion().equals(version)){
                return migrateVersion;
            }
        }
        return null ;
    }

    public boolean isContinueOnError() {
        return continueOnError;
    }

    public void setContinueOnError(boolean continueOnError) {
        this.continueOnError = continueOnError;
    }

    public boolean isBatchUpdate() {
        return batchUpdate;
    }

    public void setBatchUpdate(boolean batchUpdate) {
        this.batchUpdate = batchUpdate;
    }

    public boolean isIgnoreForInactiveTenants() {
        return ignoreForInactiveTenants;
    }

    public void setIgnoreForInactiveTenants(boolean ignoreForInactiveTenants) {
        this.ignoreForInactiveTenants = ignoreForInactiveTenants;
    }


}
