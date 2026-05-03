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
package org.wso2.identity.integration.test.oauth2;

import com.nimbusds.jwt.SignedJWT;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.AutomationContext;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.identity.application.common.model.xsd.InboundAuthenticationRequestConfig;
import org.wso2.carbon.identity.application.common.model.xsd.ServiceProvider;
import org.wso2.carbon.identity.oauth.stub.dto.OAuthConsumerAppDTO;
import org.wso2.carbon.user.mgt.stub.types.carbon.ClaimValue;
import org.wso2.identity.integration.common.clients.oauth.OauthAdminClient;
import org.wso2.identity.integration.test.application.mgt.AbstractIdentityFederationTestCase;
import org.wso2.identity.integration.test.rest.api.common.RESTTestBase;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.AccessTokenConfiguration;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.ApplicationModel;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.InboundProtocols;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.OpenIDConnectConfiguration;
import org.wso2.identity.integration.test.rest.api.server.idp.v1.model.IdentityProviderPOSTRequest;
import org.wso2.identity.integration.test.restclients.IdpMgtRestClient;
import org.wso2.identity.integration.test.restclients.OAuth2RestClient;
import org.wso2.identity.integration.test.restclients.OrgMgtRestClient;
import org.wso2.identity.integration.test.oidc.bean.OIDCApplication;
import org.wso2.identity.integration.test.utils.IdentityConstants;
import org.wso2.identity.integration.test.utils.OAuth2Constant;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.wso2.identity.integration.test.utils.OAuth2Constant.ACCESS_TOKEN_ENDPOINT;

/**
 * Integration test class for OAuth2 Token Exchange grant type in sub organizations.
 */
public class OAuth2TokenExchangeSubOrganizationTestCase extends AbstractIdentityFederationTestCase {

    private static final String SUB_ORG_NAME = "TokenExchangeTestOrg";
    private static final String SUB_ORG_APP_NAME = "subOrgApp";
    private static final String TRUSTED_TOKEN_ISSUER_NAME = "trustedTokenIssuer";
    private static final String SECONDARY_IS_SP_NAME = "secondarySP";
    private static final String SECONDARY_IS_TOKEN_ENDPOINT = "https://localhost:9854/oauth2/token";
    private static final String SECONDARY_IS_JWKS_URI = "https://localhost:9854/oauth2/jwks";
    private static final String NEW_USER_USERNAME = "secondaryUser01";
    private static final String NEW_USER_PASSWORD = "Wso2@123";
    private static final String NEW_USER_EMAIL = "secondaryUser01@gmail.com";
    public static final String EMAIL_CLAIM_URI = "http://wso2.org/claims/emailaddress";

    private static final int PORT_OFFSET_0 = 0;
    private static final int PORT_OFFSET_1 = 1;
    private static final String DEFAULT_PROFILE = "default";

    private OrgMgtRestClient orgMgtRestClient;
    private OAuth2RestClient oAuth2RestClient;
    private IdpMgtRestClient idpMgtRestClient;
    private CloseableHttpClient httpClient;
    protected OauthAdminClient adminClient;
    private final AutomationContext context;

    private String subOrgId;
    private String subOrgAppId;
    private String subOrgAppClientId;
    private String subOrgAppClientSecret;
    private String subOrgIdpId;
    private String secondaryISClientID;
    private String secondaryISClientSecret;
    private String accessTokenFromSecondaryIS;
    private String switchedM2MToken;

    @DataProvider(name = "configProvider")
    public static Object[][] configProvider() {

        return new Object[][]{{TestUserMode.SUPER_TENANT_ADMIN}};
    }

    @Factory(dataProvider = "configProvider")
    public OAuth2TokenExchangeSubOrganizationTestCase(TestUserMode userMode) throws Exception {

        context = new AutomationContext("IDENTITY", userMode);
    }

