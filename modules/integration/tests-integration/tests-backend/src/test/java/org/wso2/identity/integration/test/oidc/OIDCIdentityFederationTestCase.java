/*
 * Copyright (c) 2020, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.identity.integration.test.oidc;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
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
import org.opensaml.xml.util.Base64;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.AutomationContext;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.identity.integration.test.application.mgt.AbstractIdentityFederationTestCase;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.ApplicationModel;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.ApplicationResponseModel;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.AuthenticationSequence;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.AuthenticationSequence.TypeEnum;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.Authenticator;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.InboundProtocols;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.OpenIDConnectConfiguration;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.SAML2Configuration;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.SAML2ServiceProvider;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.SAMLAssertionConfiguration;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.SAMLAttributeProfile;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.SAMLResponseSigning;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.SingleLogoutProfile;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.SingleSignOnProfile;
import org.wso2.identity.integration.test.rest.api.server.idp.v1.model.FederatedAuthenticatorRequest;
import org.wso2.identity.integration.test.rest.api.server.idp.v1.model.FederatedAuthenticatorRequest.FederatedAuthenticator;
import org.wso2.identity.integration.test.rest.api.server.idp.v1.model.IdentityProviderPOSTRequest;
import org.wso2.identity.integration.test.rest.api.server.idp.v1.model.ProvisioningRequest;
import org.wso2.identity.integration.test.rest.api.server.idp.v1.model.ProvisioningRequest.JustInTimeProvisioning;
import org.wso2.identity.integration.test.rest.api.user.common.model.ListObject;
import org.wso2.identity.integration.test.rest.api.user.common.model.PatchOperationRequestObject;
import org.wso2.identity.integration.test.rest.api.user.common.model.RoleItemAddGroupobj;
import org.wso2.identity.integration.test.rest.api.user.common.model.UserObject;
import org.wso2.identity.integration.test.restclients.SCIM2RestClient;
import org.wso2.identity.integration.test.util.Utils;
import org.wso2.identity.integration.test.utils.DataExtractUtil;
import org.wso2.identity.integration.test.utils.IdentityConstants;
import org.wso2.identity.integration.test.utils.OAuth2Constant;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Integration test cases for SAML-OIDC federation scenarios.
 */
public class OIDCIdentityFederationTestCase extends AbstractIdentityFederationTestCase {

    private static final String SAML_SSO_URL = "http://localhost:8490/travelocity.com/samlsso?SAML2" +
            ".HTTPBinding=HTTP-Redirect";
    private static final String SAML_LOGOUT_URL = "http://localhost:8490/travelocity.com/logout?SAML2" +
            ".HTTPBinding=HTTP-Redirect";
    private static final String USER_AGENT = "Apache-HttpClient/4.2.5 (java 1.5)";

    private static final String PRIMARY_IS_SP_NAME = "travelocity";

    private static final String PRIMARY_IS_SAML_ISSUER_NAME = "travelocity.com";
    private static final String PRIMARY_IS_SAML_ACS_URL = "http://localhost:8490/travelocity.com/home.jsp";
    private static final String PRIMARY_IS_SAML_NAME_ID_FORMAT = "urn:oasis:names:tc:SAML:1.1:nameid-format:emailAddress";

    private static final String PRIMARY_IS_IDP_NAME = "trustedIdP";
    private static final String PRIMARY_IS_IDP_AUTHENTICATOR_NAME_OIDC = "OpenIDConnectAuthenticator";
    private static final String ENCODED_PRIMARY_IS_IDP_AUTHENTICATOR_ID_OIDC = "T3BlbklEQ29ubmVjdEF1dGhlbnRpY2F0b3I";
    private static final String PRIMARY_IS_IDP_CALLBACK_URL = "https://localhost:9853/commonauth";

    private static final String SECONDARY_IS_TEST_USERNAME = "testFederatedUser";
    private static final String SECONDARY_IS_TEST_PASSWORD = "TestFederatePassword@123";
    private static final String SECONDARY_IS_TEST_USER_ROLES = "admin";

