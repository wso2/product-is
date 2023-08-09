/*
 * Copyright (c) 2015, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.identity.integration.test.saml;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
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
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.cookie.RFC6265CookieSpecProvider;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.integration.common.utils.mgt.ServerConfigurationManager;
import org.wso2.identity.integration.test.application.mgt.AbstractIdentityFederationTestCase;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.ApplicationModel;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.AuthenticationSequence;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.AuthenticationSequence.TypeEnum;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.Authenticator;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.IdpInitiatedSingleLogout;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.InboundProtocols;
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
import org.wso2.identity.integration.test.rest.api.server.idp.v1.model.Property;
import org.wso2.identity.integration.test.util.Utils;
import org.wso2.identity.integration.test.utils.IdentityConstants;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChangeACSUrlTestCase extends AbstractIdentityFederationTestCase {

    private static final String PRIMARY_IS_SERVICE_PROVIDER_NAME = "travelocity-changeACS";
    private static final String SECONDARY_IS_SERVICE_PROVIDER_NAME = "secondaryChangeACSSP";
    private static final String IDENTITY_PROVIDER_NAME = "ChangeACSIdP";
    private static final String PRIMARY_IS_SAML_ISSUER_NAME = "travelocity.com";
    private static final String PRIMARY_IS_SAML_ACS_URL = "http://localhost:8490/travelocity.com/home.jsp";
    private static final String SECONDARY_IS_SAML_ISSUER_NAME = "samlChangeACSSP";
    private static final String SAML_NAME_ID_FORMAT = "urn:oasis:names:tc:SAML:1.1:nameid-format:emailAddress";
    private static final String SAML_SSO_URL = "http://localhost:8490/travelocity.com/samlsso?SAML2" +
            ".HTTPBinding=HTTP-Redirect";
    private static final String USER_AGENT = "Apache-HttpClient/4.2.5 (java 1.5)";
    private static final int PORT_OFFSET_0 = 0;
    private static final int PORT_OFFSET_1 = 1;
    private static final String PRIMARY_IS_IDP_AUTHENTICATOR_NAME_SAML = "SAMLSSOAuthenticator";
    private static final String ENCODED_PRIMARY_IS_IDP_AUTHENTICATOR_ID_SAML = "U0FNTFNTT0F1dGhlbnRpY2F0b3I";
    private String COMMON_AUTH_URL = "https://localhost:%s/commonauth";
    private String COMMON_AUTH_URL_CHANGED = "https://localhost:%s/commonauth1";

    private String usrName = "admin";
    private String usrPwd = "admin";

    private ServerConfigurationManager serverConfigurationManager;
    private String primaryISIdpId;
    private String primaryISAppId;
    private String secondaryISAppId;

    @BeforeClass(alwaysRun = true)
    public void initTest() throws Exception {

        super.initTest();

        String carbonHome = Utils.getResidentCarbonHome();
        File defaultTomlFile = getDeploymentTomlFile(carbonHome);
        File configuredTomlFile = new File(getISResourceLocation() + File.separator + "saml" + File.separator
                + "application_authentication_changed_acs.toml");

        serverConfigurationManager = new ServerConfigurationManager(isServer);
        serverConfigurationManager.applyConfigurationWithoutRestart(configuredTomlFile, defaultTomlFile, true);
        serverConfigurationManager.restartGracefully();

        super.initTest();

        createServiceClients(PORT_OFFSET_0, new IdentityConstants.ServiceClientType[]{
                IdentityConstants.ServiceClientType.APPLICATION_MANAGEMENT,
                IdentityConstants.ServiceClientType.IDENTITY_PROVIDER_MGT});

        createServiceClients(PORT_OFFSET_1, new IdentityConstants.ServiceClientType[]{
                IdentityConstants.ServiceClientType.APPLICATION_MANAGEMENT});

        createIdpInPrimaryIS();
        createApplicationInPrimaryIS();
        createApplicationInSecondaryIS();
    }

    @AfterClass(alwaysRun = true)
    public void endTest() throws Exception {

        deleteApplication(PORT_OFFSET_0, primaryISAppId);
        deleteIdp(PORT_OFFSET_0, primaryISIdpId);

        deleteApplication(PORT_OFFSET_1, secondaryISAppId);
        serverConfigurationManager.restoreToLastConfiguration(false);
    }

    @Test(groups = "wso2.is", description = "Check SAML To SAML fedaration flow")
    public void testChangeACSUrl() throws Exception {

        Lookup<CookieSpecProvider> cookieSpecRegistry = RegistryBuilder.<CookieSpecProvider>create()
                .register(CookieSpecs.DEFAULT, new RFC6265CookieSpecProvider())
                .build();
        RequestConfig requestConfig = RequestConfig.custom()
                .setCookieSpec(CookieSpecs.DEFAULT)
                .build();
        try (CloseableHttpClient client = HttpClientBuilder.create()
                             .setDefaultCookieSpecRegistry(cookieSpecRegistry)
                             .setDefaultRequestConfig(requestConfig)
                             .setDefaultCookieStore(new BasicCookieStore())
                             .build()) {
            String sessionId = sendSAMLRequestToPrimaryIS(client);
            Assert.assertNotNull(sessionId, "Unable to acquire 'sessionDataKey' value");

            String redirectURL = authenticateWithSecondaryIS(client, sessionId);
            Assert.assertNotNull(redirectURL, "Unable to acquire redirect url after login to secondary IS");

            Map<String, String> responseParameters = getSAMLResponseFromSecondaryIS(client, redirectURL);
            Assert.assertNotNull(responseParameters.get("SAMLResponse"), "Unable to acquire 'SAMLResponse' value");
            Assert.assertNotNull(responseParameters.get("RelayState"), "Unable to acquire 'RelayState' value");

            redirectURL = sendSAMLResponseToPrimaryIS(client, responseParameters);
            Assert.assertNotNull(redirectURL, "Unable to acquire redirect url after sending SAML response to primary IS");

            String samlResponse = getSAMLResponseFromPrimaryIS(client, redirectURL);
            Assert.assertNotNull(samlResponse, "Unable to acquire SAML response from primary IS");

            boolean validResponse = sendSAMLResponseToWebApp(client, samlResponse);
            Assert.assertTrue(validResponse, "Invalid SAML response received by travelocity app");
        }

    }

    private void createIdpInPrimaryIS() throws Exception {

        FederatedAuthenticator authenticator = new FederatedAuthenticator()
                .authenticatorId(ENCODED_PRIMARY_IS_IDP_AUTHENTICATOR_ID_SAML)
                .name(PRIMARY_IS_IDP_AUTHENTICATOR_NAME_SAML)
                .isEnabled(true)
                .addProperty(new Property()
                        .key(IdentityConstants.Authenticator.SAML2SSO.IDP_ENTITY_ID)
                        .value("samlChangeACSIdP"))
                .addProperty(new Property()
                        .key(IdentityConstants.Authenticator.SAML2SSO.SP_ENTITY_ID)
                        .value("samlChangeACSSP"))
                .addProperty(new Property()
                        .key(IdentityConstants.Authenticator.SAML2SSO.SSO_URL)
                        .value("https://localhost:9854/samlsso"))
                .addProperty(new Property()
                        .key(IdentityConstants.Authenticator.SAML2SSO.IS_AUTHN_REQ_SIGNED)
                        .value("false"))
                .addProperty(new Property()
                        .key(IdentityConstants.Authenticator.SAML2SSO.IS_LOGOUT_ENABLED)
                        .value("true"))
                .addProperty(new Property()
                        .key(IdentityConstants.Authenticator.SAML2SSO.LOGOUT_REQ_URL))
                .addProperty(new Property()
                        .key(IdentityConstants.Authenticator.SAML2SSO.IS_LOGOUT_REQ_SIGNED)
                        .value("false"))
                .addProperty(new Property()
                        .key(IdentityConstants.Authenticator.SAML2SSO.IS_AUTHN_RESP_SIGNED)
                        .value("false"))
                .addProperty(new Property()
                        .key(IdentityConstants.Authenticator.SAML2SSO.IS_USER_ID_IN_CLAIMS)
                        .value("false"))
                .addProperty(new Property()
                        .key(IdentityConstants.Authenticator.SAML2SSO.IS_ENABLE_ASSERTION_ENCRYPTION)
                        .value("false"))
                .addProperty(new Property()
                        .key(IdentityConstants.Authenticator.SAML2SSO.IS_ENABLE_ASSERTION_SIGNING)
                        .value("false"))
                .addProperty(new Property()
                        .key("commonAuthQueryParams"))
                .addProperty(new Property()
                        .key("AttributeConsumingServiceIndex"));

        FederatedAuthenticatorRequest oidcAuthnConfig = new FederatedAuthenticatorRequest()
                .defaultAuthenticatorId(ENCODED_PRIMARY_IS_IDP_AUTHENTICATOR_ID_SAML)
                .addAuthenticator(authenticator);

        IdentityProviderPOSTRequest idpPostRequest = new IdentityProviderPOSTRequest()
                .name(IDENTITY_PROVIDER_NAME)
                .federatedAuthenticators(oidcAuthnConfig);

        primaryISIdpId = addIdentityProvider(PORT_OFFSET_0, idpPostRequest);
    }

    private void createApplicationInPrimaryIS() throws Exception {

        ApplicationModel applicationCreationModel = new ApplicationModel()
                .name(PRIMARY_IS_SERVICE_PROVIDER_NAME)
                .description("This is a test Service Provider")
                .isManagementApp(true)
                .inboundProtocolConfiguration(new InboundProtocols()
                        .saml(getSAMLConfigurations(PRIMARY_IS_SAML_ISSUER_NAME, PRIMARY_IS_SAML_ACS_URL)))
                .authenticationSequence(new AuthenticationSequence()
                        .type(TypeEnum.USER_DEFINED)
                        .addStepsItem(new org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.AuthenticationStep()
                                .id(1)
                                .addOptionsItem(new Authenticator()
                                        .idp(IDENTITY_PROVIDER_NAME)
                                        .authenticator(PRIMARY_IS_IDP_AUTHENTICATOR_NAME_SAML))));

        primaryISAppId = addApplication(PORT_OFFSET_0, applicationCreationModel);
    }

    private void createApplicationInSecondaryIS() throws Exception {

        ApplicationModel applicationCreationModel = new ApplicationModel()
                .name(SECONDARY_IS_SERVICE_PROVIDER_NAME)
                .description("This is a test Service Provider")
                .isManagementApp(true)
                .inboundProtocolConfiguration(new InboundProtocols()
                        .saml(getSAMLConfigurations(SECONDARY_IS_SAML_ISSUER_NAME,
                                String.format(COMMON_AUTH_URL_CHANGED, DEFAULT_PORT + PORT_OFFSET_0))));

        secondaryISAppId = addApplication(PORT_OFFSET_1, applicationCreationModel);
    }

    private SAML2Configuration getSAMLConfigurations(String issuerName, String acsUrl) {

        SAML2ServiceProvider serviceProvider = new SAML2ServiceProvider()
                .issuer(issuerName)
                .addAssertionConsumerUrl(acsUrl)
                .defaultAssertionConsumerUrl(acsUrl)
                .attributeProfile(new SAMLAttributeProfile()
                        .enabled(true)
                        .alwaysIncludeAttributesInResponse(true))
                .singleLogoutProfile(new SingleLogoutProfile()
                        .enabled(true)
                        .idpInitiatedSingleLogout(new IdpInitiatedSingleLogout().enabled(true)))
                .responseSigning(new SAMLResponseSigning()
                        .enabled(true))
                .singleSignOnProfile(new SingleSignOnProfile()
                        .assertion(new SAMLAssertionConfiguration().nameIdFormat(SAML_NAME_ID_FORMAT)));

        return new SAML2Configuration().manualConfiguration(serviceProvider);
    }

    private String sendSAMLRequestToPrimaryIS(HttpClient client) throws Exception {

        HttpGet request = new HttpGet(SAML_SSO_URL);
        request.addHeader("User-Agent", USER_AGENT);
        HttpResponse response = client.execute(request);

        return extractValueFromResponse(response, "name=\"sessionDataKey\"", 1);
    }

    private String authenticateWithSecondaryIS(HttpClient client, String sessionId)
            throws Exception {

        HttpPost request = new HttpPost(String.format(COMMON_AUTH_URL, DEFAULT_PORT + PORT_OFFSET_1));
        request.addHeader("User-Agent", USER_AGENT);
        request.addHeader("Referer", PRIMARY_IS_SAML_ACS_URL);

        List<NameValuePair> urlParameters = new ArrayList<NameValuePair>();
        urlParameters.add(new BasicNameValuePair("username", usrName));
        urlParameters.add(new BasicNameValuePair("password", usrPwd));
        urlParameters.add(new BasicNameValuePair("sessionDataKey", sessionId));
        request.setEntity(new UrlEncodedFormEntity(urlParameters));

        HttpResponse response = client.execute(request);

        if (Utils.requestMissingClaims(response)) {
            String pastrCookie = Utils.getPastreCookie(response);
            Assert.assertNotNull(pastrCookie, "pastr cookie not found in response.");
            EntityUtils.consume(response.getEntity());

            response = Utils.sendPOSTConsentMessage(response,
                                                    String.format(COMMON_AUTH_URL, DEFAULT_PORT + PORT_OFFSET_1),
                                                    USER_AGENT, PRIMARY_IS_SAML_ACS_URL, client, pastrCookie);
            EntityUtils.consume(response.getEntity());
        }

        closeHttpConnection(response);
        return getHeaderValue(response, "Location");
    }

    private Map<String, String> getSAMLResponseFromSecondaryIS(HttpClient client, String redirectURL) throws Exception {
        HttpPost request = new HttpPost(redirectURL);
        request.addHeader("User-Agent", USER_AGENT);
        request.addHeader("Referer", PRIMARY_IS_SAML_ACS_URL);
        HttpResponse response = client.execute(request);

        Map<String, Integer> searchParams = new HashMap<String, Integer>();
        searchParams.put("SAMLResponse", 5);
        searchParams.put("RelayState", 5);
        return extractValuesFromResponse(response, searchParams);
    }

    private String sendSAMLResponseToPrimaryIS(HttpClient client, Map<String, String> searchResults) throws Exception {

        HttpPost request = new HttpPost(String.format(COMMON_AUTH_URL, DEFAULT_PORT + PORT_OFFSET_0));
        request.setHeader("User-Agent", USER_AGENT);

        List<NameValuePair> urlParameters = new ArrayList<NameValuePair>();
        urlParameters.add(new BasicNameValuePair("SAMLResponse", searchResults.get("SAMLResponse")));
        urlParameters.add(new BasicNameValuePair("RelayState", searchResults.get("RelayState")));
        request.setEntity(new UrlEncodedFormEntity(urlParameters));

        HttpResponse response = client.execute(request);

        if (Utils.requestMissingClaims(response)) {
            String pastrCookie = Utils.getPastreCookie(response);
            Assert.assertNotNull(pastrCookie, "pastr cookie not found in response.");
            EntityUtils.consume(response.getEntity());

            response = Utils.sendPOSTConsentMessage(response,
                                                    String.format(COMMON_AUTH_URL, DEFAULT_PORT + PORT_OFFSET_0),
                                                    USER_AGENT, PRIMARY_IS_SAML_ACS_URL, client, pastrCookie);
            EntityUtils.consume(response.getEntity());
        }
        closeHttpConnection(response);
        return getHeaderValue(response, "Location");
    }

    private String getSAMLResponseFromPrimaryIS(HttpClient client, String redirectURL)
            throws Exception {

        HttpGet request = new HttpGet(redirectURL);
        request.addHeader("User-Agent", USER_AGENT);
        HttpResponse response = client.execute(request);

        return extractValueFromResponse(response, "SAMLResponse", 5);
    }

    private boolean sendSAMLResponseToWebApp(HttpClient client, String samlResponse)
            throws Exception {

        HttpPost request = new HttpPost(PRIMARY_IS_SAML_ACS_URL);
        request.setHeader("User-Agent", USER_AGENT);
        List<NameValuePair> urlParameters = new ArrayList<NameValuePair>();
        urlParameters.add(new BasicNameValuePair("SAMLResponse", samlResponse));
        request.setEntity(new UrlEncodedFormEntity(urlParameters));
        HttpResponse response = new DefaultHttpClient().execute(request);

        return validateSAMLResponse(response, usrName);
    }

    public boolean validateSAMLResponse(HttpResponse response, String userName) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
        StringBuffer buffer = new StringBuffer();
        String line = "";
        while ((line = bufferedReader.readLine()) != null) {
            buffer.append(line);
        }
        bufferedReader.close();

        return buffer.toString().contains("You are logged in as ");
    }
}
