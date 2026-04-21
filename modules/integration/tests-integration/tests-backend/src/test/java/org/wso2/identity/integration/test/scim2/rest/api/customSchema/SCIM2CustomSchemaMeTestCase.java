/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
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

package org.wso2.identity.integration.test.scim2.rest.api.customSchema;

import io.restassured.RestAssured;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpStatus;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.AutomationContext;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.identity.claim.metadata.mgt.stub.ClaimMetadataManagementServiceClaimMetadataException;
import org.wso2.carbon.identity.claim.metadata.mgt.stub.dto.AttributeMappingDTO;
import org.wso2.carbon.identity.claim.metadata.mgt.stub.dto.ClaimDialectDTO;
import org.wso2.carbon.identity.claim.metadata.mgt.stub.dto.ClaimPropertyDTO;
import org.wso2.carbon.identity.claim.metadata.mgt.stub.dto.ExternalClaimDTO;
import org.wso2.carbon.identity.claim.metadata.mgt.stub.dto.LocalClaimDTO;
import org.wso2.carbon.integration.common.utils.LoginLogoutClient;
import org.wso2.carbon.user.core.UserCoreConstants;
import org.wso2.identity.integration.common.clients.claim.metadata.mgt.ClaimMetadataManagementServiceClient;
import org.wso2.identity.integration.test.rest.api.server.api.resource.v1.model.APIResourceListItem;
import org.wso2.identity.integration.test.rest.api.server.api.resource.v1.model.ScopeGetModel;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.AccessTokenConfiguration;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.ApplicationModel;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.AuthorizedAPICreationModel;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.InboundProtocols;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.OpenIDConnectConfiguration;
import org.wso2.identity.integration.test.restclients.OAuth2RestClient;
import org.wso2.identity.integration.test.scim2.rest.api.SCIM2BaseTest;
import org.wso2.identity.integration.test.utils.CarbonUtils;
import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;
import org.wso2.identity.integration.test.utils.OAuth2Constant;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import java.rmi.RemoteException;
import java.util.Arrays;
import java.util.LinkedHashMap;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;

import static org.wso2.identity.integration.test.scim2.SCIM2BaseTestCase.USERS_ENDPOINT;

/**
 * Test cases for SCIM2 /Me api relates operations with custom schema related claims.
 */
public class SCIM2CustomSchemaMeTestCase extends SCIM2BaseTest {

    private static final Log log = LogFactory.getLog(SCIM2CustomSchemaMeTestCase.class);

    private String userIdEndpointURL;
    private String userId = null;
    private TestUserMode mode;
    private String cookie;
    private ClaimMetadataManagementServiceClient claimMetadataManagementServiceClient = null;

    // Custom schema related constants.
    private static final String CUSTOM_SCHEMA_URI = "urn:scim:schemas:extension:custom:User";
    private static final String CUSTOM_SCHEMA_URI_WITH_ESCAPE_CHARS = "\"urn:scim:schemas:extension:custom:User\"";
    // Country claim related constants.
    private static final String COUNTRY_CLAIM_ATTRIBUTE_NAME = "country";
    private static final String COUNTRY_CLAIM_ATTRIBUTE_URI = CUSTOM_SCHEMA_URI + ":" + COUNTRY_CLAIM_ATTRIBUTE_NAME;
    private static final String COUNTRY_LOCAL_CLAIM_URI = "http://wso2.org/claims/country";
    private static final String COUNTRY_LOCAL_CLAIM_VALUE = "France";
    private static final String COUNTRY_LOCAL_CLAIM_VALUE_AFTER_REPLACE = "Sri Lanka";
    private static final String COUNTRY_LOCAL_CLAIM_VALUE_AFTER_PUT = "India";
    // Manager claim related constants.
    private static final String MANAGER_CLAIM_ATTRIBUTE_NAME = "manager";
    private static final String MANAGER_CLAIM_ATTRIBUTE_URI = "urn:scim:schemas:extension:custom:User:manager";
    private static final String MANAGER_EMAIL_CLAIM_ATTRIBUTE_NAME = "email";
    private static final String MANAGER_EMAIL_CLAIM_ATTRIBUTE_URI =
            MANAGER_CLAIM_ATTRIBUTE_URI + "." + MANAGER_EMAIL_CLAIM_ATTRIBUTE_NAME;
    private static final String MANAGER_LOCAL_CLAIM_URI = "http://wso2.org/claims/manager";
    private static final String MANAGER_EMAIL_LOCAL_CLAIM_URI = "http://wso2.org/claims/emails.work";
    private static final String MANAGER_EMAIL_LOCAL_CLAIM_VALUE = "piraveena@gmail.com";
    private static final String MANAGER_EMAIL_LOCAL_CLAIM_VALUE_AFTER_REPLACE = "piraveenaReplace@gmail.com";

