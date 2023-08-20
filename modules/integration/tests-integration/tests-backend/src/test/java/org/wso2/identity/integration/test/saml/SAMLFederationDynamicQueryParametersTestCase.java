/*
 * Copyright (c) 2018, WSO2 LLC. (http://www.wso2.com).
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

import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
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
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.InboundProtocols;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.SAML2Configuration;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.SAML2ServiceProvider;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.SAMLAssertionConfiguration;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.SAMLResponseSigning;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.SingleLogoutProfile;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.SingleSignOnProfile;
import org.wso2.identity.integration.test.rest.api.server.idp.v1.model.FederatedAuthenticatorRequest;
import org.wso2.identity.integration.test.rest.api.server.idp.v1.model.FederatedAuthenticatorRequest.FederatedAuthenticator;
import org.wso2.identity.integration.test.rest.api.server.idp.v1.model.IdentityProviderPOSTRequest;
import org.wso2.identity.integration.test.rest.api.server.idp.v1.model.Property;
import org.wso2.identity.integration.test.restclients.IdpMgtRestClient;
import org.wso2.identity.integration.test.restclients.OAuth2RestClient;
import org.wso2.identity.integration.test.util.Utils;
import org.wso2.identity.integration.test.utils.IdentityConstants;

import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Integration test for Dynamic Query parameter support for SAML Federated Authenticator.
 */
public class SAMLFederationDynamicQueryParametersTestCase extends AbstractIdentityFederationTestCase {

    private static final String IDENTITY_PROVIDER_NAME = "testIdp";
    public static final String SERVICE_PROVIDER = "SERVICE_PROVIDER";
    public static final String INBOUND_AUTH_KEY = "travelocity.com";
    public static final String INBOUND_AUTH_TYPE = "samlsso";
    private static final String IDP_AUTHENTICATOR_NAME_SAML = "SAMLSSOAuthenticator";
    private static final String ENCODED_IDP_AUTHENTICATOR_NAME_SAML = "U0FNTFNTT0F1dGhlbnRpY2F0b3I";
    private static final String NAME_ID_FORMAT = "urn:oasis:names:tc:SAML:1.1:nameid-format:emailAddress";

    private static final String INBOUND_QUERY_PARAM = "inbound_request_param_key";
    private static final String INBOUND_QUERY_PARAM_VALUE = "inbound_request_param_value";

    private static final String DYNAMIC_QUERY_PARAM_KEY = "dynamic_query";
    private static final String DYNAMIC_QUERY = "dynamic_query={inbound_request_param_key}";
    private static final String TRAVELOCITY_SAMPLE_APP_URL = "http://localhost:8490/travelocity.com";
    private IdpMgtRestClient idpMgtRestClient;
    private OAuth2RestClient applicationMgtRestClient;
    private String idpId;
    private String appId;

    @BeforeClass(alwaysRun = true)
    public void initTest() throws Exception {

        super.initTest();

        idpMgtRestClient = new IdpMgtRestClient(serverURL, tenantInfo);
        applicationMgtRestClient = new OAuth2RestClient(serverURL, tenantInfo);
    }

    @AfterClass(alwaysRun = true)
    public void endTest() throws Exception {

        applicationMgtRestClient.deleteApplication(appId);
        idpMgtRestClient.deleteIdp(idpId);

        applicationMgtRestClient.closeHttpClient();
        idpMgtRestClient.closeHttpClient();
    }

