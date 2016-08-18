/*
*  Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/

package org.wso2.identity.integration.test.provisioning;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.wink.client.ClientConfig;
import org.apache.wink.client.Resource;
import org.apache.wink.client.RestClient;
import org.apache.wink.client.handlers.ClientHandler;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.annotations.ExecutionEnvironment;
import org.wso2.carbon.automation.engine.annotations.SetEnvironment;
import org.wso2.carbon.automation.engine.context.AutomationContext;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.automation.extensions.servers.carbonserver.MultipleServersManager;
import org.wso2.carbon.identity.application.common.model.idp.xsd.IdentityProvider;
import org.wso2.carbon.identity.application.common.model.idp.xsd.Property;
import org.wso2.carbon.identity.application.common.model.idp.xsd.ProvisioningConnectorConfig;
import org.wso2.carbon.identity.application.common.model.xsd.InboundProvisioningConfig;
import org.wso2.carbon.identity.application.common.model.xsd.JustInTimeProvisioningConfig;
import org.wso2.carbon.identity.application.common.model.xsd.OutboundProvisioningConfig;
import org.wso2.carbon.identity.application.common.model.xsd.ServiceProvider;
import org.wso2.carbon.integration.common.admin.client.AuthenticatorClient;
import org.wso2.carbon.user.mgt.stub.types.carbon.FlaggedName;
import org.wso2.charon.core.client.SCIMClient;
import org.wso2.charon.core.exceptions.CharonException;
import org.wso2.charon.core.objects.User;
import org.wso2.charon.core.schema.SCIMConstants;
import org.wso2.identity.integration.common.clients.Idp.IdentityProviderMgtServiceClient;
import org.wso2.identity.integration.common.clients.UserManagementClient;
import org.wso2.identity.integration.common.clients.application.mgt.ApplicationManagementServiceClient;
import org.wso2.identity.integration.common.utils.CarbonTestServerManager;
import org.wso2.identity.integration.common.utils.ISIntegrationTest;
import org.wso2.identity.integration.test.scim.utils.SCIMResponseHandler;
import org.wso2.identity.integration.test.utils.BasicAuthHandler;
import org.wso2.identity.integration.test.utils.BasicAuthInfo;
import org.wso2.identity.integration.test.utils.CommonConstants;

import javax.xml.xpath.XPathExpressionException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProvisioningTestCase extends ISIntegrationTest {

    private String servicesUrl = "https://localhost:%s/services/";
    private MultipleServersManager manager;
    private Map<Integer, UserManagementClient> userMgtServiceClients;
    private Map<Integer, IdentityProviderMgtServiceClient> identityProviderMgtServiceClients;
    private Map<Integer, ApplicationManagementServiceClient> applicationManagementServiceClients;
    private Map<Integer, AutomationContext> automationContextMap;

    private String scimUserId = null;
    String scim_url_0;
    String scim_url_1;
    String scim_url_2;
    String serviceEndPoint = null;
    String backendUrl = null;

    protected static final String userName = "testProvisioningUser1";
    protected static final String userName2 = "testProvisioningUser2";
    private static final String externalID = "test";
    private static final String[] emails = {"testProvisioningUser@gmail.com", "testProvisioningUser@wso2.com"};
    private static final String displayName = "dharshana";
    private static final String password = "testPW";
    private static final String language = "Englinsh";
    private static final String phone_number = "0112145300";

    public static final String SAMPLE_IDENTITY_PROVIDER_NAME = "sample";
    public static final String PORT_OFFSET_PARAM = "-DportOffset";

    public static final int DEFAULT_PORT = 9853;
    public static final int adminUserId = 0;
    public static final int PORT_OFFSET_0 = 0;
    public static final int PORT_OFFSET_1 = 1;
    public static final int PORT_OFFSET_2 = 2;

    //protected EnvironmentVariables isServer;
    protected SCIMClient scimClient;

    @BeforeClass(alwaysRun = true)
    public void testInit() throws Exception {
        super.init();

        userMgtServiceClients = new HashMap<Integer, UserManagementClient>();
        identityProviderMgtServiceClients = new HashMap<Integer, IdentityProviderMgtServiceClient>();
        applicationManagementServiceClients = new HashMap<Integer, ApplicationManagementServiceClient>();
        automationContextMap = new HashMap<Integer, AutomationContext>();
        manager = new MultipleServersManager();

        automationContextMap.put(PORT_OFFSET_0, isServer);

        startOtherCarbonServers();

        createServiceClientsForServers(sessionCookie, PORT_OFFSET_0, new CommonConstants.AdminClients[]{
                CommonConstants.AdminClients.APPLICATION_MANAGEMENT_SERVICE_CLIENT, CommonConstants.AdminClients.IDENTITY_PROVIDER_MGT_SERVICE_CLIENT });

        createServiceClientsForServers(null, PORT_OFFSET_1, new CommonConstants.AdminClients[]{
                CommonConstants.AdminClients.APPLICATION_MANAGEMENT_SERVICE_CLIENT, CommonConstants.AdminClients.IDENTITY_PROVIDER_MGT_SERVICE_CLIENT,
                CommonConstants.AdminClients.USER_MANAGEMENT_CLIENT });

        createServiceClientsForServers(null, PORT_OFFSET_2, new CommonConstants.AdminClients[]{ CommonConstants.AdminClients.USER_MANAGEMENT_CLIENT });

        // TODO: port offset will no longer needed if TAF 4.3.1 issue get fixed
        scim_url_0 = getSCIMUrl(PORT_OFFSET_0, automationContextMap.get(PORT_OFFSET_0).getContextUrls().getSecureServiceUrl());
        scim_url_1 = getSCIMUrl(PORT_OFFSET_1, automationContextMap.get(PORT_OFFSET_1).getContextUrls().getSecureServiceUrl());
        scim_url_2 = getSCIMUrl(PORT_OFFSET_2, automationContextMap.get(PORT_OFFSET_2).getContextUrls().getSecureServiceUrl());
    }

    @AfterClass(alwaysRun = true)
    public void atEnd() throws Exception {

        ServiceProvider serviceProvider = applicationManagementServiceClients.get(PORT_OFFSET_0)
                .getApplication("wso2carbon-local-sp");
        if (serviceProvider != null) {
            serviceProvider.setOutboundProvisioningConfig(new OutboundProvisioningConfig());
            applicationManagementServiceClients.get(PORT_OFFSET_0).updateApplicationData(serviceProvider);

            IdentityProviderMgtServiceClient identityProviderMgtServiceClient = identityProviderMgtServiceClients.get
                    (PORT_OFFSET_0);
            if (identityProviderMgtServiceClient != null) {
                identityProviderMgtServiceClient.deleteIdP("sample");
            }
        }

        manager.stopAllServers();

    }


    @Test(alwaysRun = true, description = "Add SCIM Provisioning user", priority = 1)
    @SetEnvironment(executionEnvironments = {ExecutionEnvironment.STANDALONE})
    public void createUser() throws Exception {

        buildSCIMProvisioningConnector(PORT_OFFSET_0);
        addSP(PORT_OFFSET_0);
        scimClient = new SCIMClient();

        String encodedUser = getScimUser(1);
        //create a apache wink ClientHandler to intercept and identify response messages
        Resource userResource = getResource(scimClient, scim_url_0);
        BasicAuthInfo encodedBasicAuthInfo = getBasicAuthInfo(automationContextMap.get(PORT_OFFSET_0));
        String response = userResource.
                header(SCIMConstants.AUTHORIZATION_HEADER, encodedBasicAuthInfo.getAuthorizationHeader()).
                contentType(SCIMConstants.APPLICATION_JSON).accept(SCIMConstants.APPLICATION_JSON).
                post(String.class, encodedUser);
        log.info(response);
        scimUserId = response.split(",")[0].split(":")[1].replace('"', ' ').trim();
        // userMgtServiceClients.get(PORT_OFFSET_2).listUsers(userName, 100);

        Assert.assertNotNull(scimUserId);
        Assert.assertTrue(isUserExists(userName));
    }

    @Test(alwaysRun = true, description = "Add SCIM provisioning user on second server",
          dependsOnMethods = "createUser")
    @SetEnvironment(executionEnvironments = {ExecutionEnvironment.ALL})
    public void createUserForSecondServer() throws Exception {

        buildSCIMProvisioningConnector(PORT_OFFSET_1);
        addSP(PORT_OFFSET_1);

        scimClient = new SCIMClient();

        String encodedUser = getScimUser(2);
        //create a apache wink ClientHandler to intercept and identify response messages
        Resource userResource = getResource(scimClient, scim_url_1);
        BasicAuthInfo encodedBasicAuthInfo = getBasicAuthInfo(automationContextMap.get(PORT_OFFSET_0));

        String response = userResource.
                header(SCIMConstants.AUTHORIZATION_HEADER, encodedBasicAuthInfo.getAuthorizationHeader()).
                contentType(SCIMConstants.APPLICATION_JSON).accept(SCIMConstants.APPLICATION_JSON).
                post(String.class, encodedUser);
        Assert.assertTrue(response.contains(userName2));
    }

    private void buildSCIMProvisioningConnector(int portOffset) throws Exception {

        String url = "TEST";

        if (portOffset != PORT_OFFSET_1) {
            url = scim_url_2;
        }

        IdentityProvider fedIdp = new IdentityProvider();
        fedIdp.setIdentityProviderName(SAMPLE_IDENTITY_PROVIDER_NAME);

        ProvisioningConnectorConfig proConnector = new ProvisioningConnectorConfig();
        proConnector.setName("scim");
        proConnector.setEnabled(true);
        fedIdp.setDefaultProvisioningConnectorConfig(proConnector);

        Property userNameProp = new Property();
        userNameProp.setName("scim-username");
        userNameProp.setValue("admin");

        Property passwordProp = new Property();
        passwordProp.setConfidential(true);
        passwordProp.setName("scim-password");
        passwordProp.setValue("admin");


        Property userEpProp = new Property();
        userEpProp.setName("scim-user-ep");
        userEpProp.setValue(url + "Users");

        Property groupEpProp = new Property();
        groupEpProp.setName("scim-group-ep");
        groupEpProp.setValue(url + "Groups");

        Property generatePassword = new Property();
        generatePassword.setName("scim-enable-pwd-provisioning");
        generatePassword.setValue("true");

        Property[] proProperties = new Property[]{userNameProp, passwordProp, userEpProp,
                groupEpProp, generatePassword, null};
        proConnector.setProvisioningProperties(proProperties);
        fedIdp.setProvisioningConnectorConfigs(new ProvisioningConnectorConfig[]{proConnector});

        try {
            identityProviderMgtServiceClients.get(portOffset).addIdP(fedIdp);
            IdentityProvider addedIdp = identityProviderMgtServiceClients.get(portOffset).getIdPByName(SAMPLE_IDENTITY_PROVIDER_NAME);
            Assert.assertNotNull(addedIdp, "Failed to create Identity Provider ");
            identityProviderMgtServiceClients.get(portOffset).updateIdP(SAMPLE_IDENTITY_PROVIDER_NAME,addedIdp);

        } catch (Exception ex) {
            //  log.error("Error occurred during handling identityProviderMgtServiceClient", ex);
            throw new Exception("Error occurred during handling identityProviderMgtServiceClient", ex);
        }

    }


    private void addSP(int portOffset) throws Exception {

        ServiceProvider serviceProvider = applicationManagementServiceClients.get(portOffset)
                .getApplication("wso2carbon-local-sp");

        if (serviceProvider == null) {
            serviceProvider = new ServiceProvider();
            serviceProvider.setApplicationName("wso2carbon-local-sp");
            try {
                applicationManagementServiceClients.get(portOffset).createApplication(serviceProvider);
                serviceProvider = applicationManagementServiceClients.get(portOffset).getApplication
                        ("wso2carbon-local-sp");
            } catch (Exception ex) {
                // log.error("Error occurred during obtaining applicationManagementServiceClients", ex);
                throw new Exception("Error occurred during obtaining applicationManagementServiceClients", ex);
            }

        }

        InboundProvisioningConfig inBoundProConfig = new InboundProvisioningConfig();
        inBoundProConfig.setProvisioningUserStore("");
        serviceProvider.setInboundProvisioningConfig(inBoundProConfig);

        String proProvider = "sample";
        String connector = "scim";
        JustInTimeProvisioningConfig jitpro = new JustInTimeProvisioningConfig();
        jitpro.setProvisioningEnabled(false);


        org.wso2.carbon.identity.application.common.model.xsd.IdentityProvider proIdp = new
                org.wso2.carbon.identity.application.common.model.xsd.IdentityProvider();
        proIdp.setIdentityProviderName(proProvider);
        org.wso2.carbon.identity.application.common.model.xsd.ProvisioningConnectorConfig proCon =
                new org.wso2.carbon.identity.application.common.model.xsd.ProvisioningConnectorConfig();
        proCon.setBlocking(true);
        proCon.setName(connector);
        proIdp.setJustInTimeProvisioningConfig(jitpro);
        proIdp.setDefaultProvisioningConnectorConfig(proCon);


        List<org.wso2.carbon.identity.application.common.model.xsd.IdentityProvider> provisioningIdps
                = new ArrayList<org.wso2.carbon.identity.application.common.model.xsd.IdentityProvider>();
        provisioningIdps.add(proIdp);
        if (provisioningIdps.size() > 0) {
            OutboundProvisioningConfig outboundProConfig = new OutboundProvisioningConfig();
            outboundProConfig.setProvisioningIdentityProviders(provisioningIdps.toArray(
                    new org.wso2.carbon.identity.application.common.model.xsd.IdentityProvider[provisioningIdps.size()]));
            serviceProvider.setOutboundProvisioningConfig(outboundProConfig);
        }
        applicationManagementServiceClients.get(portOffset).updateApplicationData(serviceProvider);
    }


    private void createServiceClientsForServers(String sessionCookie, int portOffset,
                                               CommonConstants.AdminClients [] adminClients) throws Exception {

        if (adminClients == null) {
            return;
        }

        //TODO: Need to remove getSecureServiceUrl method when server start issue got fixed / TAF 4.3.1
        String serviceUrl = getSecureServiceUrl(portOffset,
                                                automationContextMap.get(portOffset).getContextUrls()
                                                        .getSecureServiceUrl());

        if (sessionCookie == null) {

            AuthenticatorClient authenticatorClient = new AuthenticatorClient(serviceUrl);

            sessionCookie = authenticatorClient.login(automationContextMap.get(portOffset).getSuperTenant()
                    .getTenantAdmin().getUserName(), automationContextMap.get(portOffset).getSuperTenant()
                    .getTenantAdmin().getPassword(), automationContextMap.get(portOffset).getDefaultInstance()
                    .getHosts().get("default"));

            if (sessionCookie == null) {
                throw new Exception("Unable to login to the server instance : " + automationContextMap.get
                        (portOffset).getInstance().getName());
            }
        }

        ConfigurationContext configContext = ConfigurationContextFactory.createConfigurationContextFromFileSystem
                (null, null);

        for (CommonConstants.AdminClients client : adminClients) {
            if (CommonConstants.AdminClients.APPLICATION_MANAGEMENT_SERVICE_CLIENT.equals(client)) {

                applicationManagementServiceClients.put(portOffset, new ApplicationManagementServiceClient
                        (sessionCookie, serviceUrl,configContext));

            } else if (CommonConstants.AdminClients.IDENTITY_PROVIDER_MGT_SERVICE_CLIENT.equals(client)) {

                identityProviderMgtServiceClients.put(portOffset, new IdentityProviderMgtServiceClient(sessionCookie,
                                                                                                       serviceUrl));

            } else if (CommonConstants.AdminClients.USER_MANAGEMENT_CLIENT.equals(client)) {

                userMgtServiceClients.put(portOffset, new UserManagementClient(serviceUrl, sessionCookie));
            }
        }

    }


    private String getScimUser(int type) throws CharonException {
        //create a user according to SCIM User Schema
        User scimUser = scimClient.createUser();

        if (type == 1) {
            scimUser.setUserName(userName);
        } else {
            scimUser.setUserName(userName2);
        }
        scimUser.setExternalId(externalID);
        scimUser.setEmails(emails);
        scimUser.setDisplayName(displayName);
        scimUser.setPassword(password);
        scimUser.setPreferredLanguage(language);
        scimUser.setPhoneNumber(phone_number, null, false);
        //encode the user in JSON format
        return scimClient.encodeSCIMObject(scimUser, SCIMConstants.JSON);
    }

    private Resource getResource(SCIMClient scimClient, String scim_url) {
        SCIMResponseHandler responseHandler = new SCIMResponseHandler();
        responseHandler.setSCIMClient(scimClient);
        //set the handler in wink client config
        ClientConfig clientConfig = new ClientConfig();
        clientConfig.handlers(new ClientHandler[]{responseHandler});
        //create a wink rest client with the above config
        RestClient restClient = new RestClient(clientConfig);
        //create resource endpoint to access User resource
        return restClient.resource(scim_url + "Users");
    }

    private BasicAuthInfo getBasicAuthInfo(AutomationContext context) throws XPathExpressionException {
        BasicAuthInfo basicAuthInfo = new BasicAuthInfo();
        basicAuthInfo.setUserName(context.getSuperTenant().getTenantAdmin().getUserName());
        basicAuthInfo.setPassword(context.getSuperTenant().getTenantAdmin().getPassword());

        BasicAuthHandler basicAuthHandler = new BasicAuthHandler();
        return (BasicAuthInfo) basicAuthHandler.getAuthenticationToken(basicAuthInfo);
    }

    private boolean isUserExists(String userName) throws Exception {
        FlaggedName[] nameList = userMgtServiceClients.get(PORT_OFFSET_2).listAllUsers(userName, 100);
        for (FlaggedName name : nameList) {
            if (name.getItemName().contains(userName)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Start additional carbon servers
     *
     * @throws Exception
     */
    private void startOtherCarbonServers() throws Exception {

        Map<String, String> startupParameterMap1 = new HashMap<String, String>();
        startupParameterMap1.put(PORT_OFFSET_PARAM, String.valueOf(CommonConstants.IS_DEFAULT_OFFSET + PORT_OFFSET_1));

        AutomationContext context1 = new AutomationContext("IDENTITY", "identity002", TestUserMode.SUPER_TENANT_ADMIN);
        automationContextMap.put(PORT_OFFSET_1, context1);

        CarbonTestServerManager server1 = new CarbonTestServerManager(context1, System.getProperty("carbon.zip"),
                                                                      startupParameterMap1);

        Map<String, String> startupParameterMap2 = new HashMap<String, String>();
        startupParameterMap2.put(PORT_OFFSET_PARAM, String.valueOf(CommonConstants.IS_DEFAULT_OFFSET + PORT_OFFSET_2));

        AutomationContext context2 = new AutomationContext("IDENTITY", "identity003", TestUserMode.SUPER_TENANT_ADMIN);
        automationContextMap.put(PORT_OFFSET_2, context2);

        CarbonTestServerManager server2 = new CarbonTestServerManager(context2, System.getProperty("carbon.zip"),
                                                                      startupParameterMap2);

        manager.startServers(server1, server2);
    }

    //TODO: Need to remove

    private String getSecureServiceUrl(int portOffset, String baseUrl) {
        return baseUrl.replace("9853", String.valueOf(DEFAULT_PORT + portOffset)) + "/";
    }

    private String getSCIMUrl(int portOffset, String baseUrl) {
        return baseUrl.replace("9853/services", String.valueOf(DEFAULT_PORT + portOffset)) + "/wso2/scim/";
    }
}
