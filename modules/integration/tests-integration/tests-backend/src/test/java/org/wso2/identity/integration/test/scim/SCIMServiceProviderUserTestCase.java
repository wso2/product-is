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
import org.apache.wink.client.ClientConfig;
import org.apache.wink.client.Resource;
import org.apache.wink.client.RestClient;
import org.apache.wink.client.handlers.ClientHandler;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.annotations.ExecutionEnvironment;
import org.wso2.carbon.automation.engine.annotations.SetEnvironment;
import org.wso2.carbon.automation.engine.context.AutomationContext;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.automation.engine.context.beans.User;
import org.wso2.identity.integration.common.clients.UserManagementClient;
import org.wso2.carbon.integration.common.utils.LoginLogoutClient;
import org.wso2.carbon.user.mgt.stub.types.carbon.FlaggedName;
import org.wso2.charon.core.client.SCIMClient;
import org.wso2.charon.core.schema.SCIMConstants;
import org.wso2.identity.integration.test.scim.utils.SCIMResponseHandler;
import org.wso2.identity.integration.test.scim.utils.SCIMUtils;
import org.wso2.identity.integration.test.utils.BasicAuthInfo;

public class SCIMServiceProviderUserTestCase {
    private static final Log log = LogFactory.getLog(SCIMServiceProviderUserTestCase.class);
    private static final String USERNAME = "SCIMUser1";
    String scimUserId = null;
    private User provider_userInfo;
    UserManagementClient userMgtClient;
    String scim_url;
    String serviceEndPoint = null;
    String backendUrl = null;
    String sessionCookie = null;
    private SCIMClient scimClient;

    @BeforeClass(alwaysRun = true)
    public void initiate() throws Exception {
        AutomationContext automationContext = new AutomationContext("IDENTITY", TestUserMode.SUPER_TENANT_ADMIN);
        provider_userInfo = automationContext.getContextTenant().getContextUser();
        backendUrl = automationContext.getContextUrls().getBackEndUrl();
        scim_url = backendUrl.substring(0, 22) + "/wso2/scim/";
        serviceEndPoint = automationContext.getContextUrls().getServiceUrl();
        sessionCookie = new LoginLogoutClient(automationContext).login();
        userMgtClient = new UserManagementClient(backendUrl, sessionCookie);
    }

    @BeforeMethod
    public void initiateSkimClient() {
        scimClient = new SCIMClient();
    }

    @Test(alwaysRun = true, description = "Add SCIM user")
    @SetEnvironment(executionEnvironments = { ExecutionEnvironment.ALL})
    public void createUser() throws Exception {
        //create SCIM client
        String encodedUser = SCIMUtils.getEncodedSCIMUser(scimClient, USERNAME, "test",
                             new String[] { "scimuser1@gmail.com", "scimuser1@wso2.com" }, "SCIMUser1", "password1",
                             "sinhala", "0772202595");
        //create a apache wink ClientHandler to intercept and identify response messages
        Resource userResource = SCIMUtils.getUserResource(scimClient, scim_url);
        BasicAuthInfo encodedBasicAuthInfo = SCIMUtils.getBasicAuthInfo(provider_userInfo);
        String response = userResource.
                header(SCIMConstants.AUTHORIZATION_HEADER, encodedBasicAuthInfo.getAuthorizationHeader()).
                contentType(SCIMConstants.APPLICATION_JSON).accept(SCIMConstants.APPLICATION_JSON).
                post(String.class, encodedUser);
        log.info(response);
        Object obj= JSONValue.parse(response);
        scimUserId = ((JSONObject)obj).get("id").toString();
        userMgtClient.listUsers(USERNAME, 100);
        Assert.assertTrue(isUserExists());
        Assert.assertNotNull(scimUserId);
    }