    private static final String SECONDARY_IS_SP_NAME = "secondarySP";
    private static final String SECONDARY_IS_IDP_CALLBACK_URL = "https://localhost:9854/commonauth";
    private static final String SECONDARY_IS_TOKEN_ENDPOINT = "https://localhost:9854/oauth2/token";
    private static final String SECONDARY_IS_LOGOUT_ENDPOINT = "https://localhost:9854/oidc/logout";
    private static final String SECONDARY_IS_AUTHORIZE_ENDPOINT = "https://localhost:9854/oauth2/authorize";
    private static final String HTTPS_LOCALHOST_SERVICES = "https://localhost:%s/";
    private String secondaryISClientID;
    private String secondaryISClientSecret;
    private final String username;
    private final String userPassword;
    private final AutomationContext context;

    private static final int PORT_OFFSET_0 = 0;
    private static final int PORT_OFFSET_1 = 1;

    CookieStore cookieStore;
    private Lookup<CookieSpecProvider> cookieSpecRegistry;
    private RequestConfig requestConfig;
    private CloseableHttpClient client;
    private String secondaryISAppId;
    private String primaryISIdpId;
    private String primaryISAppId;
    private SCIM2RestClient scim2RestClient;
    private String secondaryISUserId;

    @DataProvider(name = "configProvider")
    public static Object[][] configProvider() {
        return new Object[][]{{TestUserMode.SUPER_TENANT_ADMIN}};
    }

    @Factory(dataProvider = "configProvider")
    public OIDCIdentityFederationTestCase(TestUserMode userMode) throws Exception {

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
        createIDPInPrimaryIS();
        createApplicationInPrimaryIS();

        cookieStore = new BasicCookieStore();
        cookieSpecRegistry = RegistryBuilder.<CookieSpecProvider>create()
                .register(CookieSpecs.DEFAULT, new RFC6265CookieSpecProvider())
                .build();
        requestConfig = RequestConfig.custom()
                .setCookieSpec(CookieSpecs.DEFAULT)
                .build();
        client = HttpClientBuilder.create()
                .setDefaultCookieSpecRegistry(cookieSpecRegistry)
                .setDefaultRequestConfig(requestConfig)
                .setDefaultCookieStore(cookieStore)
                .build();

        scim2RestClient = new SCIM2RestClient(getSecondaryISURI(), tenantInfo);
        addUserToSecondaryIS();
    }

    @AfterClass(alwaysRun = true)
    public void endTest() throws Exception {

        try {
            deleteApplication(PORT_OFFSET_0, primaryISAppId);
            deleteIdp(PORT_OFFSET_0, primaryISIdpId);
            deleteApplication(PORT_OFFSET_1, secondaryISAppId);

            deleteAddedUsersInSecondaryIS();

            client.close();
            scim2RestClient.closeHttpClient();
        } catch (Exception e) {
            log.error("Failure occured due to :" + e.getMessage(), e);
            throw e;
        }
    }

    @Test(groups = "wso2.is", description = "Check SAML-to-OIDC federated login")
    public void testFederatedLogin() throws Exception {

        String sessionDataKeyOfSecondaryISLogin = sendSAMLRequestToPrimaryIS();
        Assert.assertNotNull(sessionDataKeyOfSecondaryISLogin,
                "Unable to acquire 'sessionDataKey' value in secondary IS");

        String sessionDataKeyConsentOfSecondaryIS = doAuthenticationInSecondaryIS(sessionDataKeyOfSecondaryISLogin);
        Assert.assertNotNull(sessionDataKeyConsentOfSecondaryIS, "Invalid sessionDataKeyConsent.");

        String callbackURLOfPrimaryIS = doConsentApprovalInSecondaryIS(sessionDataKeyConsentOfSecondaryIS);
        Assert.assertNotNull(callbackURLOfPrimaryIS, "Unable to acquire authorizeCallbackURL in primary IS");

        String samlResponse = getSAMLResponseFromPrimaryIS(callbackURLOfPrimaryIS);
        Assert.assertNotNull(samlResponse, "Unable to acquire SAML response from primary IS");

        String decodedSAMLResponse = new String(Base64.decode(samlResponse));
        Assert.assertTrue(decodedSAMLResponse.contains("AuthnContextClassRef"),
                "AuthnContextClassRef is not received.");

        String homepageContent = sendSAMLResponseToWebApp(samlResponse);
        boolean isValidLogin = validateLoginHomePageContent(homepageContent);
        Assert.assertTrue(isValidLogin, "Invalid SAML login response received by travelocity app");
    }

