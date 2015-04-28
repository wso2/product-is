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

package org.wso2.identity.integration.common.utils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.automation.engine.context.AutomationContext;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.automation.engine.context.beans.ContextUrls;
import org.wso2.carbon.automation.engine.context.beans.Tenant;
import org.wso2.carbon.automation.engine.context.beans.User;
import org.wso2.carbon.automation.engine.frameworkutils.FrameworkPathUtil;
import org.wso2.carbon.automation.test.utils.common.TestConfigurationProvider;
import org.wso2.carbon.identity.user.store.configuration.stub.api.Properties;
import org.wso2.carbon.identity.user.store.configuration.stub.api.Property;
import org.wso2.carbon.identity.user.store.configuration.stub.dto.PropertyDTO;
import org.wso2.carbon.identity.user.store.configuration.stub.dto.UserStoreDTO;
import org.wso2.carbon.integration.common.admin.client.AuthenticatorClient;
import org.wso2.carbon.integration.common.utils.LoginLogoutClient;

import javax.xml.xpath.XPathExpressionException;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;

public class ISIntegrationTest {

    protected Log log = LogFactory.getLog(getClass());
    protected AutomationContext isServer;
    protected String backendURL;
    protected String sessionCookie;
    protected Tenant tenantInfo;
    protected User userInfo;
    protected ContextUrls identityContextUrls;
    private static String jdbcClassName = "org.wso2.carbon.user.core.jdbc.JDBCUserStoreManager";
    private LoginLogoutClient loginLogoutClient;

    protected void init() throws Exception {
        init(TestUserMode.SUPER_TENANT_ADMIN);
    }

    protected void init(TestUserMode userMode) throws Exception {
        isServer = new AutomationContext("IDENTITY", userMode);
        backendURL = isServer.getContextUrls().getBackEndUrl();
        loginLogoutClient = new LoginLogoutClient(isServer);
        sessionCookie = loginLogoutClient.login();
        identityContextUrls = isServer.getContextUrls();
        tenantInfo = isServer.getContextTenant();
        userInfo = tenantInfo.getContextUser();
    }

    protected void init(String instance, String domainKey, String userKey) throws Exception {
        isServer = new AutomationContext("IDENTITY", instance, domainKey, userKey);
        loginLogoutClient = new LoginLogoutClient(isServer);
        sessionCookie = loginLogoutClient.login();
        backendURL = isServer.getContextUrls().getBackEndUrl();
    }

    protected String login() throws Exception{
        return  new AuthenticatorClient(backendURL).login(isServer.getSuperTenant().getTenantAdmin().getUserName(),
                isServer.getSuperTenant().getTenantAdmin().getPassword(),
                isServer.getInstance().getHosts().get("default"));
    }

    protected String getSessionCookie() {
        return sessionCookie;
    }

    protected String getISResourceLocation() {
        return TestConfigurationProvider.getResourceLocation("IS");
    }

    protected void initPublisher(String productGroupName, String instanceName, TestUserMode userMode, String userKey)
            throws XPathExpressionException {
        isServer = new AutomationContext(productGroupName, instanceName, userMode);
        backendURL = isServer.getContextUrls().getBackEndUrl();
    }

    protected String getBackendURL() throws XPathExpressionException {
        return isServer.getContextUrls().getBackEndUrl();
    }

    protected String getServiceURL() throws XPathExpressionException {
        return isServer.getContextUrls().getServiceUrl();
    }

    protected String getTestArtifactLocation() {
        return FrameworkPathUtil.getSystemResourceLocation();
    }




//
//    protected boolean isRunningOnStratos() {
//        return FrameworkFactory.getFrameworkProperties(ProductConstant.IS_SERVER_NAME)
//                .getEnvironmentSettings().is_runningOnStratos();
//    }
//
//    protected String getISResourceLocation() {
//        return ProductConstant.getResourceLocations(ProductConstant.IS_SERVER_NAME);
//    }
//
//    protected boolean isBuilderEnabled() {
//        return FrameworkFactory.getFrameworkProperties(ProductConstant.IS_SERVER_NAME)
//                .getEnvironmentSettings().is_builderEnabled();
//    }
//
//    protected boolean isClusterEnabled() {
//        return FrameworkFactory.getFrameworkProperties(ProductConstant.IS_SERVER_NAME)
//                .getEnvironmentSettings().isClusterEnable();
//    }
//
//    protected String getExecutionEnvironment() {
//        return FrameworkFactory.getFrameworkProperties(ProductConstant.IS_SERVER_NAME)
//                .getEnvironmentSettings().executionEnvironment();
//    }

