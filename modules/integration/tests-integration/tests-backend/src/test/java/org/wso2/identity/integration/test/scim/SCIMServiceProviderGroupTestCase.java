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
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.annotations.ExecutionEnvironment;
import org.wso2.carbon.automation.engine.annotations.SetEnvironment;
import org.wso2.carbon.automation.engine.context.AutomationContext;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.automation.engine.context.beans.User;
import org.wso2.carbon.integration.common.admin.client.UserManagementClient;
import org.wso2.carbon.integration.common.utils.LoginLogoutClient;
import org.wso2.carbon.user.mgt.stub.types.carbon.FlaggedName;
import org.wso2.charon.core.client.SCIMClient;
import org.wso2.charon.core.exceptions.CharonException;
import org.wso2.charon.core.objects.Group;
import org.wso2.charon.core.schema.SCIMConstants;
import org.wso2.identity.integration.common.clients.scim.SCIMConfigAdminClient;
import org.wso2.identity.integration.test.scim.utils.SCIMResponseHandler;
import org.wso2.identity.integration.test.scim.utils.SCIMUtils;
import org.wso2.identity.integration.test.utils.BasicAuthInfo;

public class SCIMServiceProviderGroupTestCase {
    private static final Log log = LogFactory.getLog(SCIMServiceProviderGroupTestCase.class);
    public static final String DISPLAY_NAME = "eng";
    public static final String EXTERNAL_ID = "eng";
    private static final String USERNAME = "SCIMUser2";
    private static final String USERNAME2 = "dharshana2";
    String scimUserId = null;
    private String scimUserId2 = null;
    String scimGroupId = null;
    String scim_url;
    private SCIMClient scimClient;

    private User userInfo;
    String serviceEndPoint = null;
    String backendUrl = null;
    String sessionCookie = null;
    UserManagementClient userMgtClient;
    SCIMConfigAdminClient scimConfigAdminClient;

    @BeforeClass(alwaysRun = true)
    public void initiate() throws Exception {
        AutomationContext automationContext = new AutomationContext("IDENTITY", TestUserMode.SUPER_TENANT_ADMIN);
        userInfo = automationContext.getContextTenant().getContextUser();
        backendUrl = automationContext.getContextUrls().getBackEndUrl();
        serviceEndPoint = automationContext.getContextUrls().getServiceUrl();
        scim_url = backendUrl.substring(0, 22) + "/wso2/scim/";
        sessionCookie = new LoginLogoutClient(automationContext).login();
        userMgtClient = new UserManagementClient(backendUrl, sessionCookie);
        scimConfigAdminClient = new SCIMConfigAdminClient(backendUrl, sessionCookie);
        scimClient = new SCIMClient();
        //creating users for the test
        createUser();
    }

    @Test(alwaysRun = true, description = "Add SCIM Group")
    @SetEnvironment(executionEnvironments = { ExecutionEnvironment.ALL})
    public void createGroupTest() throws Exception {
        //create a group according to SCIM Group Schema
        Group scimGroup = SCIMUtils.getSCIMGroup(scimClient, scimUserId, USERNAME, EXTERNAL_ID, DISPLAY_NAME);
        String encodedGroup = scimClient.encodeSCIMObject(scimGroup, SCIMConstants.JSON);
        Resource groupResource = SCIMUtils.getGroupResource(scimClient, scim_url);
        BasicAuthInfo encodedBasicAuthInfo = SCIMUtils.getBasicAuthInfo(userInfo);

        //send previously registered SCIM consumer credentials in http headers.
        String response = groupResource.
                header(SCIMConstants.AUTHORIZATION_HEADER, encodedBasicAuthInfo.getAuthorizationHeader()).
                contentType(SCIMConstants.APPLICATION_JSON).accept(SCIMConstants.APPLICATION_JSON).
                post(String.class, encodedGroup);
        //decode the response
        log.info(response);
        scimGroupId = response.split(",")[0].split(":")[1].replace('"', ' ').trim();
        Assert.assertTrue(userMgtClient.roleNameExists(DISPLAY_NAME));
    }

