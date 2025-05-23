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

package org.wso2.identity.integration.test.rest.api.user.association.v1;

import io.restassured.RestAssured;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.Header;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.message.BasicHeader;
import org.json.JSONArray;
import org.json.JSONObject;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.identity.integration.common.utils.ISIntegrationTest;
import org.wso2.identity.integration.test.rest.api.server.idp.v1.model.IdentityProviderPOSTRequest;
import org.wso2.identity.integration.test.rest.api.user.common.model.UserObject;
import org.wso2.identity.integration.test.restclients.IdpMgtRestClient;
import org.wso2.identity.integration.test.restclients.RestBaseClient;
import org.wso2.identity.integration.test.restclients.SCIM2RestClient;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.rmi.RemoteException;
import java.util.Base64;
import javax.xml.xpath.XPathExpressionException;

import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.core.Is.is;
import static org.wso2.identity.integration.test.restclients.RestBaseClient.AUTHORIZATION_ATTRIBUTE;
import static org.wso2.identity.integration.test.restclients.RestBaseClient.BASIC_AUTHORIZATION_ATTRIBUTE;
import static org.wso2.identity.integration.test.restclients.RestBaseClient.CONTENT_TYPE_ATTRIBUTE;

/**
 * Test REST API for managing user associations.
 */
public class UserMeSuccessTestBase extends UserAssociationTestBase {

    private static final Log log = LogFactory.getLog(UserMeSuccessTestBase.class);
    private static final String TEST_USER_1 = "TestUser01";
    private static final String TEST_USER_2 = "TestUser02";
    private static final String TEST_USER_PW = "Test@123";
    private static final String EXTERNAL_IDP_NAME = "ExternalIDP";
    private static final String EXTERNAL_USER_ID_1 = "ExternalUser1";
    private static final String EXTERNAL_USER_ID_2 = "ExternalUser2";
    private String federatedAssociationIdHolder;
    private String associatedUserIdHolder;
    private TestUserMode userMode;
    private SCIM2RestClient scim2RestClient;
    private IdpMgtRestClient idpMgtRestClient;
    private String testUserId1;
    private String testUserId2;
    private String adminId;
    private String idpId;
    private RestBaseClient restBaseClient;

    @Factory(dataProvider = "restAPIUserConfigProvider")
    public UserMeSuccessTestBase(TestUserMode userMode) throws Exception {
        super.init(userMode);
        this.userMode = userMode;
        this.context = isServer;
        this.authenticatingUserName = context.getContextTenant().getTenantAdmin().getUserName();
        this.authenticatingCredential = context.getContextTenant().getTenantAdmin().getPassword();
        this.tenant = context.getContextTenant().getDomain();
    }

    @BeforeClass(alwaysRun = true)
    public void init() throws XPathExpressionException, RemoteException {

        scim2RestClient = new SCIM2RestClient(serverURL, tenantInfo);
        idpMgtRestClient = new IdpMgtRestClient(serverURL, tenantInfo);
        restBaseClient = new RestBaseClient();

        super.testInit(API_VERSION, swaggerDefinition, tenant);
        initUrls("me");

        try {
            testUserId1 = createUser(TEST_USER_1, TEST_USER_PW);
            testUserId2 = createUser(TEST_USER_2, TEST_USER_PW);

            adminId = getLoginUserId();
            idpId = createIdP(EXTERNAL_IDP_NAME);
            createFederatedAssociation(EXTERNAL_IDP_NAME, EXTERNAL_USER_ID_1);
            createFederatedAssociation(EXTERNAL_IDP_NAME, EXTERNAL_USER_ID_2);
        } catch (Exception e) {
            log.error("Error while creating the users :" + TEST_USER_1 + ", and " + TEST_USER_2, e);
        }
    }

    @AfterClass(alwaysRun = true)
    public void testConclude() {

        super.conclude();

        try {
            deleteUser(TEST_USER_1, testUserId1);
            deleteUser(TEST_USER_2, testUserId2);

            deleteIdP(idpId);

            // Clear up any possible associations that may have created while running tests. The following delete
            // operations are expected to silently ignore any non-existing federated associations.
            deleteFederatedAssociation();
            scim2RestClient.closeHttpClient();
            idpMgtRestClient.closeHttpClient();
            restBaseClient.client.close();
        } catch (Exception e) {
            log.error("Error while deleting the users :" + TEST_USER_1 + ", and " + TEST_USER_2, e);
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

    @DataProvider(name = "restAPIUserConfigProvider")
    public static Object[][] restAPIUserConfigProvider() {

        return new Object[][]{
                {TestUserMode.SUPER_TENANT_ADMIN},
                {TestUserMode.TENANT_ADMIN}
        };
    }

    @Test
    public void testCreateAssociation() throws IOException {

        String associationBody01;
        String associationBody02;
        if (TestUserMode.SUPER_TENANT_ADMIN.equals(userMode)) {
            associationBody01 = readResource("association-creation-1.json");
            associationBody02 = readResource("association-creation-2.json");
        } else {
            associationBody01 = readResource("association-creation-tenant-1.json").replace("TENANT",
                    tenant);
            associationBody02 = readResource("association-creation-tenant-2.json").replace("TENANT",
                    tenant);
        }
        createLocalAssociation(associationBody01);
        createLocalAssociation(associationBody02);
    }

    @Test(dependsOnMethods = {"testCreateAssociation"})
    public void testGetAssociations() {

        Response response = getResponseOfGet(this.userAssociationEndpointURI);
        response.then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .log().ifValidationFails()
                .body("size()", is(2))
                .body("username", hasItems(TEST_USER_1, TEST_USER_2))
                .body("userStoreDomain", hasItems("PRIMARY"));
        JsonPath jsonPath = response.jsonPath();
        associatedUserIdHolder = jsonPath.getList("findAll{it.username=='" + TEST_USER_1 + "'}.userId").get(0)
                .toString();
    }

    @Test(dependsOnMethods = {"testGetAssociations"})
    public void testRemoveAssociationById() {

        getResponseOfDelete(this.userAssociationEndpointURI + "/" + associatedUserIdHolder)
                .then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_NO_CONTENT);
    }

    @Test(dependsOnMethods = {"testRemoveAssociationById"})
    public void testRemoveAssociations() {

        getResponseOfDelete(this.userAssociationEndpointURI)
                .then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_NO_CONTENT);
    }

