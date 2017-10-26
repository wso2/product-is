package org.wso2.carbon.is.migration;

import org.wso2.carbon.is.migration.config.Config;
import org.wso2.carbon.is.migration.config.Version;
import org.wso2.carbon.is.migration.config.MigratorConfig;

import org.wso2.carbon.is.migration.service.Migrator;
import org.wso2.carbon.is.migration.service.SchemaMigrator;
import org.wso2.carbon.is.migration.util.Constant;

import java.util.ArrayList;
import java.util.List;

public abstract class VersionMigration {

    public void migrate() throws MigrationClientException {
        List<Migrator> migrators = getMigrators();
        for (Migrator migrator : migrators) {
            migrator.migrate();
        }
    }

    public List<Migrator> getMigrators() throws MigrationClientException {

        List<Migrator> migrators = new ArrayList<>();
        Version version = getMigrationConfig();
        List<MigratorConfig> migratorConfigs = version.getMigratorConfigs();
        for (MigratorConfig migratorConfig : migratorConfigs) {
            String migratorName = migratorConfig.getName();
            Migrator migrator = null ;
            if(migratorName.equals(Constant.SCHEMA_MIGRATOR_NAME)){
                migrator = new SchemaMigrator() ;
            }else{
                migrator = getMigrator(migratorName) ;
            }
            migrator.setMigratorConfig(migratorConfig);
            migrator.setVersionConfig(version);
            migrators.add(migrator);
        }
        return migrators;
    }

    public abstract String getPreviousVersion();
    public abstract String getCurrentVersion();

    public Migrator getMigrator(String migratorName) {
        Package aPackage = this.getClass().getPackage();
        String basePackage = aPackage.getName() + ".migrator";
        try {
            Class<?> migratorClass = Class.forName(basePackage + "." + migratorName);
            Migrator migrator = (Migrator)migratorClass.newInstance();
            return migrator ;
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }
    public Version getMigrationConfig() throws MigrationClientException {
        Config config = Config.getInstance();
        Version version = config.getMigrateVersion(getCurrentVersion());
        return version;
    }

}
