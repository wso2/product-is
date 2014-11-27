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

package org.wso2.carbon.identity.tests.provisioning;

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
import org.wso2.carbon.authenticator.stub.LoginAuthenticationExceptionException;
import org.wso2.carbon.automation.api.clients.authenticators.AuthenticatorClient;
import org.wso2.carbon.automation.api.clients.identity.Idp.IdentityProviderMgtServiceClient;
import org.wso2.carbon.automation.api.clients.identity.application.mgt.ApplicationManagementServiceClient;
import org.wso2.carbon.automation.api.clients.user.mgt.UserManagementClient;
import org.wso2.carbon.automation.core.annotations.ExecutionEnvironment;
import org.wso2.carbon.automation.core.annotations.SetEnvironment;
import org.wso2.carbon.automation.core.environmentcontext.ContextProvider;
import org.wso2.carbon.automation.core.environmentcontext.GroupContextProvider;
import org.wso2.carbon.automation.core.environmentcontext.environmentvariables.EnvironmentContext;
import org.wso2.carbon.automation.core.environmentcontext.environmentvariables.GroupContext;
import org.wso2.carbon.automation.core.utils.UserInfo;
import org.wso2.carbon.automation.core.utils.UserListCsvReader;
import org.wso2.carbon.automation.core.utils.environmentutils.EnvironmentBuilder;
import org.wso2.carbon.automation.core.utils.environmentutils.EnvironmentVariables;
import org.wso2.carbon.identity.application.common.model.idp.xsd.IdentityProvider;
import org.wso2.carbon.identity.application.common.model.idp.xsd.Property;
import org.wso2.carbon.identity.application.common.model.idp.xsd.ProvisioningConnectorConfig;
import org.wso2.carbon.identity.application.common.model.xsd.InboundProvisioningConfig;
import org.wso2.carbon.identity.application.common.model.xsd.JustInTimeProvisioningConfig;
import org.wso2.carbon.identity.application.common.model.xsd.OutboundProvisioningConfig;
import org.wso2.carbon.identity.application.common.model.xsd.ServiceProvider;
import org.wso2.carbon.identity.tests.ISIntegrationTest;
import org.wso2.carbon.identity.tests.scim.utils.SCIMResponseHandler;
import org.wso2.carbon.identity.tests.utils.BasicAuthHandler;
import org.wso2.carbon.identity.tests.utils.BasicAuthInfo;
import org.wso2.carbon.identity.tests.utils.CarbonTestServerManager;
import org.wso2.carbon.user.mgt.stub.types.carbon.FlaggedName;
import org.wso2.charon.core.client.SCIMClient;
import org.wso2.charon.core.exceptions.CharonException;
import org.wso2.charon.core.objects.User;
import org.wso2.charon.core.schema.SCIMConstants;

import java.io.IOException;
import java.rmi.RemoteException;
import java.util.*;

public class ProvisioningTestCase extends ISIntegrationTest {

    private String servicesUrl = "https://localhost:%s/services/";


    private Map<Integer, CarbonTestServerManager> serverManagers;
    private Map<Integer, UserManagementClient> userMgtServiceClients;
    private Map<Integer, IdentityProviderMgtServiceClient> identityProviderMgtServiceClients;
    private Map<Integer, ApplicationManagementServiceClient> applicationManagementServiceClients;

    private String scimUserId = null;
    private UserInfo provider_userInfo;
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

    public static final int DEFAULT_PORT = 9443;
    public static final int adminUserId = 0;
    public static final int PORT_OFFSET_0 = 0;
    public static final int PORT_OFFSET_1 = 1;
    public static final int PORT_OFFSET_2 = 2;

    protected EnvironmentVariables isServer;
    protected SCIMClient scimClient;