    public void setSystemproperties() {
        URL resourceUrl = getClass().getResource(File.separator + "keystores" + File.separator
                + "products" + File.separator + "wso2carbon.jks");
        System.setProperty("javax.net.ssl.trustStore", resourceUrl.getPath());
        System.setProperty("javax.net.ssl.trustStorePassword",
                "wso2carbon");
        System.setProperty("javax.net.ssl.trustStoreType", "JKS");
    }

//    protected void addJDBCUserStore(String dbURI, String driverName, String userName, String password,
//                                    boolean disabled, String description, String domainName) throws Exception {
//        UserStoreConfigAdminServiceClient userStoreConfigurationClient =
//                new UserStoreConfigAdminServiceClient(backendURL, sessionCookie);
//        userStoreConfigurationClient.addUserStore(getUserStoreDTO(dbURI, driverName, userName,
//                password, disabled, description, domainName));
//
//    }
//
//    protected UserStoreDTO getUserStoreDTO(String dbURI, String driverName, String userName, String password,
//                                           boolean disabled, String description, String domainName) throws Exception {
//        ArrayList<PropertyDTO> propertyDTOsList = new ArrayList<PropertyDTO>();
//        UserStoreDTO userStoreDTO = new UserStoreDTO();
//        UserStoreConfigAdminServiceClient userStoreConfigurationClient =
//                new UserStoreConfigAdminServiceClient(backendURL, sessionCookie);
//        //set mandatory properties
//        userStoreDTO.setClassName(jdbcClassName);
//        userStoreDTO.setDescription(description);
//        userStoreDTO.setDisabled(disabled);
//        userStoreDTO.setDomainId(domainName);
//
//        Properties properties = userStoreConfigurationClient.getUserStoreProperties(jdbcClassName);
//        Property[] advanceProperties = properties.getAdvancedProperties();
//        Property[] mandatoryProperties = properties.getMandatoryProperties();
//        Property[] optionalProperties = properties.getOptionalProperties();
//
//        for (Property mandatoryProperty : mandatoryProperties) {
//            if (mandatoryProperty.getName().equals("userName")) {
//                mandatoryProperty.setValue(userName);
//            } else if (mandatoryProperty.getName().equals("password")) {
//                mandatoryProperty.setValue(password);
//            } else if (mandatoryProperty.getName().equals("url")) {
//                mandatoryProperty.setValue(dbURI);
//            } else if (mandatoryProperty.getName().equals("driverName")) {
//                mandatoryProperty.setValue(driverName);
//            }
//        }
//
//        for (Property mandatoryProperty : mandatoryProperties) {
//            PropertyDTO propertyDTO = new PropertyDTO();
//            propertyDTO.setName(mandatoryProperty.getName());
//            propertyDTO.setValue(mandatoryProperty.getValue());
//            propertyDTOsList.add(propertyDTO);
//        }
//
//        for (Property advanceProperty : advanceProperties) {
//            PropertyDTO propertyDTO = new PropertyDTO();
//            propertyDTO.setName(advanceProperty.getName());
//            propertyDTO.setValue(advanceProperty.getValue());
//            propertyDTOsList.add(propertyDTO);
//        }
//
//        for (Property optionalProperty : optionalProperties) {
//            PropertyDTO propertyDTO = new PropertyDTO();
//            propertyDTO.setName(optionalProperty.getName());
//            propertyDTO.setValue(optionalProperty.getValue());
//            System.out.println(optionalProperty.getValue());
//            propertyDTOsList.add(propertyDTO);
//        }
//
//        log.info("Adding mandatory and optional properties completed..");
//
//        PropertyDTO[] propertyDTOArray = new PropertyDTO[propertyDTOsList.size()];
//        propertyDTOArray = propertyDTOsList.toArray(propertyDTOArray);
//
//        userStoreDTO.setProperties(propertyDTOArray);
//
//        return userStoreDTO;
//    }
}
