/*
*Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*WSO2 Inc. licenses this file to you under the Apache License,
*Version 2.0 (the "License"); you may not use this file except
*in compliance with the License.
*You may obtain a copy of the License at
*
*http://www.apache.org/licenses/LICENSE-2.0
*
*Unless required by applicable law or agreed to in writing,
*software distributed under the License is distributed on an
*"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*KIND, either express or implied.  See the License for the
*specific language governing permissions and limitations
*under the License.
*/

package org.wso2.carbon.identity.tests;

import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.automation.api.clients.identity.user.store.config.UserStoreConfigAdminServiceClient;
import org.wso2.carbon.automation.core.ProductConstant;
import org.wso2.carbon.automation.core.utils.UserInfo;
import org.wso2.carbon.automation.core.utils.UserListCsvReader;
import org.wso2.carbon.automation.core.utils.environmentutils.EnvironmentBuilder;
import org.wso2.carbon.automation.core.utils.environmentutils.EnvironmentVariables;
import org.wso2.carbon.automation.core.utils.frameworkutils.FrameworkFactory;
import org.wso2.carbon.identity.user.store.configuration.stub.api.Properties;
import org.wso2.carbon.identity.user.store.configuration.stub.api.Property;
import org.wso2.carbon.identity.user.store.configuration.stub.dto.PropertyDTO;
import org.wso2.carbon.identity.user.store.configuration.stub.dto.UserStoreDTO;

import java.util.ArrayList;

public abstract class ISIntegrationTest {

    protected Log log = LogFactory.getLog(getClass());
    protected EnvironmentVariables isServer;
    protected UserInfo userInfo;
    private EnvironmentBuilder builder;
    private String jdbcClassName = "org.wso2.carbon.user.core.jdbc.JDBCUserStoreManager";
    private UserStoreConfigAdminServiceClient userStoreConfigurationClient;

    protected void init() throws Exception {
        init(2);
    }

    protected void init(int userId) throws Exception {
        userInfo = UserListCsvReader.getUserInfo(userId);
        builder = new EnvironmentBuilder().is(userId);
        isServer = builder.build().getIs();
    }

    protected void addJDBCUserStore(String dbURI, String driverName, String userName, String password,
                                    boolean disabled, String description, String domainName) throws Exception {
        userStoreConfigurationClient =
                new UserStoreConfigAdminServiceClient(isServer.getBackEndUrl(), isServer.getSessionCookie());
        userStoreConfigurationClient.addUserStore(getUserStoreDTO(dbURI, driverName, userName,
                password, disabled, description, domainName));

    }

