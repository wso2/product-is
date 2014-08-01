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
import org.apache.wink.client.ClientConfig;
import org.apache.wink.client.Resource;
import org.apache.wink.client.RestClient;
import org.apache.wink.client.handlers.ClientHandler;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.authenticator.stub.LoginAuthenticationExceptionException;
import org.wso2.carbon.automation.api.clients.user.mgt.UserManagementClient;
import org.wso2.carbon.automation.core.environmentcontext.ContextProvider;
import org.wso2.carbon.automation.core.environmentcontext.environmentvariables.EnvironmentContext;
import org.wso2.carbon.automation.core.environmentcontext.environmentvariables.GroupContext;
import org.wso2.carbon.automation.core.environmentcontext.GroupContextProvider;
import org.wso2.carbon.automation.core.annotations.ExecutionEnvironment;
import org.wso2.carbon.automation.core.annotations.SetEnvironment;
import org.wso2.carbon.automation.core.utils.UserInfo;
import org.wso2.carbon.automation.core.utils.UserListCsvReader;
import org.wso2.carbon.identity.tests.utils.BasicAuthInfo;
import org.wso2.carbon.identity.tests.scim.utils.SCIMResponseHandler;
import org.wso2.carbon.user.mgt.stub.types.carbon.FlaggedName;
import org.wso2.charon.core.client.SCIMClient;
import org.wso2.charon.core.objects.User;
import org.wso2.charon.core.schema.SCIMConstants;

import java.rmi.RemoteException;

public class SCIMServiceProviderUserTestCase extends MasterSCIMInitiator {
    private static final Log log = LogFactory.getLog(SCIMServiceProviderUserTestCase.class);
    public static final int providerUserId = 0;
    public static final int consumerUserId = 0;
    String scimUserId = null;
    private UserInfo provider_userInfo;
    UserManagementClient userMgtClient;
    String scim_url;
    String serviceEndPoint = null;
    String backendUrl = null;
    String sessionCookie = null;

    @BeforeClass(alwaysRun = true)
    public void initiate() throws RemoteException, LoginAuthenticationExceptionException {
        provider_userInfo = UserListCsvReader.getUserInfo(providerUserId);
        GroupContextProvider consumerGroupContext = new GroupContextProvider();
        GroupContext consumerGroup = consumerGroupContext.getGroupContext("node1");

        ContextProvider consumer = new ContextProvider();
        EnvironmentContext consumerNodeContext = consumer.getNodeContext(consumerGroup.getNode().getNodeId(), consumerUserId);
        backendUrl = consumerNodeContext.getBackEndUrl();
        scim_url = "https://" + consumerNodeContext.getWorkerVariables().getHostName() + ":" + consumerNodeContext.getWorkerVariables().getHttpsPort() + "/wso2/scim/";
        serviceEndPoint = consumerNodeContext.getBackEndUrl();
        sessionCookie = consumerNodeContext.getSessionCookie();
        userMgtClient = new UserManagementClient(backendUrl, sessionCookie);
    }

    @BeforeMethod
    public void initiateSkimClient() {
        scimClient = new SCIMClient();
    }

    @Test(alwaysRun = true, description = "Add SCIM user", priority = 1)
    @SetEnvironment(executionEnvironments = {ExecutionEnvironment.integration_all})
    public void createUser() throws Exception {
        //create SCIM client
        String encodedUser = getScimUser();
        //create a apache wink ClientHandler to intercept and identify response messages
        Resource userResource = getResource(scimClient, scim_url);
        BasicAuthInfo encodedBasicAuthInfo = getBasicAuthInfo(provider_userInfo);
        String response = userResource.
                header(SCIMConstants.AUTHORIZATION_HEADER, encodedBasicAuthInfo.getAuthorizationHeader()).
                contentType(SCIMConstants.APPLICATION_JSON).accept(SCIMConstants.APPLICATION_JSON).
                post(String.class, encodedUser);
        log.info(response);
        scimUserId = response.split(",")[0].split(":")[1].replace('"', ' ').trim();
        userMgtClient.listUsers(userName, 100);
        Assert.assertTrue(isUserExists());
        Assert.assertNotNull(scimUserId);
    }

