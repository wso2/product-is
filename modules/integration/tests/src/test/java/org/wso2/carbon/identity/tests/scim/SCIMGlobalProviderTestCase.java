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

package org.wso2.carbon.identity.tests.scim;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.api.clients.identity.scim.SCIMConfigAdminClient;
import org.wso2.carbon.automation.api.clients.user.mgt.UserManagementClient;
import org.wso2.carbon.automation.core.MultipleServersManager;
import org.wso2.carbon.automation.core.ProductConstant;
import org.wso2.carbon.automation.core.annotations.ExecutionEnvironment;
import org.wso2.carbon.automation.core.annotations.SetEnvironment;
import org.wso2.carbon.automation.core.environmentcontext.ContextProvider;
import org.wso2.carbon.automation.core.environmentcontext.GroupContextProvider;
import org.wso2.carbon.automation.core.environmentcontext.environmentvariables.EnvironmentContext;
import org.wso2.carbon.automation.core.environmentcontext.environmentvariables.GroupContext;
import org.wso2.carbon.automation.core.utils.UserInfo;
import org.wso2.carbon.automation.core.utils.UserListCsvReader;
import org.wso2.carbon.identity.scim.common.stub.config.SCIMProviderDTO;
import org.wso2.carbon.identity.tests.utils.CarbonTestServerManager;
import org.wso2.carbon.user.mgt.stub.types.carbon.FlaggedName;

import java.util.HashMap;
import java.util.Map;

public class SCIMGlobalProviderTestCase extends MasterSCIMInitiator {
    private static final Log log = LogFactory.getLog(SCIMGlobalProviderTestCase.class);
    String scim_url;
    String scim_Provider_url;
    public static final String providerid = "testProvider";
    public static final String scimuser = "scimuser";
    public static final int providerUserId = 0;
    public static final int consumerUserId = 0;
    private UserInfo userInfo;
    String serviceEndPoint = null;
    String backendUrl = null;
    String sessionCookie = null;
    UserManagementClient userMgtProviderClient;
    UserManagementClient userMgtConsumerClient;
    SCIMConfigAdminClient scimConfigAdminClient;
    private MultipleServersManager serverManager = new MultipleServersManager();
    private Map<String, String> startupParameterMap = new HashMap<String, String>();
    CarbonTestServerManager providerServer;

    @BeforeClass(alwaysRun = true)
    public void initiate() throws Exception {
        userInfo = UserListCsvReader.getUserInfo(providerUserId);
        //Starting Provider server with offset of 2 refer node2 instance.xml
        startupParameterMap.put("-DportOffset", "2");
        providerServer = new CarbonTestServerManager(System.getProperty("carbon.zip"),
                startupParameterMap);
        serverManager.startServers(providerServer);
        //Registering Consumer node -refer instance.xml for node information
        Thread.sleep(5000);
        GroupContextProvider consumerGroupContext = new GroupContextProvider();
        GroupContext consumerGroup = consumerGroupContext.getGroupContext("node1");
        ContextProvider consumer = new ContextProvider();
        EnvironmentContext consumerNodeContext = consumer.getNodeContext(consumerGroup.getNode().getNodeId(), consumerUserId);
        //Registering Provider Node
        GroupContextProvider providerGroupContext = new GroupContextProvider();
        GroupContext providerGroup = providerGroupContext.getGroupContext("node2");
        ContextProvider provider = new ContextProvider();
        EnvironmentContext providerNodeContext = provider.getNodeContext(providerGroup.getNode().getNodeId(), providerUserId);
        //Set SCIM url for provider and consumer
        scim_url = "https://" + consumerNodeContext.getWorkerVariables().getHostName() + ":" + consumerNodeContext.getWorkerVariables().getHttpsPort() + "/wso2/scim/";
        scim_Provider_url = "https://" + providerNodeContext.getWorkerVariables().getHostName() + ":" + providerNodeContext.getWorkerVariables().getHttpsPort() + "/wso2/scim/";
        serviceEndPoint = consumerNodeContext.getBackEndUrl();
        sessionCookie = consumerNodeContext.getSessionCookie();
        //Initiating UserManagement client for provider and consumer
        userMgtProviderClient = new UserManagementClient(providerNodeContext.getBackEndUrl(), providerNodeContext.getSessionCookie());
        userMgtConsumerClient = new UserManagementClient(consumerNodeContext.getBackEndUrl(), consumerNodeContext.getSessionCookie());
        //Initiating SCIM admin client for consumer
        scimConfigAdminClient = new SCIMConfigAdminClient(consumerNodeContext.getBackEndUrl(), consumerNodeContext.getSessionCookie());
        System.setProperty("javax.net.ssl.trustStore", providerNodeContext.getFrameworkContext().getEnvironmentVariables().getKeystorePath());
        System.setProperty("javax.net.ssl.trustStorePassword", providerNodeContext.getFrameworkContext().getEnvironmentVariables().getKeyStrorePassword());
    }

