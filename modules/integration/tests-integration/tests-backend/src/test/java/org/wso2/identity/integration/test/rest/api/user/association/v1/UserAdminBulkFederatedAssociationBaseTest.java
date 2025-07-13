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

import com.atlassian.oai.validator.restassured.OpenApiValidationFilter;
import io.restassured.RestAssured;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import io.restassured.response.ValidatableResponse;
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
import org.wso2.identity.integration.test.rest.api.server.idp.v1.model.IdentityProviderPOSTRequest;
import org.wso2.identity.integration.test.rest.api.user.common.model.UserObject;
import org.wso2.identity.integration.test.restclients.IdpMgtRestClient;
import org.wso2.identity.integration.test.restclients.SCIM2RestClient;

import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.xpath.XPathExpressionException;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * Test for Bulk Rest API for Federated Associations.
 */
public class UserAdminBulkFederatedAssociationBaseTest extends UserAssociationTestBase {

    private static final Log log = LogFactory.getLog(UserAdminBulkFederatedAssociationBaseTest.class);

    private static final String TEST_USER_1 = "TestUser01";
    private static final String TEST_USER_2 = "TestUser02";
    private static final String TEST_USER_PW = "Test@123";
    private static final String EXTERNAL_IDP_NAME = "ExternalIDP";
    private static final String EXTERNAL_USER_ID_1 = "ExternalUser1";
    private static final String EXTERNAL_USER_ID_2 = "ExternalUser2";
    private static final String EXTERNAL_USER_ID_3 = "ExternalUser3";
    private static final String EXTERNAL_USER_ID_4 = "ExternalUser4";

    private static final String bulk_api_pass =
            "{\n" + "  \"failOnErrors\": 0,\n" + "  \"operations\": [\n" + "    {\n" + "      \"method\": \"POST\",\n" +
                    "      \"bulkId\": \"ytrewq\",\n" +
                    "      \"path\": \"/b1781d25-bde5-460a-a58a-8fe8dbfd8487/federated-associations\",\n" +
                    "      \"data\": {\n" + "        \"idp\": \"exampleIdP\",\n" +
                    "        \"federatedUserId\": \"john@example.com\"\n" + "      }\n" + "    },\n" + "    {\n" +
                    "      \"method\": \"POST\",\n" + "      \"bulkId\": \"qweascae\",\n" +
                    "      \"path\": \"/b1781d25-bde5-460a-a58a-8fe8dbfd8487/federated-associations\",\n" +
                    "      \"data\": {\n" + "        \"idp\": \"exampleIdP\",\n" +
                    "        \"federatedUserId\": \"sam@example.com\"\n" + "      }\n" + "    },\n" + "    {\n" +
                    "      \"method\": \"DELETE\",\n" + "      \"bulkId\": \"ieabhvsd\",\n" +
                    "      \"path\": \"/b1781d25-bde5-460a-a58a-8fe8dbfd8487/federated-associations/ff2d1f1e-0736-43c1-b402-cc9ef5a1c184\"\n" +
                    "    },\n" + "    {\n" + "      \"method\": \"DELETE\",\n" + "      \"bulkId\": \"pqd98aks\",\n" +
                    "      \"path\": \"/b1781d25-bde5-460a-a58a-8fe8dbfd8487/federated-associations/ee2d1f1e-0656-43c1-b402-cc56f5a1c184\"\n" +
                    "    }\n" + "  ]\n" + "}";
    private static final String bulkAPIBody =
            "{\n" + "  %s,\n" + "  \"operations\": [\n" + "    {\n" + "      \"method\": \"POST\",\n" +
                    "      \"bulkId\": \"ytrewq\",\n" + "      \"path\": \"/%s/federated-associations\",\n" +
                    "      \"data\": {\n" + "        \"idp\": \"%s\",\n" + "        \"federatedUserId\": \"%s\"\n" +
                    "      }\n" + "    },\n" + "    {\n" + "      \"method\": \"POST\",\n" +
                    "      \"bulkId\": \"qweascae\",\n" + "      \"path\": \"/%s/federated-associations\",\n" +
                    "      \"data\": {\n" + "        \"idp\": \"%s\",\n" + "        \"federatedUserId\": \"%s\"\n" +
                    "      }\n" + "    },\n" + "    {\n" + "      \"method\": \"DELETE\",\n" +
                    "      \"bulkId\": \"ieabhvsd\",\n" + "      \"path\": \"/%s/federated-associations/%s\"\n" +
                    "    },\n" + "    {\n" + "      \"method\": \"DELETE\",\n" + "      \"bulkId\": \"pqd98aks\",\n" +
                    "      \"path\": \"/%s/federated-associations/%s\"\n" + "    }\n" + "  ]\n" + "}";