    @BeforeClass(alwaysRun = true)
    public void initTest() throws Exception {

        super.initTest();
        
        // Initialize service clients for both primary and secondary IS
        super.createServiceClients(PORT_OFFSET_0, null,
                new IdentityConstants.ServiceClientType[]{
                        IdentityConstants.ServiceClientType.APPLICATION_MANAGEMENT,
                        IdentityConstants.ServiceClientType.IDENTITY_PROVIDER_MGT,
                        IdentityConstants.ServiceClientType.USER_MGT,
                        IdentityConstants.ServiceClientType.OAUTH_ADMIN});

        super.createServiceClients(PORT_OFFSET_1, null,
                new IdentityConstants.ServiceClientType[]{
                        IdentityConstants.ServiceClientType.APPLICATION_MANAGEMENT,
                        IdentityConstants.ServiceClientType.USER_MGT,
                        IdentityConstants.ServiceClientType.OAUTH_ADMIN});
        
        // Initialize REST clients for primary IS
        oAuth2RestClient = new OAuth2RestClient(serverURL, tenantInfo);
        idpMgtRestClient = new IdpMgtRestClient(serverURL, tenantInfo);
        
        // Read authorized APIs for organization management
        org.json.JSONObject authorizedAPIs = new org.json.JSONObject();
        try {
            String apisJson = RESTTestBase.readResource("organization-service-apis.json", this.getClass());
            authorizedAPIs = new org.json.JSONObject(apisJson);
        } catch (Exception e) {
            throw new RuntimeException("Could not load authorized APIs JSON.");
        }
        
        orgMgtRestClient = new OrgMgtRestClient(context, tenantInfo, serverURL, authorizedAPIs);
        httpClient = HttpClientBuilder.create().build();

        // Create service provider in secondary IS and user
        createServiceProviderInSecondaryIS();
        createUserInSecondaryIS();
        
        // Create sub organization
        createSubOrganization();
        
        // Create application in sub organization
        createApplicationInSubOrganization();
        
        // Add trusted token issuer (IDP) in sub organization
        addTrustedTokenIssuerInSubOrganization();
        
        // Get access token from secondary IS
        getAccessTokenFromSecondaryIS();
    }

    @AfterClass(alwaysRun = true)
    public void endTest() throws Exception {

        try {
            // Delete user from secondary IS
            super.deleteUser(PORT_OFFSET_1, NEW_USER_USERNAME);
            
            // Delete IDP from sub organization
            if (subOrgIdpId != null) {
                idpMgtRestClient.deleteIdpInOrganization(subOrgIdpId, switchedM2MToken);
            }
            
            // Delete application from sub organization
            if (subOrgAppId != null) {
                oAuth2RestClient.deleteOrganizationApplication(subOrgAppId, switchedM2MToken);
            }
            
            // Delete service provider from secondary IS
            super.deleteServiceProvider(PORT_OFFSET_1, SECONDARY_IS_SP_NAME);
            
            // Clean up sub organization
            if (subOrgId != null) {
                orgMgtRestClient.deleteOrganization(subOrgId);
            }

            // Close HTTP clients
            httpClient.close();
            orgMgtRestClient.closeHttpClient();
            oAuth2RestClient.closeHttpClient();
            idpMgtRestClient.closeHttpClient();
        } catch (Exception e) {
            log.error("Failure occurred during cleanup: " + e.getMessage(), e);
            throw e;
        }
    }

    @Test(groups = "wso2.is", description = "Exchange access token in sub organization")
    public void testTokenExchangeInSubOrganization() throws Exception {

        List<NameValuePair> postParameters = new ArrayList<>();
        postParameters.add(new BasicNameValuePair("subject_token", accessTokenFromSecondaryIS));
        postParameters.add(new BasicNameValuePair("subject_token_type", "urn:ietf:params:oauth:token-type:jwt"));
        postParameters.add(new BasicNameValuePair("requested_token_type", "urn:ietf:params:oauth:token-type:jwt"));
        postParameters.add(new BasicNameValuePair("grant_type", "urn:ietf:params:oauth:grant-type:token-exchange"));

        // Construct sub organization token endpoint
        String subOrgTokenEndpoint = getRootTenantQualifiedOrgURL(ACCESS_TOKEN_ENDPOINT, tenantInfo.getDomain(),
                subOrgId);
        
        JSONObject responseObject = sendPOSTMessage(subOrgTokenEndpoint, subOrgAppClientId, subOrgAppClientSecret,
                postParameters);
        
        String exchangedToken = responseObject.get("access_token").toString();
        Assert.assertNotNull(exchangedToken, "Exchanged access token is null.");
        
        // Verify subject in exchanged token matches original token
        String originalSubject = getTokenSubject(accessTokenFromSecondaryIS);
        String exchangedSubject = getTokenSubject(exchangedToken);
        
        Assert.assertEquals(exchangedSubject, originalSubject, 
                "Subject of the exchanged token should be the same as the subject token");
    }