    @Test(alwaysRun = true, description = "Get SCIM user", dependsOnMethods = { "createUser" })
    @SetEnvironment(executionEnvironments = {ExecutionEnvironment.ALL})
    public void getUser() {
        //create a apache wink ClientHandler to intercept and identify response messages
        SCIMResponseHandler responseHandler = new SCIMResponseHandler();
        responseHandler.setSCIMClient(scimClient);
        //set the handler in wink client config
        ClientConfig clientConfig = new ClientConfig();
        clientConfig.handlers(new ClientHandler[]{responseHandler});
        //create a wink rest client with the above config
        RestClient restClient = new RestClient(clientConfig);
        BasicAuthInfo encodedBasicAuthInfo = SCIMUtils.getBasicAuthInfo(provider_userInfo);
        //create resource endpoint to access a known user resource.
        Resource userResource = restClient.resource(scim_url + "Users/" + scimUserId);
        String response = userResource.
                header(SCIMConstants.AUTHORIZATION_HEADER, encodedBasicAuthInfo.getAuthorizationHeader()).
                contentType(SCIMConstants.APPLICATION_JSON).accept(SCIMConstants.APPLICATION_JSON)
                .get(String.class);

        //decode the response
        log.info(response);
        Assert.assertTrue(response.contains(""));
    }

    @Test(alwaysRun = true, description = "list all SCIM users", dependsOnMethods = { "getUser" })
    @SetEnvironment(executionEnvironments = {ExecutionEnvironment.ALL})
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

        BasicAuthInfo encodedBasicAuthInfo = SCIMUtils.getBasicAuthInfo(provider_userInfo);
        //create resource endpoint to access a known user resource.
        Resource userResource = restClient.resource(scim_url + "Users");
        String response = userResource.
                header(SCIMConstants.AUTHORIZATION_HEADER, encodedBasicAuthInfo.getAuthorizationHeader()).
                contentType(SCIMConstants.APPLICATION_JSON).accept(SCIMConstants.APPLICATION_JSON)
                .get(String.class);
        Assert.assertTrue(isAllUsersExists(response));
    }

    @Test(alwaysRun = true, description = "filter all SCIM users", dependsOnMethods = { "listUser" })
    @SetEnvironment(executionEnvironments = {ExecutionEnvironment.ALL})
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

        BasicAuthInfo encodedBasicAuthInfo = SCIMUtils.getBasicAuthInfo(provider_userInfo);
        //create resource endpoint to access a known user resource.
        Resource userResource = restClient.resource(scim_url + "Users?filter=userName%20Eq%20" + USERNAME);
        String response = userResource.
                header(SCIMConstants.AUTHORIZATION_HEADER, encodedBasicAuthInfo.getAuthorizationHeader()).
                contentType(SCIMConstants.APPLICATION_JSON).accept(SCIMConstants.APPLICATION_JSON)
                .get(String.class);
        Assert.assertTrue(response.contains("\"userName\":\"" + USERNAME + "\""));
    }

    @Test(alwaysRun = true, description = "Update SCIM user", dependsOnMethods = { "filterUser" })
    @SetEnvironment(executionEnvironments = {ExecutionEnvironment.ALL})
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
        BasicAuthInfo encodedBasicAuthInfo = SCIMUtils.getBasicAuthInfo(provider_userInfo);
        //create resource endpoint to access a known user resource.
        Resource userResource = restClient.resource(scim_url + "Users/" + scimUserId);
        org.wso2.charon.core.objects.User decodedUser = SCIMUtils
                .getSCIMUser(scimClient, USERNAME, "test",
                             new String[] { "scimuser1@gmail.com", "scimuser1@wso2.com" }, "SCIMUser1", "password1",
                             "sinhala", "0772202595");
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

    @Test(alwaysRun = true, description = "Delete SCIM user", dependsOnMethods = { "UpdateUser" })
    @SetEnvironment(executionEnvironments = {ExecutionEnvironment.ALL})
    public void DeleteUser() throws Exception {
        ClientConfig clientConfig = new ClientConfig();
        SCIMResponseHandler responseHandler = new SCIMResponseHandler();
        responseHandler.setSCIMClient(scimClient);
        clientConfig.handlers(new ClientHandler[]{responseHandler});
        RestClient restClient = new RestClient(clientConfig);
        BasicAuthInfo encodedBasicAuthInfo = SCIMUtils.getBasicAuthInfo(provider_userInfo);
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
        FlaggedName[] nameList = userMgtClient.listAllUsers(USERNAME, 100);
        for (FlaggedName name : nameList) {
            if (name.getItemName().contains(USERNAME)) {
                userExists = true;
            }
        }
        return userExists;
    }

    private boolean isAllUsersExists(String response) throws Exception {
        boolean usersExists = false;
        FlaggedName[] nameList = userMgtClient.listAllUsers(USERNAME, 100);
        for (FlaggedName name : nameList) {

            if (response.contains(name.getItemName())) {
                usersExists = true;

            }
        }
        return usersExists;
    }
}