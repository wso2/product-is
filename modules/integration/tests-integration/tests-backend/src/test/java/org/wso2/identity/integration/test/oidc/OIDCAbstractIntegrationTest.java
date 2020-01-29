/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.identity.integration.test.oidc;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.testng.Assert;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.identity.application.common.model.xsd.Claim;
import org.wso2.carbon.identity.application.common.model.xsd.ClaimConfig;
import org.wso2.carbon.identity.application.common.model.xsd.ClaimMapping;
import org.wso2.carbon.identity.application.common.model.xsd.InboundAuthenticationRequestConfig;
import org.wso2.carbon.identity.application.common.model.xsd.OutboundProvisioningConfig;
import org.wso2.carbon.identity.application.common.model.xsd.Property;
import org.wso2.carbon.identity.application.common.model.xsd.ServiceProvider;
import org.wso2.carbon.identity.oauth.stub.dto.OAuthConsumerAppDTO;
import org.wso2.carbon.um.ws.api.stub.ClaimValue;
import org.wso2.identity.integration.test.oauth2.OAuth2ServiceAbstractIntegrationTest;
import org.wso2.identity.integration.test.oidc.bean.OIDCApplication;
import org.wso2.identity.integration.test.oidc.bean.OIDCUser;
import org.wso2.identity.integration.test.util.Utils;
import org.wso2.identity.integration.test.utils.OAuth2Constant;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class defines basic functionality needed to initiate an OIDC test
 */
public class OIDCAbstractIntegrationTest extends OAuth2ServiceAbstractIntegrationTest {

    public static final int TOMCAT_PORT = 8490;

    private static final Log log = LogFactory.getLog(OIDCAbstractIntegrationTest.class);

    @Override
    protected void init(TestUserMode userMode) throws Exception {

        super.init(userMode);
        setSystemproperties();
    }

    /**
     * Clear the intialized clients.
     */
    public void clear() {

        appMgtclient = null;
        remoteUSMServiceClient = null;
        adminClient = null;
    }

    /**
     * Creates a user
     *
     * @param user user instance
     * @throws Exception
     */
    public void createUser(OIDCUser user) throws Exception {

        log.info("Creating User " + user.getUsername());

        ClaimValue[] claims = null;
        if (MapUtils.isNotEmpty(user.getUserClaims())) {
            claims = new ClaimValue[user.getUserClaims().size()];

            int i = 0;
            for (Map.Entry<String, String> entry : user.getUserClaims().entrySet()) {
                ClaimValue claimValue = new ClaimValue();
                claimValue.setClaimURI(entry.getKey());
                claimValue.setValue(entry.getValue());
                claims[i++] = claimValue;
            }
        }

        String[] roles = null;
        if (!user.getRoles().isEmpty()) {
            roles = new String[user.getRoles().size()];
            roles = user.getRoles().toArray(roles);
        }

        // creating the user
        remoteUSMServiceClient.addUser(user.getUsername(), user.getPassword(), roles, claims, user.getProfile(), true);
    }

    /**
     * Deletes a user
     *
     * @param user user instance
     * @throws Exception
     */
    public void deleteUser(OIDCUser user) throws Exception {

        log.info("Deleting User " + user.getUsername());
        remoteUSMServiceClient.deleteUser(user.getUsername());
    }