    private static final String ME_ENDPOINT = "/Me";
    private static final String SCIM2_ME_API_IDENTIFIER = "/scim2/Me";
    private static final String SCIM2_USERS_API_IDENTIFIER = "/scim2/Users";
    private static final String USER_TOKEN_SCOPES = "internal_login internal_user_mgt_update";

    private static String USERNAME = "userkim";
    private static String PASSWORD = "Wso2@test123";

    private OAuth2RestClient oauth2RestClient;
    private String appId;
    private String clientId;
    private String clientSecret;
    private String userAccessToken;

    @Factory(dataProvider = "restAPIUserConfigProvider")
    public SCIM2CustomSchemaMeTestCase(TestUserMode userMode) throws Exception {

        super.init(userMode);
        this.context = isServer;
        this.authenticatingUserName = context.getContextTenant().getTenantAdmin().getUserName();
        this.authenticatingCredential = context.getContextTenant().getTenantAdmin().getPassword();
        this.tenant = context.getContextTenant().getDomain();
        this.mode = userMode;
    }

    @DataProvider(name = "restAPIUserConfigProvider")
    public static Object[][] restAPIUserConfigProvider() {

        return new Object[][]{
                {TestUserMode.SUPER_TENANT_ADMIN},
                {TestUserMode.TENANT_ADMIN}
        };
    }

    @BeforeClass(alwaysRun = true)
    public void init() throws Exception {

        super.testInit(swaggerDefinition, tenant);
        Thread.sleep(5000);

        oauth2RestClient = new OAuth2RestClient(serverURL, tenantInfo);

        ApplicationModel appModel = new ApplicationModel();
        appModel.setName("SCIM2CustomeSchemaMeTestApp");
        appModel.setIsManagementApp(true);
        OpenIDConnectConfiguration oidcConfig = new OpenIDConnectConfiguration();
        oidcConfig.setGrantTypes(Arrays.asList("authorization_code", "password"));
        oidcConfig.setCallbackURLs(Collections.singletonList(OAuth2Constant.CALLBACK_URL));
        InboundProtocols inboundProtocols = new InboundProtocols();
        inboundProtocols.setOidc(oidcConfig);
        appModel.setInboundProtocolConfiguration(inboundProtocols);
        appId = oauth2RestClient.createApplication(appModel);

        OpenIDConnectConfiguration oidcDetails = oauth2RestClient.getOIDCInboundDetails(appId);
        clientId = oidcDetails.getClientId();
        clientSecret = oidcDetails.getClientSecret();

        if (!CarbonUtils.isLegacyAuthzRuntimeEnabled()) {
            authorizeAPIForApp(SCIM2_ME_API_IDENTIFIER);
            authorizeAPIForApp(SCIM2_USERS_API_IDENTIFIER);
        }
    }

    @AfterClass(alwaysRun = true)
    public void testFinish() {

        try {
            claimMetadataManagementServiceClient.removeExternalClaim(CUSTOM_SCHEMA_URI, COUNTRY_CLAIM_ATTRIBUTE_URI);
            claimMetadataManagementServiceClient.removeExternalClaim(CUSTOM_SCHEMA_URI,
                    MANAGER_EMAIL_CLAIM_ATTRIBUTE_URI);
            claimMetadataManagementServiceClient.removeExternalClaim(CUSTOM_SCHEMA_URI, MANAGER_CLAIM_ATTRIBUTE_URI);
            claimMetadataManagementServiceClient.removeLocalClaim(MANAGER_LOCAL_CLAIM_URI);
            claimMetadataManagementServiceClient.removeClaimDialect(CUSTOM_SCHEMA_URI);
        } catch (RemoteException | ClaimMetadataManagementServiceClaimMetadataException e) {
           log.error(e);
        }
        try {
            if (appId != null) {
                oauth2RestClient.deleteApplication(appId);
            }
            if (oauth2RestClient != null) {
                oauth2RestClient.closeHttpClient();
            }
        } catch (Exception e) {
            log.error("Error cleaning up OAuth2 application", e);
        }
        super.conclude();
    }

