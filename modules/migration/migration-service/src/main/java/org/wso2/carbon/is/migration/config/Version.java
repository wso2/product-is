package org.wso2.carbon.is.migration.config;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Version {

    private String version ;
    private List<MigratorConfig> migratorConfigs = new ArrayList<>();

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public MigratorConfig getMigrationItem(String name){
        for (MigratorConfig migratorConfig : migratorConfigs) {
            if(migratorConfig.getName().equals(name)){
                return migratorConfig;
            }
        }
        return null ;
    }
    public List<MigratorConfig> getMigratorConfigs(){
        Collections.sort(migratorConfigs, new MigratorConfig.Comparator());
        return migratorConfigs;
    }
}
