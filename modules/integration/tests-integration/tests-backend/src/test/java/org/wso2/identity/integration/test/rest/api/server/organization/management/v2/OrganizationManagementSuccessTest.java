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
package org.wso2.identity.integration.test.rest.api.server.organization.management.v2;

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
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpStatus;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.ApplicationListItem;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.ApplicationModel;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.ApplicationPatchModel;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.AssociatedRolesConfig;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.InboundProtocols;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.OpenIDConnectConfiguration;
import org.wso2.identity.integration.test.restclients.OAuth2RestClient;
import org.wso2.identity.integration.test.utils.OAuth2Constant;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

/**
 * Tests for successful cases of the Organization Management REST APIs.
 */
public class OrganizationManagementSuccessTest extends OrganizationManagementBaseTest {

    private String organizationId;
    private String selfServiceAppId;
    private String applicationId;

    @Factory(dataProvider = "restAPIUserConfigProvider")
    public OrganizationManagementSuccessTest(TestUserMode userMode) throws Exception {

        super.init(userMode);
        this.context = isServer;
        this.authenticatingUserName = context.getContextTenant().getTenantAdmin().getUserName();
        this.authenticatingCredential = context.getContextTenant().getTenantAdmin().getPassword();
        this.tenant = context.getContextTenant().getDomain();
    }

    @BeforeClass(alwaysRun = true)
    public void init() throws Exception {

        super.testInit(API_VERSION, swaggerDefinition, tenant);
        oAuth2RestClient = new OAuth2RestClient(serverURL, tenantInfo);
        //applicationId = addApplication();
    }

    @AfterClass(alwaysRun = true)
    public void testConclude() {

        super.conclude();
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
    public void testAddOrganization() throws IOException {

        String body = readResource("add-organization-request-body.json");
        Response response = getResponseOfJSONPost(ORGANIZATION_MANAGEMENT_API_BASE_PATH,
                body, new HashMap<>());
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED)
                .header(HttpHeaders.LOCATION, notNullValue());

        String location = response.getHeader(HttpHeaders.LOCATION);
        assertNotNull(location);
        organizationId = location.substring(location.lastIndexOf("/") + 1);
        assertNotNull(organizationId);
    }

    @Test(dependsOnMethods = "testAddOrganization")
    public void testGetOrganization() {

        Response response = getResponseOfGet(ORGANIZATION_MANAGEMENT_API_BASE_PATH + PATH_SEPARATOR + organizationId);
        validateHttpStatusCode(response, HttpStatus.SC_OK);
        Assert.assertNotNull(response.asString());
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("id", equalTo(organizationId));
    }

    @Test
    public void enableSelfOrganizationOnboardService() throws IOException {

        String endpointURL = "self-service/preferences";
        String body = readResource("enable-self-organization-onboard-request-body.json");

        Response response = given().auth().preemptive().basic(authenticatingUserName, authenticatingCredential)
                .contentType(ContentType.JSON)
                .body(body)
                .when()
                .patch(endpointURL);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK);

        Optional<ApplicationListItem> b2bSelfServiceApp = oAuth2RestClient.getAllApplications().getApplications().stream()
                .filter(application -> application.getName().equals("B2B-Self-Service-Mgt-Application"))
                .findAny();
        Assert.assertTrue(b2bSelfServiceApp.isPresent(), "B2B self organization onboard feature is not enabled properly");
        selfServiceAppId = b2bSelfServiceApp.get().getId();
    }

    @Test(dependsOnMethods = "enableSelfOrganizationOnboardService")
    public void createUser() throws Exception {

        OpenIDConnectConfiguration openIDConnectConfiguration = oAuth2RestClient.getOIDCInboundDetails(selfServiceAppId);
        String consumerKey = openIDConnectConfiguration.getClientId();
        ClientID clientID = new ClientID(consumerKey);
        String consumerSecret = openIDConnectConfiguration.getClientSecret();
        Secret clientSecret = new Secret(consumerSecret);
        Assert.assertNotNull(clientID);
        Assert.assertNotNull(clientSecret);
    }

    private void associateRoles(String applicationId) throws Exception {

        AssociatedRolesConfig associatedRolesConfig = new AssociatedRolesConfig().allowedAudience(
                AssociatedRolesConfig.AllowedAudienceEnum.ORGANIZATION);
        String audienceType = AssociatedRolesConfig.AllowedAudienceEnum.ORGANIZATION.toString().toLowerCase();
        List<String> roles = oAuth2RestClient.getRoles("admin", audienceType, null);
        String adminRoleId = null;
        if (roles.size() == 1) {
            adminRoleId =  roles.get(0);
        }
        ApplicationPatchModel applicationPatch = new ApplicationPatchModel();
        applicationPatch.associatedRoles(associatedRolesConfig);
        associatedRolesConfig.addRolesItem(
                new org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.Role().id(
                        adminRoleId));
        oAuth2RestClient.updateApplication(applicationId, applicationPatch);
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
}
