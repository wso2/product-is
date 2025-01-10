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
package org.wso2.identity.integration.test.oauth2;

import com.nimbusds.jwt.SignedJWT;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.CookieStore;
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
import org.wso2.carbon.identity.application.common.model.xsd.Claim;
import org.wso2.carbon.identity.application.common.model.xsd.ClaimMapping;
import org.wso2.carbon.identity.application.common.model.idp.xsd.IdentityProvider;
import org.wso2.carbon.identity.application.common.model.idp.xsd.IdentityProviderProperty;
import org.wso2.carbon.identity.application.common.model.xsd.InboundAuthenticationRequestConfig;
import org.wso2.carbon.identity.application.common.model.xsd.OutboundProvisioningConfig;
import org.wso2.carbon.identity.application.common.model.xsd.ServiceProvider;
import org.wso2.carbon.identity.oauth.stub.dto.OAuthConsumerAppDTO;
import org.wso2.carbon.integration.common.admin.client.AuthenticatorClient;
import org.wso2.carbon.user.mgt.stub.types.carbon.ClaimValue;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;
import org.wso2.identity.integration.common.clients.oauth.OauthAdminClient;
import org.wso2.identity.integration.test.application.mgt.AbstractIdentityFederationTestCase;
import org.wso2.identity.integration.test.oidc.bean.OIDCApplication;
import org.wso2.identity.integration.test.utils.IdentityConstants;
import org.wso2.identity.integration.test.utils.OAuth2Constant;
import org.wso2.identity.integration.test.utils.UserUtil;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class OAuth2TokenExchangeGrantTypeTestCase extends AbstractIdentityFederationTestCase {

    private static final String PRIMARY_IS_IDP_NAME = "trustedIdP";
    private static final String PRIMARY_IS_SP_NAME = "primarySP";
    private static final String PRIMARY_IS_TOKEN_ENDPOINT = "https://localhost:9853/oauth2/token";
    private static final String SECONDARY_IS_SP_NAME = "secondarySP";
    private static final String SECONDARY_IS_TOKEN_ENDPOINT = "https://localhost:9854/oauth2/token";
    private static final String SECONDARY_IS_JWKS_URI = "https://localhost:9854/oauth2/jwks";
    private static final String NEW_USER_USERNAME = "secondaryUser";
    private static final String NEW_USER_EMAIL = "secondaryUser@gmail.com";
    private static final String NEW_USER_PASSWORD = "Wso2@123";
    public static final String EMAIL_CLAIM_URI = "http://wso2.org/claims/emailaddress";

    protected OauthAdminClient adminClient;
    private String secondaryISClientID;
    private String secondaryISClientSecret;
    private String primaryISClientID;
    private String primaryISClientSecret;
    private final String username;
    private final String userPassword;
    private final AutomationContext context;
    private String accessTokenFromSecondaryIS;
    private String primaryISUserId;
    private String secondaryISUserId;

    private static final int PORT_OFFSET_0 = 0;
    private static final int PORT_OFFSET_1 = 1;
    private static final String DEFAULT_PROFILE = "default";

    CookieStore cookieStore;
    private CloseableHttpClient client;

    @DataProvider(name = "configProvider")
    public static Object[][] configProvider() {

        return new Object[][]{{TestUserMode.SUPER_TENANT_ADMIN}};
    }

    @Factory(dataProvider = "configProvider")
    public OAuth2TokenExchangeGrantTypeTestCase(TestUserMode userMode) throws Exception {

        context = new AutomationContext("IDENTITY", userMode);
        this.username = context.getContextTenant().getTenantAdmin().getUserName();
        this.userPassword = context.getContextTenant().getTenantAdmin().getPassword();
    }

    @BeforeClass(alwaysRun = true)
    public void initTest() throws Exception {

        super.initTest();
        String backendURL = context.getContextUrls().getBackEndUrl();
        AuthenticatorClient logManger = new AuthenticatorClient(backendURL);
        String sessionCookie = logManger.login(username, userPassword, context.getInstance().getHosts().get("default"));

        adminClient = new OauthAdminClient(backendURL, sessionCookie);

        super.createServiceClients(PORT_OFFSET_0, sessionCookie,
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

        createServiceProviderInSecondaryIS();
        createServiceProviderInPrimaryIS();
        createIdentityProviderInPrimaryIS();
        createUser(PORT_OFFSET_1);
        updateServiceProviderRequestedClaimsInSecondaryIS();

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
                .setDefaultCookieStore(cookieStore).build();
    }

    @AfterClass(alwaysRun = true)
    public void endTest() throws Exception {

        try {
            super.deleteServiceProvider(PORT_OFFSET_0, PRIMARY_IS_SP_NAME);
            super.deleteIdentityProvider(PORT_OFFSET_0, PRIMARY_IS_IDP_NAME);
            super.deleteServiceProvider(PORT_OFFSET_1, SECONDARY_IS_SP_NAME);

            client.close();
        } catch (Exception e) {
            log.error("Failure occured due to :" + e.getMessage(), e);
            throw e;
        }
    }

    @Test(groups = "wso2.is", description = "Get a Access Token From Secondary IS")
    public void testGetAccessTokenFromSecondaryIS() throws Exception {

        List<NameValuePair> postParameters = new ArrayList<>();
        postParameters.add(new BasicNameValuePair("username", OAuth2TokenExchangeGrantTypeTestCase.NEW_USER_USERNAME));
        postParameters.add(new BasicNameValuePair("password", OAuth2TokenExchangeGrantTypeTestCase.NEW_USER_PASSWORD));
        postParameters.add(new BasicNameValuePair("grant_type", OAuth2Constant.OAUTH2_GRANT_TYPE_RESOURCE_OWNER));
        postParameters.add(new BasicNameValuePair("scope", "email"));
        JSONObject responseObject = sendPOSTMessage(SECONDARY_IS_TOKEN_ENDPOINT, secondaryISClientID,
                secondaryISClientSecret, postParameters);
        accessTokenFromSecondaryIS = responseObject.get("access_token").toString();

        Assert.assertNotNull(accessTokenFromSecondaryIS, "Access token is null.");
        secondaryISUserId = getTokenSubject(accessTokenFromSecondaryIS);
    }

    @Test(groups = "wso2.is", description = "Exchange access token for federated user",
            dependsOnMethods = "testGetAccessTokenFromSecondaryIS")
    public void testTokenExchangeForFederatedUser() throws Exception {

        String exchangedToken;
        List<NameValuePair> postParameters = getTokenExchangePostParameters();

        // assert local subject identifier config is disabled
        exchangedToken = exchangeToken(postParameters);
        Assert.assertEquals(getTokenSubject(exchangedToken), secondaryISUserId, "Subject of the exchanged " +
                "token should be the same as the subject of the federated user");


        // assert local subject identifier config is optional, no local user available
        updateAssertLocalSubjectIdentifierConfig(IdentityConstants.AssertLocalSubjectMode.OPTIONAL);
        exchangedToken = exchangeToken(postParameters);
        Assert.assertEquals(getTokenSubject(exchangedToken), secondaryISUserId, "Subject of the exchanged " +
                "token should be the same as the subject of the federated user");

        // create a similar user in primary IS
        createUser(PORT_OFFSET_0);
        primaryISUserId = UserUtil.getUserId(MultitenantUtils.getTenantAwareUsername(NEW_USER_USERNAME),
                isServer.getContextTenant());

        //assert local subject identifier config is optional, local user is available and implicit association config is disabled
        exchangedToken = exchangeToken(postParameters);
        Assert.assertEquals(getTokenSubject(exchangedToken), secondaryISUserId, "Subject of the exchanged " +
                "token should be the same as the subject of the federated user");
    }

    @Test(groups = "wso2.is", description = "Exchange access token for local user with implicit association config disabled",
            dependsOnMethods = "testTokenExchangeForFederatedUser")
    public void testTokenExchangeForLocalUserWithImplicitAssociationConfigDisabled() throws Exception {

        //assert local subject identifier config is mandatory, implicit association config disabled, no associations
        updateIdentityProviderAssociationConfig(false);
        updateAssertLocalSubjectIdentifierConfig(IdentityConstants.AssertLocalSubjectMode.MANDATORY);
        List<NameValuePair> postParameters = getTokenExchangePostParameters();
        int responseCode = getResponseStatus(postParameters);
        Assert.assertEquals(responseCode, 400, "400 response expected but got: " + responseCode);

    }

    @Test(groups = "wso2.is", description = "Exchange access token for local user",
            dependsOnMethods = "testTokenExchangeForLocalUserWithImplicitAssociationConfigDisabled")
    public void testTokenExchangeForLocalUser() throws Exception {

        //enable implicit association config and set lookup attribute to email
        updateIdentityProviderAssociationConfig(true);
        List<NameValuePair> postParameters = getTokenExchangePostParameters();
        String exchangedToken = exchangeToken(postParameters);
        Assert.assertEquals(getTokenSubject(exchangedToken), primaryISUserId, "Subject of the exchanged " +
                "token should be the same as the subject of the matching local user.");
    }

    @Test(groups = "wso2.is", description = "Exchange access token for local user with implicit association config disabled",
            dependsOnMethods = "testTokenExchangeForLocalUser")
    public void testTokenExchangeForAssociatedLocalUserWithImplicitAssociationConfigDisabled() throws Exception {

        //assert local subject identifier config is mandatory, implicit association config disabled, association exists
        updateIdentityProviderAssociationConfig(false);
        updateAssertLocalSubjectIdentifierConfig(IdentityConstants.AssertLocalSubjectMode.MANDATORY);
        List<NameValuePair> postParameters = getTokenExchangePostParameters();
        String exchangedToken = exchangeToken(postParameters);
        Assert.assertEquals(getTokenSubject(exchangedToken), primaryISUserId, "Subject of the exchanged " +
                "token should be the same as the subject of the matching local user.");
    }

    @Test(groups = "wso2.is", description = "Exchange access token for local user with no local account",
            dependsOnMethods = "testTokenExchangeForAssociatedLocalUserWithImplicitAssociationConfigDisabled")
    public void testTokenExchangeForLocalUserWithNoLocalAccount() throws Exception {

        //assert local subject identifier config is mandatory, no matching local user account
        deleteUser(PORT_OFFSET_0, NEW_USER_USERNAME);
        List<NameValuePair> postParameters = getTokenExchangePostParameters();
        int responseCode = getResponseStatus(postParameters);
        Assert.assertEquals(responseCode, 400, "400 response expected but got: " + responseCode);
    }

    private List<NameValuePair> getTokenExchangePostParameters() {

        List<NameValuePair> postParameters = new ArrayList<>();
        postParameters.add(new BasicNameValuePair("subject_token", accessTokenFromSecondaryIS));
        postParameters.add(new BasicNameValuePair("subject_token_type", "urn:ietf:params:oauth:token-type:jwt"));
        postParameters.add(new BasicNameValuePair("requested_token_type", "urn:ietf:params:oauth:token-type:jwt"));
        postParameters.add(new BasicNameValuePair("grant_type", "urn:ietf:params:oauth:grant-type:token-exchange"));
        return postParameters;
    }

    private String exchangeToken(List<NameValuePair> postParameters) throws Exception {

        JSONObject responseObject = sendPOSTMessage(PRIMARY_IS_TOKEN_ENDPOINT, primaryISClientID, primaryISClientSecret,
                postParameters);
        String exchangedToken = responseObject.get("access_token").toString();
        if (StringUtils.isBlank(exchangedToken)) {
            throw new Exception("Access token is null.");
        }

        return exchangedToken;
    }

    private int getResponseStatus(List<NameValuePair> postParameters) throws Exception {

        HttpPost httpPost = new HttpPost(PRIMARY_IS_TOKEN_ENDPOINT);
        httpPost.setHeader("Authorization", "Basic " + getBase64EncodedString(primaryISClientID, primaryISClientSecret));
        httpPost.setHeader("Content-Type", "application/x-www-form-urlencoded");
        httpPost.setEntity(new UrlEncodedFormEntity(postParameters));
        HttpResponse response = client.execute(httpPost);
        return response.getStatusLine().getStatusCode();
    }

    private String getTokenSubject(String token) throws Exception {

        SignedJWT signedJWT;
        try {
            signedJWT = SignedJWT.parse(token);
        } catch (ParseException e) {
            throw new Exception("Error while parsing the JWT", e);
        }

        return signedJWT.getJWTClaimsSet().getSubject();
    }

    private void createUser(int portOffset) throws Exception {

        List<ClaimValue> claimValues = new ArrayList<>();
        ClaimValue claimValue = new ClaimValue();
        claimValue.setClaimURI(EMAIL_CLAIM_URI);
        claimValue.setValue(NEW_USER_EMAIL);
        claimValues.add(claimValue);
        super.addUser(portOffset, OAuth2TokenExchangeGrantTypeTestCase.NEW_USER_USERNAME,
                OAuth2TokenExchangeGrantTypeTestCase.NEW_USER_PASSWORD, null, DEFAULT_PROFILE, claimValues.toArray(new ClaimValue[0]));
        Set<String> users = super.getUserList(PORT_OFFSET_1);
        if (users == null || !users.contains(OAuth2TokenExchangeGrantTypeTestCase.NEW_USER_USERNAME)) {
            throw new Exception("User creation failed in IS: " + portOffset);
        }
    }

    private void updateAssertLocalSubjectIdentifierConfig(IdentityConstants.AssertLocalSubjectMode updateMode) throws Exception {

        ServiceProvider serviceProvider = getServiceProvider(PORT_OFFSET_0, PRIMARY_IS_SP_NAME);
        if (serviceProvider == null) {
            throw new Exception("Failed to get service provider 'primarySP' in primary IS");
        }

        switch (updateMode) {
            case OPTIONAL:
                serviceProvider.getClaimConfig().setAlwaysSendMappedLocalSubjectId(true);
                serviceProvider.getClaimConfig().setMappedLocalSubjectMandatory(false);
                break;
            case MANDATORY:
                serviceProvider.getClaimConfig().setAlwaysSendMappedLocalSubjectId(true);
                serviceProvider.getClaimConfig().setMappedLocalSubjectMandatory(true);
                break;
            default:
                serviceProvider.getClaimConfig().setAlwaysSendMappedLocalSubjectId(false);
                serviceProvider.getClaimConfig().setMappedLocalSubjectMandatory(false);
        }

        super.updateServiceProvider(PORT_OFFSET_0, serviceProvider);

        ServiceProvider updatedServiceProvider = getServiceProvider(PORT_OFFSET_0, PRIMARY_IS_SP_NAME);

        switch (updateMode) {
            case OPTIONAL:
                if (!updatedServiceProvider.getClaimConfig().getAlwaysSendMappedLocalSubjectId() ||
                        updatedServiceProvider.getClaimConfig().getMappedLocalSubjectMandatory()) {
                    throw new Exception("Assert local subject identifier configuration is not updated properly");
                }
                break;
            case MANDATORY:
                if (!updatedServiceProvider.getClaimConfig().getAlwaysSendMappedLocalSubjectId() ||
                        !updatedServiceProvider.getClaimConfig().getMappedLocalSubjectMandatory()) {
                    throw new Exception("Assert local subject identifier configuration is not updated properly");
                }
                break;
            default:
                if (updatedServiceProvider.getClaimConfig().getAlwaysSendMappedLocalSubjectId() ||
                        updatedServiceProvider.getClaimConfig().getMappedLocalSubjectMandatory()) {
                    throw new Exception("Assert local subject identifier configuration is not updated properly");
                }
        }
    }

    private void updateIdentityProviderAssociationConfig(boolean enabled) throws Exception {

        IdentityProvider identityProvider = super.getIdentityProvider(PORT_OFFSET_0, PRIMARY_IS_IDP_NAME);
        if (identityProvider == null) {
            throw new Exception("Failed to get identity provider 'trustedIdP' in primary IS");
        }

        identityProvider.getFederatedAssociationConfig().setEnabled(enabled);
        if (enabled) {
            identityProvider.getFederatedAssociationConfig().setLookupAttributes(new String[]{EMAIL_CLAIM_URI});

        }

        super.updateIdentityProvider(PORT_OFFSET_0, PRIMARY_IS_IDP_NAME, identityProvider);

        IdentityProvider updatedIdentityProvider = super.getIdentityProvider(PORT_OFFSET_0, PRIMARY_IS_IDP_NAME);
        if (updatedIdentityProvider == null) {
            throw new Exception("Failed to get updated identity provider 'trustedIdP' in primary IS");
        }
        if (enabled) {
            if (!updatedIdentityProvider.getFederatedAssociationConfig().getEnabled() ||
                    !EMAIL_CLAIM_URI.equals(updatedIdentityProvider.getFederatedAssociationConfig().getLookupAttributes()[0])) {
                throw new Exception("Federated association config is not updated properly");
            }
        } else {
            if (updatedIdentityProvider.getFederatedAssociationConfig().getEnabled()) {
                throw new Exception("Federated association config is not updated properly");
            }
        }

    }

    private void updateServiceProviderRequestedClaimsInSecondaryIS() throws Exception {

        ServiceProvider serviceProvider = getServiceProvider(PORT_OFFSET_1, SECONDARY_IS_SP_NAME);
        if (serviceProvider == null) {
            throw new Exception("Failed to get service provider 'secondarySP' in secondary IS");
        }

        List<ClaimMapping> claimMappings = getClaimMappings();
        serviceProvider.getClaimConfig().setClaimMappings(claimMappings.toArray(new ClaimMapping[0]));

        super.updateServiceProvider(PORT_OFFSET_1, serviceProvider);
        ServiceProvider updatedServiceProvider = getServiceProvider(PORT_OFFSET_1, SECONDARY_IS_SP_NAME);
        if (updatedServiceProvider == null) {
            throw new Exception("Failed to get updated service provider 'secondarySP' in secondary IS");
        }

        if (updatedServiceProvider.getClaimConfig().getClaimMappings() == null) {
            throw new Exception("Claim mappings are not updated properly");
        }
    }

    private List<ClaimMapping> getClaimMappings() {

        List<ClaimMapping> claimMappings = new ArrayList<>();
        ClaimMapping claimMapping = new ClaimMapping();
        Claim localClaim = new Claim();
        Claim remoteClaim = new Claim();

        localClaim.setClaimUri(EMAIL_CLAIM_URI);
        remoteClaim.setClaimUri(EMAIL_CLAIM_URI);
        claimMapping.setLocalClaim(localClaim);
        claimMapping.setRemoteClaim(remoteClaim);
        claimMapping.setRequested(true);
        claimMappings.add(claimMapping);
        return claimMappings;
    }

    private JSONObject sendPOSTMessage(String endpoint, String clientID, String clientSecret,
                                       List<NameValuePair> postParameters) throws Exception {

        HttpPost httpPost = new HttpPost(endpoint);
        httpPost.setHeader("Authorization", "Basic " + getBase64EncodedString(clientID, clientSecret));
        httpPost.setHeader("Content-Type", "application/x-www-form-urlencoded");
        httpPost.setEntity(new UrlEncodedFormEntity(postParameters));
        HttpResponse response = client.execute(httpPost);
        String responseString = EntityUtils.toString(response.getEntity(), "UTF-8");
        EntityUtils.consume(response.getEntity());

        JSONParser parser = new JSONParser();
        JSONObject json = (JSONObject) parser.parse(responseString);
        if (json == null) {
            throw new Exception(
                    "Error occurred while getting the response");
        }
        return json;
    }

    public String getBase64EncodedString(String consumerKey, String consumerSecret) {

        return new String(Base64.encodeBase64((consumerKey + ":" + consumerSecret).getBytes()));
    }

    private void createServiceProviderInPrimaryIS() throws Exception {

        super.addServiceProvider(PORT_OFFSET_0, PRIMARY_IS_SP_NAME);

        ServiceProvider serviceProvider = getServiceProvider(PORT_OFFSET_0, PRIMARY_IS_SP_NAME);
        if (serviceProvider == null) {
            throw new Exception("Failed to create service provider 'travelocity' in primary IS");
        }

        updateServiceProviderWithOIDCConfigs(PORT_OFFSET_0, PRIMARY_IS_SP_NAME, serviceProvider);

        serviceProvider = getServiceProvider(PORT_OFFSET_0, PRIMARY_IS_SP_NAME);

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
            throw new Exception("Failed to update service provider with inbound OIDC configs in primary IS");
        }
    }

    private void createServiceProviderInSecondaryIS() throws Exception {

        super.addServiceProvider(PORT_OFFSET_1, SECONDARY_IS_SP_NAME);

        ServiceProvider serviceProvider = getServiceProvider(PORT_OFFSET_1, SECONDARY_IS_SP_NAME);
        if (serviceProvider == null) {
            throw new Exception("Failed to create service provider 'travelocity' in secondary IS");
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

    private void createIdentityProviderInPrimaryIS() throws Exception {

        IdentityProvider identityProvider = new IdentityProvider();
        identityProvider.setIdentityProviderName(PRIMARY_IS_IDP_NAME);
        identityProvider.setAlias(secondaryISClientID);

        IdentityProviderProperty jwksUriProperty = new IdentityProviderProperty();
        jwksUriProperty.setName("jwksUri");
        jwksUriProperty.setValue(SECONDARY_IS_JWKS_URI);
        IdentityProviderProperty issuerProperty = new IdentityProviderProperty();
        issuerProperty.setName("idpIssuerName");
        issuerProperty.setValue(SECONDARY_IS_TOKEN_ENDPOINT);
        IdentityProviderProperty[] properties = {jwksUriProperty, issuerProperty};
        identityProvider.setIdpProperties(properties);

        super.addIdentityProvider(PORT_OFFSET_0, identityProvider);
        if (getIdentityProvider(PORT_OFFSET_0, PRIMARY_IS_IDP_NAME) == null) {
            throw new Exception("Failed to create Identity Provider 'trustedIdP' in primary IS");
        }
    }

    private OAuthConsumerAppDTO getOAuthConsumerAppDTO(OIDCApplication application) {

        OAuthConsumerAppDTO appDTO = new OAuthConsumerAppDTO();
        appDTO.setApplicationName(application.getApplicationName());
        appDTO.setCallbackUrl(application.getCallBackURL());
        appDTO.setOAuthVersion(OAuth2Constant.OAUTH_VERSION_2);
        appDTO.setTokenType("JWT");
        appDTO.setGrantTypes("authorization_code implicit password client_credentials refresh_token " +
                "urn:ietf:params:oauth:grant-type:token-exchange");
        String[] accessTokenClaims = {"username", "email"};
        appDTO.setAccessTokenClaims(accessTokenClaims);

        return appDTO;
    }

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

        serviceProvider.setOutboundProvisioningConfig(new OutboundProvisioningConfig());

        if (application.getClientId() != null) {
            InboundAuthenticationRequestConfig inboundAuthenticationRequestConfig = new
                    InboundAuthenticationRequestConfig();
            inboundAuthenticationRequestConfig.setInboundAuthKey(application.getClientId());
            if (portOffset == PORT_OFFSET_0) {
                primaryISClientID = application.getClientId();
            } else if (portOffset == PORT_OFFSET_1) {
                secondaryISClientID = application.getClientId();
            }
            inboundAuthenticationRequestConfig.setInboundAuthType(OAuth2Constant.OAUTH_2);
            if (StringUtils.isNotBlank(application.getClientSecret())) {
                org.wso2.carbon.identity.application.common.model.xsd.Property property = new org.wso2.carbon.identity.
                        application.common.model.xsd.Property();
                property.setName(OAuth2Constant.OAUTH_CONSUMER_SECRET);
                property.setValue(application.getClientSecret());
                if (portOffset == PORT_OFFSET_0) {
                    primaryISClientSecret = application.getClientSecret();
                } else if (portOffset == PORT_OFFSET_1) {
                    secondaryISClientSecret = application.getClientSecret();
                }
                org.wso2.carbon.identity.application.common.model.xsd.Property[] properties = {property};
                inboundAuthenticationRequestConfig.setProperties(properties);
            }
            serviceProvider.getInboundAuthenticationConfig().setInboundAuthenticationRequestConfigs(new
                    InboundAuthenticationRequestConfig[]{inboundAuthenticationRequestConfig});
        }
        super.updateServiceProvider(portOffset, serviceProvider);
    }
}