    @Test(alwaysRun = true, description = "Get SCIM Group", dependsOnMethods = { "createGroupTest" })
    @SetEnvironment(executionEnvironments = {ExecutionEnvironment.ALL})
    public void getGroup() throws Exception {

        SCIMResponseHandler responseHandler = new SCIMResponseHandler();
        responseHandler.setSCIMClient(scimClient);
        //set the handler in wink client config
        ClientConfig clientConfig = new ClientConfig();
        clientConfig.handlers(new ClientHandler[]{responseHandler});
        //create a wink rest client with the above config
        RestClient restClient = new RestClient(clientConfig);
        BasicAuthInfo encodedBasicAuthInfo = SCIMUtils.getBasicAuthInfo(userInfo);
        //create resource endpoint to access a known user resource.
        Resource groupResource = restClient.resource(scim_url + "Groups/" + scimGroupId);
        String response = groupResource.
                header(SCIMConstants.AUTHORIZATION_HEADER, encodedBasicAuthInfo.getAuthorizationHeader()).
                contentType(SCIMConstants.APPLICATION_JSON).accept(SCIMConstants.APPLICATION_JSON)
                .get(String.class);
        log.info(response.toString());
        Assert.assertTrue(response.split(",")[0].split(":")[1].replace('"', ' ').trim().contains(scimGroupId));
    }


    @Test(alwaysRun = true, description = "list SCIM Groups", dependsOnMethods = { "getGroup" })
    @SetEnvironment(executionEnvironments = {ExecutionEnvironment.ALL})
    public void listGroup() throws Exception {
        SCIMResponseHandler responseHandler = new SCIMResponseHandler();
        responseHandler.setSCIMClient(scimClient);

        //set the handler in wink client config
        ClientConfig clientConfig = new ClientConfig();
        clientConfig.handlers(new ClientHandler[]{responseHandler});
        //create a wink rest client with the above config
        RestClient restClient = new RestClient(clientConfig);
        BasicAuthInfo encodedBasicAuthInfo = SCIMUtils.getBasicAuthInfo(userInfo);
        //create resource endpoint to access a known user resource.
        Resource groupResource = restClient.resource(scim_url + "Groups/" + scimGroupId);
        String response = groupResource.
                header(SCIMConstants.AUTHORIZATION_HEADER, encodedBasicAuthInfo.getAuthorizationHeader()).
                contentType(SCIMConstants.APPLICATION_JSON).accept(SCIMConstants.APPLICATION_JSON)
                .get(String.class);
        log.info(response.toString());
        FlaggedName[] roleNames = userMgtClient.listRoles("eng", 100);
        for (FlaggedName role : roleNames) {
            if (!role.getItemName().contains("false")) {
                Assert.assertTrue(response.contains(role.getItemName()));
            }
        }

    }

    @Test(alwaysRun = true, description = "filter SCIM Groups", dependsOnMethods = { "listGroup" })
    @SetEnvironment(executionEnvironments = {ExecutionEnvironment.ALL})
    public void filterGroup() throws Exception {
        SCIMResponseHandler responseHandler = new SCIMResponseHandler();
        responseHandler.setSCIMClient(scimClient);

        //set the handler in wink client config
        ClientConfig clientConfig = new ClientConfig();
        clientConfig.handlers(new ClientHandler[]{responseHandler});
        //create a wink rest client with the above config
        RestClient restClient = new RestClient(clientConfig);
        BasicAuthInfo encodedBasicAuthInfo = SCIMUtils.getBasicAuthInfo(userInfo);
        //create resource endpoint to access a known user resource.
        Resource groupResource = restClient.resource(scim_url + "Groups?filter=displayNameEqeng");
        String response = groupResource.
                header(SCIMConstants.AUTHORIZATION_HEADER, encodedBasicAuthInfo.getAuthorizationHeader()).
                contentType(SCIMConstants.APPLICATION_JSON).accept(SCIMConstants.APPLICATION_JSON)
                .get(String.class);
        log.info(response.toString());
        Assert.assertTrue(response.contains("\"displayName\":\"PRIMARY/eng\""));

    }


