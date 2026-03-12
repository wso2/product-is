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
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.cookie.RFC6265CookieSpecProvider;
import org.apache.http.message.BasicNameValuePair;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.identity.integration.test.application.mgt.AbstractIdentityFederationTestCase;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.ApplicationModel;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.ApplicationResponseModel;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.AuthenticationSequence;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.AuthenticationSequence.TypeEnum;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.Authenticator;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.AuthenticationStep;
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
import org.wso2.identity.integration.test.rest.api.server.idp.v1.model.PatchRequest;
import org.wso2.identity.integration.test.rest.api.server.idp.v1.model.Property;
import org.wso2.identity.integration.test.rest.api.user.common.model.Email;
import org.wso2.identity.integration.test.rest.api.user.common.model.Name;
import org.wso2.identity.integration.test.rest.api.user.common.model.UserObject;
import org.wso2.identity.integration.test.restclients.SCIM2RestClient;
import org.wso2.identity.integration.test.utils.IdentityConstants;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.http.util.EntityUtils;
import org.w3c.dom.Document;

/**
 * Integration test for SAML federation between two IS instances with key rotation in place.
 */
public class SAMLFederationWithKeyRotationTestCase extends AbstractIdentityFederationTestCase {

    private static final int PORT_OFFSET_0 = 0;
    private static final int PORT_OFFSET_1 = 1;

    // Travelocity app running on embedded Tomcat
    private static final String SAML_SSO_URL =
            "http://localhost:8490/travelocity.com/samlsso?SAML2.HTTPBinding=HTTP-Redirect";
    private static final String PRIMARY_IS_SAML_ACS_URL =
            "http://localhost:8490/travelocity.com/home.jsp";

    // SP / IdP names and SAML issuer IDs
    private static final String PRIMARY_IS_SP_NAME = "travelocity-key-rotation";
    private static final String PRIMARY_IS_SAML_ISSUER = "travelocity.com";

    private static final String SECONDARY_IS_SP_NAME = "primaryIS-key-rotation";
    private static final String SECONDARY_IS_SAML_ISSUER = "is-sp-saml-key-rotation";

    private static final String IDENTITY_PROVIDER_NAME = "secondaryIS-keyRotationIdP";
    private static final String IDP_AUTHENTICATOR_NAME = "SAMLSSOAuthenticator";
    private static final String ENCODED_IDP_AUTHENTICATOR_ID = "U0FNTFNTT0F1dGhlbnRpY2F0b3I";

    private static final String SAML_NAME_ID_FORMAT =
            "urn:oasis:names:tc:SAML:1.1:nameid-format:emailAddress";
    private static final String COMMON_AUTH_URL = "https://localhost:%s/commonauth";

    // Test user created in the secondary IS.
    private static final String USER_NAME = "samlKeyRotationUser";
    private static final String PASSWORD = "SamlKeyRotationPassword@1";
    private static final String EMAIL = "samlKeyRotationUser@wso2.com";

    private SCIM2RestClient scim2RestClient;

    private String userId;
    private String primaryISIdpId;
    private String primaryISAppId;
    private String secondaryISAppId;

    @BeforeClass(alwaysRun = true)
    public void initTest() throws Exception {

        super.initTest();

        createServiceClients(PORT_OFFSET_0, new IdentityConstants.ServiceClientType[]{
                IdentityConstants.ServiceClientType.APPLICATION_MANAGEMENT,
                IdentityConstants.ServiceClientType.IDENTITY_PROVIDER_MGT});
        createServiceClients(PORT_OFFSET_1, new IdentityConstants.ServiceClientType[]{
                IdentityConstants.ServiceClientType.APPLICATION_MANAGEMENT});

        scim2RestClient = new SCIM2RestClient(getSecondaryISURI(), tenantInfo);

        createUserInSecondaryIS();
        createIdpInPrimaryIS();
        createApplicationInPrimaryIS();
        createApplicationInSecondaryIS();
    }

    @AfterClass(alwaysRun = true)
    public void endTest() throws Exception {

        try {
            deleteApplication(PORT_OFFSET_0, primaryISAppId);
            deleteIdp(PORT_OFFSET_0, primaryISIdpId);
            deleteApplication(PORT_OFFSET_1, secondaryISAppId);
            deleteUserInSecondaryIS();
        } finally {
            scim2RestClient.closeHttpClient();
        }
    }