    @Test(groups = "wso2.is", description = "Check SAML-to-OIDC federated logout", dependsOnMethods = {
            "testFederatedLogin"})
    public void testLogout() throws Exception {

        sendLogoutRequestToPrimaryIS();

        String samlLogoutResponseToWebapp = doLogoutConsentApprovalInSecondaryIS();
        Assert.assertNotNull(samlLogoutResponseToWebapp,
                "Unable to acquire SAML Logout response from travelocity app");

        String decodedSAMLResponse = new String(Base64.decode(samlLogoutResponseToWebapp));
        Assert.assertNotNull(decodedSAMLResponse);

        String logoutPageContent = sendSAMLResponseToWebApp(samlLogoutResponseToWebapp);
        boolean isValidLogout = validateLogoutPageContent(logoutPageContent);
        Assert.assertTrue(isValidLogout, "Invalid SAML Logout response received by travelocity app");
    }

    /**TODO Test case for consent denial from the federated IdP during the logout. Implement after resolving
     * @link https://github.com/wso2/product-is/issues/10636
     */
//    @Test(groups = "wso2.is", description = "Check SAML-to-OIDC federated logout deny-consent", dependsOnMethods = {
//            "testFederatedLogin"})
//    public void testLogoutDenyConsent() throws Exception {
//
//        sendLogoutRequestToPrimaryIS();
//
//        String consentDeniedResponseToWebapp = doLogoutConsentDenyInSecondaryIS();
//        Assert.assertNotNull(consentDeniedResponseToWebapp,
//                "Unable to acquire logout consent deny response");
//        Assert.assertTrue(consentDeniedResponseToWebapp.contains("access_denied"));
//    }

    private void addUserToSecondaryIS() throws Exception {

        UserObject user = new UserObject()
                .userName(SECONDARY_IS_TEST_USERNAME)
                .password(SECONDARY_IS_TEST_PASSWORD);

        secondaryISUserId = scim2RestClient.createUser(user);
        Assert.assertNotNull(secondaryISUserId, "User creation failed in secondary IS.");

        RoleItemAddGroupobj rolePatchReqObject = new RoleItemAddGroupobj();
        rolePatchReqObject.setOp(RoleItemAddGroupobj.OpEnum.ADD);
        rolePatchReqObject.setPath("users");
        rolePatchReqObject.addValue(new ListObject().value(secondaryISUserId));

        String adminRoleId = scim2RestClient.getRoleIdByName(SECONDARY_IS_TEST_USER_ROLES);
        scim2RestClient.updateUserRole(new PatchOperationRequestObject().addOperations(rolePatchReqObject), adminRoleId);
    }

    private void deleteAddedUsersInSecondaryIS() throws IOException {

        scim2RestClient.deleteUser(secondaryISUserId);
    }

    protected String getSecondaryISURI() {

        return String.format(HTTPS_LOCALHOST_SERVICES, DEFAULT_PORT + PORT_OFFSET_1);
    }