    @Test(alwaysRun = true, description = "Add SCIM user", dependsOnMethods = { "filterGroup" })
    @SetEnvironment(executionEnvironments = {ExecutionEnvironment.ALL})
    public void updateGroup() throws Exception {
        SCIMResponseHandler responseHandler = new SCIMResponseHandler();
        responseHandler.setSCIMClient(scimClient);
        //set the handler in wink client config
        ClientConfig clientConfig = new ClientConfig();
        clientConfig.handlers(new ClientHandler[]{responseHandler});
        //create a wink rest client with the above config
        RestClient restClient = new RestClient(clientConfig);
        BasicAuthInfo encodedBasicAuthInfo = SCIMUtils.getBasicAuthInfo(userInfo);
        //create resource endpoint to access a known user resource.
        Resource groupResource = restClient.resource(scim_url + "Groups/" + scimGroupId);
        String response = groupResource.
                header(SCIMConstants.AUTHORIZATION_HEADER, encodedBasicAuthInfo.getAuthorizationHeader()).
                contentType(SCIMConstants.APPLICATION_JSON).accept(SCIMConstants.APPLICATION_JSON)
                .get(String.class);

        log.info("Retrieved group: " + response);
        //decode retrieved group
        Group decodedGroup = (Group) scimClient.decodeSCIMResponse(response.replace("PRIMARY/",""), SCIMConstants.JSON, 2);

        decodedGroup.setDisplayName("testeng2");
        String updatedGroupString = scimClient.encodeSCIMObject(decodedGroup, SCIMConstants.JSON);

        Resource updateGroupResource = restClient.resource(scim_url + "Groups/" + scimGroupId);
        String responseUpdated = updateGroupResource.
                header(SCIMConstants.AUTHORIZATION_HEADER, encodedBasicAuthInfo.getAuthorizationHeader()).
                contentType(SCIMConstants.APPLICATION_JSON).accept(SCIMConstants.APPLICATION_JSON)
                .put(String.class, updatedGroupString);
        log.info("Updated group: " + responseUpdated);
        Assert.assertTrue(responseUpdated.contains("testeng2"));
    }

    @Test(alwaysRun = true, description = "Add new SCIM user member to group testeng2 without removing existing users",
          dependsOnMethods = { "updateGroup" })
    @SetEnvironment(executionEnvironments = {ExecutionEnvironment.ALL})
    public void patchGroup() throws Exception {

        SCIMResponseHandler responseHandler = new SCIMResponseHandler();
        responseHandler.setSCIMClient(scimClient);
        //set the handler in wink client config
        ClientConfig clientConfig = new ClientConfig();
        clientConfig.handlers(new ClientHandler[]{responseHandler});
        //create a wink rest client with the above config
        RestClient restClient = new RestClient(clientConfig);
        BasicAuthInfo encodedBasicAuthInfo = SCIMUtils.getBasicAuthInfo(userInfo);
        //create resource endpoint to access a known user resource.
        Resource groupResource = restClient.resource(scim_url + "Groups/" + scimGroupId);
        String response = groupResource.header(SCIMConstants.AUTHORIZATION_HEADER,
                                               encodedBasicAuthInfo.getAuthorizationHeader())
                                       .contentType(SCIMConstants.APPLICATION_JSON)
                                       .accept(SCIMConstants.APPLICATION_JSON).get(String.class);

        log.info("Retrieved group: " + response);

        //decode retrieved group
        Group decodedGroup =
                (Group) scimClient.decodeSCIMResponse(response.replace("PRIMARY/", ""), SCIMConstants.JSON, 2);

        decodedGroup.setUserMember(scimUserId2, USERNAME2);
        String updatedGroupString = scimClient.encodeSCIMObject(decodedGroup, SCIMConstants.JSON);

        Resource updateGroupResource = restClient.resource(scim_url + "Groups/" + scimGroupId);
        String responseUpdated = updateGroupResource.header(SCIMConstants.AUTHORIZATION_HEADER,
                                                            encodedBasicAuthInfo.getAuthorizationHeader()).
                                                            contentType(SCIMConstants.APPLICATION_JSON)
                                                    .header("X-HTTP-Method-Override", "PATCH")
                                                    .accept(SCIMConstants.APPLICATION_JSON)
                                                    .post(String.class, updatedGroupString);

        log.info("Updated group: " + responseUpdated);

        Assert.assertTrue(userMgtClient.userNameExists("testeng2", USERNAME)
                          && userMgtClient.userNameExists("testeng2", USERNAME2));
    }