    @Test(groups = "wso2.is", description = "Verify the IdP was created in primary IS")
    public void testIdpCreatedInPrimaryIS() {

        Assert.assertNotNull(primaryISIdpId,
                "Failed to create federated IdP '" + IDENTITY_PROVIDER_NAME + "' in primary IS");
    }

    @Test(groups = "wso2.is",
            dependsOnMethods = "testIdpCreatedInPrimaryIS",
            description = "Verify the SP was created and configured in primary IS")
    public void testApplicationCreatedInPrimaryIS() throws Exception {

        ApplicationResponseModel app = getApplication(PORT_OFFSET_0, primaryISAppId);
        Assert.assertNotNull(app,
                "Failed to retrieve application '" + PRIMARY_IS_SP_NAME + "' from primary IS");
        Assert.assertEquals(app.getName(), PRIMARY_IS_SP_NAME,
                "Application name mismatch in primary IS");
    }

    @Test(groups = "wso2.is",
            dependsOnMethods = "testApplicationCreatedInPrimaryIS",
            description = "Verify the SP was created and configured in secondary IS")
    public void testApplicationCreatedInSecondaryIS() throws Exception {

        ApplicationResponseModel app = getApplication(PORT_OFFSET_1, secondaryISAppId);
        Assert.assertNotNull(app,
                "Failed to retrieve application '" + SECONDARY_IS_SP_NAME + "' from secondary IS");
        Assert.assertEquals(app.getName(), SECONDARY_IS_SP_NAME,
                "Application name mismatch in secondary IS");
    }

    @Test(groups = "wso2.is",
            dependsOnMethods = {"testApplicationCreatedInPrimaryIS", "testApplicationCreatedInSecondaryIS"},
            description = "Verify SAML federation login flow works before any key rotation")
    public void testSAMLFederationBeforeKeyRotation() throws Exception {

        try (CloseableHttpClient client = buildHttpClient()) {
            String sessionDataKey = sendSAMLRequestToPrimaryIS(client);
            Assert.assertNotNull(sessionDataKey,
                    "Could not obtain 'sessionDataKey' from secondary IS");

            String redirectUrl = authenticateWithSecondaryIS(client, sessionDataKey);
            Assert.assertNotNull(redirectUrl,
                    "No redirect URL returned after authentication at secondary IS");

            Map<String, String> samlParams = getSAMLResponseFromSecondaryIS(client, redirectUrl);
            Assert.assertNotNull(samlParams.get("SAMLResponse"),
                    "Secondary IS did not return a SAMLResponse");
            Assert.assertNotNull(samlParams.get("RelayState"),
                    "Secondary IS did not return a RelayState");

            redirectUrl = sendSAMLResponseToPrimaryIS(client, samlParams);
            Assert.assertNotNull(redirectUrl,
                    "Primary IS did not redirect after receiving SAMLResponse");

            String samlResponse = getSAMLResponseFromPrimaryIS(client, redirectUrl);
            Assert.assertNotNull(samlResponse,
                    "Could not obtain final SAMLResponse from primary IS");

            assertValidSAMLResponse(samlResponse);
        }
    }

    /**
     * Verifies that SAML federation works correctly when the secondary IS signs its SAML response.
     */
    @Test(groups = "wso2.is",
            dependsOnMethods = "testSAMLFederationBeforeKeyRotation",
            description = "Verify federation with signed responses: secondary IS signs the response "
                    + "and primary IS validates it using the configured certificate")
    public void testSAMLFederationWithResponseSigningValidation() throws Exception {

        // Fetch the active signing certificate from the secondary IS SAML metadata.
        String secondaryISCert = fetchSecondaryISSigningCertificate();

        // Update the existing primary IS IdP with cert and enable signed response validation.
        updateIdpForSignedResponseValidation(secondaryISCert);

        try (CloseableHttpClient client = buildHttpClient()) {
            String sessionDataKey = sendSAMLRequestToPrimaryIS(client);
            Assert.assertNotNull(sessionDataKey,
                    "Could not obtain 'sessionDataKey' from primary IS");

            String redirectUrl = authenticateWithSecondaryIS(client, sessionDataKey);
            Assert.assertNotNull(redirectUrl,
                    "No redirect URL returned after authentication at secondary IS");

            Map<String, String> samlParams = getSAMLResponseFromSecondaryIS(client, redirectUrl);
            Assert.assertNotNull(samlParams.get("SAMLResponse"),
                    "Secondary IS did not return a SAMLResponse");
            Assert.assertNotNull(samlParams.get("RelayState"),
                    "Secondary IS did not return a RelayState");

            redirectUrl = sendSAMLResponseToPrimaryIS(client, samlParams);
            Assert.assertNotNull(redirectUrl,
                    "Primary IS did not redirect after receiving the signed SAMLResponse");

            String samlResponse = getSAMLResponseFromPrimaryIS(client, redirectUrl);
            Assert.assertNotNull(samlResponse,
                    "Could not obtain final SAMLResponse from primary IS");

            assertValidSAMLResponse(samlResponse);
        }
    }

