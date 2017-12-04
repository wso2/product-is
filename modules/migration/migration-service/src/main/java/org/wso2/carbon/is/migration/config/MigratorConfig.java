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
package org.wso2.carbon.is.migration.config;


import java.util.Properties;

/**
 * Migrator config bean
 */
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

    /**
     * Comparator implementation for Migration config
     */
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
