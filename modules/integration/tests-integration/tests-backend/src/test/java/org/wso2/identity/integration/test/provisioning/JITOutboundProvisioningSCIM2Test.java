/*
 * Copyright (c) 2026, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.identity.integration.test.provisioning;

import com.nimbusds.oauth2.sdk.AuthorizationCode;
import com.nimbusds.oauth2.sdk.AuthorizationCodeGrant;
import com.nimbusds.oauth2.sdk.TokenErrorResponse;
import com.nimbusds.oauth2.sdk.TokenRequest;
import com.nimbusds.oauth2.sdk.TokenResponse;
import com.nimbusds.oauth2.sdk.auth.ClientSecretBasic;
import com.nimbusds.oauth2.sdk.auth.Secret;
import com.nimbusds.oauth2.sdk.http.HTTPResponse;
import com.nimbusds.oauth2.sdk.id.ClientID;
import com.nimbusds.openid.connect.sdk.OIDCTokenResponse;
import com.nimbusds.openid.connect.sdk.OIDCTokenResponseParser;
import com.nimbusds.openid.connect.sdk.token.OIDCTokens;
import io.restassured.http.ContentType;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.commons.lang.StringUtils;
import org.apache.http.Header;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.config.Lookup;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.cookie.CookieSpecProvider;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.cookie.RFC6265CookieSpecProvider;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.AutomationContext;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.identity.integration.common.clients.application.mgt.ApplicationManagementServiceClient;
import org.wso2.identity.integration.test.application.mgt.AbstractIdentityFederationTestCase;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.AdvancedApplicationConfiguration;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.ApplicationModel;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.AuthenticationSequence;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.AuthenticationSequence.TypeEnum;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.AuthenticationStep;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.Authenticator;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.Claim;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.ClaimConfiguration;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.InboundProtocols;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.OpenIDConnectConfiguration;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.RequestedClaimConfiguration;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.SubjectConfig;
import org.wso2.identity.integration.test.rest.api.server.idp.v1.model.FederatedAuthenticatorRequest;
import org.wso2.identity.integration.test.rest.api.server.idp.v1.model.FederatedAuthenticatorRequest.FederatedAuthenticator;
import org.wso2.identity.integration.test.rest.api.server.idp.v1.model.IdentityProviderPOSTRequest;
import org.wso2.identity.integration.test.rest.api.server.idp.v1.model.ProvisioningRequest;
import org.wso2.identity.integration.test.rest.api.server.idp.v1.model.ProvisioningRequest.JustInTimeProvisioning;
import org.wso2.identity.integration.test.rest.api.user.common.model.Name;
import org.wso2.identity.integration.test.rest.api.user.common.model.UserObject;
import org.wso2.identity.integration.test.rest.api.server.tenant.management.v1.model.Owner;
import org.wso2.identity.integration.test.rest.api.server.tenant.management.v1.model.TenantModel;
import org.wso2.identity.integration.test.restclients.IdpMgtRestClient;
import org.wso2.identity.integration.test.restclients.SCIM2RestClient;
import org.wso2.identity.integration.test.restclients.TenantMgtRestClient;
import org.wso2.identity.integration.test.utils.DataExtractUtil;
import org.wso2.identity.integration.test.utils.IdentityConstants;
import org.wso2.identity.integration.test.utils.OAuth2Constant;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.wso2.carbon.automation.engine.context.beans.Tenant;
import org.wso2.carbon.automation.engine.context.beans.User;

import static io.restassured.RestAssured.given;
import static org.wso2.identity.integration.test.applicationNativeAuthentication.Constants.ADDITIONAL_DATA;
import static org.wso2.identity.integration.test.applicationNativeAuthentication.Constants.AUTHENTICATOR;
import static org.wso2.identity.integration.test.applicationNativeAuthentication.Constants.AUTHENTICATORS;
import static org.wso2.identity.integration.test.applicationNativeAuthentication.Constants.AUTHENTICATOR_ID;
import static org.wso2.identity.integration.test.applicationNativeAuthentication.Constants.AUTH_DATA_CODE;
import static org.wso2.identity.integration.test.applicationNativeAuthentication.Constants.AUTH_DATA_SESSION_STATE;
import static org.wso2.identity.integration.test.applicationNativeAuthentication.Constants.CONTENT_TYPE_APPLICATION_JSON;
import static org.wso2.identity.integration.test.applicationNativeAuthentication.Constants.FLOW_ID;
import static org.wso2.identity.integration.test.applicationNativeAuthentication.Constants.FLOW_STATUS;
import static org.wso2.identity.integration.test.applicationNativeAuthentication.Constants.FLOW_TYPE;
import static org.wso2.identity.integration.test.applicationNativeAuthentication.Constants.HREF;
import static org.wso2.identity.integration.test.applicationNativeAuthentication.Constants.IDP;
import static org.wso2.identity.integration.test.applicationNativeAuthentication.Constants.LINKS;
import static org.wso2.identity.integration.test.applicationNativeAuthentication.Constants.METADATA;
import static org.wso2.identity.integration.test.applicationNativeAuthentication.Constants.NEXT_STEP;
import static org.wso2.identity.integration.test.applicationNativeAuthentication.Constants.PROMPT_TYPE;
import static org.wso2.identity.integration.test.applicationNativeAuthentication.Constants.REDIRECT_URL;
import static org.wso2.identity.integration.test.applicationNativeAuthentication.Constants.REQUIRED_PARAMS;
import static org.wso2.identity.integration.test.applicationNativeAuthentication.Constants.RESPONSE_MODE;
import static org.wso2.identity.integration.test.applicationNativeAuthentication.Constants.STATE;
import static org.wso2.identity.integration.test.applicationNativeAuthentication.Constants.STEP_TYPE;
import static org.wso2.identity.integration.test.applicationNativeAuthentication.Constants.SUCCESS_COMPLETED;
import static org.wso2.identity.integration.test.applicationNativeAuthentication.Constants.UTF_8;
import static org.wso2.identity.integration.test.provisioning.OutboundProvisioningTestUtils.clearAppOutboundProvisioning;
import static org.wso2.identity.integration.test.provisioning.OutboundProvisioningTestUtils.enableAppOutboundProvisioning;
import static org.wso2.identity.integration.test.provisioning.OutboundProvisioningTestUtils.createOutboundProvisioningIdP;
import static org.wso2.identity.integration.test.provisioning.OutboundProvisioningTestUtils.getProvisionedUserId;
import static org.wso2.identity.integration.test.provisioning.OutboundProvisioningTestUtils.waitForProvisionedUser;

/**
 * Integration tests for outbound provisioning triggered through JIT provisioning.
 * Secondary IS serves as both OIDC federation source and outbound provisioning target.
 */
