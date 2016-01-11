/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.identity.ui.integration.test.tenant.mgt;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.frameworkutils.FrameworkPathUtil;
import org.wso2.carbon.automation.extensions.selenium.BrowserManager;
import org.wso2.carbon.identity.application.common.model.xsd.InboundAuthenticationConfig;
import org.wso2.carbon.identity.application.common.model.xsd.InboundAuthenticationRequestConfig;
import org.wso2.carbon.identity.application.common.model.xsd.Property;
import org.wso2.carbon.identity.application.common.model.xsd.ServiceProvider;
import org.wso2.carbon.identity.sso.saml.stub.types.SAMLSSOServiceProviderDTO;
import org.wso2.carbon.integration.common.utils.mgt.ServerConfigurationManager;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.identity.integration.common.clients.TenantManagementServiceClient;
import org.wso2.identity.integration.common.clients.application.mgt.ApplicationManagementServiceClient;
import org.wso2.identity.integration.common.clients.sso.saml.SAMLSSOConfigServiceClient;
import org.wso2.identity.ui.integration.test.login.ISLoginTestCase;

import java.io.File;
import java.lang.String;
import java.nio.file.Paths;
import java.util.List;

public class TenantDropDownTestCase extends ISLoginTestCase {
    private static final String USERNAME = "admin";
    private static final String PASSWORD = "password";
    private static final String EMAIL = "admin@dropdown.com";
    private static final String FIRST_NAME = "Dropdown";
    private static final String LAST_NAME = "User";
    private WebDriver driver;
    private SAMLSSOConfigServiceClient ssoConfigServiceClient;
    private static final String ATTRIBUTE_CS_INDEX_VALUE = "1239245949";
    private static final String ATTRIBUTE_CS_INDEX_NAME = "attrConsumServiceIndex";
    private static final String NAMEID_FORMAT =
            "urn:oasis:names:tc:SAML:1.1:nameid-format:emailAddress";
    private static final String ACS_URL = "http://localhost:8490/%s/home.jsp";
    private static final String LOGIN_URL = "/carbon/admin/login.jsp";
    private File applicationAuthenticationXml;
    private File authenticatorsXml;
    private File catalinaServerXml;
    private File endpointConfigProperties;
    private ServerConfigurationManager userIdentityMgt;
    private static final String APPLICATION_NAME = "SAML-SSO-TestApplication";
    private static final String INBOUND_AUTH_TYPE = "samlsso";
    private ApplicationManagementServiceClient applicationManagementServiceClient;
    private TenantManagementServiceClient tenantServiceClient;
    private String TENANT_DOMAIN = "dropdown.com";
    private String SAML_ISSUER = "travelocity.com";

    @BeforeClass(alwaysRun = true)
    public void setUp() throws Exception {
        super.init();

        applicationAuthenticationXml = Paths.get(CarbonUtils.getCarbonHome(), "repository", "conf", "identity",
                "application-authentication.xml").toFile();
        File applicationAuthenticationXmlToCopy = Paths.get(FrameworkPathUtil.getSystemResourceLocation(),
                "artifacts","IS","tenantDropdown","application-authentication-tanantDropdown-enabled.xml").toFile();

        authenticatorsXml = Paths.get(CarbonUtils.getCarbonHome(), "repository", "conf", "security", "authenticators" +
                ".xml").toFile();
        File authenticatorsXmlToCopy = Paths.get(FrameworkPathUtil.getSystemResourceLocation(),"artifacts", "IS",
                "tenantDropdown", "authenticators-tenantDropdown-enabled.xml").toFile();

        catalinaServerXml = Paths.get(CarbonUtils.getCarbonHome(), "repository", "conf", "tomcat", "catalina-server" +
                ".xml").toFile();
        File catalinaServerXmlToCopy = Paths.get(FrameworkPathUtil.getSystemResourceLocation(),"artifacts", "IS",
                "tenantDropdown", "catalina-server-tanantDropdown-enabled.xml").toFile();

        endpointConfigProperties = Paths.get(CarbonUtils.getCarbonHome(), "repository", "conf", "identity",
                "EndpointConfig.properties").toFile();
        File endpointConfigPropertiesToCopy = Paths.get(FrameworkPathUtil.getSystemResourceLocation(),"artifacts",
                "IS", "tenantDropdown", "EndpointConfigTenantDropdownEnabled.properties").toFile();

        userIdentityMgt = new ServerConfigurationManager(isServer);
        userIdentityMgt.applyConfigurationWithoutRestart(applicationAuthenticationXmlToCopy,
                applicationAuthenticationXml, true);
        userIdentityMgt.applyConfigurationWithoutRestart(authenticatorsXmlToCopy, authenticatorsXml, true);
        userIdentityMgt.applyConfigurationWithoutRestart(catalinaServerXmlToCopy, catalinaServerXml, true);
        userIdentityMgt.applyConfigurationWithoutRestart(endpointConfigPropertiesToCopy, endpointConfigProperties,
                true);
        userIdentityMgt.restartGracefully();

        super.init();

        ConfigurationContext configContext = ConfigurationContextFactory.createConfigurationContextFromFileSystem
                (null, null);

        driver = BrowserManager.getWebDriver();

        tenantServiceClient = new TenantManagementServiceClient(isServer.getContextUrls().getBackEndUrl(),
                sessionCookie);
        tenantServiceClient.addTenant(TENANT_DOMAIN, USERNAME, PASSWORD, EMAIL, FIRST_NAME, LAST_NAME);

        ssoConfigServiceClient = new SAMLSSOConfigServiceClient(backendURL, sessionCookie);
        applicationManagementServiceClient =
                new ApplicationManagementServiceClient(sessionCookie, backendURL, configContext);
        ssoConfigServiceClient.addServiceProvider(createSsoServiceProviderDTO());
        createApplication();
        driver.get(isServer.getContextUrls().getWebAppURLHttps() + "/samlsso?spEntityID=" + SAML_ISSUER);
    }