    private void createApplicationInPrimaryIS() throws Exception {

        ApplicationModel applicationCreationModel = new ApplicationModel()
                .name(PRIMARY_IS_SP_NAME)
                .description("This is a test Service Provider")
                .isManagementApp(true)
                .inboundProtocolConfiguration(new InboundProtocols().saml(getSAMLConfigurations()))
                .authenticationSequence(new AuthenticationSequence()
                        .type(TypeEnum.USER_DEFINED)
                        .addStepsItem(new org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.AuthenticationStep()
                                .id(1)
                                .addOptionsItem(new Authenticator()
                                        .idp(PRIMARY_IS_IDP_NAME)
                                        .authenticator(PRIMARY_IS_IDP_AUTHENTICATOR_NAME_OIDC))));

        primaryISAppId = addApplication(PORT_OFFSET_0, applicationCreationModel);
        ApplicationResponseModel application = getApplication(PORT_OFFSET_0, primaryISAppId);
        Assert.assertNotNull(application, "Failed to create service provider 'travelocity' in primary IS");

        SAML2ServiceProvider saml2AppConfig = getSAMLInboundDetailsOfApplication(PORT_OFFSET_0, primaryISAppId);
        Assert.assertNotNull(saml2AppConfig, "Failed to update service provider with inbound SAML2 configs in primary IS");

        Assert.assertEquals(TypeEnum.USER_DEFINED, application.getAuthenticationSequence().getType(),
                "Failed to update local and outbound configs in primary IS");
    }

    private void createApplicationInSecondaryIS() throws Exception {

        ApplicationModel applicationCreationModel = new ApplicationModel()
                .name(SECONDARY_IS_SP_NAME)
                .description("This is a test Service Provider")
                .isManagementApp(true)
                .inboundProtocolConfiguration(new InboundProtocols().oidc(getOIDCConfigurations()));

        secondaryISAppId = addApplication(PORT_OFFSET_1, applicationCreationModel);
        Assert.assertNotNull(secondaryISAppId, "Failed to create service provider 'secondarySP' in secondary IS");

        OpenIDConnectConfiguration oidcConfig = getOIDCInboundDetailsOfApplication(PORT_OFFSET_1, secondaryISAppId);
        secondaryISClientID = oidcConfig.getClientId();
        Assert.assertNotNull(secondaryISClientID,
                "Failed to update service provider with inbound OIDC configs in secondary IS");
        secondaryISClientSecret = oidcConfig.getClientSecret();
        Assert.assertNotNull(secondaryISClientSecret,
                "Failed to update service provider with inbound OIDC configs in secondary IS");
    }

    private void createIDPInPrimaryIS() throws Exception {

        FederatedAuthenticator authenticator = new FederatedAuthenticator()
                .authenticatorId(ENCODED_PRIMARY_IS_IDP_AUTHENTICATOR_ID_OIDC)
                .name(PRIMARY_IS_IDP_AUTHENTICATOR_NAME_OIDC)
                .isEnabled(true)
                .addProperty(new org.wso2.identity.integration.test.rest.api.server.idp.v1.model.Property()
                        .key(IdentityConstants.Authenticator.OIDC.IDP_NAME)
                        .value("oidcFedIdP"))
                .addProperty(new org.wso2.identity.integration.test.rest.api.server.idp.v1.model.Property()
                        .key(IdentityConstants.Authenticator.OIDC.CLIENT_ID)
                        .value(secondaryISClientID))
                .addProperty(new org.wso2.identity.integration.test.rest.api.server.idp.v1.model.Property()
                        .key(IdentityConstants.Authenticator.OIDC.CLIENT_SECRET)
                        .value(secondaryISClientSecret))
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
                .jit(new JustInTimeProvisioning()
                        .isEnabled(true)
                        .userstore("PRIMARY"));

        IdentityProviderPOSTRequest idpPostRequest = new IdentityProviderPOSTRequest()
                .name(PRIMARY_IS_IDP_NAME)
                .federatedAuthenticators(oidcAuthnConfig)
                .provisioning(provision);

        primaryISIdpId = addIdentityProvider(PORT_OFFSET_0, idpPostRequest);
        Assert.assertNotNull(primaryISIdpId, "Failed to create Identity Provider 'trustedIdP' in primary IS");
    }