public class JITOutboundProvisioningSCIM2Test extends AbstractIdentityFederationTestCase {

    private static final String PRIMARY_IS_SP_NAME = "jit-outbound-test-app";
    private static final String PRIMARY_IS_IDP_NAME = "jit-outbound-federated-idp";
    private static final String PRIMARY_IS_OUTBOUND_IDP_NAME = "jit-outbound-scim2-idp";
    private static final String SECONDARY_IS_SP_NAME = "jit-outbound-secondary-sp";

    private static final String OUTBOUND_TENANT_DOMAIN = "outboundprov.com";
    private static final String OUTBOUND_TENANT_ADMIN_USERNAME = "admin@outboundprov.com";
    private static final String OUTBOUND_TENANT_ADMIN_PASSWORD = "Admin@123";
    private static final String OUTBOUND_TENANT_ADMIN_EMAIL = "admin@outboundprov.com";

    private static final String JIT_USERNAME = "jit.outbound.test.user";
    private static final String JIT_PASSWORD = "Wso2@test123";
    private static final String JIT_GIVEN_NAME = "JITOutbound";
    private static final String JIT_FAMILY_NAME = "TestUser";

    private static final String DIRECT_SCIM_USERNAME = "jit.outbound.direct.user";
    private static final String DIRECT_SCIM_PASSWORD = "Wso2@test456";
    private static final String DIRECT_SCIM_GIVEN_NAME = "DirectSCIM";
    private static final String DIRECT_SCIM_FAMILY_NAME = "TestUser";

    private static final String PRIMARY_IS_IDP_AUTHENTICATOR_NAME_OIDC = "OpenIDConnectAuthenticator";
    private static final String ENCODED_PRIMARY_IS_IDP_AUTHENTICATOR_ID_OIDC =
            "T3BlbklEQ29ubmVjdEF1dGhlbnRpY2F0b3I";

    private static final String PRIMARY_IS_IDP_CALLBACK_URL = "https://localhost:9853/commonauth";
    private static final String PRIMARY_IS_TOKEN_URL = "https://localhost:9853/oauth2/token";
    private static final String SECONDARY_IS_COMMONAUTH_URL = "https://localhost:9854/commonauth";
    private static final String SECONDARY_IS_AUTHORIZE_ENDPOINT = "https://localhost:9854/oauth2/authorize";
    private static final String SECONDARY_IS_TOKEN_ENDPOINT = "https://localhost:9854/oauth2/token";
    private static final String SECONDARY_IS_LOGOUT_ENDPOINT = "https://localhost:9854/oidc/logout";
    private static final String HTTPS_LOCALHOST_SERVICES = "https://localhost:%s/";

    private static final int PORT_OFFSET_0 = 0;
    private static final int PORT_OFFSET_1 = 1;

    private AutomationContext context;
    private String username;
    private String userPassword;

    private String secondaryISAppId;
    private String secondaryISClientID;
    private String secondaryISClientSecret;
    private String primaryISAppId;
    private String primaryISClientID;
    private String primaryISClientSecret;
    private String primaryISFederatedIdpId;
    private String primaryISOutboundIdpId;

    private String secondaryISJITUserId;
    private String primaryISJITUserId;
    private String primaryISDirectUserId;

    private String flowId;
    private String flowStatus;
    private String authenticatorId;
    private String federatedAuthenticatorId;
    private String href;
    private String redirectURL;
    private String nonce;
    private String state;
    private String code;

    private ApplicationManagementServiceClient appMgtClient;
    private SCIM2RestClient primaryISScim2RestClient;
    private SCIM2RestClient secondaryISScim2RestClient;
    private SCIM2RestClient secondaryISTenantScim2RestClient;
    private TenantMgtRestClient secondaryISTenantMgtRestClient;
    private IdpMgtRestClient idpMgtRestClient;
    private CloseableHttpClient client;
    private CookieStore cookieStore;