    @Test(alwaysRun = true, description = "Get SCIM user", priority = 2)
    @SetEnvironment(executionEnvironments = {ExecutionEnvironment.integration_all})
    public void getUser() {
        //create a apache wink ClientHandler to intercept and identify response messages
        SCIMResponseHandler responseHandler = new SCIMResponseHandler();
        responseHandler.setSCIMClient(scimClient);
        //set the handler in wink client config
        ClientConfig clientConfig = new ClientConfig();
        clientConfig.handlers(new ClientHandler[]{responseHandler});
        //create a wink rest client with the above config
        RestClient restClient = new RestClient(clientConfig);
        BasicAuthInfo encodedBasicAuthInfo = getBasicAuthInfo(provider_userInfo);
        //create resource endpoint to access a known user resource.
        Resource userResource = restClient.resource(scim_url + "Users/" + scimUserId);
        String response = userResource.
                header(SCIMConstants.AUTHORIZATION_HEADER, encodedBasicAuthInfo.getAuthorizationHeader()).
                contentType(SCIMConstants.APPLICATION_JSON).accept(SCIMConstants.APPLICATION_JSON)
                .get(String.class);

        //decode the response
        log.info(response);
        Assert.assertTrue(response.contains(userName));
    }

    @Test(alwaysRun = true, description = "list all SCIM users", priority = 3)
    @SetEnvironment(executionEnvironments = {ExecutionEnvironment.integration_all})
    public void listUser() throws Exception {
        //create SCIM client
        SCIMClient scimClient = new SCIMClient();
        //create a apache wink ClientHandler to intercept and identify response messages
        SCIMResponseHandler responseHandler = new SCIMResponseHandler();
        responseHandler.setSCIMClient(scimClient);
        //set the handler in wink client config
        ClientConfig clientConfig = new ClientConfig();
        clientConfig.handlers(new ClientHandler[]{responseHandler});
        //create a wink rest client with the above config
        RestClient restClient = new RestClient(clientConfig);

        BasicAuthInfo encodedBasicAuthInfo = getBasicAuthInfo(provider_userInfo);
        //create resource endpoint to access a known user resource.
        Resource userResource = restClient.resource(scim_url + "Users");
        String response = userResource.
                header(SCIMConstants.AUTHORIZATION_HEADER, encodedBasicAuthInfo.getAuthorizationHeader()).
                contentType(SCIMConstants.APPLICATION_JSON).accept(SCIMConstants.APPLICATION_JSON)
                .get(String.class);
        Assert.assertTrue(isAllUsersExists(response));
    }

    @Test(alwaysRun = true, description = "filter all SCIM users", priority = 4)
    @SetEnvironment(executionEnvironments = {ExecutionEnvironment.integration_all})
    public void filterUser() throws Exception {
        //create SCIM client
        SCIMClient scimClient = new SCIMClient();
        //create a apache wink ClientHandler to intercept and identify response messages
        SCIMResponseHandler responseHandler = new SCIMResponseHandler();
        responseHandler.setSCIMClient(scimClient);
        //set the handler in wink client config
        ClientConfig clientConfig = new ClientConfig();
        clientConfig.handlers(new ClientHandler[]{responseHandler});
        //create a wink rest client with the above config
        RestClient restClient = new RestClient(clientConfig);

        BasicAuthInfo encodedBasicAuthInfo = getBasicAuthInfo(provider_userInfo);
        //create resource endpoint to access a known user resource.
        Resource userResource = restClient.resource(scim_url + "Users?filter=userNameEqdharshana");
        String response = userResource.
                header(SCIMConstants.AUTHORIZATION_HEADER, encodedBasicAuthInfo.getAuthorizationHeader()).
                contentType(SCIMConstants.APPLICATION_JSON).accept(SCIMConstants.APPLICATION_JSON)
                .get(String.class);
        Assert.assertTrue(response.contains("\"userName\":\"dharshana\""));
    }

