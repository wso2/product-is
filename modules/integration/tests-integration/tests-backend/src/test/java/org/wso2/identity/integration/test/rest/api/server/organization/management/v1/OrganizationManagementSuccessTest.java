/*
 * Copyright (c) 2023, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.identity.integration.test.rest.api.server.organization.management.v1;

import com.nimbusds.oauth2.sdk.AccessTokenResponse;
import com.nimbusds.oauth2.sdk.AuthorizationGrant;
import com.nimbusds.oauth2.sdk.ResourceOwnerPasswordCredentialsGrant;
import com.nimbusds.oauth2.sdk.Scope;
import com.nimbusds.oauth2.sdk.TokenRequest;
import com.nimbusds.oauth2.sdk.auth.ClientAuthentication;
import com.nimbusds.oauth2.sdk.auth.ClientSecretBasic;
import com.nimbusds.oauth2.sdk.auth.Secret;
import com.nimbusds.oauth2.sdk.http.HTTPResponse;
import com.nimbusds.oauth2.sdk.id.ClientID;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.simple.parser.JSONParser;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.identity.integration.test.rest.api.server.api.resource.v1.model.APIResourceListItem;
import org.wso2.identity.integration.test.rest.api.server.api.resource.v1.model.ScopeGetModel;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.ApplicationModel;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.ApplicationPatchModel;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.AssociatedRolesConfig;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.AuthorizedAPICreationModel;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.InboundProtocols;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.OpenIDConnectConfiguration;
import org.wso2.identity.integration.test.rest.api.server.organization.management.v1.model.OrganizationLevel;
import org.wso2.identity.integration.test.restclients.OAuth2RestClient;
import org.wso2.identity.integration.test.utils.OAuth2Constant;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.wso2.identity.integration.test.rest.api.server.organization.management.v1.OrganizationManagementTestData.APPLICATION_PAYLOAD;
import static org.wso2.identity.integration.test.rest.api.server.organization.management.v1.Utils.assertNotBlank;
import static org.wso2.identity.integration.test.rest.api.server.organization.management.v1.Utils.extractOrganizationIdFromLocationHeader;

/**
 * Tests for happy paths of the Organization Management REST API.
 */
public class OrganizationManagementSuccessTest extends OrganizationManagementBaseTest {

    private Set<String> createdOrgs = new HashSet<>();
    private String createdOrganizationId;
    private String createdOrganizationName;
    private String consumerKey;
    private String consumerSecret;
    private String orgSwitchedToken;
    private String applicationId;
    private static final String SYSTEM_SCOPE = "SYSTEM";

    @Factory(dataProvider = "restAPIUserConfigProvider")
    public OrganizationManagementSuccessTest(TestUserMode userMode, OrganizationLevel organizationLevel)
            throws Exception {

        super(userMode, organizationLevel);
    }

    @BeforeClass(alwaysRun = true)
    private void testInit() throws Exception {

        oAuth2RestClient = new OAuth2RestClient(serverURL, tenantInfo);
        if (organizationLevel == OrganizationLevel.SUB_ORGANIZATION) {
            applicationId = addApplication();
        }
    }

    @AfterClass(alwaysRun = true)
    @Override
    public void testFinish() throws Exception {

        cleanUpOrganizations(createdOrgs);
        if (organizationLevel == OrganizationLevel.SUB_ORGANIZATION) {
            deleteApplication(applicationId);
        }
        oAuth2RestClient.closeHttpClient();
        super.testFinish();
    }

    @Test
    public void createOrganization() throws Exception {

        JSONObject organizationObject = new JSONObject();
        String org;
        String parentId;

        Response responseOfPost;
        if (OrganizationLevel.SUPER_ORGANIZATION.equals(this.organizationLevel)) {
            org = "Level1Org";
            parentId = SUPER_ORGANIZATION_ID;
            organizationObject.put(ORGANIZATION_NAME, org);
            organizationObject.put(ORGANIZATION_PARENT_ID, parentId);
            responseOfPost = getResponseOfPost(ORGANIZATION_MANAGEMENT_API_BASE_PATH,
                    organizationObject.toString());
        } else {
            org = "Level2Org";
            parentId = subOrganizationId;
            shareApplication(applicationId);

            if (!isLegacyRuntimeEnabled) {
                authorizeSystemOrgApi(applicationId, Arrays.asList("/o/api/server/v1/organizations",
                        "/o/api/server/v1/identity-governance", "/o/api/server/v1/applications"));
                associateRoles(applicationId);
            }
            String passwordGrantToken = getPasswordGrantToken(applicationId);
            HttpClient client = HttpClientBuilder.create().build();
            orgSwitchedToken = getSwitchToken(parentId, passwordGrantToken, client);

            organizationObject.put(ORGANIZATION_NAME, org);
            organizationObject.put(ORGANIZATION_PARENT_ID, parentId);
            responseOfPost = given().auth().preemptive().oauth2(orgSwitchedToken)
                    .contentType(ContentType.JSON)
                    .header(HttpHeaders.ACCEPT, ContentType.JSON)
                    .body(organizationObject.toString())
                    .log().ifValidationFails()
                    .log().ifValidationFails()
                    .when()
                    .log().ifValidationFails()
                    .post(ORGANIZATION_MANAGEMENT_API_BASE_PATH);
        }

        responseOfPost.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED)
                .header(HttpHeaders.LOCATION, notNullValue());