    /**
     * Create service provider in secondary IS with OAuth configuration.
     */
    private void createServiceProviderInSecondaryIS() throws Exception {

        super.addServiceProvider(PORT_OFFSET_1, SECONDARY_IS_SP_NAME);

        ServiceProvider serviceProvider = getServiceProvider(PORT_OFFSET_1, SECONDARY_IS_SP_NAME);
        if (serviceProvider == null) {
            throw new Exception("Failed to create service provider in secondary IS");
        }

        updateServiceProviderWithOIDCConfigs(PORT_OFFSET_1, SECONDARY_IS_SP_NAME, serviceProvider);

        serviceProvider = getServiceProvider(PORT_OFFSET_1, SECONDARY_IS_SP_NAME);

        InboundAuthenticationRequestConfig[] configs = serviceProvider.getInboundAuthenticationConfig().
                getInboundAuthenticationRequestConfigs();
        boolean success = false;
        if (configs != null) {
            for (InboundAuthenticationRequestConfig config : configs) {
                if (OAuth2Constant.OAUTH_2.equals(config.getInboundAuthType())) {
                    success = true;
                    break;
                }
            }
        }

        if (!success) {
            throw new Exception("Failed to update service provider with inbound OIDC configs in secondary IS");
        }
    }

    /**
     * Create user in secondary IS.
     */
    private void createUserInSecondaryIS() throws Exception {

        List<ClaimValue> claimValues = new ArrayList<>();
        ClaimValue claimValue = new ClaimValue();
        claimValue.setClaimURI(EMAIL_CLAIM_URI);
        claimValue.setValue(NEW_USER_EMAIL);
        claimValues.add(claimValue);
        
        super.addUser(PORT_OFFSET_1, NEW_USER_USERNAME, NEW_USER_PASSWORD, null, DEFAULT_PROFILE, 
                claimValues.toArray(new ClaimValue[0]));
        
        Set<String> users = super.getUserList(PORT_OFFSET_1);
        if (users == null || !users.contains(NEW_USER_USERNAME)) {
            throw new Exception("User creation failed in secondary IS");
        }
    }

    /**
     * Create a sub organization.
     */
    private void createSubOrganization() throws Exception {

        subOrgId = orgMgtRestClient.addOrganization(SUB_ORG_NAME);
        Assert.assertNotNull(subOrgId, "Sub organization creation failed");
        log.info("Created sub organization with ID: " + subOrgId);
        
        // Get switched M2M token for sub organization
        switchedM2MToken = orgMgtRestClient.switchM2MToken(subOrgId);
        Assert.assertNotNull(switchedM2MToken, "Failed to get switched M2M token for sub organization");
    }

    /**
     * Create an application in the sub organization with token exchange grant enabled.
     */
    private void createApplicationInSubOrganization() throws Exception {

        // Create application using REST API in sub organization context
        ApplicationModel application = new ApplicationModel();
        application.setName(SUB_ORG_APP_NAME);
        application.setDescription("Application for token exchange testing in sub organization");
        
        // Configure OIDC inbound
        OpenIDConnectConfiguration oidcConfig = new OpenIDConnectConfiguration();
        oidcConfig.setGrantTypes(List.of(
            "refresh_token",
            "urn:ietf:params:oauth:grant-type:token-exchange"
        ));
        AccessTokenConfiguration accessTokenConfig = new AccessTokenConfiguration();
        accessTokenConfig.setType("JWT");
        accessTokenConfig.applicationAccessTokenExpiryInSeconds(86400L);
        accessTokenConfig.userAccessTokenExpiryInSeconds(300L);
        oidcConfig.setAccessToken(accessTokenConfig);
        
        InboundProtocols inboundProtocols = new InboundProtocols();
        inboundProtocols.setOidc(oidcConfig);
        application.setInboundProtocolConfiguration(inboundProtocols);

        // Create application in sub org in primary IS
        subOrgAppId = oAuth2RestClient.createOrganizationApplication(application, switchedM2MToken);
        Assert.assertNotNull(subOrgAppId, "Failed to create application");
        log.info("Created application with ID: " + subOrgAppId);
        
        // Get OAuth client credentials
        OpenIDConnectConfiguration createdOidcConfig = oAuth2RestClient.
                getOIDCInboundDetailsOfOrganizationApp(subOrgAppId, switchedM2MToken);
        subOrgAppClientId = createdOidcConfig.getClientId();
        subOrgAppClientSecret = createdOidcConfig.getClientSecret();
        
        Assert.assertNotNull(subOrgAppClientId, "Sub org app client ID is null");
        Assert.assertNotNull(subOrgAppClientSecret, "Sub org app client secret is null");
    }

