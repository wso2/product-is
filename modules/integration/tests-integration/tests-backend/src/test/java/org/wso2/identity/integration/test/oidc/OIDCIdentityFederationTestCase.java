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
import org.opensaml.xml.util.Base64;
import org.testng.Assert;
import org.testng.annotations.*;
import org.wso2.carbon.automation.engine.context.AutomationContext;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.identity.integration.test.application.mgt.AbstractIdentityFederationTestCase;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.*;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.AuthenticationSequence.TypeEnum;
import org.wso2.identity.integration.test.rest.api.server.idp.v1.model.FederatedAuthenticatorRequest;
import org.wso2.identity.integration.test.rest.api.server.idp.v1.model.FederatedAuthenticatorRequest.FederatedAuthenticator;
import org.wso2.identity.integration.test.rest.api.server.idp.v1.model.IdentityProviderPOSTRequest;
import org.wso2.identity.integration.test.rest.api.server.idp.v1.model.ProvisioningRequest;
import org.wso2.identity.integration.test.rest.api.server.idp.v1.model.ProvisioningRequest.JustInTimeProvisioning;
import org.wso2.identity.integration.test.utils.IdentityConstants;
import org.wso2.identity.integration.test.utils.OAuth2Constant;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

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

    private final AutomationContext context;

    private static final int PORT_OFFSET_0 = 0;

    CookieStore cookieStore;
    private CloseableHttpClient client;
    private String primaryISIdpId;
    private String primaryISAppId;
    private MockOIDCService mockOIDCService;

    @DataProvider(name = "configProvider")
    public static Object[][] configProvider() {
        return new Object[][]{{TestUserMode.SUPER_TENANT_ADMIN}};
    }

    @Factory(dataProvider = "configProvider")
    public OIDCIdentityFederationTestCase(TestUserMode userMode) throws Exception {

        context = new AutomationContext("IDENTITY", userMode);
    }

    @BeforeClass(alwaysRun = true)
    public void initTest() throws Exception {

        mockOIDCService = new MockOIDCService();
        mockOIDCService.start();
        super.initTest();

        createServiceClients(PORT_OFFSET_0, new IdentityConstants.ServiceClientType[]{
                IdentityConstants.ServiceClientType.APPLICATION_MANAGEMENT,
                IdentityConstants.ServiceClientType.IDENTITY_PROVIDER_MGT});

        createIDPInPrimaryIS();
        createApplicationInPrimaryIS();

        cookieStore = new BasicCookieStore();
        Lookup<CookieSpecProvider> cookieSpecRegistry = RegistryBuilder.<CookieSpecProvider>create()
                .register(CookieSpecs.DEFAULT, new RFC6265CookieSpecProvider())
                .build();
        RequestConfig requestConfig = RequestConfig.custom()
                .setCookieSpec(CookieSpecs.DEFAULT)
                .build();
        client = HttpClientBuilder.create()
                .setDefaultCookieSpecRegistry(cookieSpecRegistry)
                .setDefaultRequestConfig(requestConfig)
                .setDefaultCookieStore(cookieStore)
                .build();
    }

    @AfterClass(alwaysRun = true)
    public void endTest() throws Exception {

        try {
            deleteApplication(PORT_OFFSET_0, primaryISAppId);
            deleteIdp(PORT_OFFSET_0, primaryISIdpId);

            client.close();
            mockOIDCService.stop();
        } catch (Exception e) {
            log.error("Failure occurred due to :" + e.getMessage(), e);
            throw e;
        }
    }

    @Test(groups = "wso2.is", description = "Check SAML-to-OIDC federated login")
    public void testFederatedLogin() throws Exception {

        // Sending the SAML request to the primary IS
        // Client will handle all the redirections and will return the final response of the flow which contains the
        // SAMLResponse. This is because the mock server is not prompting anything to the user.
        HttpGet request = new HttpGet(SAML_SSO_URL);
        request.setHeader("User-Agent", USER_AGENT);
        HttpResponse response = client.execute(request);

        String samlResponse = extractValueFromResponse(response, "SAMLResponse", 5);
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

        HttpResponse response = sendGetRequest(client, SAML_LOGOUT_URL);
        Assert.assertNotNull(response);

        String samlLogoutResponseToWebapp = extractValueFromResponse(response, "SAMLResponse", 5);
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
                        .value("secondaryISClientID"))
                .addProperty(new org.wso2.identity.integration.test.rest.api.server.idp.v1.model.Property()
                        .key(IdentityConstants.Authenticator.OIDC.CLIENT_SECRET)
                        .value("secondaryISClientSecret"))
                .addProperty(new org.wso2.identity.integration.test.rest.api.server.idp.v1.model.Property()
                        .key(IdentityConstants.Authenticator.OIDC.OAUTH2_AUTHZ_URL)
                        .value("http://localhost:8089/authorize"))
                .addProperty(new org.wso2.identity.integration.test.rest.api.server.idp.v1.model.Property()
                        .key(IdentityConstants.Authenticator.OIDC.OAUTH2_TOKEN_URL)
                        .value("http://localhost:8089/token"))
                .addProperty(new org.wso2.identity.integration.test.rest.api.server.idp.v1.model.Property()
                        .key(IdentityConstants.Authenticator.OIDC.CALLBACK_URL)
                        .value(PRIMARY_IS_IDP_CALLBACK_URL))
                .addProperty(new org.wso2.identity.integration.test.rest.api.server.idp.v1.model.Property()
                        .key(IdentityConstants.Authenticator.OIDC.OIDC_LOGOUT_URL)
                        .value("http://localhost:8089/oidc/logout"))
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
        List<NameValuePair> urlParameters = new ArrayList<>();
        urlParameters.add(new BasicNameValuePair("SAMLResponse", samlResponse));
        request.setEntity(new UrlEncodedFormEntity(urlParameters));
        return client.execute(request);
    }

    private boolean validateLoginHomePageContent(String homepageContent) {

        return homepageContent.contains("You are logged in as ");
    }

    private boolean validateLogoutPageContent(String logoutPageContent) {

        return logoutPageContent.contains("location.href = \"index.jsp\"");
    }

    private HttpResponse sendGetRequest(HttpClient client, String locationURL) throws ClientProtocolException,
            IOException {

        HttpGet getRequest = new HttpGet(locationURL);
        getRequest.addHeader("User-Agent", OAuth2Constant.USER_AGENT);
        HttpResponse response = client.execute(getRequest);

        return response;
    }
}