    @Test(alwaysRun = true, description = "Add user Provider", priority = 1)
    @SetEnvironment(executionEnvironments = {ExecutionEnvironment.integration_all})
    public void addProviderTest() throws Exception {
        boolean providerAvailable = false;
        scimConfigAdminClient.addGlobalProvider("carbon.super", providerid, userInfo.getUserName(), userInfo.getPassword(), scim_Provider_url + "Users", scim_Provider_url + "Groups");
        Thread.sleep(5000);
        SCIMProviderDTO[] scimProviders = scimConfigAdminClient.listGlobalProviders("carbon.super", providerid);
        for (SCIMProviderDTO scimProvider : scimProviders) {
            if (scimProvider.getProviderId().equals(providerid)) {
                providerAvailable = true;
            }
        }
        Assert.assertTrue(providerAvailable, "Provider adding failed");
    }

    @Test(alwaysRun = true, description = "Add user for consumer and assert on provider", priority = 2)
    @SetEnvironment(executionEnvironments = {ExecutionEnvironment.integration_all})
    public void createUserTest() throws Exception {
        createUserOnConsumer();
        Thread.sleep(5000);
        Assert.assertTrue(isConsumerUserAvailable(), "Adding user to consumer failed");
        Assert.assertTrue(isProviderUserAvailable(), "Adding user to provider failed");
    }

    @Test(alwaysRun = true, description = "Delete user for consumer and assert on provider", priority = 3,enabled=false)
    @SetEnvironment(executionEnvironments = {ExecutionEnvironment.integration_all})
    public void deleteUserTest() throws Exception {
        deleteUserOnConsumer();
        Thread.sleep(5000);
        Assert.assertFalse(isConsumerUserAvailable(), "Deleting user to consumer failed");
        Assert.assertTrue(isProviderUserNotAvailable(), "Deleting user to provider failed");
    }

    @AfterClass(alwaysRun = true)
    public void cleanOut() throws Exception {
        scimConfigAdminClient.deleteGlobalProvider("carbon.super", providerid);
        serverManager.stopAllServers();
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
        FlaggedName[] userList = userMgtProviderClient.listAllUsers(scimuser, 10);
        for (int loopCount = 0; loopCount <= 5; loopCount++) {
            for (FlaggedName user : userList) {
                if (user.getItemName().contains(scimuser)) {
                    userAvailable = true;
                }
            }
            if (userAvailable)
                break;
        }
        return userAvailable;
    }


    private boolean isProviderUserNotAvailable() throws Exception {
        boolean userNotAvailable = true;
        for (int loopCount = 0; loopCount <= 5; loopCount++) {
            Thread.sleep(1000);
           /* FlaggedName[] userList = userMgtProviderClient.listUsers(scimuser, 10);
            for (FlaggedName user : userList) {

                if (user.getItemName().contains(scimuser)) {
                    userNotAvailable = false;
                }
            }*/
            userNotAvailable= userMgtProviderClient.userNameExists(ProductConstant.DEFAULT_PRODUCT_ROLE,scimuser);
            if (!userNotAvailable)
                break;
        }
        return userNotAvailable;
    }


    private void createUserOnConsumer() throws Exception {
        userMgtConsumerClient.addUser(scimuser, "scimpwd",
                new String[]{ProductConstant.DEFAULT_PRODUCT_ROLE}, null);

    }

    private void deleteUserOnConsumer() throws Exception {
        userMgtConsumerClient.deleteUser(scimuser);
    }
}
