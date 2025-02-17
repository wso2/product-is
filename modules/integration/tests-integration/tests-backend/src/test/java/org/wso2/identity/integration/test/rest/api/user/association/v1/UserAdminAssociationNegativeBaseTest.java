/*
 * Copyright (c) 2025, WSO2 LLC. (http://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
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

package org.wso2.identity.integration.test.rest.api.user.association.v1;

import io.restassured.RestAssured;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpStatus;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.identity.application.common.model.idp.xsd.IdentityProvider;
import org.wso2.identity.integration.test.rest.api.user.common.model.UserObject;
import org.wso2.identity.integration.test.restclients.SCIM2RestClient;

import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Map;

import javax.xml.xpath.XPathExpressionException;

import static org.hamcrest.core.Is.is;

/**
 * Test REST API for managing user associations.
 */
public class UserAdminAssociationNegativeBaseTest extends UserAssociationTestBase {

    private static final Log log = LogFactory.getLog(UserMeSuccessTestBase.class);
    private static final String TEST_USER_1 = "TestUser01";
    private static final String TEST_USER_PW = "Test@123";
    private static final String EXTERNAL_USER_ID_1 = "ExternalUser1";
    private static final String EXTERNAL_IDP_NAME = "ExternalIDP";
    SCIM2RestClient scim2RestClient;
    private String testUser1Id = "randomUserId";

    public UserAdminAssociationNegativeBaseTest() throws Exception {

        super.init(TestUserMode.SUPER_TENANT_ADMIN);
        this.context = isServer;
        this.authenticatingUserName = context.getContextTenant().getTenantAdmin().getUserName();
        this.authenticatingCredential = context.getContextTenant().getTenantAdmin().getPassword();
        this.tenant = context.getContextTenant().getDomain();
        this.scim2RestClient = new SCIM2RestClient(serverURL, tenantInfo);
    }

    @BeforeClass(alwaysRun = true)
    public void init() throws XPathExpressionException, RemoteException {

        super.testInit(API_VERSION, swaggerDefinition, tenant);
    }

    @AfterClass(alwaysRun = true)
    public void testConclude() {

        super.conclude();
        try {
            deleteUser(testUser1Id);
        } catch (Exception e) {
            log.error("Error while deleting the users :" + TEST_USER_1 + ".");
        }
    }

    @BeforeMethod(alwaysRun = true)
    public void testInit() {

        RestAssured.basePath = basePath;
    }

    @AfterMethod(alwaysRun = true)
    public void testFinish() {

        RestAssured.basePath = StringUtils.EMPTY;
    }

    @Test
    public void testCreateFederatedAssociationWithoutUser() {

        createFederatedAssociation(testUser1Id, EXTERNAL_USER_ID_1, HttpStatus.SC_NOT_FOUND);
    }

    @Test(dependsOnMethods = "testCreateFederatedAssociationWithoutUser")
    public void testCreateFederatedAssociationWithoutIdP() {

        try {
            testUser1Id = createUser(TEST_USER_1, TEST_USER_PW);
        } catch (Exception e) {
            log.error("Error while creating the users :" + TEST_USER_1 + ".");
        }
        createFederatedAssociation(testUser1Id, null, HttpStatus.SC_BAD_REQUEST);
    }

    private void createFederatedAssociation(String userId, String federatedUserId, int statusCode) {

        String body = "{\"idp\":\"" + EXTERNAL_IDP_NAME + "\",\"federatedUserId\":\"" + federatedUserId + "\"}";
        String url = String.format(FEDERATED_ASSOCIATION_ENDPOINT_URI, userId);
        getResponseOfPost(url, body)
                .then()
                .assertThat()
                .statusCode(statusCode)
                .log().ifValidationFails();
    }

    protected String createUser(String username, String password) throws Exception {

        log.info("Creating User " + username);
        UserObject userObject = new UserObject().userName(username).password(password);
        return scim2RestClient.createUser(userObject);
    }

    protected void deleteUser(String userId) throws Exception {

        log.info("Deleting User " + userId);
        scim2RestClient.deleteUser(userId);
    }
}