        String location = responseOfPost.getHeader(HttpHeaders.LOCATION);
        String createdOrgId = extractOrganizationIdFromLocationHeader(location);
        createdOrgs.add(createdOrgId);
        createdOrganizationId = createdOrgId;
        createdOrganizationName = org;

        assertNotBlank(createdOrgId);
        if (organizationLevel == OrganizationLevel.SUB_ORGANIZATION) {
            // Check whether password recovery is enabled in the created sub-organization.
            given()
                    .auth().preemptive().oauth2(orgSwitchedToken)
                    .contentType(ContentType.JSON)
                    .header(HttpHeaders.ACCEPT, ContentType.JSON)
                    .log().ifValidationFails()
                    .when()
                    .get("/identity-governance/QWNjb3VudCBNYW5hZ2VtZW50/connectors/YWNjb3VudC1yZWNvdmVyeQ")
                    .then()
                    .log().ifValidationFails()
                    .assertThat()
                    .statusCode(HttpStatus.SC_OK)
                    .body("properties.find { it.name == 'Recovery.Notification.Password.Enable' }.value",
                            equalTo("true"))
                    .body("properties.find { it.name == 'Recovery.NotifySuccess' }.value", equalTo("true"));

            // Check whether application creation is disabled in the sub-organization.
            Response response = given()
                    .auth().preemptive().oauth2(orgSwitchedToken)
                    .contentType(ContentType.JSON)
                    .header(HttpHeaders.ACCEPT, ContentType.JSON)
                    .body(APPLICATION_PAYLOAD)
                    .log().ifValidationFails()
                    .when()
                    .post("/applications");

            response.then()
                    .log().ifValidationFails()
                    .assertThat()
                    .statusCode(HttpStatus.SC_FORBIDDEN);
        }
    }

    @Test(dependsOnMethods = {"createOrganization"})
    public void testGetOrganizationById() throws Exception {

        getResponseOfGet(ORGANIZATION_MANAGEMENT_API_BASE_PATH + "/" + createdOrganizationId)
                .then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body(ORGANIZATION_NAME, equalTo(createdOrganizationName));
    }

    private void authorizeSystemOrgApi(String applicationId, List<String> apiIdentifiers) {

        apiIdentifiers.forEach(apiIdentifier -> {
            try {
                List<APIResourceListItem> filteredAPIResource =
                        oAuth2RestClient.getAPIResourcesWithFiltering("type+eq+SYSTEM_ORG+and+identifier+eq+"
                                + apiIdentifier);
                if (filteredAPIResource == null || filteredAPIResource.isEmpty()) {
                    return;
                }
                String apiId = filteredAPIResource.get(0).getId();
                List<ScopeGetModel> apiResourceScopes = oAuth2RestClient.getAPIResourceScopes(apiId);
                AuthorizedAPICreationModel authorizedAPICreationModel = new AuthorizedAPICreationModel();
                authorizedAPICreationModel.setId(apiId);
                authorizedAPICreationModel.setPolicyIdentifier("RBAC");
                apiResourceScopes.forEach(scope -> {
                    authorizedAPICreationModel.addScopesItem(scope.getName());
                });
                oAuth2RestClient.addAPIAuthorizationToApplication(applicationId, authorizedAPICreationModel);
            } catch (Exception e) {
                throw new RuntimeException("Error while authorizing system API " + apiIdentifier + " to application "
                        + applicationId, e);
            }
        });
    }

    private String addApplication() throws Exception {

        ApplicationModel application = new ApplicationModel();
        List<String> grantTypes = new ArrayList<>();
        Collections.addAll(grantTypes, "password", "organization_switch");
        OpenIDConnectConfiguration oidcConfig = new OpenIDConnectConfiguration();
        oidcConfig.setGrantTypes(grantTypes);
        InboundProtocols inboundProtocolsConfig = new InboundProtocols();
        inboundProtocolsConfig.setOidc(oidcConfig);
        application.setInboundProtocolConfiguration(inboundProtocolsConfig);
        application.setName("org-mgt-token-app");
        return oAuth2RestClient.createApplication(application);
    }

    private String getPasswordGrantToken(String appId) throws Exception {

        OpenIDConnectConfiguration openIDConnectConfiguration = oAuth2RestClient.getOIDCInboundDetails(appId);
        Secret password = new Secret(tenantInfo.getContextUser().getPassword());
        AuthorizationGrant passwordGrant = new ResourceOwnerPasswordCredentialsGrant(
                tenantInfo.getContextUser().getUserName(), password);
        consumerKey = openIDConnectConfiguration.getClientId();
        ClientID clientID = new ClientID(consumerKey);
        consumerSecret = openIDConnectConfiguration.getClientSecret();
        Secret clientSecret = new Secret(consumerSecret);
        ClientAuthentication clientAuth = new ClientSecretBasic(clientID, clientSecret);
        URI tokenEndpoint = new URI(getTenantQualifiedURL(
                OAuth2Constant.ACCESS_TOKEN_ENDPOINT, tenantInfo.getDomain()));
        Scope systemScope = new Scope(SYSTEM_SCOPE);
        TokenRequest request = new TokenRequest(tokenEndpoint, clientAuth, passwordGrant, systemScope);

        HTTPResponse tokenHTTPResp = request.toHTTPRequest().send();
        Assert.assertNotNull(tokenHTTPResp, "Access token http response is null.");
        AccessTokenResponse tokenResponse = AccessTokenResponse.parse(tokenHTTPResp);
        Assert.assertNotNull(tokenResponse, "Access token response is null.");
        return tokenResponse.getTokens().getAccessToken().getValue();
    }

    private String getSwitchToken(String organizationId, String token, HttpClient client) throws Exception {

        List<NameValuePair> urlParameters = new ArrayList<>();
        urlParameters.add(new BasicNameValuePair(OAuth2Constant.GRANT_TYPE_NAME,
                "organization_switch"));
        urlParameters.add(new BasicNameValuePair("token", token));
        urlParameters.add(new BasicNameValuePair("scope", "SYSTEM"));
        urlParameters.add(new BasicNameValuePair("switching_organization", organizationId));

        HttpPost httpPost = new HttpPost(OAuth2Constant.ACCESS_TOKEN_ENDPOINT);
        httpPost.setHeader("Authorization", "Basic " +
                new String(Base64.encodeBase64((consumerKey + ":" + consumerSecret).getBytes())));
        httpPost.setHeader("Content-Type", "application/x-www-form-urlencoded");
        httpPost.setEntity(new UrlEncodedFormEntity(urlParameters));

        HttpResponse response = client.execute(httpPost);
        String responseString = EntityUtils.toString(response.getEntity(), "UTF-8");
        EntityUtils.consume(response.getEntity());
        JSONParser parser = new JSONParser();
        org.json.simple.JSONObject json = (org.json.simple.JSONObject) parser.parse(responseString);
        if (json == null) {
            throw new Exception("Error occurred while getting the response.");
        }
        Assert.assertNotNull(json.get(OAuth2Constant.ACCESS_TOKEN), "Access token is null.");
        return (String) json.get(OAuth2Constant.ACCESS_TOKEN);
    }

    private void shareApplication(String applicationId) throws JSONException {

        String shareApplicationUrl = "/api/server/v1" + ORGANIZATION_MANAGEMENT_API_BASE_PATH +
                "/" + SUPER_ORGANIZATION_ID + "/applications/" + applicationId + "/share";
        JSONObject shareAppObject = new JSONObject();
        shareAppObject.put("shareWithAllChildren", true);
        getResponseOfPost(shareApplicationUrl, shareAppObject.toString());
    }

    private void associateRoles(String applicationId) throws Exception {

        AssociatedRolesConfig associatedRolesConfig = new AssociatedRolesConfig().allowedAudience(
                AssociatedRolesConfig.AllowedAudienceEnum.ORGANIZATION);
        String adminRoleId = getRoleV2ResourceId(
                AssociatedRolesConfig.AllowedAudienceEnum.ORGANIZATION.toString().toLowerCase());
        ApplicationPatchModel applicationPatch = new ApplicationPatchModel();
        applicationPatch.associatedRoles(associatedRolesConfig);
        associatedRolesConfig.addRolesItem(
                new org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.Role().id(
                        adminRoleId));
        oAuth2RestClient.updateApplication(applicationId, applicationPatch);
    }

    private String getRoleV2ResourceId(String audienceType) throws Exception {

        List<String> roles = oAuth2RestClient.getRoles("admin", audienceType, null);
        if (roles.size() == 1) {
            return roles.get(0);
        }
        return null;
    }

    private void deleteApplication(String applicationId) throws Exception {

        oAuth2RestClient.deleteApplication(applicationId);
    }
}
