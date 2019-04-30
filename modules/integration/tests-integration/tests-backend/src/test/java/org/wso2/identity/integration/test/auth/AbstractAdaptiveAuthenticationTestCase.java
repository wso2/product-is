/*
 *  Copyright (c) 2018 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.identity.integration.test.auth;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.testng.Assert;
import org.wso2.carbon.identity.application.common.model.idp.xsd.FederatedAuthenticatorConfig;
import org.wso2.carbon.identity.application.common.model.idp.xsd.IdentityProvider;
import org.wso2.carbon.identity.application.common.model.idp.xsd.IdentityProviderProperty;
import org.wso2.carbon.identity.application.common.model.script.xsd.AuthenticationScriptConfig;
import org.wso2.carbon.identity.application.common.model.xsd.AuthenticationStep;
import org.wso2.carbon.identity.application.common.model.xsd.InboundAuthenticationConfig;
import org.wso2.carbon.identity.application.common.model.xsd.InboundAuthenticationRequestConfig;
import org.wso2.carbon.identity.application.common.model.xsd.LocalAndOutboundAuthenticationConfig;
import org.wso2.carbon.identity.application.common.model.xsd.LocalAuthenticatorConfig;
import org.wso2.carbon.identity.application.common.model.xsd.Property;
import org.wso2.carbon.identity.application.common.model.xsd.ServiceProvider;
import org.wso2.carbon.identity.oauth.stub.OAuthAdminServiceIdentityOAuthAdminException;
import org.wso2.carbon.identity.oauth.stub.dto.OAuthConsumerAppDTO;
import org.wso2.identity.integration.common.clients.Idp.IdentityProviderMgtServiceClient;
import org.wso2.identity.integration.common.clients.application.mgt.ApplicationManagementServiceClient;
import org.wso2.identity.integration.common.clients.oauth.OauthAdminClient;
import org.wso2.identity.integration.test.oauth2.OAuth2ServiceAbstractIntegrationTest;
import org.wso2.identity.integration.test.util.Utils;
import org.wso2.identity.integration.test.utils.CommonConstants;
import org.wso2.identity.integration.test.utils.DataExtractUtil;
import org.wso2.identity.integration.test.utils.OAuth2Constant;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.wso2.identity.integration.test.utils.OAuth2Constant.CALLBACK_URL;

/**
 * Test class to test the conditional authentication support using Javascript feature.
 */
public class AbstractAdaptiveAuthenticationTestCase extends OAuth2ServiceAbstractIntegrationTest {


    protected IdentityProviderMgtServiceClient superTenantIDPMgtClient;

    @Override
    protected void init() throws Exception {

        super.init();
        superTenantIDPMgtClient = new IdentityProviderMgtServiceClient(sessionCookie, backendURL);
    }

    protected String getConditionalAuthScript(String filename) throws IOException {

        try (InputStream resourceAsStream = this.getClass().getResourceAsStream(filename);
             BufferedInputStream bufferedInputStream = new BufferedInputStream(resourceAsStream)) {
            StringBuilder resourceFile = new StringBuilder();

            int character;
            while ((character = bufferedInputStream.read()) != -1) {
                char value = (char) character;
                resourceFile.append(value);
            }

            return resourceFile.toString();
        }
    }

    protected HttpResponse loginWithOIDC(String appName, String consumerKey, HttpClient client) throws Exception {

        String sessionDataKey = redirectToLoginPage(appName, consumerKey, client, "sessionDataKey");

        return sendLoginPost(client, sessionDataKey);
    }