    @Test
    public void testGetFederatedAssociations() {

        Response response = getResponseOfGet(this.federatedUserAssociationEndpointURI);
        response.then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .log().ifValidationFails()
                .body("size()", is(2))
                .body("federatedUserId", hasItems(EXTERNAL_USER_ID_1, EXTERNAL_USER_ID_2));
        JsonPath jsonPath = response.jsonPath();
        federatedAssociationIdHolder = jsonPath.getList("findAll{it.federatedUserId=='" + EXTERNAL_USER_ID_1
                + "'}.id").get(0).toString();
    }

    @Test(dependsOnMethods = {"testGetFederatedAssociations"})
    public void testRemoveFederatedAssociationById() {

        getResponseOfDelete(this.federatedUserAssociationEndpointURI + "/"
                + federatedAssociationIdHolder)
                .then().log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_NO_CONTENT);
    }

    @Test(dependsOnMethods = {"testRemoveFederatedAssociationById"})
    public void testRemoveFederatedAssociation() {

        getResponseOfDelete(this.federatedUserAssociationEndpointURI)
                .then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_NO_CONTENT);
    }

    protected String createUser(String username, String password) throws Exception {

        log.info("Creating User " + username);
        UserObject userObject = new UserObject();
        userObject.setUserName(username);
        userObject.setPassword(password);
        return scim2RestClient.createUser(userObject);
    }

    protected void createFederatedAssociation(String idpName, String associatedUserId)
            throws Exception {

        log.info("Creating federated association with the idp: " + idpName + ", and associated user: "
                + associatedUserId);
        String endpoint = "/api/users/v1/" + adminId + "/federated-associations" ;
        String requestURL = serverURL + ISIntegrationTest.getTenantedRelativePath(endpoint,
                tenantInfo.getDomain());
        String body = "{\n" +
                "  \"federatedUserId\": \"" + associatedUserId + "\",\n" +
                "  \"idp\": \"" + idpName + "\"\n" +
                "}";

        Header[] headers = new Header[2];
        headers[0] = new BasicHeader(AUTHORIZATION_ATTRIBUTE, BASIC_AUTHORIZATION_ATTRIBUTE +
                org.apache.commons.codec.binary.Base64.encodeBase64String((
                        tenantInfo.getTenantAdmin().getUserName() + ":" +
                        tenantInfo.getTenantAdmin().getPassword()).getBytes()).trim());
        headers[1] = new BasicHeader(CONTENT_TYPE_ATTRIBUTE, "application/json");
        CloseableHttpResponse response = restBaseClient.getResponseOfHttpPost(requestURL, body, headers);
        Assert.assertEquals(response.getStatusLine().getStatusCode(), HttpStatus.SC_OK);
    }

    protected void deleteUser(String username, String testUserId) throws Exception {

        log.info("Deleting User " + username);
        scim2RestClient.deleteUser(testUserId);
    }

    protected void deleteFederatedAssociation() throws Exception {

        String endpoint = "/api/users/v1/" + adminId + "/federated-associations" ;
        String requestURL = serverURL + ISIntegrationTest.getTenantedRelativePath(endpoint,
                tenantInfo.getDomain());
        Header[] headers = new Header[1];
        headers[0] = new BasicHeader(AUTHORIZATION_ATTRIBUTE, BASIC_AUTHORIZATION_ATTRIBUTE +
                org.apache.commons.codec.binary.Base64.encodeBase64String((
                        tenantInfo.getTenantAdmin().getUserName() + ":" +
                        tenantInfo.getTenantAdmin().getPassword()).getBytes()).trim());
        restBaseClient.getResponseOfHttpDelete(requestURL, headers);
    }

    private void createLocalAssociation(String body) {

        getResponseOfPost(this.userAssociationEndpointURI, body)
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED)
                .log().ifValidationFails();
    }

    private String getEncodedUserId(String username) {

        return Base64.getUrlEncoder().encodeToString(username.getBytes(StandardCharsets.UTF_8));
    }

    private String createIdP(String idpName) throws Exception {

        IdentityProviderPOSTRequest idpPostRequest = new IdentityProviderPOSTRequest()
                .name(idpName);
        return idpMgtRestClient.createIdentityProvider(idpPostRequest);
    }

    private void deleteIdP(String idpId) throws Exception {

        idpMgtRestClient.deleteIdp(idpId);
    }

    private String getLoginUserId() throws Exception {

        String userSearchReq = new JSONObject()
                .put("schemas", new JSONArray().put("urn:ietf:params:scim:api:messages:2.0:SearchRequest"))
                .put("attributes", new JSONArray().put("id"))
                .put("filter", "userName eq " + tenantInfo.getTenantAdmin().getUserNameWithoutDomain())
                .toString();

        org.json.simple.JSONObject userSearchResponse = scim2RestClient.searchUser(userSearchReq);
        return ((org.json.simple.JSONObject) ((org.json.simple.JSONArray) userSearchResponse.get("Resources")).get(0))
                .get("id").toString();
    }
}