    /**
     * Update service provider with OIDC configurations.
     */
    private void updateServiceProviderWithOIDCConfigs(int portOffset, String applicationName,
                                                      ServiceProvider serviceProvider) throws Exception {

        OIDCApplication application = new OIDCApplication(applicationName, OAuth2Constant.CALLBACK_URL);
        OAuthConsumerAppDTO appDTO = getOAuthConsumerAppDTO(application);

        OAuthConsumerAppDTO[] appDtos = createOIDCConfiguration(portOffset, appDTO);

        for (OAuthConsumerAppDTO appDto : appDtos) {
            if (appDto.getApplicationName().equals(application.getApplicationName())) {
                application.setClientId(appDto.getOauthConsumerKey());
                application.setClientSecret(appDto.getOauthConsumerSecret());
            }
        }

        if (application.getClientId() != null) {
            InboundAuthenticationRequestConfig inboundAuthenticationRequestConfig = new
                    InboundAuthenticationRequestConfig();
            inboundAuthenticationRequestConfig.setInboundAuthKey(application.getClientId());
            
            if (portOffset == PORT_OFFSET_1) {
                secondaryISClientID = application.getClientId();
                secondaryISClientSecret = application.getClientSecret();
            }
            
            inboundAuthenticationRequestConfig.setInboundAuthType(OAuth2Constant.OAUTH_2);
            if (StringUtils.isNotBlank(application.getClientSecret())) {
                org.wso2.carbon.identity.application.common.model.xsd.Property property = 
                        new org.wso2.carbon.identity.application.common.model.xsd.Property();
                property.setName(OAuth2Constant.OAUTH_CONSUMER_SECRET);
                property.setValue(application.getClientSecret());
                org.wso2.carbon.identity.application.common.model.xsd.Property[] properties = {property};
                inboundAuthenticationRequestConfig.setProperties(properties);
            }
            serviceProvider.getInboundAuthenticationConfig().setInboundAuthenticationRequestConfigs(new
                    InboundAuthenticationRequestConfig[]{inboundAuthenticationRequestConfig});
        }
        super.updateServiceProvider(portOffset, serviceProvider);
    }

    /**
     * Get OAuth consumer app DTO with required configurations.
     */
    private OAuthConsumerAppDTO getOAuthConsumerAppDTO(OIDCApplication application) {

        OAuthConsumerAppDTO appDTO = new OAuthConsumerAppDTO();
        appDTO.setApplicationName(application.getApplicationName());
        appDTO.setCallbackUrl(application.getCallBackURL());
        appDTO.setOAuthVersion(OAuth2Constant.OAUTH_VERSION_2);
        appDTO.setTokenType("JWT");
        appDTO.setGrantTypes("authorization_code implicit password client_credentials refresh_token " +
                "urn:ietf:params:oauth:grant-type:token-exchange");
        return appDTO;
    }

    /**
     * Create OIDC configuration for service provider.
     */
    public OAuthConsumerAppDTO[] createOIDCConfiguration(int portOffset, OAuthConsumerAppDTO appDTO) 
            throws Exception {

        if (portOffset == PORT_OFFSET_0) {
            adminClient = new OauthAdminClient(automationContextMap.get(portOffset).getContextUrls()
                    .getBackEndUrl(), automationContextMap.get(portOffset).getContextTenant()
                    .getTenantAdmin().getUserName(), automationContextMap.get(portOffset).getContextTenant()
                    .getTenantAdmin().getPassword());
        } else {
            adminClient = new OauthAdminClient(automationContextMap.get(portOffset).getContextUrls()
                    .getBackEndUrl(), automationContextMap.get(portOffset).getContextTenant()
                    .getTenantAdmin().getUserName(), automationContextMap.get(portOffset).getContextTenant()
                    .getTenantAdmin().getPassword());
        }
        
        adminClient.registerOAuthApplicationData(appDTO);
        return adminClient.getAllOAuthApplicationData();
    }