    protected String redirectToLoginPage(String appName, String consumerKey, HttpClient client, String keyToExtract)
            throws IOException {

        HttpResponse response;
        List<NameValuePair> urlParameters = new ArrayList<>();
        urlParameters.add(new BasicNameValuePair("response_type", OAuth2Constant.OAUTH2_GRANT_TYPE_CODE));
        urlParameters.add(new BasicNameValuePair("scope", "openid"));
        urlParameters.add(new BasicNameValuePair("redirect_uri", CALLBACK_URL));
        urlParameters.add(new BasicNameValuePair("client_id", consumerKey));
        urlParameters.add(new BasicNameValuePair("acr_values", "acr1"));
        urlParameters.add(new BasicNameValuePair("accessEndpoint", OAuth2Constant.ACCESS_TOKEN_ENDPOINT));
        urlParameters.add(new BasicNameValuePair("authorize", OAuth2Constant.AUTHORIZE_PARAM));
        response = sendPostRequestWithParameters(client, urlParameters, OAuth2Constant.APPROVAL_URL);
        Assert.assertNotNull(response, "Authorization request failed. Authorized response is null.");

        Header locationHeader = response.getFirstHeader(OAuth2Constant.HTTP_RESPONSE_HEADER_LOCATION);
        Assert.assertNotNull(locationHeader, "Authorized response header is null.");
        EntityUtils.consume(response.getEntity());

        response = sendGetRequest(client, locationHeader.getValue());
        Assert.assertNotNull(response, "Authorization request failed for " + appName + ". "
                + "Authorized user response is null.");

        Map<String, Integer> keyPositionMap = new HashMap<>(1);
        keyPositionMap.put("name=\"" + keyToExtract + "\"", 1);
        List<DataExtractUtil.KeyValue> keyValues = DataExtractUtil.extractDataFromResponse(response, keyPositionMap);
        Assert.assertNotNull(keyValues, "sessionDataKey key value is null for " + appName);

        String extractedValue = keyValues.get(0).getValue();
        Assert.assertNotNull(extractedValue, "Invalid sessionDataKey for " + appName);
        EntityUtils.consume(response.getEntity());
        return extractedValue;
    }

    protected void createOauthApp(String callback, String appName, OauthAdminClient oAuthAdminClient) throws
            RemoteException, OAuthAdminServiceIdentityOAuthAdminException {


        OAuthConsumerAppDTO appDTO = new OAuthConsumerAppDTO();
        appDTO.setCallbackUrl(callback);
        appDTO.setGrantTypes("authorization_code implicit password client_credentials refresh_token "
                + "urn:ietf:params:oauth:grant-type:saml2-bearer iwa:ntlm");
        appDTO.setOAuthVersion(OAuth2Constant.OAUTH_VERSION_2);
        appDTO.setApplicationName(appName);
        oAuthAdminClient.registerOAuthApplicationData(appDTO);

        OAuthConsumerAppDTO[] appDtos = oAuthAdminClient.getAllOAuthApplicationData();
        for (OAuthConsumerAppDTO appDto : appDtos) {
            if (appDto.getApplicationName().equals(appName)) {
                consumerKey = appDto.getOauthConsumerKey();
                consumerSecret = appDto.getOauthConsumerSecret();
            }
        }
    }

    protected ServiceProvider createServiceProvider(String appName, ApplicationManagementServiceClient
            applicationManagementServiceClient, OauthAdminClient oauthAdminClient, String script)
            throws Exception {

        OAuthConsumerAppDTO[] appDtos = oauthAdminClient.getAllOAuthApplicationData();

        for (OAuthConsumerAppDTO appDto : appDtos) {
            if (appDto.getApplicationName().equals(appName)) {
                consumerKey = appDto.getOauthConsumerKey();
                consumerSecret = appDto.getOauthConsumerSecret();
            }
        }

        ServiceProvider serviceProvider = new ServiceProvider();
        serviceProvider.setApplicationName(appName);
        serviceProvider.setDescription("This is a test Service Provider for conditional authentication flow test.");
        applicationManagementServiceClient.createApplication(serviceProvider);
        serviceProvider = applicationManagementServiceClient.getApplication(appName);

        InboundAuthenticationRequestConfig requestConfig = new InboundAuthenticationRequestConfig();
        requestConfig.setInboundAuthKey(consumerKey);
        requestConfig.setInboundAuthType("oauth2");
        if (StringUtils.isNotBlank(consumerSecret)) {
            Property property = new Property();
            property.setName("oauthConsumerSecret");
            property.setValue(consumerSecret);
            Property[] properties = {property};
            requestConfig.setProperties(properties);
        }

        InboundAuthenticationConfig inboundAuthenticationConfig = new InboundAuthenticationConfig();
        inboundAuthenticationConfig
                .setInboundAuthenticationRequestConfigs(new InboundAuthenticationRequestConfig[]{requestConfig});
        serviceProvider.setInboundAuthenticationConfig(inboundAuthenticationConfig);

        LocalAndOutboundAuthenticationConfig outboundAuthConfig = createLocalAndOutboundAuthenticationConfig();
        outboundAuthConfig.setEnableAuthorization(false);
        AuthenticationScriptConfig config = new AuthenticationScriptConfig();
        config.setContent(script);
        config.setEnabled(true);
        outboundAuthConfig.setAuthenticationScriptConfig(config);
        serviceProvider.setLocalAndOutBoundAuthenticationConfig(outboundAuthConfig);
        applicationManagementServiceClient.updateApplicationData(serviceProvider);
        return serviceProvider;
    }