    @DataProvider(name = "configProvider")
    public static Object[][] configProvider() {

        return new Object[][]{{TestUserMode.SUPER_TENANT_ADMIN}};
    }

    @Factory(dataProvider = "configProvider")
    public JITOutboundProvisioningSCIM2Test(TestUserMode userMode) throws Exception {

        context = new AutomationContext("IDENTITY", userMode);
        this.username = context.getContextTenant().getTenantAdmin().getUserName();
        this.userPassword = context.getContextTenant().getTenantAdmin().getPassword();
    }

    @BeforeClass(alwaysRun = true)
    public void initTest() throws Exception {

        super.initTest();

        createServiceClients(PORT_OFFSET_0, new IdentityConstants.ServiceClientType[]{
                IdentityConstants.ServiceClientType.APPLICATION_MANAGEMENT,
                IdentityConstants.ServiceClientType.IDENTITY_PROVIDER_MGT});
        createServiceClients(PORT_OFFSET_1, new IdentityConstants.ServiceClientType[]{
                IdentityConstants.ServiceClientType.APPLICATION_MANAGEMENT});

        createApplicationInSecondaryIS();
        createFederatedIdPInPrimaryIS();
        createApplicationInPrimaryIS();

        secondaryISTenantMgtRestClient = new TenantMgtRestClient(getSecondaryISURI(), tenantInfo);
        createOutboundTenantOnSecondaryIS();

        Tenant outboundTenant = buildOutboundTenantInfo();
        secondaryISTenantScim2RestClient = new SCIM2RestClient(getSecondaryISURI(), outboundTenant);

        idpMgtRestClient = new IdpMgtRestClient(getPrimaryISURI(), tenantInfo);
        User outboundTenantAdmin = new User();
        outboundTenantAdmin.setUserName(OUTBOUND_TENANT_ADMIN_USERNAME);
        outboundTenantAdmin.setPassword(OUTBOUND_TENANT_ADMIN_PASSWORD);
        String tenantScimBaseUrl = getSecondaryISURI() + "t/" + OUTBOUND_TENANT_DOMAIN + "/";
        primaryISOutboundIdpId = createOutboundProvisioningIdP(
                idpMgtRestClient, PRIMARY_IS_OUTBOUND_IDP_NAME,
                "JIT outbound provisioning SCIM2 IdP", null,
                outboundTenantAdmin, null, tenantScimBaseUrl);
        Assert.assertNotNull(primaryISOutboundIdpId,
                "Failed to create outbound provisioning IdP on primary IS");

        ConfigurationContext configContext =
                ConfigurationContextFactory.createConfigurationContextFromFileSystem(null, null);
        appMgtClient = new ApplicationManagementServiceClient(sessionCookie, backendURL, configContext);
        enableAppOutboundProvisioning(appMgtClient, PRIMARY_IS_SP_NAME, PRIMARY_IS_OUTBOUND_IDP_NAME, true);

        primaryISScim2RestClient = new SCIM2RestClient(getPrimaryISURI(), tenantInfo);
        secondaryISScim2RestClient = new SCIM2RestClient(getSecondaryISURI(), tenantInfo);

        UserObject jitUser = new UserObject()
                .userName(JIT_USERNAME)
                .password(JIT_PASSWORD)
                .name(new Name().givenName(JIT_GIVEN_NAME).familyName(JIT_FAMILY_NAME));
        secondaryISJITUserId = secondaryISScim2RestClient.createUser(jitUser);
        Assert.assertNotNull(secondaryISJITUserId, "JIT test user creation failed on secondary IS");

        federatedAuthenticatorId = Base64.getUrlEncoder().withoutPadding()
                .encodeToString((PRIMARY_IS_IDP_AUTHENTICATOR_NAME_OIDC + ":" + PRIMARY_IS_IDP_NAME)
                        .getBytes(StandardCharsets.UTF_8));
    }

    @BeforeMethod(alwaysRun = true)
    public void initTestRun() {

        cookieStore = new BasicCookieStore();
        Lookup<CookieSpecProvider> cookieSpecRegistry = RegistryBuilder.<CookieSpecProvider>create()
                .register(CookieSpecs.DEFAULT, new RFC6265CookieSpecProvider()).build();
        RequestConfig requestConfig = RequestConfig.custom().setCookieSpec(CookieSpecs.DEFAULT).build();
        client = HttpClientBuilder.create()
                .setDefaultCookieSpecRegistry(cookieSpecRegistry)
                .setDefaultRequestConfig(requestConfig)
                .setDefaultCookieStore(cookieStore)
                .build();
    }