    protected UserStoreDTO getUserStoreDTO(String dbURI, String driverName, String userName, String password,
                                           boolean disabled, String description, String domainName) throws Exception {
        ArrayList<PropertyDTO> propertyDTOsList = new ArrayList<PropertyDTO>();
        UserStoreDTO userStoreDTO = new UserStoreDTO();

        //set mandatory properties
        userStoreDTO.setClassName(jdbcClassName);
        userStoreDTO.setDescription(description);
        userStoreDTO.setDisabled(disabled);
        userStoreDTO.setDomainId(domainName);

        Properties properties = userStoreConfigurationClient.getUserStoreProperties(jdbcClassName);
        Property[] advanceProperties = properties.getAdvancedProperties();
        Property[] mandatoryProperties = properties.getMandatoryProperties();
        Property[] optionalProperties = properties.getOptionalProperties();

        for (Property mandatoryProperty : mandatoryProperties) {
            if (mandatoryProperty.getName().equals("userName")) {
                mandatoryProperty.setValue(userName);
            } else if (mandatoryProperty.getName().equals("password")) {
                mandatoryProperty.setValue(password);
            } else if (mandatoryProperty.getName().equals("url")) {
                mandatoryProperty.setValue(dbURI);
            } else if (mandatoryProperty.getName().equals("driverName")) {
                mandatoryProperty.setValue(driverName);
            }
        }

        for (Property mandatoryProperty : mandatoryProperties) {
            PropertyDTO propertyDTO = new PropertyDTO();
            propertyDTO.setName(mandatoryProperty.getName());
            propertyDTO.setValue(mandatoryProperty.getValue());
            propertyDTOsList.add(propertyDTO);
        }

        for (Property advanceProperty : advanceProperties) {
            PropertyDTO propertyDTO = new PropertyDTO();
            propertyDTO.setName(advanceProperty.getName());
            propertyDTO.setValue(advanceProperty.getValue());
            propertyDTOsList.add(propertyDTO);
        }

        for (Property optionalProperty : optionalProperties) {
            PropertyDTO propertyDTO = new PropertyDTO();
            propertyDTO.setName(optionalProperty.getName());
            propertyDTO.setValue(optionalProperty.getValue());
            System.out.println(optionalProperty.getValue());
            propertyDTOsList.add(propertyDTO);
        }

        log.info("Adding mandatory and optional properties completed..");

        PropertyDTO[] propertyDTOArray = new PropertyDTO[propertyDTOsList.size()];
        propertyDTOArray = propertyDTOsList.toArray(propertyDTOArray);

        userStoreDTO.setProperties(propertyDTOArray);

        return userStoreDTO;
    }

    protected boolean waitForUserStoreDeployment(String domain) throws Exception {
        userStoreConfigurationClient =
                new UserStoreConfigAdminServiceClient(isServer.getBackEndUrl(), isServer.getSessionCookie());

        long waitTime = System.currentTimeMillis() + 30000; //wait for 45 seconds
        while (System.currentTimeMillis() < waitTime) {
            UserStoreDTO[] userStoreDTOs = userStoreConfigurationClient.getActiveDomains();
            for (UserStoreDTO userStoreDTO : userStoreDTOs) {
                if (userStoreDTO != null) {
                    if (userStoreDTO.getDomainId().equalsIgnoreCase(domain)) {
                        return true;
                    }
                }
            }
            Thread.sleep(500);
        }
        return false;
    }

    protected boolean waitForUserStoreUnDeployment(String domain) throws Exception {
        userStoreConfigurationClient =
                new UserStoreConfigAdminServiceClient(isServer.getBackEndUrl(), isServer.getSessionCookie());

        long waitTime = System.currentTimeMillis() + 20000; //wait for 15 seconds
        while (System.currentTimeMillis() < waitTime) {
            UserStoreDTO[] userStoreDTOs = userStoreConfigurationClient.getActiveDomains();
            for (UserStoreDTO userStoreDTO : userStoreDTOs) {
                if (userStoreDTO != null) {
                    if (userStoreDTO.getDomainId().equalsIgnoreCase(domain)) {
                        Thread.sleep(500);
                    }
                }
            }
        }
        return true;
    }

    protected boolean isRunningOnStratos() {
        return FrameworkFactory.getFrameworkProperties(ProductConstant.IS_SERVER_NAME)
                .getEnvironmentSettings().is_runningOnStratos();
    }

    protected String getISResourceLocation() {
        return ProductConstant.getResourceLocations(ProductConstant.IS_SERVER_NAME);
    }

    protected boolean isBuilderEnabled() {
        return FrameworkFactory.getFrameworkProperties(ProductConstant.IS_SERVER_NAME)
                .getEnvironmentSettings().is_builderEnabled();
    }

    protected boolean isClusterEnabled() {
        return FrameworkFactory.getFrameworkProperties(ProductConstant.IS_SERVER_NAME)
                .getEnvironmentSettings().isClusterEnable();
    }

    protected String getExecutionEnvironment() {
        return FrameworkFactory.getFrameworkProperties(ProductConstant.IS_SERVER_NAME)
                .getEnvironmentSettings().executionEnvironment();
    }

}