    @BeforeClass(alwaysRun = true)
    public void testInit() throws Exception {
        //super.init(0);
        userMgtServiceClients = new HashMap<Integer, UserManagementClient>();
        identityProviderMgtServiceClients = new HashMap<Integer, IdentityProviderMgtServiceClient>();
        applicationManagementServiceClients = new HashMap<Integer, ApplicationManagementServiceClient>();

        EnvironmentBuilder builder = new EnvironmentBuilder().is(0);
        isServer = builder.build().getIs();
        startOtherCarbonServers();

        createServiceClientsForServerOne();
        createServiceClientsForServerTwo();
        createServiceClientsForServerThree();


        provider_userInfo = UserListCsvReader.getUserInfo(adminUserId);
        GroupContextProvider consumerGroupContext = new GroupContextProvider();
        GroupContext consumerGroup = consumerGroupContext.getGroupContext("node1");

        ContextProvider consumer = new ContextProvider();
        EnvironmentContext consumerNodeContext = consumer.getNodeContext(consumerGroup.getNode().
                getNodeId(), adminUserId);
        backendUrl = consumerNodeContext.getBackEndUrl();
        int port = Integer.parseInt(consumerNodeContext.getWorkerVariables().getHttpsPort());
        scim_url_0 = "https://" + consumerNodeContext.getWorkerVariables().getHostName() + ":" +
                String.valueOf(port + PORT_OFFSET_0) + "/wso2/scim/";
        scim_url_1 = "https://" + consumerNodeContext.getWorkerVariables().getHostName() + ":" +
                String.valueOf(port + PORT_OFFSET_1) + "/wso2/scim/";
        scim_url_2 = "https://" + consumerNodeContext.getWorkerVariables().getHostName() + ":" +
                String.valueOf(port + PORT_OFFSET_2) + "/wso2/scim/";
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

        Iterator it = serverManagers.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pairs = (Map.Entry) it.next();
            CarbonTestServerManager serverManager1 = (CarbonTestServerManager) pairs.getValue();
            serverManager1.stopServer();
            Thread.sleep(1000);
            it.remove(); // avoids a ConcurrentModificationException
        }