    @Test(alwaysRun = true, description = "Update SCIM user", priority = 5)
    @SetEnvironment(executionEnvironments = {ExecutionEnvironment.integration_all})
    public void UpdateUser() throws Exception {
        String updatedMiddleName = "testChange11";
        String updatedEmail = "test123@wso2.com";
        //create a apache wink ClientHandler to intercept and identify response messages
        SCIMResponseHandler responseHandler = new SCIMResponseHandler();
        responseHandler.setSCIMClient(scimClient);
        //set the handler in wink client config
        ClientConfig clientConfig = new ClientConfig();
        clientConfig.handlers(new ClientHandler[]{responseHandler});
        //create a wink rest client with the above config
        RestClient restClient = new RestClient(clientConfig);
        BasicAuthInfo encodedBasicAuthInfo = getBasicAuthInfo(provider_userInfo);
        //create resource endpoint to access a known user resource.
        Resource userResource = restClient.resource(scim_url + "Users/" + scimUserId);
        User decodedUser = getScimUserUnEncoded();
        decodedUser.setDisplayName(updatedMiddleName);
        decodedUser.setWorkEmail(updatedEmail, true);

        //encode updated user
        String updatedUserString = scimClient.encodeSCIMObject(decodedUser, SCIMConstants.JSON);
        //update user
        Resource updateUserResource = restClient.resource(scim_url + "Users/" + scimUserId);
        String responseUpdated = updateUserResource.
                header(SCIMConstants.AUTHORIZATION_HEADER, encodedBasicAuthInfo.getAuthorizationHeader()).
                contentType(SCIMConstants.APPLICATION_JSON).accept(SCIMConstants.APPLICATION_JSON)
                .put(String.class, updatedUserString);
        log.info("Updated user: " + responseUpdated);

        String response = userResource.
                header(SCIMConstants.AUTHORIZATION_HEADER, encodedBasicAuthInfo.getAuthorizationHeader()).
                contentType(SCIMConstants.APPLICATION_JSON).accept(SCIMConstants.APPLICATION_JSON)
                .get(String.class);
        log.info("Retrieved user: " + response);
        Assert.assertTrue(response.contains(updatedEmail));
        Assert.assertTrue(response.contains(updatedMiddleName));
    }

    @Test(alwaysRun = true, description = "Delete SCIM user", priority = 6)
    @SetEnvironment(executionEnvironments = {ExecutionEnvironment.integration_all})
    public void DeleteUser() throws Exception {
        ClientConfig clientConfig = new ClientConfig();
        SCIMResponseHandler responseHandler = new SCIMResponseHandler();
        responseHandler.setSCIMClient(scimClient);
        clientConfig.handlers(new ClientHandler[]{responseHandler});
        RestClient restClient = new RestClient(clientConfig);
        BasicAuthInfo encodedBasicAuthInfo = getBasicAuthInfo(provider_userInfo);
        Resource userResource = restClient.resource(scim_url + "Users/" + scimUserId);
        String response = userResource.
                header(SCIMConstants.AUTHORIZATION_HEADER, encodedBasicAuthInfo.getAuthorizationHeader()).
                accept(SCIMConstants.APPLICATION_JSON).
                delete(String.class);
        //decode the response
        Assert.assertTrue(response.isEmpty());
        Assert.assertFalse(isUserExists());
    }


    private boolean isUserExists() throws Exception {
        boolean userExists = false;
        FlaggedName[] nameList = userMgtClient.listAllUsers(userName, 100);
        for (FlaggedName name : nameList) {
            if (name.getItemName().contains(userName)) {
                userExists = true;
            }
        }
        return userExists;
    }

    private boolean isAllUsersExists(String response) throws Exception {
        boolean usersExists = false;
        FlaggedName[] nameList = userMgtClient.listAllUsers(userName, 100);
        for (FlaggedName name : nameList) {

            if (response.contains(name.getItemName())) {
                usersExists = true;

            }
        }
        return usersExists;
    }
}