    SCIM2RestClient scim2RestClient;
    IdpMgtRestClient idpMgtRestClient;
    private final Map<String, List<String>> userAssociationIdHolder = new HashMap<>();
    private String testUser1Id;
    private String testUser2Id;
    private final String invalidUserId1 = "invalidUserId1";
    private String idpId;

    public UserAdminBulkFederatedAssociationBaseTest() throws Exception {

        super.init(TestUserMode.SUPER_TENANT_ADMIN);
        this.context = isServer;
        this.authenticatingUserName = context.getContextTenant().getTenantAdmin().getUserName();
        this.authenticatingCredential = context.getContextTenant().getTenantAdmin().getPassword();
        this.tenant = context.getContextTenant().getDomain();
        this.scim2RestClient = new SCIM2RestClient(serverURL, tenantInfo);
        this.idpMgtRestClient = new IdpMgtRestClient(serverURL, tenantInfo);
    }

    @BeforeClass(alwaysRun = true)
    public void init() throws XPathExpressionException, RemoteException {

        super.testInit(API_VERSION, swaggerDefinition, tenant);
        try {
            testUser1Id = createUser(TEST_USER_1, TEST_USER_PW);
            testUser2Id = createUser(TEST_USER_2, TEST_USER_PW);
            createIdP();
        } catch (Exception e) {
            log.error("Error while creating the users :" + TEST_USER_1 + ", and " + TEST_USER_2, e);
        }
    }

    @AfterClass(alwaysRun = true)
    public void testConclude() {

        super.conclude();
        try {
            deleteUser(testUser1Id);
            deleteUser(testUser2Id);
            deleteIdP();
        } catch (Exception e) {
            log.error("Error while deleting the users :" + TEST_USER_1 + ", and " + TEST_USER_2, e);
        }
    }

    @DataProvider
    public Object[][] bulkDataProvider() {

        return new Object[][]{
                // All are valid operations
                {"\"failOnErrors\": 0", testUser1Id, EXTERNAL_USER_ID_3, testUser2Id, EXTERNAL_USER_ID_4, testUser1Id,
                        true, testUser2Id, true, 4, new int[]{201, 201, 204, 204}},
                // First operation is invalid and, the failOnErrors flag is false.
                {"\"failOnErrors\": 0", invalidUserId1, EXTERNAL_USER_ID_3, testUser2Id, EXTERNAL_USER_ID_4, testUser1Id,
                        true, testUser2Id, true, 4, new int[]{404, 201, 204, 204}},
                // First operation is invalid and, the failOnErrors flag is true.
                {"\"failOnErrors\": 1", invalidUserId1, EXTERNAL_USER_ID_3, testUser2Id, EXTERNAL_USER_ID_4,
                        testUser1Id, true, testUser2Id, true, 1, new int[]{404}}
        };
    }

    @BeforeMethod(alwaysRun = true)
    public void testInit() {

        RestAssured.basePath = basePath;
        createFederatedAssociationById(testUser1Id, EXTERNAL_USER_ID_1);
        getFederatedAssociations(testUser1Id);
        createFederatedAssociationById(testUser2Id, EXTERNAL_USER_ID_2);
        getFederatedAssociations(testUser2Id);
    }