    /**
     * Register an OIDC application in OP
     *
     * @param application application instance
     * @throws Exception
     */
    public void createApplication(OIDCApplication application) throws Exception {

        log.info("Creating application " + application.getApplicationName());

        OAuthConsumerAppDTO appDTO = new OAuthConsumerAppDTO();
        appDTO.setApplicationName(application.getApplicationName());
        appDTO.setCallbackUrl(application.getCallBackURL());
        appDTO.setOAuthVersion(OAuth2Constant.OAUTH_VERSION_2);
        appDTO.setGrantTypes("authorization_code implicit password client_credentials refresh_token " +
                "urn:ietf:params:oauth:grant-type:saml2-bearer iwa:ntlm");

        adminClient.registerOAuthApplicationData(appDTO);
        OAuthConsumerAppDTO[] appDtos = adminClient.getAllOAuthApplicationData();

        for (OAuthConsumerAppDTO appDto : appDtos) {
            if (appDto.getApplicationName().equals(application.getApplicationName())) {
                application.setClientId(appDto.getOauthConsumerKey());
                application.setClientSecret(appDto.getOauthConsumerSecret());
            }
        }

        ServiceProvider serviceProvider = new ServiceProvider();
        serviceProvider.setApplicationName(application.getApplicationName());
        serviceProvider.setDescription(application.getApplicationName());
        appMgtclient.createApplication(serviceProvider);

        serviceProvider = appMgtclient.getApplication(application.getApplicationName());

        ClaimConfig claimConfig = null;
        if (!application.getRequiredClaims().isEmpty()) {
            claimConfig = new ClaimConfig();
            for (String claimUri : application.getRequiredClaims()) {
                Claim claim = new Claim();
                claim.setClaimUri(claimUri);
                ClaimMapping claimMapping = new ClaimMapping();
                claimMapping.setRequested(true);
                claimMapping.setLocalClaim(claim);
                claimMapping.setRemoteClaim(claim);
                claimConfig.addClaimMappings(claimMapping);
            }
        }

        serviceProvider.setClaimConfig(claimConfig);
        serviceProvider.setOutboundProvisioningConfig(new OutboundProvisioningConfig());
        List<InboundAuthenticationRequestConfig> authRequestList = new ArrayList<>();

        if (application.getClientId() != null) {
            InboundAuthenticationRequestConfig inboundAuthenticationRequestConfig = new InboundAuthenticationRequestConfig();
            inboundAuthenticationRequestConfig.setInboundAuthKey(application.getClientId());
            inboundAuthenticationRequestConfig.setInboundAuthType(OAuth2Constant.OAUTH_2);
            if (StringUtils.isNotBlank(application.getClientSecret())) {
                Property property = new Property();
                property.setName(OAuth2Constant.OAUTH_CONSUMER_SECRET);
                property.setValue(application.getClientSecret());
                Property[] properties = {property};
                inboundAuthenticationRequestConfig.setProperties(properties);
            }
            authRequestList.add(inboundAuthenticationRequestConfig);
        }

        if (authRequestList.size() > 0) {
            serviceProvider.getInboundAuthenticationConfig().setInboundAuthenticationRequestConfigs(authRequestList
                    .toArray(new InboundAuthenticationRequestConfig[authRequestList.size()]));
        }

        appMgtclient.updateApplicationData(serviceProvider);
    }

    /**
     * Deletes the registered OIDC application in OP
     *
     * @param application application instance
     * @throws Exception
     */
    public void deleteApplication(OIDCApplication application) throws Exception {

        log.info("Deleting application " + application.getApplicationName());
        appMgtclient.deleteApplication(application.getApplicationName());
    }

    /**
     * Sends Authentication Request for an OIDC Flow.
     * @param application application
     * @param isFirstAuthenticationRequest true if the request is the first authentication request.
     * @param client http  client
     * @param cookieStore cookie store
     * @throws Exception throws if an error occurs when sending the authentication request.
     */
    public void testSendAuthenticationRequest(OIDCApplication application, boolean isFirstAuthenticationRequest,
                                              HttpClient client, CookieStore cookieStore)
            throws Exception {

        List<NameValuePair> urlParameters = OIDCUtilTest.getNameValuePairs(application);

        HttpResponse response = sendPostRequestWithParameters(client, urlParameters, String.format
                (OIDCUtilTest.targetApplicationUrl, application.getApplicationContext() + OAuth2Constant.PlaygroundAppPaths
                        .appUserAuthorizePath));

        Header locationHeader = response.getFirstHeader(OAuth2Constant.HTTP_RESPONSE_HEADER_LOCATION);
        EntityUtils.consume(response.getEntity());

        if (isFirstAuthenticationRequest) {
            response = sendGetRequest(client, locationHeader.getValue());
        } else {
            HttpClient httpClientWithoutAutoRedirections = HttpClientBuilder.create().disableRedirectHandling()
                    .setDefaultCookieStore(cookieStore).build();
            response = sendGetRequest(httpClientWithoutAutoRedirections, locationHeader.getValue());
        }

        Map<String, Integer> keyPositionMap = new HashMap<>(1);
        if (isFirstAuthenticationRequest) {
            OIDCUtilTest.setSessionDataKey(response, keyPositionMap);

        } else {
            Assert.assertFalse(Utils.requestMissingClaims(response));
        }
    }

}
