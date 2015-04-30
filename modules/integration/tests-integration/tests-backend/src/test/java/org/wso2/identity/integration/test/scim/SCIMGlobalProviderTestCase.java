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

package org.wso2.identity.integration.test.scim;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.annotations.ExecutionEnvironment;
import org.wso2.carbon.automation.engine.annotations.SetEnvironment;
import org.wso2.carbon.automation.engine.context.AutomationContext;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.identity.scim.common.stub.config.SCIMProviderDTO;
import org.wso2.carbon.integration.common.admin.client.AuthenticatorClient;
import org.wso2.carbon.integration.common.admin.client.UserManagementClient;
import org.wso2.carbon.user.mgt.stub.types.carbon.FlaggedName;
import org.wso2.identity.integration.common.clients.scim.SCIMConfigAdminClient;
import org.wso2.carbon.automation.extensions.servers.carbonserver.MultipleServersManager;
import org.wso2.identity.integration.common.utils.CarbonTestServerManager;

import java.util.HashMap;
import java.util.Map;

public class SCIMGlobalProviderTestCase extends MasterSCIMInitiator {

    private static final Log log = LogFactory.getLog(SCIMGlobalProviderTestCase.class);
    private static final int DEFAULT_PORT = 9443;
    private static final String providerid = "testProvider";
    private static final String scimuser = "scimuser";
    private static final String ADMIN_ROLE = "admin";

    private String scim_Provider_url;
    private String serviceEndPoint1 = null;
    private String serverEndpoint2 = null;
    private String sessionKey1 = null;
    private String sessionKey2 = null;
    private UserManagementClient userMgtProviderClient;
    private UserManagementClient userMgtConsumerClient;
    private SCIMConfigAdminClient scimConfigAdminClient;
    private MultipleServersManager serverManager = new MultipleServersManager();
    private Map<String, String> startupParameterMap = new HashMap<String, String>();
    private CarbonTestServerManager providerServer;

    @BeforeClass(alwaysRun = true)
    public void initiate() throws Exception {

        super.initTest();
        //Starting Provider server with offset of 2 refer node2 instance.xml
        AutomationContext context = new AutomationContext("IDENTITY", "identity002", TestUserMode.SUPER_TENANT_ADMIN);
        startupParameterMap.put("-DportOffset", "1");
        providerServer = new CarbonTestServerManager(context, System.getProperty("carbon.zip"), startupParameterMap);
        serverManager.startServers(providerServer);
        //Registering Consumer node -refer instance.xml for node information
        Thread.sleep(5000);

        // TODO: FIX port to get from context variable
        scim_Provider_url = "https://" + context.getInstance().getHosts().get("default") + ":" + (Integer.valueOf
                (isServer.getInstance().getPorts().get("https")) + 1) + "/wso2/scim/";

        serviceEndPoint1 = isServer.getContextUrls().getBackEndUrl();
        sessionKey1 = sessionCookie;

        serverEndpoint2 = getSecureServiceUrl(1, context.getContextUrls().getBackEndUrl());

        AuthenticatorClient authenticatorClient = new AuthenticatorClient(serverEndpoint2);
        sessionKey2 = authenticatorClient.login(context.getSuperTenant().getTenantAdmin().getUserName(),
                                                context.getSuperTenant().getTenantAdmin().getPassword(),
                                                context.getDefaultInstance().getHosts().get("default"));

        //Initiating UserManagement client for provider and consumer
        userMgtProviderClient = new UserManagementClient(serverEndpoint2, sessionKey2);
        userMgtConsumerClient = new UserManagementClient(serviceEndPoint1, sessionKey1);
        //Initiating SCIM admin client for consumer
        scimConfigAdminClient = new SCIMConfigAdminClient(serviceEndPoint1, sessionKey1);

        setSystemproperties();
    }

    @AfterClass(alwaysRun = true)
    public void cleanOut() throws Exception {
        scimConfigAdminClient.deleteGlobalProvider("carbon.super", providerid);
        serverManager.stopAllServers();
    }

    @Test(alwaysRun = true, description = "Add user Provider")
    @SetEnvironment(executionEnvironments = {ExecutionEnvironment.ALL})
    public void addProviderTest() throws Exception {
        boolean providerAvailable = false;
        scimConfigAdminClient.addGlobalProvider("carbon.super", providerid, userInfo.getUserName(),
                                                userInfo.getPassword(), scim_Provider_url + "Users",
                                                scim_Provider_url + "Groups");
        Thread.sleep(5000);
        SCIMProviderDTO[] scimProviders = scimConfigAdminClient.listGlobalProviders("carbon.super", providerid);
        for (SCIMProviderDTO scimProvider : scimProviders) {
            if (scimProvider.getProviderId().equals(providerid)) {
                providerAvailable = true;
            }
        }
        Assert.assertTrue(providerAvailable, "Provider adding failed");
    }

    @Test(alwaysRun = true, description = "Add user for consumer and assert on provider", dependsOnMethods = "addProviderTest")
    @SetEnvironment(executionEnvironments = {ExecutionEnvironment.ALL})
    public void createUserTest() throws Exception {
        createUserOnConsumer();
        Thread.sleep(5000);
        Assert.assertTrue(isConsumerUserAvailable(), "Adding user to consumer failed");
        Assert.assertTrue(isProviderUserAvailable(), "Adding user to provider failed");
    }

    @Test(alwaysRun = true, description = "Delete user for consumer and assert on provider", dependsOnMethods = "createUserTest")
    @SetEnvironment(executionEnvironments = {ExecutionEnvironment.ALL})
    public void deleteUserTest() throws Exception {
        deleteUserOnConsumer();
        Thread.sleep(5000);
        Assert.assertFalse(isConsumerUserAvailable(), "Deleting user to consumer failed");
        Assert.assertTrue(isProviderUserNotAvailable(), "Deleting user to provider failed");
    }

    private boolean isConsumerUserAvailable() throws Exception {
        boolean userAvailable = false;
        FlaggedName[] userList = userMgtConsumerClient.listAllUsers(scimuser, 10);
        for (FlaggedName user : userList) {
            if (user.getItemName().contains(scimuser)) {
                userAvailable = true;
            }
        }
        return userAvailable;
    }

    private boolean isProviderUserAvailable() throws Exception {
        boolean userAvailable = false;
        for (int loopCount = 0; loopCount <= 5; loopCount++) {
            Thread.sleep(1000);
            userAvailable = userMgtProviderClient.userNameExists(ADMIN_ROLE, scimuser);
            if (userAvailable) {
                break;
            }
        }
        return userAvailable;
    }

    private boolean isProviderUserNotAvailable() throws Exception {
        boolean userAvailable = false;
        for (int loopCount = 0; loopCount <= 5; loopCount++) {
            Thread.sleep(1000);
            userAvailable = userMgtProviderClient.userNameExists(ADMIN_ROLE, scimuser);
            if (!userAvailable) {
                return true;
            }
        }
        return false;
    }

    private void createUserOnConsumer() throws Exception {
        userMgtConsumerClient.addUser(scimuser, "scimpwd",
                                      new String[]{ADMIN_ROLE}, null);

    }

    private void deleteUserOnConsumer() throws Exception {
        userMgtConsumerClient.deleteUser(scimuser);
    }

    private String getSecureServiceUrl(int portOffset, String baseUrl) {
        return baseUrl.replace("9443", String.valueOf(DEFAULT_PORT + portOffset));
    }
}