    @AfterClass(alwaysRun = true)
    public void endTest() throws Exception {

        try {
            if (appMgtClient != null) {
                clearAppOutboundProvisioning(appMgtClient, PRIMARY_IS_SP_NAME);
            }

            if (primaryISAppId != null) {
                deleteApplication(PORT_OFFSET_0, primaryISAppId);
            }
            if (secondaryISAppId != null) {
                deleteApplication(PORT_OFFSET_1, secondaryISAppId);
            }

            if (primaryISFederatedIdpId != null) {
                deleteIdp(PORT_OFFSET_0, primaryISFederatedIdpId);
            }
            if (primaryISOutboundIdpId != null) {
                idpMgtRestClient.deleteIdp(primaryISOutboundIdpId);
            }

            if (primaryISDirectUserId != null) {
                primaryISScim2RestClient.deleteUser(primaryISDirectUserId);
            }
            if (primaryISJITUserId != null) {
                primaryISScim2RestClient.deleteUser(primaryISJITUserId);
            }
            if (secondaryISJITUserId != null) {
                secondaryISScim2RestClient.deleteUser(secondaryISJITUserId);
            }

            String outboundProvisionedUserId = getProvisionedUserId(
                    secondaryISTenantScim2RestClient, JIT_USERNAME);
            if (outboundProvisionedUserId != null) {
                secondaryISTenantScim2RestClient.deleteUser(outboundProvisionedUserId);
            }
        } finally {
            if (idpMgtRestClient != null) {
                idpMgtRestClient.closeHttpClient();
            }
            if (primaryISScim2RestClient != null) {
                primaryISScim2RestClient.closeHttpClient();
            }
            if (secondaryISScim2RestClient != null) {
                secondaryISScim2RestClient.closeHttpClient();
            }
            if (secondaryISTenantScim2RestClient != null) {
                secondaryISTenantScim2RestClient.closeHttpClient();
            }
            if (secondaryISTenantMgtRestClient != null) {
                secondaryISTenantMgtRestClient.closeHttpClient();
            }
            if (client != null) {
                client.close();
            }
        }
    }

    // ---- Test methods -----------------------------------------------------------------------

    @Test(groups = "wso2.is",
            description = "Verify that a JIT-provisioned user is outbound provisioned to the outbound tenant")
    public void testJITCreatedUserIsOutboundProvisioned() throws Exception {

        Assert.assertNull(getProvisionedUserId(primaryISScim2RestClient, JIT_USERNAME),
                "User should not exist on primary IS before JIT login");

        authorizePrimaryIDP();
        authorizeFederatedIDP();
        authenticatePrimaryIDPWithFederatedResponse();
        verifyTokenRetrieved();

        primaryISJITUserId = getProvisionedUserId(primaryISScim2RestClient, JIT_USERNAME);
        Assert.assertNotNull(primaryISJITUserId,
                "JIT provisioning failed — user was not created on primary IS after federated login");

        String outboundUserId = waitForProvisionedUser(
                secondaryISTenantScim2RestClient, JIT_USERNAME, 10);
        Assert.assertNotNull(outboundUserId,
                "Outbound provisioning failed — user not found in outbound tenant on secondary IS "
                        + "after JIT provisioning");
    }

    @Test(groups = "wso2.is",
            description = "Verify that a user created directly via SCIM is not outbound provisioned")
    public void testDirectSCIMUserNotOutboundProvisioned() throws Exception {

        UserObject directUser = new UserObject()
                .userName(DIRECT_SCIM_USERNAME)
                .password(DIRECT_SCIM_PASSWORD)
                .name(new Name().givenName(DIRECT_SCIM_GIVEN_NAME).familyName(DIRECT_SCIM_FAMILY_NAME));
        primaryISDirectUserId = primaryISScim2RestClient.createUser(directUser);
        Assert.assertNotNull(primaryISDirectUserId,
                "Direct SCIM user creation failed on primary IS");

        Assert.assertNull(getProvisionedUserId(secondaryISTenantScim2RestClient, DIRECT_SCIM_USERNAME),
                "Direct SCIM user should NOT be outbound provisioned to the outbound tenant on "
                        + "secondary IS, because the outbound connector is configured only on the "
                        + "login application");
    }

    private void createOutboundTenantOnSecondaryIS() throws Exception {

        Owner tenantOwner = new Owner();
        tenantOwner.setUsername("admin");
        tenantOwner.setPassword(OUTBOUND_TENANT_ADMIN_PASSWORD);
        tenantOwner.setEmail(OUTBOUND_TENANT_ADMIN_EMAIL);
        tenantOwner.setFirstname("Outbound");
        tenantOwner.setLastname("Admin");
        tenantOwner.setProvisioningMethod("inline-password");

        TenantModel tenantModel = new TenantModel();
        tenantModel.setDomain(OUTBOUND_TENANT_DOMAIN);
        tenantModel.addOwnersItem(tenantOwner);

        String tenantId = secondaryISTenantMgtRestClient.addTenant(tenantModel);
        Assert.assertNotNull(tenantId,
                "Failed to create outbound provisioning tenant on secondary IS");
    }

    private Tenant buildOutboundTenantInfo() {

        User tenantAdmin = new User();
        tenantAdmin.setUserName(OUTBOUND_TENANT_ADMIN_USERNAME);
        tenantAdmin.setPassword(OUTBOUND_TENANT_ADMIN_PASSWORD);

        Tenant tenant = new Tenant();
        tenant.setContextUser(tenantAdmin);
        tenant.setDomain(OUTBOUND_TENANT_DOMAIN);
        return tenant;
    }