    @Test(alwaysRun = true, description = "Add SCIM user", dependsOnMethods = { "patchGroup" })
    @SetEnvironment(executionEnvironments = {ExecutionEnvironment.ALL})
    public void deleteGroup() throws Exception {
        SCIMResponseHandler responseHandler = new SCIMResponseHandler();
        responseHandler.setSCIMClient(scimClient);
        //set the handler in wink client config
        ClientConfig clientConfig = new ClientConfig();
        clientConfig.handlers(new ClientHandler[]{responseHandler});
        //create a wink rest client with the above config
        RestClient restClient = new RestClient(clientConfig);
        BasicAuthInfo encodedBasicAuthInfo = SCIMUtils.getBasicAuthInfo(userInfo);
        //create resource endpoint to access a known user resource.
        Resource groupResource = restClient.resource(scim_url + "Groups/" + scimGroupId);
        //had to set content type for the delete request as well, coz wink client sets */* by default.
        String response = groupResource.
                header(SCIMConstants.AUTHORIZATION_HEADER, encodedBasicAuthInfo.getAuthorizationHeader()).
                accept(SCIMConstants.APPLICATION_JSON).
                delete(String.class);

        //decode the response
        log.info(response.toString());
        Assert.assertFalse(userMgtClient.roleNameExists(DISPLAY_NAME));
    }


    @AfterClass(alwaysRun = true)
    public void cleanOut() throws Exception {
        deleteUser();
    }

    public void createUser() throws CharonException {
        //create SCIM client
        String encodedUser = SCIMUtils.getEncodedSCIMUser(scimClient, USERNAME, "test",
                             new String[] { "scimuser1@gmail.com", "scimuser2@wso2.com" }, "SCIMUser2", "password1",
                             "sinhala", "0772202595");
        //create a apache wink ClientHandler to intercept and identify response messages
        Resource userResource = SCIMUtils.getUserResource(scimClient, scim_url);
        BasicAuthInfo encodedBasicAuthInfo = SCIMUtils.getBasicAuthInfo(userInfo);
        String response = userResource.
                header(SCIMConstants.AUTHORIZATION_HEADER, encodedBasicAuthInfo.getAuthorizationHeader()).
                contentType(SCIMConstants.APPLICATION_JSON).accept(SCIMConstants.APPLICATION_JSON).
                post(String.class, encodedUser);
        log.info(response);
        scimUserId = response.split(",")[0].split(":")[1].replace('"', ' ').trim();


        encodedUser = SCIMUtils.getEncodedSCIMUser(scimClient, USERNAME2, "test2",
                                                          new String[] { "dkasunw2@gmail.com", "dharshanaw2@wso2.com" }, USERNAME2, "testPW2",
                                                          "sinhala", "0712202541");
        response =
                userResource.header(SCIMConstants.AUTHORIZATION_HEADER, encodedBasicAuthInfo.getAuthorizationHeader())
                            .contentType(
                                    SCIMConstants.APPLICATION_JSON).accept(SCIMConstants.APPLICATION_JSON)
                            .post(String.class, encodedUser);
        scimUserId2 = response.split(",")[0].split(":")[1].replace('"', ' ').trim();
    }


    public void deleteUser() {
        ClientConfig clientConfig = new ClientConfig();
        SCIMResponseHandler responseHandler = new SCIMResponseHandler();
        responseHandler.setSCIMClient(scimClient);
        clientConfig.handlers(new ClientHandler[]{responseHandler});
        RestClient restClient = new RestClient(clientConfig);
        BasicAuthInfo encodedBasicAuthInfo = SCIMUtils.getBasicAuthInfo(userInfo);
        Resource userResource = restClient.resource(scim_url + "Users/" + scimUserId);
        String response = userResource.
                header(SCIMConstants.AUTHORIZATION_HEADER, encodedBasicAuthInfo.getAuthorizationHeader()).
                accept(SCIMConstants.APPLICATION_JSON).
                delete(String.class);
    }
}