    @Test(groups = "wso2.identity", description = "verify login to IS Server")
    public void testLogin() throws Exception {
        WebElement tenantList = driver.findElement(By.id("tenantList"));
        Select select = new Select(tenantList);
        List<WebElement> allOptions = select.getOptions();

        boolean tenantFound = false;
        for (WebElement option : allOptions) {
            if (option != null && TENANT_DOMAIN.equals(option.getText())) {
                tenantFound = true;
                break;
            }
        }
        Assert.assertTrue(tenantFound);
        driver.close();
    }

    @AfterClass(alwaysRun = true)
    public void tearDown() throws Exception {

        driver.quit();

        ssoConfigServiceClient.removeServiceProvider(SAML_ISSUER);
        applicationManagementServiceClient.deleteApplication(APPLICATION_NAME);

        File applicationAuthenticationXmlToCopy = Paths.get(FrameworkPathUtil.getSystemResourceLocation(),
                "artifacts", "IS", "tenantDropdown", "application-authentication-default.xml").toFile();

        File authenticatorsXmlToCopy = Paths.get(FrameworkPathUtil.getSystemResourceLocation(), "artifacts", "IS",
                "tenantDropdown", "authenticators-default.xml").toFile();

        File catalinaServerXmlToCopy = Paths.get(FrameworkPathUtil.getSystemResourceLocation(), "artifacts", "IS",
                "tenantDropdown", "catalina-server-default.xml").toFile();

        File endpointConfigPropertiesToCopy = Paths.get(FrameworkPathUtil.getSystemResourceLocation(), "artifacts",
                "IS", "tenantDropdown", "EndpointConfigDefault.properties").toFile();

        userIdentityMgt.applyConfigurationWithoutRestart(applicationAuthenticationXmlToCopy,
                applicationAuthenticationXml, true);
        userIdentityMgt.applyConfigurationWithoutRestart(authenticatorsXmlToCopy, authenticatorsXml, true);
        userIdentityMgt.applyConfigurationWithoutRestart(catalinaServerXmlToCopy, catalinaServerXml, true);
        userIdentityMgt.applyConfigurationWithoutRestart(endpointConfigPropertiesToCopy, endpointConfigProperties,
                true);
        userIdentityMgt.restartGracefully();
    }

    private SAMLSSOServiceProviderDTO createSsoServiceProviderDTO() {
        SAMLSSOServiceProviderDTO samlssoServiceProviderDTO = new SAMLSSOServiceProviderDTO();
        samlssoServiceProviderDTO.setIssuer(SAML_ISSUER);
        samlssoServiceProviderDTO.setAssertionConsumerUrls(new String[]{String.format(ACS_URL,
                SAML_ISSUER)});
        samlssoServiceProviderDTO.setDefaultAssertionConsumerUrl(String.format(ACS_URL, SAML_ISSUER));
        samlssoServiceProviderDTO.setAttributeConsumingServiceIndex(ATTRIBUTE_CS_INDEX_VALUE);
        samlssoServiceProviderDTO.setNameIDFormat(NAMEID_FORMAT);
        samlssoServiceProviderDTO.setDoSignAssertions(false);
        samlssoServiceProviderDTO.setDoSignResponse(false);
        samlssoServiceProviderDTO.setDoSingleLogout(true);
        samlssoServiceProviderDTO.setLoginPageURL(LOGIN_URL);


        return samlssoServiceProviderDTO;
    }

    private void createApplication() throws Exception {
        ServiceProvider serviceProvider = new ServiceProvider();
        serviceProvider.setApplicationName(APPLICATION_NAME);
        serviceProvider.setDescription("This is a test Service Provider");
        applicationManagementServiceClient.createApplication(serviceProvider);

        serviceProvider = applicationManagementServiceClient.getApplication(APPLICATION_NAME);

        InboundAuthenticationRequestConfig requestConfig = new InboundAuthenticationRequestConfig();
        requestConfig.setInboundAuthType(INBOUND_AUTH_TYPE);
        requestConfig.setInboundAuthKey(SAML_ISSUER);

        Property attributeConsumerServiceIndexProp = new Property();
        attributeConsumerServiceIndexProp.setName(ATTRIBUTE_CS_INDEX_NAME);
        attributeConsumerServiceIndexProp.setValue(ATTRIBUTE_CS_INDEX_VALUE);
        requestConfig.setProperties(new Property[]{attributeConsumerServiceIndexProp});

        InboundAuthenticationConfig inboundAuthenticationConfig = new InboundAuthenticationConfig();
        inboundAuthenticationConfig.setInboundAuthenticationRequestConfigs(
                new InboundAuthenticationRequestConfig[]{requestConfig});

        serviceProvider.setInboundAuthenticationConfig(inboundAuthenticationConfig);
        serviceProvider.setSaasApp(true);
        applicationManagementServiceClient.updateApplicationData(serviceProvider);
    }
}