    @BeforeMethod(alwaysRun = true)
    public void testInit() {

        RestAssured.basePath = basePath;
    }

    @Test(description = "Creates simple attribute and complex attributes in urn:scim:schemas:extension:custom:User.")
    private void createClaims() throws Exception {

        AutomationContext context = new AutomationContext("IDENTITY", mode);
        backendURL = context.getContextUrls().getBackEndUrl();
        loginLogoutClient = new LoginLogoutClient(context);
        cookie = loginLogoutClient.login();
        claimMetadataManagementServiceClient = new ClaimMetadataManagementServiceClient(backendURL, cookie);

        //Set claims.
        setCustomDialect();
        setSimpleAttribute();
        setComplexAttribute();

        ExternalClaimDTO[] externalClaimDTOs =
                claimMetadataManagementServiceClient.getExternalClaims(CUSTOM_SCHEMA_URI);
        Assert.assertTrue(Arrays.stream(externalClaimDTOs)
                .anyMatch(claim -> StringUtils.equals(claim.getExternalClaimURI(), COUNTRY_CLAIM_ATTRIBUTE_URI)));
        Assert.assertTrue(Arrays.stream(externalClaimDTOs)
                .anyMatch(claim -> StringUtils.equals(claim.getExternalClaimURI(), MANAGER_CLAIM_ATTRIBUTE_URI)));
        Assert.assertTrue(Arrays.stream(externalClaimDTOs)
                .anyMatch(claim -> StringUtils.equals(claim.getExternalClaimURI(), MANAGER_EMAIL_CLAIM_ATTRIBUTE_URI)));
    }

    @Test(dependsOnMethods = "createClaims", description = "Create user with custom schema dialect.")
    public void testCreateUser() throws Exception {

        String body = readResource("scim2-custom-schema-add-user.json");

        Response response = getResponseOfPost(USERS_ENDPOINT, body, SCIM_CONTENT_TYPE);
        ExtractableResponse<Response> extractableResponse = response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED)
                .header(HttpHeaders.LOCATION, notNullValue())
                .and()
                .assertThat()
                .header(HttpHeaders.CONTENT_TYPE, SCIM_CONTENT_TYPE)
                .extract();
        Assert.assertNotNull(extractableResponse);

        userId = extractableResponse.path("id");
        userIdEndpointURL = USERS_ENDPOINT + "/" + userId;
        Assert.assertNotNull(userId, "The user did not get created.");
        log.info("Created a user with userId :" + userId);

        Object customSchema = extractableResponse.path(CUSTOM_SCHEMA_URI_WITH_ESCAPE_CHARS);
        assertNotNull(customSchema);

        String country = ((LinkedHashMap) customSchema).get(COUNTRY_CLAIM_ATTRIBUTE_NAME).toString();
        assertEquals(country, COUNTRY_LOCAL_CLAIM_VALUE);

        LinkedHashMap manager = (LinkedHashMap) ((LinkedHashMap) customSchema).get(MANAGER_CLAIM_ATTRIBUTE_NAME);
        assertNotNull(manager);