        serverManagers.clear();
    }


    @Test(alwaysRun = true, description = "Add SCIM Provisioning user", priority = 1)
    @SetEnvironment(executionEnvironments = {ExecutionEnvironment.integration_all})
    public void createUser() throws Exception {

        buildSCIMProvisioningConnector(PORT_OFFSET_0);
        addSP(PORT_OFFSET_0);
        scimClient = new SCIMClient();

        String encodedUser = getScimUser(1);
        //create a apache wink ClientHandler to intercept and identify response messages
        Resource userResource = getResource(scimClient, scim_url_0);
        BasicAuthInfo encodedBasicAuthInfo = getBasicAuthInfo(provider_userInfo);
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
            expectedExceptions = org.apache.wink.client.ClientRuntimeException.class,
            dependsOnMethods = "createUser")
    @SetEnvironment(executionEnvironments = {ExecutionEnvironment.integration_all})
    public void createUserForSecondServer() throws Exception {

        buildSCIMProvisioningConnector(PORT_OFFSET_1);
        addSP(PORT_OFFSET_1);

        scimClient = new SCIMClient();

        String encodedUser = getScimUser(2);
        //create a apache wink ClientHandler to intercept and identify response messages
        Resource userResource = getResource(scimClient, scim_url_1);
        BasicAuthInfo encodedBasicAuthInfo = getBasicAuthInfo(provider_userInfo);

        String response = userResource.
                header(SCIMConstants.AUTHORIZATION_HEADER, encodedBasicAuthInfo.getAuthorizationHeader()).
                contentType(SCIMConstants.APPLICATION_JSON).accept(SCIMConstants.APPLICATION_JSON).
                post(String.class, encodedUser);
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

        Property[] proProperties = new Property[]{userNameProp, passwordProp, userEpProp,
                groupEpProp, null};
        proConnector.setProvisioningProperties(proProperties);
        fedIdp.setProvisioningConnectorConfigs(new ProvisioningConnectorConfig[]{proConnector});

        try {
            identityProviderMgtServiceClients.get(portOffset).addIdP(fedIdp);
            Assert.assertNotNull(identityProviderMgtServiceClients.get(portOffset).getIdPByName(
                    SAMPLE_IDENTITY_PROVIDER_NAME), "Failed to create Identity Provider ");
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


    private void createServiceClientsForServerOne() throws Exception {

        try {
            String sessionId = isServer.getSessionCookie();
            if (sessionId == null || sessionId.isEmpty()) {
                AuthenticatorClient authenticatorClient = new AuthenticatorClient(String.format
                        (servicesUrl, DEFAULT_PORT + PORT_OFFSET_0));
                sessionId = authenticatorClient.login("admin", "admin", null);
            }

            ConfigurationContext configContext = ConfigurationContextFactory.
                    createConfigurationContextFromFileSystem(null, null);

            String backendURL = String.format(servicesUrl,
                    DEFAULT_PORT +
                            PORT_OFFSET_0);
            IdentityProviderMgtServiceClient identityProviderMgtServiceClient = new
                    IdentityProviderMgtServiceClient(sessionId, backendURL, configContext);
            identityProviderMgtServiceClients.put(PORT_OFFSET_0, identityProviderMgtServiceClient);

            ApplicationManagementServiceClient applicationManagementServiceClient = new ApplicationManagementServiceClient(
                    sessionId, String.format(servicesUrl, DEFAULT_PORT +
                    PORT_OFFSET_0), configContext);
            applicationManagementServiceClients.put(PORT_OFFSET_0, applicationManagementServiceClient);
        } catch (LoginAuthenticationExceptionException e) {
            throw new Exception("Login Failed", e);
        } catch (RemoteException e) {
            throw new Exception("RMI Invocation Failed", e);
        }

    }

    private void createServiceClientsForServerTwo() throws Exception {

        try {
            AuthenticatorClient authenticatorClient = new AuthenticatorClient(String.format
                    (servicesUrl, DEFAULT_PORT + PORT_OFFSET_1));
            String sessionId = authenticatorClient.login("admin", "admin", null);


            ConfigurationContext configContext = ConfigurationContextFactory.createConfigurationContextFromFileSystem(null, null);
            IdentityProviderMgtServiceClient identityProviderMgtServiceClient = new
                    IdentityProviderMgtServiceClient(sessionId, String.format(servicesUrl,
                    DEFAULT_PORT + PORT_OFFSET_1), configContext);

            identityProviderMgtServiceClients.put(PORT_OFFSET_1,
                    identityProviderMgtServiceClient);

            applicationManagementServiceClients.put(PORT_OFFSET_1, new
                    ApplicationManagementServiceClient(sessionId, String.format(servicesUrl,
                    DEFAULT_PORT + PORT_OFFSET_1), configContext));

            userMgtServiceClients.put(PORT_OFFSET_1, new UserManagementClient(
                    String.format(servicesUrl, DEFAULT_PORT +
                            PORT_OFFSET_1), sessionId));
        } catch (RemoteException ex) {
            throw new Exception("Remote Invocation Failed", ex);
        } catch (LoginAuthenticationExceptionException ex) {
            throw new Exception("Login Failed", ex);
        }
    }

    private void createServiceClientsForServerThree() throws Exception {

        try {
            AuthenticatorClient authenticatorClient = new AuthenticatorClient(String.format
                    (servicesUrl, DEFAULT_PORT + PORT_OFFSET_2));

            String sessionId = authenticatorClient.login("admin", "admin", null);

            userMgtServiceClients.put(PORT_OFFSET_2,
                    new UserManagementClient(String.format(servicesUrl,
                            DEFAULT_PORT + PORT_OFFSET_2)
                            , sessionId));
        } catch (RemoteException ex) {
            throw new Exception("Remote Invocation Failed", ex);
        } catch (LoginAuthenticationExceptionException ex) {
            throw new Exception("Login Failed", ex);
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

    private Resource getResource(SCIMClient scimClient, String skim_url) {
        SCIMResponseHandler responseHandler = new SCIMResponseHandler();
        responseHandler.setSCIMClient(scimClient);
        //set the handler in wink client config
        ClientConfig clientConfig = new ClientConfig();
        clientConfig.handlers(new ClientHandler[]{responseHandler});
        //create a wink rest client with the above config
        RestClient restClient = new RestClient(clientConfig);
        //create resource endpoint to access User resource
        return restClient.resource(skim_url + "Users");
    }

    private BasicAuthInfo getBasicAuthInfo(UserInfo provider_userInfo) {
        BasicAuthInfo basicAuthInfo = new BasicAuthInfo();
        basicAuthInfo.setUserName(provider_userInfo.getUserName());
        basicAuthInfo.setPassword(provider_userInfo.getPassword());

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

    private void startOtherCarbonServers() throws Exception {

        try {
            serverManagers = new HashMap<Integer, CarbonTestServerManager>();
            Map<String, String> startupParameterMap = new HashMap<String, String>();
            startupParameterMap.put(PORT_OFFSET_PARAM,
                    String.valueOf(PORT_OFFSET_1));

            CarbonTestServerManager serverManager = new CarbonTestServerManager(System.getProperty
                    ("carbon.zip"), startupParameterMap);
            serverManagers.put(PORT_OFFSET_1, serverManager);
            serverManager.startServer();
            Thread.sleep(2000);

            startupParameterMap.put(PORT_OFFSET_PARAM,
                    String.valueOf(PORT_OFFSET_2));
            serverManager = new CarbonTestServerManager(System.getProperty
                    ("carbon.zip"), startupParameterMap);
            serverManagers.put(PORT_OFFSET_2, serverManager);
            serverManager.startServer();
            Thread.sleep(2000);
        } catch (IOException e) {
            throw new Exception("IO Operation failed during carbon servers start up");
        } catch (InterruptedException e) {
            throw new Exception("Interruption Occurred ");
        }
    }
}