    private void createApplicationInSecondaryIS() throws Exception {

        ClaimConfiguration claimConfiguration = new ClaimConfiguration();
        claimConfiguration.setSubject(
                new SubjectConfig().claim(new Claim().uri("http://wso2.org/claims/username")));
        claimConfiguration.addRequestedClaimsItem(getRequestedClaim("http://wso2.org/claims/username"));
        claimConfiguration.addRequestedClaimsItem(getRequestedClaim("http://wso2.org/claims/givenname"));
        claimConfiguration.addRequestedClaimsItem(getRequestedClaim("http://wso2.org/claims/lastname"));
        claimConfiguration.addRequestedClaimsItem(getRequestedClaim("http://wso2.org/claims/fullname"));

        ApplicationModel applicationCreationModel = new ApplicationModel()
                .name(SECONDARY_IS_SP_NAME)
                .description("Secondary IS service provider for JIT outbound provisioning tests")
                .isManagementApp(true)
                .inboundProtocolConfiguration(new InboundProtocols().oidc(getSecondaryISAppOIDCConfig()))
                .advancedConfigurations(new AdvancedApplicationConfiguration()
                        .skipLoginConsent(true)
                        .skipLogoutConsent(true))
                .claimConfiguration(claimConfiguration);

        secondaryISAppId = addApplication(PORT_OFFSET_1, applicationCreationModel);
        Assert.assertNotNull(secondaryISAppId, "Failed to create OIDC application on secondary IS");

        OpenIDConnectConfiguration oidcConfig =
                getOIDCInboundDetailsOfApplication(PORT_OFFSET_1, secondaryISAppId);
        secondaryISClientID = oidcConfig.getClientId();
        secondaryISClientSecret = oidcConfig.getClientSecret();
        Assert.assertNotNull(secondaryISClientID,
                "Failed to retrieve client ID from secondary IS application");
        Assert.assertNotNull(secondaryISClientSecret,
                "Failed to retrieve client secret from secondary IS application");
    }

    private void createFederatedIdPInPrimaryIS() throws Exception {

        FederatedAuthenticator authenticator = new FederatedAuthenticator()
                .authenticatorId(ENCODED_PRIMARY_IS_IDP_AUTHENTICATOR_ID_OIDC)
                .name(PRIMARY_IS_IDP_AUTHENTICATOR_NAME_OIDC)
                .isEnabled(true)
                .addProperty(new org.wso2.identity.integration.test.rest.api.server.idp.v1.model.Property()
                        .key(IdentityConstants.Authenticator.OIDC.IDP_NAME).value("oidcFedIdP"))
                .addProperty(new org.wso2.identity.integration.test.rest.api.server.idp.v1.model.Property()
                        .key(IdentityConstants.Authenticator.OIDC.CLIENT_ID).value(secondaryISClientID))
                .addProperty(new org.wso2.identity.integration.test.rest.api.server.idp.v1.model.Property()
                        .key(IdentityConstants.Authenticator.OIDC.CLIENT_SECRET).value(secondaryISClientSecret))
                .addProperty(new org.wso2.identity.integration.test.rest.api.server.idp.v1.model.Property()
                        .key(IdentityConstants.Authenticator.OIDC.OAUTH2_AUTHZ_URL)
                        .value(SECONDARY_IS_AUTHORIZE_ENDPOINT))
                .addProperty(new org.wso2.identity.integration.test.rest.api.server.idp.v1.model.Property()
                        .key(IdentityConstants.Authenticator.OIDC.OAUTH2_TOKEN_URL)
                        .value(SECONDARY_IS_TOKEN_ENDPOINT))
                .addProperty(new org.wso2.identity.integration.test.rest.api.server.idp.v1.model.Property()
                        .key(IdentityConstants.Authenticator.OIDC.CALLBACK_URL)
                        .value(PRIMARY_IS_IDP_CALLBACK_URL))
                .addProperty(new org.wso2.identity.integration.test.rest.api.server.idp.v1.model.Property()
                        .key(IdentityConstants.Authenticator.OIDC.OIDC_LOGOUT_URL)
                        .value(SECONDARY_IS_LOGOUT_ENDPOINT))
                .addProperty(new org.wso2.identity.integration.test.rest.api.server.idp.v1.model.Property()
                        .key("commonAuthQueryParams")
                        .value("scope=" + OAuth2Constant.OAUTH2_SCOPE_OPENID_WITH_INTERNAL_LOGIN));

        FederatedAuthenticatorRequest oidcAuthnConfig = new FederatedAuthenticatorRequest()
                .defaultAuthenticatorId(ENCODED_PRIMARY_IS_IDP_AUTHENTICATOR_ID_OIDC)
                .addAuthenticator(authenticator);

        ProvisioningRequest provision = new ProvisioningRequest()
                .jit(new JustInTimeProvisioning().isEnabled(true).userstore("PRIMARY"));

        IdentityProviderPOSTRequest idpPostRequest = new IdentityProviderPOSTRequest()
                .name(PRIMARY_IS_IDP_NAME)
                .federatedAuthenticators(oidcAuthnConfig)
                .provisioning(provision);

        primaryISFederatedIdpId = addIdentityProvider(PORT_OFFSET_0, idpPostRequest);
        Assert.assertNotNull(primaryISFederatedIdpId,
                "Failed to create federated OIDC IdP on primary IS");
    }

