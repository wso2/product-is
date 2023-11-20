/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.identity.integration.test.rest.api.user.challenge.v1;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.apache.axis2.AxisFault;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpStatus;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.um.ws.api.stub.PermissionDTO;
import org.wso2.identity.integration.common.clients.usermgt.remote.RemoteUserStoreManagerServiceClient;

import java.io.IOException;
import java.rmi.RemoteException;
import javax.xml.xpath.XPathExpressionException;

public class UserMeNegativeTest extends UserChallengeTestBase {
    protected String user;
    protected String role;

    @Factory(dataProvider = "restAPIUserConfigProvider")
    public UserMeNegativeTest(TestUserMode userMode, String userName, String password, String role, String permission,
                              boolean isAddUser)
            throws Exception {
        super.init(userMode);
        this.context = isServer;
        this.remoteUSMServiceClient = new RemoteUserStoreManagerServiceClient(backendURL, sessionCookie);

        if (isAddUser) {
            if (!this.remoteUSMServiceClient.isExistingRole(role)) {
                PermissionDTO perm = new PermissionDTO();
                perm.setResourceId(permission);
                perm.setAction(CarbonConstants.UI_PERMISSION_ACTION);
                remoteUSMServiceClient.addRole(role, new String[]{}, new PermissionDTO[]{perm});
            }
            this.remoteUSMServiceClient.addUser(userName, password, new
                            String[]{role}, null, "default", false);
        }
        this.user = userName;
        this.role = role;
        this.tenant = tenantInfo.getDomain();
        this.authenticatingUserName = userName.concat("@").concat(this.tenant);
        this.authenticatingCredential = password;

    }

    @BeforeClass(alwaysRun = true)
    public void init() throws XPathExpressionException, RemoteException {

        super.testInit(API_VERSION, swaggerDefinition, tenant);
        initUrls("me");
    }

    @AfterClass(alwaysRun = true)
    @Override
    public void testConclude() throws Exception {

        super.conclude();
        this.remoteUSMServiceClient.deleteUser(this.user);
        this.remoteUSMServiceClient.deleteRole(this.role);
    }

    @BeforeMethod(alwaysRun = true)
    public void testInit() {

        RestAssured.basePath = basePath;
    }

    @AfterMethod(alwaysRun = true)
    public void testFinish() {

        RestAssured.basePath = StringUtils.EMPTY;
    }

    @DataProvider(name = "restAPIUserConfigProvider")
    public static Object[][] restAPIUserConfigProvider() {
        return new Object[][]{
                {TestUserMode.SUPER_TENANT_USER, "restApiMeUser1", "Pass@123", "testLoginRole",
                        "/permission/admin/login", true},
                {TestUserMode.TENANT_USER, "restApiMeUser1", "Pass@123", "testLoginRole",
                        "/permission/admin/login", true}
        };
    }

    @Test
    public void testAnswerChallengeForNonExistingSet() throws IOException {

        String body = readResource("challenge-answers-non-existing-set.json");
        Response response = getResponseOfPost(this.userChallengeAnswersEndpointURI, body);
        validateErrorResponse(response, HttpStatus.SC_BAD_REQUEST, "CQM-18017", this.tenant);
    }

    @Test
    public void testAnswerChallengeForNonExistingQuestion() throws IOException {

        String body = readResource("challenge-answers-non-existing-question.json");
        Response response = getResponseOfPost(this.userChallengeAnswersEndpointURI, body);
        validateErrorResponse(response, HttpStatus.SC_BAD_REQUEST, "CQM-18017", this.tenant);
    }

    @Test
    public void testAnswerChallengesWhenAlreadyAnswered() throws IOException {

        String body = readResource("challenge-answers-array-correct-1.json");
        getResponseOfPost(this.userChallengeAnswersEndpointURI, body);
        Response response = getResponseOfPost(this.userChallengeAnswersEndpointURI, body);
        validateErrorResponse(response, HttpStatus.SC_CONFLICT, "CQM-10012", this.tenant);
        getResponseOfDelete(this.userChallengeAnswersEndpointURI);
    }

    @Test
    public void testAnswerMultipleChallengesFromSameSet() throws IOException {

        String body = readResource("challenge-answers-array-multiple-from-same-set.json");
        getResponseOfPost(this.userChallengeAnswersEndpointURI, body);
        Response response = getResponseOfPost(this.userChallengeAnswersEndpointURI, body);
        validateErrorResponse(response, HttpStatus.SC_BAD_REQUEST, "CQM-20053", this.tenant);
    }

    @Test
    public void testAnswerChallengeWhenAlreadyAnswered() throws IOException {

        String endpointURI = String.format(this.userChallengeAnswerEndpointURI, "challengeQuestion1");
        String body = readResource("challenge-answer-set1-q4-correct.json");
        getResponseOfPost(endpointURI, body);
        Response response = getResponseOfPost(endpointURI, body);
        validateErrorResponse(response, HttpStatus.SC_CONFLICT, "CQM-10014", this.tenant);
        getResponseOfDelete(this.userChallengeAnswersEndpointURI);
    }

    @Test
    public void testUpdateChallengesWhenNotSet() throws IOException {

        String body = readResource("challenge-answers-array-correct-1.json");
        Response response = getResponseOfPut(this.userChallengeAnswersEndpointURI, body);
        validateErrorResponse(response, HttpStatus.SC_NOT_FOUND, "CQM-10013", this.tenant);
    }

    @Test
    public void testUpdateChallengeWhenNotSet() throws IOException {

        String endpointURI = String.format(this.userChallengeAnswerEndpointURI, "challengeQuestion1");
        String body = readResource("challenge-answer-set1-q4-correct.json");
        Response response = getResponseOfPut(endpointURI, body);
        validateErrorResponse(response, HttpStatus.SC_NOT_FOUND, "CQM-10015", this.tenant);
    }

    @Test
    public void testDeleteChallengeAnswersWhenNotSet() {

        String endpointURI = String.format(this.userChallengeAnswersEndpointURI);
        Response response = getResponseOfDelete(endpointURI);
        validateErrorResponse(response, HttpStatus.SC_NOT_FOUND, "CQM-10013", this.tenant);
    }

    @Test
    public void testDeleteChallengeAnswerWhenNotSet() {

        String endpointURI = String.format(this.userChallengeAnswerEndpointURI, "challengeQuestion1");
        Response response = getResponseOfDelete(endpointURI);
        validateErrorResponse(response, HttpStatus.SC_NOT_FOUND, "CQM-10015", this.tenant);
    }
}