    /**
     * Registers the federated IdP in the primary IS with the SAMLSSOAuthenticator
     * and the necessary properties to point to the secondary IS for SAML authentication.
     * 
     * @throws Exception If an error occurs while creating the federated IdP in the primary IS.
     */
    private void createIdpInPrimaryIS() throws Exception {

        String secondaryISSSOUrl = String.format("https://localhost:%s/samlsso",
                DEFAULT_PORT + PORT_OFFSET_1);

        FederatedAuthenticator authenticator = new FederatedAuthenticator()
                .authenticatorId(ENCODED_IDP_AUTHENTICATOR_ID)
                .name(IDP_AUTHENTICATOR_NAME)
                .isEnabled(true)
                .addProperty(new Property()
                        .key(IdentityConstants.Authenticator.SAML2SSO.IDP_ENTITY_ID)
                        .value("localhost"))
                .addProperty(new Property()
                        .key(IdentityConstants.Authenticator.SAML2SSO.SP_ENTITY_ID)
                        .value(SECONDARY_IS_SAML_ISSUER))
                .addProperty(new Property()
                        .key(IdentityConstants.Authenticator.SAML2SSO.SSO_URL)
                        .value(secondaryISSSOUrl))
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
                        .key("AttributeConsumingServiceIndex"));

        FederatedAuthenticatorRequest fedAuthnConfig = new FederatedAuthenticatorRequest()
                .defaultAuthenticatorId(ENCODED_IDP_AUTHENTICATOR_ID)
                .addAuthenticator(authenticator);

        IdentityProviderPOSTRequest idpPostRequest = new IdentityProviderPOSTRequest()
                .name(IDENTITY_PROVIDER_NAME)
                .federatedAuthenticators(fedAuthnConfig);