    /**
     * Add trusted token issuer (IDP) in the sub organization.
     */
    private void addTrustedTokenIssuerInSubOrganization() throws Exception {

        // Create IDP using REST API in sub-organization context
        IdentityProviderPOSTRequest idpPostRequest = new IdentityProviderPOSTRequest();
        idpPostRequest.setName(TRUSTED_TOKEN_ISSUER_NAME);
        idpPostRequest.setAlias(secondaryISClientID);
        idpPostRequest.setIdpIssuerName(SECONDARY_IS_TOKEN_ENDPOINT);
        IdentityProviderPOSTRequest.Certificate certificate = new IdentityProviderPOSTRequest.Certificate();
        certificate.setJwksUri(SECONDARY_IS_JWKS_URI);
        idpPostRequest.setCertificate(certificate);

        // Add IDP directly to sub-organization using switched M2M token
        subOrgIdpId = idpMgtRestClient.createOrganizationIdentityProvider(idpPostRequest, switchedM2MToken);
        
        Assert.assertNotNull(subOrgIdpId, "Failed to create Identity Provider in sub-organization");
        log.info("Created trusted token issuer IDP with ID: " + subOrgIdpId + " in sub-organization");
    }

    /**
     * Get access token from secondary IS.
     */
    private void getAccessTokenFromSecondaryIS() throws Exception {

        List<NameValuePair> postParameters = new ArrayList<>();
        postParameters.add(new BasicNameValuePair("username", NEW_USER_USERNAME));
        postParameters.add(new BasicNameValuePair("password", NEW_USER_PASSWORD));
        postParameters.add(new BasicNameValuePair("grant_type", OAuth2Constant.OAUTH2_GRANT_TYPE_RESOURCE_OWNER));
        postParameters.add(new BasicNameValuePair("scope", "email"));
        
        JSONObject responseObject = sendPOSTMessage(SECONDARY_IS_TOKEN_ENDPOINT, secondaryISClientID,
                secondaryISClientSecret, postParameters);
        
        accessTokenFromSecondaryIS = responseObject.get("access_token").toString();
        Assert.assertNotNull(accessTokenFromSecondaryIS, "Access token from secondary IS is null.");
        log.info("Successfully obtained access token from secondary IS");
    }

    /**
     * Send POST message to token endpoint.
     */
    private JSONObject sendPOSTMessage(String endpoint, String clientID, String clientSecret,
                                       List<NameValuePair> postParameters) throws Exception {

        HttpPost httpPost = new HttpPost(endpoint);
        httpPost.setHeader("Authorization", "Basic " + getBase64EncodedString(clientID, clientSecret));
        httpPost.setHeader("Content-Type", "application/x-www-form-urlencoded");
        httpPost.setEntity(new UrlEncodedFormEntity(postParameters));
        
        HttpResponse response = httpClient.execute(httpPost);
        String responseString = EntityUtils.toString(response.getEntity(), "UTF-8");
        EntityUtils.consume(response.getEntity());

        JSONParser parser = new JSONParser();
        JSONObject json = (JSONObject) parser.parse(responseString);
        if (json == null) {
            throw new Exception("Error occurred while getting the response");
        }
        return json;
    }

    /**
     * Get Base64 encoded string for Basic authentication.
     */
    private String getBase64EncodedString(String consumerKey, String consumerSecret) {

        return new String(Base64.encodeBase64((consumerKey + ":" + consumerSecret).getBytes()));
    }

    /**
     * Extract subject from JWT token.
     */
    private String getTokenSubject(String token) throws Exception {

        SignedJWT signedJWT;
        try {
            signedJWT = SignedJWT.parse(token);
        } catch (ParseException e) {
            throw new Exception("Error while parsing the JWT", e);
        }
        return signedJWT.getJWTClaimsSet().getSubject();
    }
}