        String managerEMail = manager.get(MANAGER_EMAIL_CLAIM_ATTRIBUTE_NAME).toString();
        assertEquals(managerEMail, MANAGER_EMAIL_LOCAL_CLAIM_VALUE);
    }

    @Test(dependsOnMethods = "testCreateUser", description = "Tests get users and check for claims with /Me api")
    public void testGetMe() throws Exception {

        userAccessToken = getUserToken();
        Response response = io.restassured.RestAssured.given()
                .auth().preemptive().oauth2(userAccessToken)
                .contentType(SCIM_CONTENT_TYPE)
                .header(HttpHeaders.ACCEPT, SCIM_CONTENT_TYPE)
                .when()
                .get(ME_ENDPOINT);
        if (HttpStatus.SC_INTERNAL_SERVER_ERROR == response.getStatusCode()) {
            log.info(">>> Content: >>>" + response.getBody().prettyPrint());
        }
        response.then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .log()
                .ifValidationFails()
                .and()
                .assertThat().header(HttpHeaders.CONTENT_TYPE, SCIM_CONTENT_TYPE);

        LinkedHashMap customSchema =  (LinkedHashMap)response.jsonPath().get(CUSTOM_SCHEMA_URI_WITH_ESCAPE_CHARS);
        assertNotNull(customSchema);

        String country = ((LinkedHashMap) customSchema).get(COUNTRY_CLAIM_ATTRIBUTE_NAME).toString();
        assertEquals(country, COUNTRY_LOCAL_CLAIM_VALUE);

        LinkedHashMap manager = (LinkedHashMap) ((LinkedHashMap) customSchema).get(MANAGER_CLAIM_ATTRIBUTE_NAME);
        assertNotNull(manager);

        String managerEMail = manager.get(MANAGER_EMAIL_CLAIM_ATTRIBUTE_NAME).toString();
        assertEquals(managerEMail, MANAGER_EMAIL_LOCAL_CLAIM_VALUE);
    }

    @Test(dependsOnMethods = "testGetMe" , description = "Tests patch replace operation with custom schema " +
            "attributes with /Me api.")
    public void testPatchReplaceMyAttributes() throws Exception {

        String body = readResource("scim2-custom-schema-patch-replace-attribute.json");

        Response response = getResponseOfPatchWithOAuth2(ME_ENDPOINT, body, userAccessToken);
        ExtractableResponse<Response> extractableResponse = response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .and()
                .assertThat()
                .header(HttpHeaders.CONTENT_TYPE, SCIM_CONTENT_TYPE)
                .extract();
        Assert.assertNotNull(extractableResponse);

        Object customSchema = extractableResponse.path(CUSTOM_SCHEMA_URI_WITH_ESCAPE_CHARS);
        assertNotNull(customSchema);

        String country = ((LinkedHashMap) customSchema).get(COUNTRY_CLAIM_ATTRIBUTE_NAME).toString();
        assertEquals(country, COUNTRY_LOCAL_CLAIM_VALUE_AFTER_REPLACE);

        LinkedHashMap manager = (LinkedHashMap) ((LinkedHashMap) customSchema).get(MANAGER_CLAIM_ATTRIBUTE_NAME);
        assertNotNull(manager);

        String managerEMail = manager.get(MANAGER_EMAIL_CLAIM_ATTRIBUTE_NAME).toString();
        assertEquals(managerEMail, MANAGER_EMAIL_LOCAL_CLAIM_VALUE_AFTER_REPLACE);
    }

    @Test(dependsOnMethods = "testPatchReplaceMyAttributes", description = "Tests patch remove operation for custom " +
            "schema attributes with /Me api")
    public void testPatchRemoveMyAttributes() throws Exception {

        String body = readResource("scim2-custom-schema-patch-remove-attribute.json");
        Response response = getResponseOfPatchWithOAuth2(ME_ENDPOINT, body, userAccessToken);
        ExtractableResponse<Response> extractableResponse = response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .and()
                .assertThat()
                .header(HttpHeaders.CONTENT_TYPE, SCIM_CONTENT_TYPE)
                .extract();
        Assert.assertNotNull(extractableResponse);

        Object customSchema = extractableResponse.path(CUSTOM_SCHEMA_URI_WITH_ESCAPE_CHARS);
        assertNotNull(customSchema);

        Object country = ((LinkedHashMap) customSchema).get(COUNTRY_CLAIM_ATTRIBUTE_NAME);
        assertNull(country);

        LinkedHashMap manager = (LinkedHashMap) ((LinkedHashMap) customSchema).get(MANAGER_CLAIM_ATTRIBUTE_NAME);
        assertNotNull(manager);
        String managerEMail = manager.get(MANAGER_EMAIL_CLAIM_ATTRIBUTE_NAME).toString();
        assertEquals(managerEMail, MANAGER_EMAIL_LOCAL_CLAIM_VALUE_AFTER_REPLACE);
    }

    @Test(dependsOnMethods = "testPatchRemoveMyAttributes", description = "Tests put operation with custom schema " +
            "attributes using /Me api.")
    public void testPutMyAttributes() throws Exception {

        String body = readResource("scim2-custom-schema-put-user.json");
        Response response = getResponseOfPutWithOAuth2(ME_ENDPOINT, body, userAccessToken);
        ExtractableResponse<Response> extractableResponse = response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .and()
                .assertThat()
                .header(HttpHeaders.CONTENT_TYPE, SCIM_CONTENT_TYPE)
                .extract();
        Assert.assertNotNull(extractableResponse);

        Object customSchema = extractableResponse.path(CUSTOM_SCHEMA_URI_WITH_ESCAPE_CHARS);
        assertNotNull(customSchema);

        String country = ((LinkedHashMap) customSchema).get(COUNTRY_CLAIM_ATTRIBUTE_NAME).toString();
        assertEquals(country, COUNTRY_LOCAL_CLAIM_VALUE_AFTER_PUT);

        LinkedHashMap manager = (LinkedHashMap) ((LinkedHashMap) customSchema).get(MANAGER_CLAIM_ATTRIBUTE_NAME);
        assertNull(manager);
    }

    @Test(dependsOnMethods = "testPutMyAttributes", description = "Tests patch add operation with custom schema " +
            "attributes using /Me api.")
    public void testPatchAddMyAttributes() throws Exception {

        String body = readResource("scim2-custom-schema-patch-add-attribute.json");
        Response response = getResponseOfPatchWithOAuth2(ME_ENDPOINT, body, userAccessToken);
        ExtractableResponse<Response> extractableResponse = response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .and()
                .assertThat()
                .header(HttpHeaders.CONTENT_TYPE, SCIM_CONTENT_TYPE)
                .extract();
        Assert.assertNotNull(extractableResponse);

        Object customSchema = extractableResponse.path(CUSTOM_SCHEMA_URI_WITH_ESCAPE_CHARS);
        assertNotNull(customSchema);

        LinkedHashMap manager = (LinkedHashMap) ((LinkedHashMap) customSchema).get(MANAGER_CLAIM_ATTRIBUTE_NAME);
        assertNotNull(manager);

        String managerEMail = manager.get(MANAGER_EMAIL_CLAIM_ATTRIBUTE_NAME).toString();
        assertEquals(managerEMail, "piraveenaAdd@gmail.com");
    }

    @Test(dependsOnMethods = "testPatchAddMyAttributes", description = "Tests delete user operation using /Users api.")
    public void testDeleteUser() throws Exception {

        authenticatingUserName = context.getContextTenant().getTenantAdmin().getUserName();
        authenticatingCredential = context.getContextTenant().getTenantAdmin().getPassword();
        getResponseOfDelete(userIdEndpointURL, SCIM_CONTENT_TYPE).then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_NO_CONTENT);

        getResponseOfGet(userIdEndpointURL, SCIM_CONTENT_TYPE).then().assertThat().statusCode(HttpStatus.SC_NOT_FOUND);
    }

    private String getUserToken() throws Exception {

        String tokenEndpoint = MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenant)
                ? serverURL + "oauth2/token"
                : serverURL + "t/" + tenant + "/oauth2/token";

        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpPost post = new HttpPost(tokenEndpoint);
            post.addHeader(HttpHeaders.AUTHORIZATION, OAuth2Constant.BASIC_HEADER + " " +
                    Base64.encodeBase64String((clientId + ":" + clientSecret).getBytes()).trim());
            post.addHeader(HttpHeaders.CONTENT_TYPE, "application/x-www-form-urlencoded");
            post.addHeader("User-Agent", OAuth2Constant.USER_AGENT);
            List<NameValuePair> params = new ArrayList<>();
            params.add(new BasicNameValuePair("grant_type", OAuth2Constant.OAUTH2_GRANT_TYPE_RESOURCE_OWNER));
            params.add(new BasicNameValuePair("username", USERNAME));
            params.add(new BasicNameValuePair("password", PASSWORD));
            params.add(new BasicNameValuePair("scope", USER_TOKEN_SCOPES));
            post.setEntity(new UrlEncodedFormEntity(params));
            HttpResponse response = httpClient.execute(post);
            String responseStr = EntityUtils.toString(response.getEntity());
            JSONObject json = (JSONObject) JSONValue.parse(responseStr);
            assertNotNull(json.get("access_token"), "Failed to get user token: " + responseStr);
            return json.get("access_token").toString();
        }
    }

    private void authorizeAPIForApp(String apiIdentifier) throws Exception {

        List<APIResourceListItem> filteredAPIResource =
                oauth2RestClient.getAPIResourcesWithFiltering("identifier+eq+" + apiIdentifier);
        if (filteredAPIResource != null && !filteredAPIResource.isEmpty()) {
            String apiId = filteredAPIResource.get(0).getId();
            List<ScopeGetModel> apiResourceScopes = oauth2RestClient.getAPIResourceScopes(apiId);
            AuthorizedAPICreationModel authorizedAPICreationModel = new AuthorizedAPICreationModel();
            authorizedAPICreationModel.setId(apiId);
            authorizedAPICreationModel.setPolicyIdentifier("RBAC");
            apiResourceScopes.forEach(scope -> authorizedAPICreationModel.addScopesItem(scope.getName()));
            oauth2RestClient.addAPIAuthorizationToApplication(appId, authorizedAPICreationModel);
        }
    }

    private void setCustomDialect() throws Exception {

        ClaimDialectDTO claimDialectDTO = new ClaimDialectDTO();
        claimDialectDTO.setClaimDialectURI(CUSTOM_SCHEMA_URI);
        claimMetadataManagementServiceClient.addClaimDialect(claimDialectDTO);
    }

    private void setSimpleAttribute() throws Exception {

        // Create country claim- simple attribute
        ExternalClaimDTO externalClaimDTO = new ExternalClaimDTO();
        externalClaimDTO.setExternalClaimDialectURI(CUSTOM_SCHEMA_URI);
        externalClaimDTO.setExternalClaimURI(COUNTRY_CLAIM_ATTRIBUTE_URI);
        externalClaimDTO.setMappedLocalClaimURI(COUNTRY_LOCAL_CLAIM_URI);
        claimMetadataManagementServiceClient.addExternalClaim(externalClaimDTO);
    }

    private void setComplexAttribute() throws Exception {

        // Create manager claim- complex attribute
        LocalClaimDTO localClaimDTO = new LocalClaimDTO();
        localClaimDTO.setLocalClaimURI(MANAGER_LOCAL_CLAIM_URI);

        AttributeMappingDTO[] attributeMappingDTO = new AttributeMappingDTO[1];
        AttributeMappingDTO attributeMappingDTO1 = new AttributeMappingDTO();
        attributeMappingDTO1.setAttributeName(MANAGER_CLAIM_ATTRIBUTE_NAME);
        attributeMappingDTO1.setUserStoreDomain(UserCoreConstants.PRIMARY_DEFAULT_DOMAIN_NAME);
        attributeMappingDTO[0] = attributeMappingDTO1;
        localClaimDTO.setAttributeMappings(attributeMappingDTO);

        ClaimPropertyDTO[] claimDialectDTOs = new ClaimPropertyDTO[4];
        ClaimPropertyDTO claimPropertyDTO1 = new ClaimPropertyDTO();
        claimPropertyDTO1.setPropertyName("datatype");
        claimPropertyDTO1.setPropertyValue("complex");

        ClaimPropertyDTO claimPropertyDTO2 = new ClaimPropertyDTO();
        claimPropertyDTO2.setPropertyName("subattributes");
        claimPropertyDTO2.setPropertyValue(MANAGER_EMAIL_LOCAL_CLAIM_URI);

        ClaimPropertyDTO claimPropertyDTO3 = new ClaimPropertyDTO();
        claimPropertyDTO3.setPropertyName("Description");
        claimPropertyDTO3.setPropertyValue("Manager");

        ClaimPropertyDTO claimPropertyDTO4 = new ClaimPropertyDTO();
        claimPropertyDTO4.setPropertyName("Name");
        claimPropertyDTO4.setPropertyValue("Manager");

        claimDialectDTOs[0] = claimPropertyDTO1;
        claimDialectDTOs[1] = claimPropertyDTO2;
        claimDialectDTOs[2] = claimPropertyDTO3;
        claimDialectDTOs[3] = claimPropertyDTO4;
        localClaimDTO.setClaimProperties(claimDialectDTOs);
        claimMetadataManagementServiceClient.addLocalClaim(localClaimDTO);

        ExternalClaimDTO managerExternalClaimDTO = new ExternalClaimDTO();
        managerExternalClaimDTO.setExternalClaimDialectURI(CUSTOM_SCHEMA_URI);
        managerExternalClaimDTO.setExternalClaimURI(MANAGER_CLAIM_ATTRIBUTE_URI);
        managerExternalClaimDTO.setMappedLocalClaimURI(MANAGER_LOCAL_CLAIM_URI);
        claimMetadataManagementServiceClient.addExternalClaim(managerExternalClaimDTO);

        ExternalClaimDTO managerEmailExternalClaimDTO = new ExternalClaimDTO();
        managerEmailExternalClaimDTO.setExternalClaimDialectURI(CUSTOM_SCHEMA_URI);
        managerEmailExternalClaimDTO.setExternalClaimURI(MANAGER_EMAIL_CLAIM_ATTRIBUTE_URI);
        managerEmailExternalClaimDTO.setMappedLocalClaimURI(MANAGER_EMAIL_LOCAL_CLAIM_URI);
        claimMetadataManagementServiceClient.addExternalClaim(managerEmailExternalClaimDTO);
    }
}
