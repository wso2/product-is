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

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.automation.engine.configurations.UrlGenerationUtil;
import org.wso2.carbon.automation.engine.context.AutomationContext;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.automation.engine.context.beans.ContextUrls;
import org.wso2.carbon.automation.engine.context.beans.Instance;
import org.wso2.carbon.automation.engine.context.beans.Tenant;
import org.wso2.carbon.automation.engine.context.beans.User;
import org.wso2.carbon.automation.engine.frameworkutils.FrameworkPathUtil;
import org.wso2.carbon.automation.test.utils.common.TestConfigurationProvider;
import org.wso2.carbon.integration.common.admin.client.AuthenticatorClient;
import org.wso2.carbon.integration.common.utils.LoginLogoutClient;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import javax.xml.xpath.XPathExpressionException;
import java.io.File;

public class ISIntegrationTest {

    public static final String URL_SEPARATOR = "/";
    public static final String TENANTED_URL_PATH_SPECIFIER = "/t/";
    public static final String ORGANIZATION_PATH_SPECIFIER = "/o";
    public static final String KEYSTORE_TYPE = "PKCS12";
    public static final String KEYSTORE_NAME = "wso2carbon.p12";
    private static final String PRODUCT_GROUP_PORT_HTTPS = "https";

    protected Log log = LogFactory.getLog(getClass());
    protected AutomationContext isServer;
    protected String backendURL;
    protected String serverURL;
    protected String sessionCookie;
    protected Tenant tenantInfo;
    protected User userInfo;
    protected ContextUrls identityContextUrls;
    private static String jdbcClassName = "org.wso2.carbon.user.core.jdbc.JDBCUserStoreManager";
    protected LoginLogoutClient loginLogoutClient;

    protected void init() throws Exception {
        init(TestUserMode.SUPER_TENANT_ADMIN);
    }

    protected void init(TestUserMode userMode) throws Exception {
        isServer = new AutomationContext("IDENTITY", userMode);
        backendURL = isServer.getContextUrls().getBackEndUrl();
        serverURL = backendURL.replace("services/", "");
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
        serverURL = backendURL.replace("services/", "");
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
        serverURL = backendURL.replace("services/", "");
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

    protected File getDeploymentTomlFile(String carbonHome) {
        File deploymentToml = new File(carbonHome + File.separator + "repository" + File.separator
                + "conf" + File.separator + "deployment.toml");
        return deploymentToml;
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

        System.setProperty("javax.net.ssl.trustStore", FrameworkPathUtil.getSystemResourceLocation() + File.separator +
                "keystores" + File.separator  + "products" + File.separator + KEYSTORE_NAME);
        System.setProperty("javax.net.ssl.trustStorePassword",
                "wso2carbon");
        System.setProperty("javax.net.ssl.trustStoreType", KEYSTORE_TYPE);
    }


    /**
     * Get the qualified endpoint URL with the hostname for the given tenant.
     *
     * @param endpointURL   The endpoint URL with the hostname.
     * @param tenantDomain  Tenanted domain.
     * @return Tenant qualified URL.
     */
    public String getTenantQualifiedURL(String endpointURL, String tenantDomain) {

        try {
            if(!tenantDomain.isBlank() && !tenantDomain.equalsIgnoreCase(
                    MultitenantConstants.SUPER_TENANT_DOMAIN_NAME)) {

                String baseURL = getBaseURL();
                endpointURL = endpointURL.replace(baseURL,
                        baseURL + TENANTED_URL_PATH_SPECIFIER + tenantDomain);
            }
            return endpointURL;
        } catch (XPathExpressionException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Get the qualified endpoint URL without the hostname for the given tenant.
     *
     * @param endpointURLWithHostname   The endpoint URL without the hostname.
     * @param tenantDomain              Tenanted domain.
     * @return Tenant qualified URL without hostname.
     */
    public static String getTenantedRelativePath(String endpointURLWithHostname, String tenantDomain) {

        if(!tenantDomain.isBlank() && !tenantDomain.equalsIgnoreCase(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME)) {
            endpointURLWithHostname = TENANTED_URL_PATH_SPECIFIER + tenantDomain + endpointURLWithHostname;
        }
        return endpointURLWithHostname;
    }

    /**
     * Get the tenant qualified org URL without the hostname for the given tenant.
     *
     * @param endpointURLWithoutHostname The endpointURLWithHostname URL without the hostname.
     * @param tenantDomain               Tenanted domain.
     * @return Tenant qualified org URL without hostname.
     */
    public static String getTenantedOrgRelativePath(String endpointURLWithoutHostname, String tenantDomain) {

        if (StringUtils.isBlank(endpointURLWithoutHostname) ||
                tenantDomain.equalsIgnoreCase(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME)) {
            return ORGANIZATION_PATH_SPECIFIER + endpointURLWithoutHostname;
        }
        return TENANTED_URL_PATH_SPECIFIER + tenantDomain + ORGANIZATION_PATH_SPECIFIER + endpointURLWithoutHostname;
    }

    /**
     * Get the based URL eg: https://localhost:9443.
     *
     * @return The base URL.
     */
    private String getBaseURL() throws XPathExpressionException {

        Instance instance = isServer.getInstance();
        String httpsPort = isServer.getInstance().getPorts().get(PRODUCT_GROUP_PORT_HTTPS);
        String hostName = UrlGenerationUtil.getWorkerHost(instance);

        if(httpsPort != null) {
            return PRODUCT_GROUP_PORT_HTTPS + "://" + hostName + ":" + httpsPort;
        }
        return PRODUCT_GROUP_PORT_HTTPS + "://" + hostName;
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