    private void createApplicationInPrimaryIS() throws Exception {

        ApplicationModel applicationCreationModel = new ApplicationModel()
                .name(PRIMARY_IS_SP_NAME)
                .description("Primary IS service provider for JIT outbound provisioning tests")
                .isManagementApp(true)
                .inboundProtocolConfiguration(new InboundProtocols().oidc(getPrimaryISAppOIDCConfig()))
                .authenticationSequence(new AuthenticationSequence()
                        .type(TypeEnum.USER_DEFINED)
                        .addStepsItem(new AuthenticationStep()
                                .id(1)
                                .addOptionsItem(new Authenticator()
                                        .idp(PRIMARY_IS_IDP_NAME)
                                        .authenticator(PRIMARY_IS_IDP_AUTHENTICATOR_NAME_OIDC))))
                .advancedConfigurations(new AdvancedApplicationConfiguration()
                        .enableAPIBasedAuthentication(true));

        primaryISAppId = addApplication(PORT_OFFSET_0, applicationCreationModel);
        Assert.assertNotNull(primaryISAppId, "Failed to create OIDC application on primary IS");

        OpenIDConnectConfiguration oidcConfig =
                getOIDCInboundDetailsOfApplication(PORT_OFFSET_0, primaryISAppId);
        primaryISClientID = oidcConfig.getClientId();
        primaryISClientSecret = oidcConfig.getClientSecret();
        Assert.assertNotNull(primaryISClientID,
                "Failed to retrieve client ID from primary IS application");
        Assert.assertNotNull(primaryISClientSecret,
                "Failed to retrieve client secret from primary IS application");
    }


    private void authorizePrimaryIDP() throws IOException, ParseException, URISyntaxException {

        HttpResponse response = sendPostRequestWithParameters(client,
                buildOAuth2Parameters(primaryISClientID), OAuth2Constant.AUTHORIZE_ENDPOINT_URL);
        Assert.assertNotNull(response, "Authorization POST to primary IS returned null response");

        JSONObject json = parseJsonResponse(response);
        Assert.assertNotNull(json, "ANA init response JSON is null");
        validateInitClientNativeAuthnResponse(json);
    }

    private void authorizeFederatedIDP() throws IOException, URISyntaxException {

        HttpResponse response = sendPostRequestWithParameters(client,
                buildSecondaryISAuthorizeParams(), SECONDARY_IS_AUTHORIZE_ENDPOINT);
        Assert.assertNotNull(response, "Authorization POST to secondary IS returned null response");
        validateSecondaryISFederationResponse(response);
    }

    private void authenticatePrimaryIDPWithFederatedResponse() {

        String body = buildFederatedAuthRequestBody();
        Response response = given()
                .contentType(ContentType.JSON)
                .headers(new HashMap<>())
                .body(body)
                .when()
                .post(href);

        ExtractableResponse<Response> extractable = response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .and()
                .assertThat()
                .header(HttpHeaders.CONTENT_TYPE, CONTENT_TYPE_APPLICATION_JSON)
                .extract();

        Assert.assertNotNull(extractable, "Federated authentication response is null");
        validateFederatedAuthResponse(extractable);
    }

    private void verifyTokenRetrieved()
            throws URISyntaxException, IOException, com.nimbusds.oauth2.sdk.ParseException {

        TokenRequest tokenReq = buildTokenRequest();
        HTTPResponse tokenHTTPResp = tokenReq.toHTTPRequest().send();
        Assert.assertNotNull(tokenHTTPResp, "Token HTTP response is null");

        TokenResponse tokenResponse = OIDCTokenResponseParser.parse(tokenHTTPResp);
        Assert.assertNotNull(tokenResponse, "Token response is null");
        Assert.assertFalse(tokenResponse instanceof TokenErrorResponse,
                "Token response contains errors");

        OIDCTokenResponse oidcTokenResponse = (OIDCTokenResponse) tokenResponse;
        OIDCTokens oidcTokens = oidcTokenResponse.getOIDCTokens();
        Assert.assertNotNull(oidcTokens, "OIDC tokens are null");
        Assert.assertNotNull(oidcTokens.getIDTokenString(), "ID token is null");
    }

    private TokenRequest buildTokenRequest() throws URISyntaxException {

        ClientSecretBasic clientSecretBasic = new ClientSecretBasic(
                new ClientID(primaryISClientID), new Secret(primaryISClientSecret));
        AuthorizationCodeGrant authorizationCodeGrant = new AuthorizationCodeGrant(
                new AuthorizationCode(code), new URI(PRIMARY_IS_IDP_CALLBACK_URL));
        return new TokenRequest(new URI(PRIMARY_IS_TOKEN_URL), clientSecretBasic, authorizationCodeGrant);
    }