    private OpenIDConnectConfiguration getOIDCConfigurations() {
        List<String> grantTypes = new ArrayList<>();
        Collections.addAll(grantTypes, "authorization_code", "implicit", "password", "client_credentials",
                "refresh_token", "urn:ietf:params:oauth:grant-type:saml2-bearer", "iwa:ntlm");

        OpenIDConnectConfiguration oidcConfig = new OpenIDConnectConfiguration();
        oidcConfig.setGrantTypes(grantTypes);
        oidcConfig.addCallbackURLsItem(PRIMARY_IS_IDP_CALLBACK_URL);

        return oidcConfig;
    }

    private SAML2Configuration getSAMLConfigurations() {

        SAML2ServiceProvider serviceProvider = new SAML2ServiceProvider()
                .issuer(PRIMARY_IS_SAML_ISSUER_NAME)
                .addAssertionConsumerUrl(PRIMARY_IS_SAML_ACS_URL)
                .defaultAssertionConsumerUrl(PRIMARY_IS_SAML_ACS_URL)
                .attributeProfile(new SAMLAttributeProfile()
                        .enabled(true)
                        .alwaysIncludeAttributesInResponse(true))
                .singleLogoutProfile(new SingleLogoutProfile()
                        .enabled(true))
                .responseSigning(new SAMLResponseSigning()
                        .enabled(true))
                .singleSignOnProfile(new SingleSignOnProfile()
                        .assertion(new SAMLAssertionConfiguration()
                                .nameIdFormat(PRIMARY_IS_SAML_NAME_ID_FORMAT)));

        return new SAML2Configuration().manualConfiguration(serviceProvider);
    }

    private String sendSAMLRequestToPrimaryIS() throws Exception {

        HttpGet request = new HttpGet(SAML_SSO_URL);
        request.setHeader("User-Agent", USER_AGENT);
        HttpResponse response = client.execute(request);
        return extractValueFromResponse(response, "name=\"sessionDataKey\"", 1);
    }

    private String doAuthenticationInSecondaryIS(String sessionDataKey) throws Exception {

        HttpResponse response = sendLoginPost(client, sessionDataKey);
        Assert.assertNotNull(response, "Login request failed. response is null.");

        Header locationHeader = response.getFirstHeader(OAuth2Constant.HTTP_RESPONSE_HEADER_LOCATION);
        Assert.assertNotNull(locationHeader, "Login response header is null.");
        EntityUtils.consume(response.getEntity());

        response = sendGetRequest(client, locationHeader.getValue());
        Map<String, Integer> keyPositionMap = new HashMap<>(1);
        keyPositionMap.put("name=\"sessionDataKeyConsent\"", 1);
        List<DataExtractUtil.KeyValue> keyValues = DataExtractUtil.extractSessionConsentDataFromResponse(response,
                keyPositionMap);
        Assert.assertNotNull(keyValues, "SessionDataKeyConsent key value is null.");

        String sessionDataKeyConsent = keyValues.get(0).getValue();
        EntityUtils.consume(response.getEntity());

        return sessionDataKeyConsent;
    }

    private HttpResponse sendLoginPost(HttpClient client, String sessionDataKey) throws IOException {

        List<NameValuePair> urlParameters = new ArrayList<>();
        urlParameters.add(new BasicNameValuePair("username", SECONDARY_IS_TEST_USERNAME + "@" + tenantInfo.getDomain()));
        urlParameters.add(new BasicNameValuePair("password", SECONDARY_IS_TEST_PASSWORD));
        urlParameters.add(new BasicNameValuePair("sessionDataKey", sessionDataKey));

        HttpResponse response = sendPostRequestWithParameters(client, urlParameters, SECONDARY_IS_IDP_CALLBACK_URL);

        return response;
    }