    @Test(groups = "wso2.is", description = "Test federated IDP creation with SAML Federated Authenticator")
    public void testIdpWithDynamicQueryParams() throws Exception {

        FederatedAuthenticator authenticator = new FederatedAuthenticator()
                .authenticatorId(ENCODED_IDP_AUTHENTICATOR_NAME_SAML)
                .name(IDP_AUTHENTICATOR_NAME_SAML)
                .isEnabled(true)
                .addProperty(new Property()
                        .key(IdentityConstants.Authenticator.SAML2SSO.IDP_ENTITY_ID)
                        .value("samlFedIdP"))
                .addProperty(new Property()
                        .key(IdentityConstants.Authenticator.SAML2SSO.SP_ENTITY_ID)
                        .value("samlFedSP"))
                .addProperty(new Property()
                        .key(IdentityConstants.Authenticator.SAML2SSO.SSO_URL)
                        .value("https://localhost:9453/samlsso"))
                .addProperty(new Property()
                        .key("commonAuthQueryParams")
                        .value(DYNAMIC_QUERY));

        FederatedAuthenticatorRequest oidcAuthnConfig = new FederatedAuthenticatorRequest()
                .defaultAuthenticatorId(ENCODED_IDP_AUTHENTICATOR_NAME_SAML)
                .addAuthenticator(authenticator);

        IdentityProviderPOSTRequest idpPostRequest = new IdentityProviderPOSTRequest()
                .name(IDENTITY_PROVIDER_NAME)
                .federatedAuthenticators(oidcAuthnConfig);

        idpId = idpMgtRestClient.createIdentityProvider(idpPostRequest);
        Assert.assertNotNull(idpId, "Failed to create Identity Provider 'testIdP'");
    }

    @Test(groups = "wso2.is", description = "Test Service Provider creation with SAML Federated IDP Authentication",
            dependsOnMethods = {"testIdpWithDynamicQueryParams"})
    public void testCreateServiceProviderWithSAMLConfigsAndSAMLFedIdp() throws Exception {

        ApplicationModel applicationCreationModel = new ApplicationModel()
                .name(SERVICE_PROVIDER)
                .inboundProtocolConfiguration(new InboundProtocols().saml(getSAMLConfigurations()))
                .authenticationSequence(new AuthenticationSequence()
                        .type(TypeEnum.USER_DEFINED)
                        .addStepsItem(new org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.AuthenticationStep()
                                .id(1)
                                .addOptionsItem(new Authenticator()
                                        .idp(IDENTITY_PROVIDER_NAME)
                                        .authenticator(IDP_AUTHENTICATOR_NAME_SAML))));

        appId = applicationMgtRestClient.createApplication(applicationCreationModel);
        ApplicationResponseModel application = applicationMgtRestClient.getApplication(appId);
        Assert.assertNotNull(application, "Failed to create service provider 'SERVICE_PROVIDER'");

        SAML2ServiceProvider saml2AppConfig = applicationMgtRestClient.getSAMLInboundDetails(appId);
        Assert.assertNotNull(saml2AppConfig, "Failed to update service provider with SAML inbound configs.");

        Assert.assertEquals(TypeEnum.USER_DEFINED, application.getAuthenticationSequence().getType(),
                "Failed to update local and outbound configs");
    }

    @Test(alwaysRun = true, description = "Test SAML Federation Request with Dynamic Query Parameters",
            dependsOnMethods = {"testCreateServiceProviderWithSAMLConfigsAndSAMLFedIdp"})
    public void testSAMLRedirectBindingDynamicWithInboundQueryParam() throws Exception {

        HttpGet request = new HttpGet(TRAVELOCITY_SAMPLE_APP_URL + "/samlsso?SAML2.HTTPBinding=HTTP-Redirect");
        try (CloseableHttpClient client = HttpClientBuilder.create().disableRedirectHandling().build()) {
            // Do a redirect to travelocity app.
            HttpResponse response = client.execute(request);
            EntityUtils.consume(response.getEntity());

            // Modify the location header to included the secToken.
            String location = Utils.getRedirectUrl(response) + "&" + INBOUND_QUERY_PARAM + "=" + INBOUND_QUERY_PARAM_VALUE;

            // Do a GET manually to send the SAML Request to IS.
            HttpGet requestToIS = new HttpGet(location);
            HttpResponse requestToFederatedIdp = client.execute(requestToIS);
            EntityUtils.consume(requestToFederatedIdp.getEntity());

            // 302 to SAML Federated IDP initiated from the primary IS
            String requestToFedIdpLocationHeader = Utils.getRedirectUrl(requestToFederatedIdp);
            // Assert whether the query param value sent in the inbound request was passed in the 302 to Federated IDP
            List<NameValuePair> nameValuePairs = buildQueryParamList(requestToFedIdpLocationHeader);
            boolean isDynamicQueryParamReplaced = false;
            for (NameValuePair valuePair : nameValuePairs) {
                if (StringUtils.equalsIgnoreCase(DYNAMIC_QUERY_PARAM_KEY, valuePair.getName())) {
                    // Check whether the query param value sent in inbound request was included to the additional query
                    // params defined in the SAML Application Authenticator.
                    isDynamicQueryParamReplaced = StringUtils.equals(valuePair.getValue(), INBOUND_QUERY_PARAM_VALUE);
                }
            }
            Assert.assertTrue(isDynamicQueryParamReplaced);
        }
    }