    private void validateInitClientNativeAuthnResponse(JSONObject json) throws URISyntaxException {

        if (!json.containsKey(FLOW_ID) || !json.containsKey(FLOW_STATUS) || !json.containsKey(FLOW_TYPE) ||
                !json.containsKey(NEXT_STEP) || !json.containsKey(LINKS)) {
            Assert.fail("ANA init response is not in expected format: " + json);
        }

        flowId = (String) json.get(FLOW_ID);
        flowStatus = (String) json.get(FLOW_STATUS);

        JSONObject nextStep = (JSONObject) json.get(NEXT_STEP);
        if (!nextStep.containsKey(STEP_TYPE) || !nextStep.containsKey(AUTHENTICATORS)) {
            Assert.fail("nextStep is not in expected format");
        }

        JSONArray authenticatorsArray = (JSONArray) nextStep.get(AUTHENTICATORS);
        if (authenticatorsArray.isEmpty()) {
            Assert.fail("Authenticators array is empty in ANA init response");
        }

        JSONObject authEntry = (JSONObject) authenticatorsArray.get(0);
        if (!authEntry.containsKey(AUTHENTICATOR_ID) || !authEntry.containsKey(AUTHENTICATOR) ||
                !authEntry.containsKey(IDP) || !authEntry.containsKey(METADATA) ||
                !authEntry.containsKey(REQUIRED_PARAMS)) {
            Assert.fail("Authenticator entry is not in expected format");
        }

        authenticatorId = (String) authEntry.get(AUTHENTICATOR_ID);
        if (!StringUtils.equals(authenticatorId, federatedAuthenticatorId)) {
            Assert.fail("Authenticator ID mismatch. Expected: " + federatedAuthenticatorId
                    + " but got: " + authenticatorId);
        }

        JSONObject metadata = (JSONObject) authEntry.get(METADATA);
        if (!metadata.containsKey(PROMPT_TYPE)) {
            Assert.fail("Metadata does not contain promptType");
        }

        JSONObject additionalData = (JSONObject) metadata.get(ADDITIONAL_DATA);
        if (additionalData == null || !additionalData.containsKey(REDIRECT_URL)) {
            Assert.fail("additionalData does not contain redirectUrl");
        }

        redirectURL = (String) additionalData.get(REDIRECT_URL);
        if (StringUtils.isEmpty(redirectURL)) {
            Assert.fail("redirectUrl is empty in ANA init response");
        }

        nonce = DataExtractUtil.getParamFromURIString(redirectURL, "nonce");
        state = DataExtractUtil.getParamFromURIString(redirectURL, "state");

        JSONArray links = (JSONArray) json.get(LINKS);
        JSONObject link = (JSONObject) links.get(0);
        if (!link.containsKey(HREF)) {
            Assert.fail("No href in links of ANA init response");
        }
        href = link.get(HREF).toString();
    }

    private void validateSecondaryISFederationResponse(HttpResponse response)
            throws IOException, URISyntaxException {

        String locationValue = getLocationHeaderValue(response);
        EntityUtils.consume(response.getEntity());

        Assert.assertTrue(locationValue.contains(OAuth2Constant.SESSION_DATA_KEY),
                "sessionDataKey not found in secondary IS federation response");
        String sessionDataKey = DataExtractUtil.getParamFromURIString(
                locationValue, OAuth2Constant.SESSION_DATA_KEY);
        Assert.assertNotNull(sessionDataKey, "sessionDataKey is null");

        response = sendLoginPost(client, sessionDataKey);
        Assert.assertNotNull(response, "Login POST to secondary IS returned null");

        locationValue = getLocationHeaderValue(response);
        EntityUtils.consume(response.getEntity());
        sessionDataKey = DataExtractUtil.getParamFromURIString(
                locationValue, OAuth2Constant.SESSION_DATA_KEY);

        response = sendApprovalPost(client, sessionDataKey);
        Assert.assertNotNull(response, "Approval POST to secondary IS returned null");

        locationValue = getLocationHeaderValue(response);
        code = DataExtractUtil.getParamFromURIString(
                locationValue, OAuth2Constant.AUTHORIZATION_CODE_NAME);
        state = DataExtractUtil.getParamFromURIString(locationValue, "state");
        Assert.assertNotNull(code, "Authorization code is null after secondary IS federation");
        EntityUtils.consume(response.getEntity());
    }

    private void validateFederatedAuthResponse(ExtractableResponse<Response> extractable) {

        flowStatus = extractable.jsonPath().getString(FLOW_STATUS);
        Assert.assertEquals(flowStatus, SUCCESS_COMPLETED,
                "ANA flow status is not SUCCESS_COMPLETED");

        code = extractable.jsonPath().getString(AUTH_DATA_CODE);
        Assert.assertNotNull(code, "Authorization code is null in ANA response authData");

        Assert.assertNotNull(extractable.jsonPath().getString(AUTH_DATA_SESSION_STATE),
                "Session state is null in ANA response authData");
    }


    private HttpResponse sendPostRequestWithParameters(HttpClient httpClient,
                                                       List<NameValuePair> urlParameters,
                                                       String url) throws IOException {

        HttpPost request = new HttpPost(url);
        request.setHeader("User-Agent", OAuth2Constant.USER_AGENT);
        request.setEntity(new UrlEncodedFormEntity(urlParameters));
        return httpClient.execute(request);
    }

    private HttpResponse sendLoginPost(HttpClient httpClient, String sessionDataKey) throws IOException {

        List<NameValuePair> urlParameters = new ArrayList<>();
        urlParameters.add(new BasicNameValuePair("username", JIT_USERNAME));
        urlParameters.add(new BasicNameValuePair("password", JIT_PASSWORD));
        urlParameters.add(new BasicNameValuePair("sessionDataKey", sessionDataKey));
        return sendPostRequestWithParameters(httpClient, urlParameters, SECONDARY_IS_COMMONAUTH_URL);
    }

    private HttpResponse sendApprovalPost(HttpClient httpClient, String sessionDataKeyConsent)
            throws IOException {

        List<NameValuePair> urlParameters = new ArrayList<>();
        urlParameters.add(new BasicNameValuePair("sessionDataKey", sessionDataKeyConsent));
        return sendPostRequestWithParameters(httpClient, urlParameters,
                getTenantQualifiedURL(SECONDARY_IS_AUTHORIZE_ENDPOINT, tenantInfo.getDomain()));
    }

