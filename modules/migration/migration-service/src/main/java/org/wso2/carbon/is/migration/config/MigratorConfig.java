package org.wso2.carbon.is.migration.config;


import java.util.Properties;

public class MigratorConfig {
    private String name ;
    private int order ;
    private Properties parameters = new Properties();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public Properties getParameters() {
        return parameters;
    }
    public String getParameterValue(String parameterKey){
        return getParameters().getProperty(parameterKey);
    }

    public void setParameters(Properties parameters) {
        this.parameters = parameters;
    }

    public static class Comparator implements java.util.Comparator<MigratorConfig> {
        @Override
        public int compare(MigratorConfig migratorConfigOne, MigratorConfig migratorConfigTwo) {
            if(migratorConfigOne.getOrder() > migratorConfigTwo.getOrder()){
                return 1;
            }else if(migratorConfigOne.getOrder() > migratorConfigTwo.getOrder()){
                return -1;
            }
            return 0;
        }
    }
}