    @AfterMethod(alwaysRun = true)
    public void testFinish() {

        removeFederatedAssociationById(testUser1Id);
        removeFederatedAssociationById(testUser2Id);
        RestAssured.basePath = StringUtils.EMPTY;
    }

    @Test(dataProvider = "bulkDataProvider")
    public void testPostOperation(String failOnErrors, String operation1UserId, String operation1ExternalUserId,
                                  String operation2UserId, String operation2ExternalUserId, String operation3UserId,
                                  boolean validOperation3AssociationId, String operation4UserId,
                                  boolean validOperation4AssociationId, int responseOperations, int[] operationCodes) {

        String apiBody =
                String.format(bulkAPIBody, failOnErrors, operation1UserId, EXTERNAL_IDP_NAME, operation1ExternalUserId,
                        operation2UserId, EXTERNAL_IDP_NAME, operation2ExternalUserId, operation3UserId,
                        userAssociationIdHolder.get(operation3UserId).get(0), operation4UserId,
                        userAssociationIdHolder.get(operation4UserId).get(0));

        ValidatableResponse response =
                getResponseOfPost(BULK_FEDERATED_ASSOCIATION_ENDPOINT_URI, apiBody).then().statusCode(200)
                        .header("Content-Type", "application/json").body("operations", hasSize(responseOperations));

        for (int i = 0; i < responseOperations; i++) {
            String bulkId = response.extract().path("operations[" + i + "].bulkId");
            int statusCode = response.extract().path("operations[" + i + "].status.statusCode");

            // Verify the bulkId and statusCode.
            Assert.assertNotNull(bulkId, "Bulk ID should not be null for operation " + (i + 1));
            Assert.assertEquals(statusCode, operationCodes[i], "Status code mismatch for operation " + (i + 1));
        }
    }

    private void getFederatedAssociations(String userId) {

        String url = String.format(FEDERATED_ASSOCIATION_ENDPOINT_URI, userId);
        Response response = getResponseOfGet(url);
        response.then().assertThat().statusCode(HttpStatus.SC_OK).log().ifValidationFails();

        JsonPath jsonPath = response.jsonPath();
        userAssociationIdHolder.put(userId, jsonPath.getList("id"));
    }

    private void createFederatedAssociationById(String userId, String federatedUserId) {

        String body = "{\"idp\":\"" + EXTERNAL_IDP_NAME + "\",\"federatedUserId\":\"" + federatedUserId + "\"}";
        String url = String.format(FEDERATED_ASSOCIATION_ENDPOINT_URI, userId);
        getResponseOfPost(url, body).then().assertThat().statusCode(HttpStatus.SC_OK).log().ifValidationFails();
    }

    public void removeFederatedAssociationById(String userId) {

        String url = String.format(FEDERATED_ASSOCIATION_ENDPOINT_URI, userId);
        // status code can be 204 or 400. If 400, response should have an element code with value UAA-10003
        try {
            Response response = getResponseOfDelete(url);
        } catch (OpenApiValidationFilter.OpenApiValidationException e) {
            log.info("OpenAPI validation failed for the delete operation. " +
                    "This is expected if the user does not have any federated associations.");
        }
        userAssociationIdHolder.remove(userId);
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

    private void createIdP() {

        try {
            log.info("Creating Identity Provider " + EXTERNAL_IDP_NAME);
            IdentityProviderPOSTRequest identityProviderPOSTRequest = new IdentityProviderPOSTRequest();
            identityProviderPOSTRequest.name(EXTERNAL_IDP_NAME);
            idpId = idpMgtRestClient.createIdentityProvider(identityProviderPOSTRequest);
        } catch (Exception e) {
            Assert.fail("Error while trying to create Identity Provider", e);
        }
    }

    private void deleteIdP() {

        try {
            log.info("Deleting Identity Provider " + EXTERNAL_IDP_NAME);
            idpMgtRestClient.deleteIdp(idpId);
        } catch (Exception e) {
            Assert.fail("Error while trying to delete Identity Provider", e);
        }
    }
}