    private String getLocationHeaderValue(HttpResponse response) {

        Header location = response.getFirstHeader(OAuth2Constant.HTTP_RESPONSE_HEADER_LOCATION);
        Assert.assertNotNull(location, "Location header is null");
        return location.getValue();
    }

    private JSONObject parseJsonResponse(HttpResponse response) throws IOException, ParseException {

        String responseString = EntityUtils.toString(response.getEntity(), UTF_8);
        EntityUtils.consume(response.getEntity());
        return (JSONObject) new JSONParser().parse(responseString);
    }


    private List<NameValuePair> buildOAuth2Parameters(String consumerKey) {

        List<NameValuePair> urlParameters = new ArrayList<>();
        urlParameters.add(new BasicNameValuePair(
                OAuth2Constant.OAUTH2_RESPONSE_TYPE, OAuth2Constant.AUTHORIZATION_CODE_NAME));
        urlParameters.add(new BasicNameValuePair(OAuth2Constant.OAUTH2_RESPONSE_MODE, RESPONSE_MODE));
        urlParameters.add(new BasicNameValuePair(OAuth2Constant.OAUTH2_CLIENT_ID, consumerKey));
        urlParameters.add(new BasicNameValuePair(
                OAuth2Constant.OAUTH2_REDIRECT_URI, PRIMARY_IS_IDP_CALLBACK_URL));
        urlParameters.add(new BasicNameValuePair(OAuth2Constant.OAUTH2_SCOPE,
                OAuth2Constant.OAUTH2_SCOPE_OPENID_WITH_INTERNAL_LOGIN + " "
                        + OAuth2Constant.OAUTH2_SCOPE_EMAIL + " "
                        + OAuth2Constant.OAUTH2_SCOPE_PROFILE));
        return urlParameters;
    }

    private List<NameValuePair> buildSecondaryISAuthorizeParams() {

        List<NameValuePair> urlParameters = new ArrayList<>();
        urlParameters.add(new BasicNameValuePair(
                OAuth2Constant.OAUTH2_RESPONSE_TYPE, OAuth2Constant.AUTHORIZATION_CODE_NAME));
        urlParameters.add(new BasicNameValuePair(OAuth2Constant.OAUTH2_CLIENT_ID, secondaryISClientID));
        urlParameters.add(new BasicNameValuePair(
                OAuth2Constant.OAUTH2_REDIRECT_URI, PRIMARY_IS_IDP_CALLBACK_URL));
        urlParameters.add(new BasicNameValuePair(OAuth2Constant.OAUTH2_SCOPE,
                OAuth2Constant.OAUTH2_SCOPE_OPENID_WITH_INTERNAL_LOGIN + " "
                        + OAuth2Constant.OAUTH2_SCOPE_EMAIL + " "
                        + OAuth2Constant.OAUTH2_SCOPE_PROFILE));
        urlParameters.add(new BasicNameValuePair(OAuth2Constant.OAUTH2_NONCE, nonce));
        urlParameters.add(new BasicNameValuePair(STATE, state));
        return urlParameters;
    }

    private String buildFederatedAuthRequestBody() {

        return "{\n"
                + "    \"flowId\": \"" + flowId + "\",\n"
                + "    \"selectedAuthenticator\": {\n"
                + "        \"authenticatorId\": \"" + authenticatorId + "\",\n"
                + "        \"params\": {\n"
                + "           \"code\": \"" + code + "\",\n"
                + "           \"state\": \"" + state + "\"\n"
                + "        }\n"
                + "    }\n"
                + "}";
    }


    private OpenIDConnectConfiguration getPrimaryISAppOIDCConfig() {

        List<String> grantTypes = new ArrayList<>();
        Collections.addAll(grantTypes, "authorization_code", "implicit", "password",
                "client_credentials", "refresh_token",
                "urn:ietf:params:oauth:grant-type:saml2-bearer", "iwa:ntlm");
        OpenIDConnectConfiguration oidcConfig = new OpenIDConnectConfiguration();
        oidcConfig.setGrantTypes(grantTypes);
        oidcConfig.addCallbackURLsItem(PRIMARY_IS_IDP_CALLBACK_URL);
        oidcConfig.setPublicClient(true);
        return oidcConfig;
    }

    private OpenIDConnectConfiguration getSecondaryISAppOIDCConfig() {

        List<String> grantTypes = new ArrayList<>();
        Collections.addAll(grantTypes, "authorization_code", "implicit", "password",
                "client_credentials", "refresh_token",
                "urn:ietf:params:oauth:grant-type:saml2-bearer", "iwa:ntlm");
        OpenIDConnectConfiguration oidcConfig = new OpenIDConnectConfiguration();
        oidcConfig.setGrantTypes(grantTypes);
        oidcConfig.addCallbackURLsItem(PRIMARY_IS_IDP_CALLBACK_URL);
        return oidcConfig;
    }

    private RequestedClaimConfiguration getRequestedClaim(String claimUri) {

        RequestedClaimConfiguration requestedClaim = new RequestedClaimConfiguration();
        requestedClaim.setClaim(new Claim().uri(claimUri));
        return requestedClaim;
    }


    protected String getPrimaryISURI() {

        return String.format(HTTPS_LOCALHOST_SERVICES, DEFAULT_PORT + PORT_OFFSET_0);
    }

    protected String getSecondaryISURI() {

        return String.format(HTTPS_LOCALHOST_SERVICES, DEFAULT_PORT + PORT_OFFSET_1);
    }
}