        primaryISIdpId = addIdentityProvider(PORT_OFFSET_0, idpPostRequest);
    }

    /**
     * Registers the "travelocity-key-rotation" SP in the primary IS with SAML SSO configuration and an authentication
     * sequence that uses the federated IdP created earlier.
     * 
     * @throws Exception If an error occurs while creating the application in the primary IS.
     */
    private void createApplicationInPrimaryIS() throws Exception {

        ApplicationModel appModel = new ApplicationModel()
                .name(PRIMARY_IS_SP_NAME)
                .description("Travelocity SP for key-rotation federation test")
                .isManagementApp(true)
                .inboundProtocolConfiguration(new InboundProtocols()
                        .saml(buildSAMLConfig(PRIMARY_IS_SAML_ISSUER, PRIMARY_IS_SAML_ACS_URL)))
                .authenticationSequence(new AuthenticationSequence()
                        .type(TypeEnum.USER_DEFINED)
                        .addStepsItem(new AuthenticationStep()
                                .id(1)
                                .addOptionsItem(new Authenticator()
                                        .idp(IDENTITY_PROVIDER_NAME)
                                        .authenticator(IDP_AUTHENTICATOR_NAME))));

        primaryISAppId = addApplication(PORT_OFFSET_0, appModel);
    }

    /**
     * Registers the "primaryIS-key-rotation" SP in the secondary IS with SAML SSO configuration pointing to the primary IS
     * common auth URL as the ACS URL.
     * 
     * @throws Exception If an error occurs while creating the application in the secondary IS.
     */
    private void createApplicationInSecondaryIS() throws Exception {

        String primaryISCommonAuthUrl = String.format(COMMON_AUTH_URL, DEFAULT_PORT + PORT_OFFSET_0);

        ApplicationModel appModel = new ApplicationModel()
                .name(SECONDARY_IS_SP_NAME)
                .description("Primary IS proxy SP registered in secondary IS for key-rotation test")
                .isManagementApp(true)
                .inboundProtocolConfiguration(new InboundProtocols()
                        .saml(buildSAMLConfig(SECONDARY_IS_SAML_ISSUER, primaryISCommonAuthUrl)));

        secondaryISAppId = addApplication(PORT_OFFSET_1, appModel);
    }

    /**
     * Sends a SAML authentication request to the primary IS SAML SSO URL and
     * returns the "sessionDataKey" value from the response.
     * This will redirect to the secondary IS for authentication.
     * 
     * @param client An HttpClient with cookie handling configured to maintain session state.
     * @return The sessionDataKey value from the secondary IS response, or null if not found.
     * @throws Exception If an error occurs while sending the request or processing the response.
     */
    private String sendSAMLRequestToPrimaryIS(HttpClient client) throws Exception {

        HttpGet request = new HttpGet(SAML_SSO_URL);
        HttpResponse response = client.execute(request);
        return extractValueFromResponse(response, "name=\"sessionDataKey\"", 1);
    }

    /**
     * Posts the username, password and sessionDataKey to the secondary IS authentication endpoint to perform
     * login and follows the redirect.
     * 
     * @param client         An HttpClient with cookie handling configured to maintain session state.
     * @param sessionDataKey The sessionDataKey value obtained from the secondary IS response.
     * @return The redirect URL after successful authentication.
     * @throws Exception If an error occurs while sending the request or processing the response.
     */
    private String authenticateWithSecondaryIS(HttpClient client, String sessionDataKey)
            throws Exception {

        HttpPost request = new HttpPost(
                String.format(COMMON_AUTH_URL, DEFAULT_PORT + PORT_OFFSET_1));

        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("username", USER_NAME));
        params.add(new BasicNameValuePair("password", PASSWORD));
        params.add(new BasicNameValuePair("sessionDataKey", sessionDataKey));
        request.setEntity(new UrlEncodedFormEntity(params));

        HttpResponse response = client.execute(request);
        String location = getHeaderValue(response, "Location");
        closeHttpConnection(response);
        return location;
    }

    /**
     * Follows the redirect from the secondary IS after authentication to obtain the SAMLResponse and RelayState
     * parameters from the response.
     * 
     * @param client      An HttpClient with cookie handling configured to maintain session state.
     * @param redirectUrl The redirect URL obtained after authenticating with the secondary IS.
     * @return A map containing the SAMLResponse and RelayState parameters.
     * @throws Exception If an error occurs while sending the request or processing the response.
     */
    private Map<String, String> getSAMLResponseFromSecondaryIS(HttpClient client,
            String redirectUrl) throws Exception {

        HttpGet request = new HttpGet(redirectUrl);
        HttpResponse response = client.execute(request);

        Map<String, Integer> searchParams = new HashMap<>();
        searchParams.put("SAMLResponse", 5);
        searchParams.put("RelayState", 5);
        return extractValuesFromResponse(response, searchParams);
    }

    /**
     * Posts the SAMLResponse and RelayState obtained from the secondary IS back to the primary IS and follows
     * the redirect.
     * 
     * @param client     An HttpClient with cookie handling configured to maintain session state.
     * @param samlParams A map containing the SAMLResponse and RelayState parameters obtained from the secondary IS.
     * @return The redirect URL after sending the SAMLResponse to the primary IS.
     * @throws Exception If an error occurs while sending the request or processing the response.
     */
    private String sendSAMLResponseToPrimaryIS(HttpClient client, Map<String, String> samlParams) throws Exception {

        HttpPost request = new HttpPost(
                String.format(COMMON_AUTH_URL, DEFAULT_PORT + PORT_OFFSET_0));

        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("SAMLResponse", samlParams.get("SAMLResponse")));
        params.add(new BasicNameValuePair("RelayState", samlParams.get("RelayState")));
        request.setEntity(new UrlEncodedFormEntity(params));

        HttpResponse response = client.execute(request);
        String location = getHeaderValue(response, "Location");
        closeHttpConnection(response);
        return location;
    }

    /**
     * Follows the redirect from the primary IS after sending the SAMLResponse to obtain the final SAMLResponse that
     * can be sent to the web app.
     * 
     * @param client      An HttpClient with cookie handling configured to maintain session state.
     * @param redirectUrl The redirect URL obtained after sending the SAMLResponse to the primary IS.
     * @return The final SAMLResponse that can be sent to the web app.
     * @throws Exception If an error occurs while sending the request or processing the response.
     */
    private String getSAMLResponseFromPrimaryIS(HttpClient client, String redirectUrl) throws Exception {

        HttpGet request = new HttpGet(redirectUrl);
        HttpResponse response = client.execute(request);
        return extractValueFromResponse(response, "SAMLResponse", 5);
    }

    /**
     * Fetches the active signing certificate from the secondary IS SAML metadata endpoint.
     * 
     * @return Base64-encoded signing certificate from the secondary IS SAML metadata.
     * @throws Exception If the metadata cannot be fetched or the certificate cannot be extracted.
     */
    private String fetchSecondaryISSigningCertificate() throws Exception {

        String metadataUrl = String.format(
                "https://localhost:%s/samlsso/metadata", DEFAULT_PORT + PORT_OFFSET_1);

        try (CloseableHttpClient client = buildHttpClient()) {
            HttpGet request = new HttpGet(metadataUrl);
            HttpResponse response = client.execute(request);
            String responseBody = EntityUtils.toString(response.getEntity());

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            DocumentBuilder docBuilder = factory.newDocumentBuilder();
            Document document = docBuilder.parse(
                    new ByteArrayInputStream(responseBody.getBytes(StandardCharsets.UTF_8)));

            XPath xpath = XPathFactory.newInstance().newXPath();
            // Extract the base64 cert from the signing KeyDescriptor in the SAML metadata.
            String cert = (String) xpath.evaluate(
                    "//*[local-name()='KeyDescriptor'][@use='signing']"
                            + "//*[local-name()='X509Certificate']/text()",
                    document, XPathConstants.STRING);

            Assert.assertNotNull(cert,
                    "Failed to extract signing certificate from secondary IS SAML metadata");
            Assert.assertFalse(cert.trim().isEmpty(),
                    "Signing certificate from secondary IS SAML metadata is blank");
            // Collapse any whitespace introduced by metadata XML formatting.
            return cert.trim().replaceAll("\\s+", "");
        }
    }

    /**
     * Update idp to enable signed response validation in primary IS.
     * 
     * @param secondaryISCert The certificate of secondary IS.
     * @throws Exception If an error occurs while updating the IdP in the primary IS.
     */
    private void updateIdpForSignedResponseValidation(String secondaryISCert) throws Exception {

        String secondaryISSSOUrl = String.format(
                "https://localhost:%s/samlsso", DEFAULT_PORT + PORT_OFFSET_1);

        FederatedAuthenticator authenticator = new FederatedAuthenticator()
                .authenticatorId(ENCODED_IDP_AUTHENTICATOR_ID)
                .name(IDP_AUTHENTICATOR_NAME)
                .isEnabled(true)
                .addProperty(new Property()
                        .key(IdentityConstants.Authenticator.SAML2SSO.IDP_ENTITY_ID)
                        .value("localhost"))
                .addProperty(new Property()
                        .key(IdentityConstants.Authenticator.SAML2SSO.SP_ENTITY_ID)
                        .value(SECONDARY_IS_SAML_ISSUER))
                .addProperty(new Property()
                        .key(IdentityConstants.Authenticator.SAML2SSO.SSO_URL)
                        .value(secondaryISSSOUrl))
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
                        .value("true"))
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
                        .key("AttributeConsumingServiceIndex"));

        updateFederatedAuthenticator(PORT_OFFSET_0, primaryISIdpId, ENCODED_IDP_AUTHENTICATOR_ID, authenticator);

        patchIdentityProvider(PORT_OFFSET_0, primaryISIdpId,
                Collections.singletonList(new PatchRequest()
                        .operation(PatchRequest.OperationEnum.ADD)
                        .path("/certificate/certificates/0")
                        .value(secondaryISCert)));
    }

    /**
     * Creates a test user in the secondary IS using the SCIM2 REST API.
     */
    private void createUserInSecondaryIS() {

        try {
            UserObject user = new UserObject()
                    .userName(USER_NAME)
                    .password(PASSWORD)
                    .name(new Name().givenName(USER_NAME))
                    .addEmail(new Email().value(EMAIL));
            userId = scim2RestClient.createUser(user);
        } catch (Exception e) {
            Assert.fail("Failed to create user '" + USER_NAME + "' in secondary IS: " + e.getMessage(), e);
        }
    }

    /**
     * Deletes the test user from the secondary IS using the SCIM2 REST API.
     */
    private void deleteUserInSecondaryIS() {

        try {
            scim2RestClient.deleteUser(userId);
        } catch (Exception e) {
            Assert.fail("Failed to delete user '" + USER_NAME + "' from secondary IS: " + e.getMessage(), e);
        }
    }

    /**
     * Builds a SAML2Configuration object for the given issuer and ACS URL, with common settings for the test applications.
     * 
     * @param issuer The SAML issuer to use in the configuration.
     * @param acsUrl The Assertion Consumer Service URL to use in the configuration.
     * @return A SAML2Configuration object with the specified issuer and ACS URL, and common settings for the test applications.
     */
    private SAML2Configuration buildSAMLConfig(String issuer, String acsUrl) {

        SAML2ServiceProvider sp = new SAML2ServiceProvider()
                .issuer(issuer)
                .addAssertionConsumerUrl(acsUrl)
                .defaultAssertionConsumerUrl(acsUrl)
                .attributeProfile(new SAMLAttributeProfile()
                        .enabled(true)
                        .alwaysIncludeAttributesInResponse(true))
                .singleLogoutProfile(new SingleLogoutProfile()
                        .enabled(true))
                .responseSigning(new SAMLResponseSigning()
                        .enabled(true))
                .singleSignOnProfile(new SingleSignOnProfile()
                        .assertion(new SAMLAssertionConfiguration()
                                .nameIdFormat(SAML_NAME_ID_FORMAT)));

        return new SAML2Configuration().manualConfiguration(sp);
    }

    /**
     * Helper method to construct the base URI for the secondary IS instance, which is used in various places in the test.
     * 
     * @return The base URI for the secondary IS instance, including the correct port offset.
     */
    private String getSecondaryISURI() {

        return String.format("https://localhost:%s/", DEFAULT_PORT + PORT_OFFSET_1);
    }

    /**
     * Builds an HttpClient with cookie handling configured to maintain session state.
     * 
     * @return A CloseableHttpClient instance with cookie handling enabled.
     */
    private CloseableHttpClient buildHttpClient() {

        Lookup<CookieSpecProvider> cookieSpecRegistry = RegistryBuilder.<CookieSpecProvider>create()
                .register(CookieSpecs.DEFAULT, new RFC6265CookieSpecProvider())
                .build();
        RequestConfig requestConfig = RequestConfig.custom()
                .setCookieSpec(CookieSpecs.DEFAULT)
                .build();
        return HttpClientBuilder.create()
                .setDefaultCookieSpecRegistry(cookieSpecRegistry)
                .setDefaultRequestConfig(requestConfig)
                .setDefaultCookieStore(new BasicCookieStore())
                .build();
    }

    /**
     * Asserts that the given SAML response is valid by checking that the top-level StatusCode is Success and that
     * a non-blank Subject/NameID is present.
     * 
     * @param encodedSamlResponse The Base64-encoded SAML response to validate.
     * @throws Exception If an error occurs while parsing or validating the SAML response.
     */
    private void assertValidSAMLResponse(String encodedSamlResponse) throws Exception {

        byte[] decodedBytes = Base64.getDecoder().decode(encodedSamlResponse);

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(new ByteArrayInputStream(decodedBytes));

        XPath xpath = XPathFactory.newInstance().newXPath();

        // Assert the top-level StatusCode is Success.
        String statusCode = (String) xpath.evaluate(
                "//*[local-name()='StatusCode']/@Value",
                document, XPathConstants.STRING);
        Assert.assertEquals(statusCode, "urn:oasis:names:tc:SAML:2.0:status:Success",
                "SAML response StatusCode is not Success");

        // Assert that Subject/NameID is present and non-blank.
        String nameId = (String) xpath.evaluate(
                "//*[local-name()='Subject']/*[local-name()='NameID']/text()",
                document, XPathConstants.STRING);
        Assert.assertNotNull(nameId, "Subject NameID is missing from the SAML response");
        Assert.assertFalse(nameId.trim().isEmpty(),
                "Subject NameID is blank in the SAML response");
    }
}