    /**
     * Create the AdvancedAuthenticator with Multi steps.
     * Use any attributes needed if needed to do multiple tests with different advanced authenticators.
     *
     * @throws Exception
     */
    protected LocalAndOutboundAuthenticationConfig createLocalAndOutboundAuthenticationConfig() throws Exception {

        LocalAndOutboundAuthenticationConfig localAndOutboundAuthenticationConfig = new LocalAndOutboundAuthenticationConfig();
        localAndOutboundAuthenticationConfig.setAuthenticationType("flow");
        AuthenticationStep authenticationStep1 = new AuthenticationStep();
        authenticationStep1.setStepOrder(1);
        LocalAuthenticatorConfig localConfig = new LocalAuthenticatorConfig();
        localConfig.setName(CommonConstants.BASIC_AUTHENTICATOR);
        localConfig.setDisplayName("basicauth");
        localConfig.setEnabled(true);
        authenticationStep1.setLocalAuthenticatorConfigs(new LocalAuthenticatorConfig[]{localConfig});
        authenticationStep1.setSubjectStep(true);
        authenticationStep1.setAttributeStep(true);
        localAndOutboundAuthenticationConfig.addAuthenticationSteps(authenticationStep1);

        return localAndOutboundAuthenticationConfig;
    }

    protected static void copyFileUsingStream(InputStream source, OutputStream dest) throws IOException {

        try {
            byte[] buffer = new byte[1024];
            int length;
            while ((length = source.read(buffer)) > 0) {
                dest.write(buffer, 0, length);
            }
        } finally {
            source.close();
            dest.close();
        }
    }

    protected void updateResidentIDP(IdentityProvider residentIdentityProvider) throws Exception {

        FederatedAuthenticatorConfig[] federatedAuthenticatorConfigs =
                residentIdentityProvider.getFederatedAuthenticatorConfigs();
        for (FederatedAuthenticatorConfig authenticatorConfig : federatedAuthenticatorConfigs) {
            if (!authenticatorConfig.getName().equalsIgnoreCase("samlsso")) {
                federatedAuthenticatorConfigs = (FederatedAuthenticatorConfig[])
                        ArrayUtils.removeElement(federatedAuthenticatorConfigs,
                                authenticatorConfig);
            }
        }
        residentIdentityProvider.setFederatedAuthenticatorConfigs(federatedAuthenticatorConfigs);
        superTenantIDPMgtClient.updateResidentIdP(residentIdentityProvider);
    }

    protected void updateResidentIDPProperty(IdentityProvider residentIdp, String propertyKey, String value)
            throws Exception {

        IdentityProviderProperty[] idpProperties = residentIdp.getIdpProperties();
        for (IdentityProviderProperty providerProperty : idpProperties) {
            if (propertyKey.equalsIgnoreCase(providerProperty.getName())) {
                providerProperty.setValue(value);
            }
        }
        updateResidentIDP(residentIdp);
    }


    protected HttpResponse sendConsentGetRequest(String locationURL, CookieStore cookieStore,
                                              List<NameValuePair> consentRequiredClaimsFromResponse) throws Exception {

        HttpClient httpClientWithoutAutoRedirections = HttpClientBuilder.create().disableRedirectHandling()
                .setDefaultCookieStore(cookieStore).build();
        HttpGet getRequest = new HttpGet(locationURL);
        getRequest.setHeader("User-Agent", OAuth2Constant.USER_AGENT);
        HttpResponse response = httpClientWithoutAutoRedirections.execute(getRequest);

        consentRequiredClaimsFromResponse.addAll(Utils.getConsentRequiredClaimsFromResponse(response));
        Header locationHeader = response.getFirstHeader(OAuth2Constant.HTTP_RESPONSE_HEADER_LOCATION);
        HttpResponse httpResponse = sendGetRequest(httpClientWithoutAutoRedirections, locationHeader.getValue());
        EntityUtils.consume(response.getEntity());
        return httpResponse;
    }
}