    private String doConsentApprovalInSecondaryIS(String sessionDataKeyConsent) throws Exception {

        List<NameValuePair> consentParameters = new ArrayList<>();

        HttpResponse response = sendApprovalPostWithConsent(client, sessionDataKeyConsent, consentParameters);
        Assert.assertNotNull(response, "Approval request failed.");

        Header locationHeader = response.getFirstHeader(OAuth2Constant.HTTP_RESPONSE_HEADER_LOCATION);
        EntityUtils.consume(response.getEntity());

        String authzResponseURL = locationHeader.getValue();
        Assert.assertNotNull(authzResponseURL, "Approval request failed for.");

        String authorizeURL = testAuthzCode(authzResponseURL);
        return authorizeURL;
    }

    private HttpResponse sendApprovalPostWithConsent(HttpClient client, String sessionDataKeyConsent,
                                                    List<NameValuePair> consentClaims) throws IOException {

        List<NameValuePair> urlParameters = new ArrayList<>();
        urlParameters.add(new BasicNameValuePair("sessionDataKeyConsent", sessionDataKeyConsent));
        urlParameters.add(new BasicNameValuePair("scope-approval", "approve"));
        urlParameters.add(new BasicNameValuePair("user_claims_consent", "true"));
        urlParameters.add(new BasicNameValuePair("consent_select_all", "on"));
        urlParameters.add(new BasicNameValuePair("consent_0", "on"));
        urlParameters.add(new BasicNameValuePair("consent", "approve"));

        if (consentClaims != null) {
            urlParameters.addAll(consentClaims);
        }

        HttpResponse response = sendPostRequestWithParameters(client, urlParameters, SECONDARY_IS_AUTHORIZE_ENDPOINT);
        return response;
    }

    private String testAuthzCode(String authzResponseURL) throws Exception {

        HttpClient httpClientWithoutAutoRedirections = HttpClientBuilder.create()
                .disableRedirectHandling()
                .setDefaultCookieSpecRegistry(cookieSpecRegistry)
                .setDefaultRequestConfig(requestConfig)
                .setDefaultCookieStore(cookieStore).build();

        HttpResponse response = sendGetRequest(httpClientWithoutAutoRedirections, authzResponseURL);
        Assert.assertNotNull(response, "Authorization code response to primary IS is invalid.");

        String locationHeader = getHeaderValue(response, "Location");
        Assert.assertNotNull(locationHeader, "locationHeader not found in response.");

        String pastrCookie = Utils.getPastreCookie(response);
        Assert.assertNotNull(pastrCookie, "pastr cookie not found in response.");

        if (Utils.requestMissingClaims(response)) {
            locationHeader = handleMissingClaims(response, locationHeader, client, pastrCookie);
            Assert.assertNotNull(locationHeader, "locationHeader not found in response.");
        }

        return locationHeader;
    }

    private String handleMissingClaims(HttpResponse response, String locationHeader, HttpClient client, String
            pastrCookie) throws Exception {

        EntityUtils.consume(response.getEntity());

        response = Utils.sendPOSTConsentMessage(response, PRIMARY_IS_IDP_CALLBACK_URL, USER_AGENT, locationHeader,
                client, pastrCookie);
        EntityUtils.consume(response.getEntity());

        return getHeaderValue(response, "Location");
    }

    private String getSAMLResponseFromPrimaryIS(String callbackURL) throws IOException {

        HttpResponse response = sendGetRequest(client, callbackURL);
        return extractValueFromResponse(response, "SAMLResponse", 5);
    }

    private String sendSAMLResponseToWebApp(String samlResponse)
            throws Exception {

        HttpResponse response = getHttpResponseWebApp(samlResponse);

        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
        StringBuffer buffer = new StringBuffer();
        String line = "";
        while ((line = bufferedReader.readLine()) != null) {
            buffer.append(line);
        }
        bufferedReader.close();

        return buffer.toString();
    }

    private HttpResponse getHttpResponseWebApp(String samlResponse) throws IOException {

        HttpPost request = new HttpPost(PRIMARY_IS_SAML_ACS_URL);
        request.setHeader("User-Agent", USER_AGENT);
        List<NameValuePair> urlParameters = new ArrayList<NameValuePair>();
        urlParameters.add(new BasicNameValuePair("SAMLResponse", samlResponse));
        request.setEntity(new UrlEncodedFormEntity(urlParameters));
        return client.execute(request);
    }