    @Test(alwaysRun = true, description = "Test SAML Federation Request with Dynamic Query Parameters",
            dependsOnMethods = {"testCreateServiceProviderWithSAMLConfigsAndSAMLFedIdp"})
    public void testSAMLRedirectBindingDynamicWithoutInboundQueryParam() throws Exception {

        HttpGet request = new HttpGet(TRAVELOCITY_SAMPLE_APP_URL + "/samlsso?SAML2.HTTPBinding=HTTP-Redirect");
        try (CloseableHttpClient client = HttpClientBuilder.create().disableRedirectHandling().build()) {
            // Do a redirect to travelocity app.
            HttpResponse response = client.execute(request);
            EntityUtils.consume(response.getEntity());

            // Modify the location header to included the secToken.
            String location = Utils.getRedirectUrl(response);

            // Do a GET manually to send the SAML Request to IS.
            HttpGet requestToIS = new HttpGet(location);
            HttpResponse requestToFederatedIdp = client.execute(requestToIS);
            EntityUtils.consume(requestToFederatedIdp.getEntity());

            // 302 to SAML Federated IDP initiated from the primary IS
            String requestToFedIdpLocationHeader = Utils.getRedirectUrl(requestToFederatedIdp);
            // Assert whether the query param value sent in the inbound request was passed in the 302 to Federated IDP
            List<NameValuePair> nameValuePairs = buildQueryParamList(requestToFedIdpLocationHeader);
            boolean isDynamicQuerySentInFedAuthRequestEmpty = false;
            for (NameValuePair valuePair : nameValuePairs) {
                if (StringUtils.equalsIgnoreCase(DYNAMIC_QUERY_PARAM_KEY, valuePair.getName())) {
                    // Check whether the query param value sent in inbound request was included to the additional query
                    // params defined in the SAML Application Authenticator.
                    isDynamicQuerySentInFedAuthRequestEmpty = StringUtils.isEmpty(valuePair.getValue());
                }
            }

            Assert.assertTrue(isDynamicQuerySentInFedAuthRequestEmpty);
        }
    }

    private List<NameValuePair> buildQueryParamList(String requestToFedIdpLocationHeader) {

        return URLEncodedUtils.parse(requestToFedIdpLocationHeader, StandardCharsets.UTF_8);
    }

    private SAML2Configuration getSAMLConfigurations() {

        SAML2ServiceProvider serviceProvider = new SAML2ServiceProvider()
                .issuer(INBOUND_AUTH_KEY)
                .addAssertionConsumerUrl(TRAVELOCITY_SAMPLE_APP_URL + "/home.jsp")
                .defaultAssertionConsumerUrl((TRAVELOCITY_SAMPLE_APP_URL + "/home.jsp"))
                .singleLogoutProfile(new SingleLogoutProfile()
                        .enabled(true))
                .responseSigning(new SAMLResponseSigning()
                        .enabled(true))
                .singleSignOnProfile(new SingleSignOnProfile()
                        .assertion(new SAMLAssertionConfiguration().nameIdFormat(NAME_ID_FORMAT))
                        .attributeConsumingServiceIndex("1239245949"));

        return new SAML2Configuration().manualConfiguration(serviceProvider);
    }
}
