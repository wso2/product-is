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
import org.wso2.carbon.identity.application.common.model.idp.xsd.IdentityProvider;
import org.wso2.carbon.identity.application.common.model.idp.xsd.IdentityProviderProperty;
import org.wso2.carbon.identity.application.common.model.xsd.InboundAuthenticationRequestConfig;
import org.wso2.carbon.identity.application.common.model.xsd.OutboundProvisioningConfig;
import org.wso2.carbon.identity.application.common.model.xsd.ServiceProvider;
import org.wso2.carbon.identity.oauth.stub.dto.OAuthConsumerAppDTO;
import org.wso2.carbon.integration.common.admin.client.AuthenticatorClient;
import org.wso2.identity.integration.common.clients.oauth.OauthAdminClient;
import org.wso2.identity.integration.test.application.mgt.AbstractIdentityFederationTestCase;
import org.wso2.identity.integration.test.oidc.bean.OIDCApplication;
import org.wso2.identity.integration.test.utils.IdentityConstants;
import org.wso2.identity.integration.test.utils.OAuth2Constant;

import java.util.ArrayList;
import java.util.List;

public class OAuth2TokenExchangeGrantTypeTestCase extends AbstractIdentityFederationTestCase {

    private static final String PRIMARY_IS_IDP_NAME = "trustedIdP";
    private static final String PRIMARY_IS_SP_NAME = "primarySP";
    private static final String PRIMARY_IS_TOKEN_ENDPOINT = "https://localhost:9853/oauth2/token";
    private static final String SECONDARY_IS_SP_NAME = "secondarySP";
    private static final String SECONDARY_IS_TOKEN_ENDPOINT = "https://localhost:9854/oauth2/token";
    private static final String SECONDARY_IS_JWKS_URI = "https://localhost:9854/oauth2/jwks";

    protected OauthAdminClient adminClient;
    private String secondaryISClientID;
    private String secondaryISClientSecret;
    private String primaryISClientID;
    private String primaryISClientSecret;
    private final String username;
    private final String userPassword;
    private final AutomationContext context;
    private String accessTokenFromSecondaryIS;

    private static final int PORT_OFFSET_0 = 0;
    private static final int PORT_OFFSET_1 = 1;

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
                        IdentityConstants.ServiceClientType.OAUTH_ADMIN});

        super.createServiceClients(PORT_OFFSET_1, null,
                new IdentityConstants.ServiceClientType[]{
                        IdentityConstants.ServiceClientType.APPLICATION_MANAGEMENT,
                        IdentityConstants.ServiceClientType.OAUTH_ADMIN});

        createServiceProviderInSecondaryIS();
        createServiceProviderInPrimaryIS();
        createIdentityProviderInPrimaryIS();

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
        postParameters.add(new BasicNameValuePair("username", username));
        postParameters.add(new BasicNameValuePair("password", userPassword));
        postParameters.add(new BasicNameValuePair("grant_type", OAuth2Constant.OAUTH2_GRANT_TYPE_RESOURCE_OWNER));
        JSONObject responseObject = sendPOSTMessage(SECONDARY_IS_TOKEN_ENDPOINT, secondaryISClientID,
                secondaryISClientSecret, postParameters);
        accessTokenFromSecondaryIS = responseObject.get("access_token").toString();

        Assert.assertNotNull(accessTokenFromSecondaryIS, "Access token is null.");
    }

    @Test(groups = "wso2.is", description = "Exchange Access Token", dependsOnMethods = {
            "testGetAccessTokenFromSecondaryIS"})
    public void testTokenExchange() throws Exception {

        List<NameValuePair> postParameters = new ArrayList<>();
        postParameters.add(new BasicNameValuePair("subject_token", accessTokenFromSecondaryIS));
        postParameters.add(new BasicNameValuePair("subject_token_type", "urn:ietf:params:oauth:token-type:jwt"));
        postParameters.add(new BasicNameValuePair("requested_token_type", "urn:ietf:params:oauth:token-type:jwt"));
        postParameters.add(new BasicNameValuePair("grant_type", "urn:ietf:params:oauth:grant-type:token-exchange"));
        JSONObject responseObject = sendPOSTMessage(PRIMARY_IS_TOKEN_ENDPOINT, primaryISClientID, primaryISClientSecret,
                postParameters);
        String exchangedToken = responseObject.get("access_token").toString();

        Assert.assertNotNull(exchangedToken, "Access token is null.");
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
        Assert.assertNotNull(serviceProvider, "Failed to create service provider 'travelocity' in primary IS");

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
        Assert.assertTrue(success, "Failed to update service provider with inbound OIDC configs in primary IS");
    }

    private void createServiceProviderInSecondaryIS() throws Exception {

        super.addServiceProvider(PORT_OFFSET_1, SECONDARY_IS_SP_NAME);

        ServiceProvider serviceProvider = getServiceProvider(PORT_OFFSET_1, SECONDARY_IS_SP_NAME);
        Assert.assertNotNull(serviceProvider, "Failed to create service provider 'travelocity' in secondary IS");

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
        Assert.assertTrue(success, "Failed to update service provider with inbound OIDC configs in secondary IS");
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
        Assert.assertNotNull(getIdentityProvider(PORT_OFFSET_0, PRIMARY_IS_IDP_NAME), "Failed to create " +
                "Identity Provider 'trustedIdP' in primary IS");
    }

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

    private void updateServiceProviderWithOIDCConfigs(int portOffset, String applicationName,
                                                      ServiceProvider serviceProvider) throws Exception {

        OIDCApplication application = new OIDCApplication(applicationName, "/" + applicationName,
                OAuth2Constant.CALLBACK_URL);

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