    private boolean validateLoginHomePageContent(String homepageContent) {

        return homepageContent.contains("You are logged in as ");
    }

    private HttpResponse sendLogoutRequestToPrimaryIS() throws IOException {

        HttpResponse response = sendGetRequest(client, SAML_LOGOUT_URL);
        EntityUtils.consume(response.getEntity());
        Assert.assertNotNull(response);
        return response;
    }

    private String doLogoutConsentApprovalInSecondaryIS() throws Exception {

        HttpResponse response = sendLogoutApprovalPostWithConsent(client);
        Assert.assertNotNull(response, "Approval request failed.");

        Header locationHeader = response.getFirstHeader(OAuth2Constant.HTTP_RESPONSE_HEADER_LOCATION);
        Assert.assertNotNull(locationHeader, "Approval request failed for.");
        EntityUtils.consume(response.getEntity());

        String logoutResponseToPrimaryIS = locationHeader.getValue();

        response = sendGetRequest(client, logoutResponseToPrimaryIS);
        return extractValueFromResponse(response, "SAMLResponse", 5);
    }

    private String doLogoutConsentDenyInSecondaryIS() throws Exception {

        HttpResponse response = sendLogoutDenyPostWithConsent(client);
        Assert.assertNotNull(response, "Approval request failed.");

        Header locationHeader = response.getFirstHeader(OAuth2Constant.HTTP_RESPONSE_HEADER_LOCATION);
        Assert.assertNotNull(locationHeader, "Approval request failed for.");
        EntityUtils.consume(response.getEntity());

        String logoutResponseToPrimaryIS = locationHeader.getValue();

        response = sendGetRequest(client, logoutResponseToPrimaryIS);
        return extractValueFromResponse(response, "error", 3);
    }

    private boolean validateLogoutPageContent(String logoutPageContent) {

        return logoutPageContent.contains("location.href = \"index.jsp\"");
    }

    private HttpResponse sendPostRequestWithParameters(HttpClient client, List<NameValuePair> urlParameters, String url)
            throws ClientProtocolException, IOException {

        HttpPost request = new HttpPost(url);
        request.setHeader("User-Agent", OAuth2Constant.USER_AGENT);
        request.setEntity(new UrlEncodedFormEntity(urlParameters));

        HttpResponse response = client.execute(request);
        return response;
    }

    private HttpResponse sendGetRequest(HttpClient client, String locationURL) throws ClientProtocolException,
            IOException {

        HttpGet getRequest = new HttpGet(locationURL);
        getRequest.addHeader("User-Agent", OAuth2Constant.USER_AGENT);
        HttpResponse response = client.execute(getRequest);

        return response;
    }

    private HttpResponse sendLogoutApprovalPostWithConsent(HttpClient client) throws IOException {

        List<NameValuePair> urlParameters = new ArrayList<>();
        urlParameters.add(new BasicNameValuePair("consent", "approve"));

        HttpResponse response = sendPostRequestWithParameters(client, urlParameters, SECONDARY_IS_LOGOUT_ENDPOINT);
        return response;
    }

    private HttpResponse sendLogoutDenyPostWithConsent(HttpClient client) throws IOException {

        List<NameValuePair> urlParameters = new ArrayList<>();
        urlParameters.add(new BasicNameValuePair("consent", "deny"));

        HttpResponse response = sendPostRequestWithParameters(client, urlParameters, SECONDARY_IS_LOGOUT_ENDPOINT);
        return response;
    }

    private HttpResponse sendPostRequest(HttpClient client, String locationURL) throws ClientProtocolException,
            IOException {

        HttpPost postRequest = new HttpPost(locationURL);
        postRequest.setHeader("User-Agent", OAuth2Constant.USER_AGENT);
        HttpResponse response = client.execute(postRequest);

        return response;
    }
}
