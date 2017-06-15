/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */

package org.wso2.identity.integration.test.oauth2;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.AutomationContext;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.identity.application.common.model.xsd.ServiceProvider;
import org.wso2.carbon.identity.oauth.stub.dto.OAuthConsumerAppDTO;
import org.wso2.carbon.identity.oauth2.stub.dto.OAuth2TokenValidationRequestDTO;
import org.wso2.carbon.identity.oauth2.stub.dto.OAuth2TokenValidationRequestDTO_OAuth2AccessToken;
import org.wso2.carbon.identity.oauth2.stub.dto.OAuth2TokenValidationResponseDTO;
import org.wso2.identity.integration.common.clients.application.mgt.ApplicationManagementServiceClient;
import org.wso2.identity.integration.common.clients.oauth.Oauth2TokenValidationClient;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/**
 * Test class to test token validation after service provider is SaaS-disabled.
 */
public class OAuth2SaaSAppTokenRevocationTestCase extends OAuth2ServiceAbstractIntegrationTest {

    private static final String TOKEN_API_ENDPOINT = "/oauth2/token";
    private static final String TOKEN_TYPE = "bearer";

    private String consumerKey;
    private String consumerSecret;
    private String tenantAccessToken;
    private String superTenantAccessToken;
    private String tokenEndpointUrl;
    private String adminUsername;
    private String adminPassword;
    private String tenantAdminUsername;
    private String tenantAdminPassword;
    private ApplicationManagementServiceClient applicationManagementServiceClient;
    private Oauth2TokenValidationClient oauth2TokenValidationClient;

    @BeforeClass(alwaysRun = true)
    public void init() throws Exception {

        AutomationContext tenantContext = new AutomationContext("IDENTITY", TestUserMode.TENANT_ADMIN);

        super.init(TestUserMode.SUPER_TENANT_ADMIN);
        ConfigurationContext configContext = ConfigurationContextFactory
                .createConfigurationContextFromFileSystem(null, null);
        applicationManagementServiceClient =
                new ApplicationManagementServiceClient(sessionCookie, backendURL, configContext);
        oauth2TokenValidationClient = new Oauth2TokenValidationClient(backendURL, sessionCookie);
        tokenEndpointUrl = isServer.getContextUrls().getWebAppURLHttps().concat(TOKEN_API_ENDPOINT);
        adminUsername = isServer.getSuperTenant().getTenantAdmin().getUserName();
        adminPassword = isServer.getSuperTenant().getTenantAdmin().getPassword();
        tenantAdminUsername = tenantContext.getContextTenant().getTenantAdmin().getUserName();
        tenantAdminPassword = tenantContext.getContextTenant().getTenantAdmin().getPassword();
        OAuthConsumerAppDTO authConsumerAppDTO = createApplication();
        assertEquals(applicationManagementServiceClient.getApplication(SERVICE_PROVIDER_NAME).getApplicationName(),
                SERVICE_PROVIDER_NAME, "Failed to create a Service Provider");

        consumerKey = authConsumerAppDTO.getOauthConsumerKey();
        consumerSecret = authConsumerAppDTO.getOauthConsumerSecret();
        ServiceProvider serviceProvider = applicationManagementServiceClient.getApplication(SERVICE_PROVIDER_NAME);
        serviceProvider.setSaasApp(true);
        applicationManagementServiceClient.updateApplicationData(serviceProvider);
        superTenantAccessToken = requestAccessToken(consumerKey, consumerSecret, tokenEndpointUrl,
                adminUsername, adminPassword);
        tenantAccessToken = requestAccessToken(consumerKey, consumerSecret, tokenEndpointUrl,
                tenantAdminUsername, tenantAdminPassword);
    }

    @Test(alwaysRun = true, description = "Test validation of tokens issued to same tenant users " +
            "when service provider is SaaS-disabled", groups = "wso2.is")
    public void testSameTenantTokenValidationWhenSaasDisabled() throws Exception {

        ServiceProvider serviceProvider = applicationManagementServiceClient.getApplication(SERVICE_PROVIDER_NAME);
        serviceProvider.setSaasApp(false);
        applicationManagementServiceClient.updateApplicationData(serviceProvider);
        OAuth2TokenValidationRequestDTO requestDTO = new OAuth2TokenValidationRequestDTO();
        OAuth2TokenValidationRequestDTO_OAuth2AccessToken accessTokenDTO = new
                OAuth2TokenValidationRequestDTO_OAuth2AccessToken();
        accessTokenDTO.setIdentifier(superTenantAccessToken);
        accessTokenDTO.setTokenType(TOKEN_TYPE);
        requestDTO.setAccessToken(accessTokenDTO);
        OAuth2TokenValidationResponseDTO responseDTO;
        //at application update, token validation is done in async mode which may take some time at the server,
        // hence polling until tokens are invalidated...
        boolean tokenState = false;
        int count = 50;
        while (count > 0 && !tokenState) {
            responseDTO = oauth2TokenValidationClient.validateToken(requestDTO);
            if (responseDTO.getValid()) {
                tokenState = true;
            }
            Thread.sleep(100);
            count--;
        }
        assertTrue(tokenState, "Token validation should be successful for the users of same tenant" +
                " when service provider is SaaS-disabled");
    }

    @Test(alwaysRun = true, description = "Test the validation of tokens issued to other tenants when " +
            "service provider is SaaS-disabled", groups = "wso2.is")
    public void testOtherTenantTokenValidationWhenSaasDisabled() throws Exception {

        ServiceProvider serviceProvider = applicationManagementServiceClient.getApplication(SERVICE_PROVIDER_NAME);
        serviceProvider.setSaasApp(false);
        applicationManagementServiceClient.updateApplicationData(serviceProvider);
        OAuth2TokenValidationRequestDTO requestDTO = new OAuth2TokenValidationRequestDTO();
        OAuth2TokenValidationRequestDTO_OAuth2AccessToken accessTokenDTO = new
                OAuth2TokenValidationRequestDTO_OAuth2AccessToken();
        accessTokenDTO.setIdentifier(tenantAccessToken);
        accessTokenDTO.setTokenType(TOKEN_TYPE);
        requestDTO.setAccessToken(accessTokenDTO);
        OAuth2TokenValidationResponseDTO responseDTO;
        //at application update, token revocation is done in async mode which may take some time at the server,
        // hence polling until tokens are invalidated...
        boolean tokenState = true;
        int count = 50;
        while (count > 0 && tokenState) {
            responseDTO = oauth2TokenValidationClient.validateToken(requestDTO);
            if (!responseDTO.getValid()) {
                tokenState = false;
            }
            Thread.sleep(100);
            count--;
        }
        assertFalse(tokenState, "Token validation should fail for the users of other tenants" +
                " when service provider is SaaS-disabled");
    }

    @AfterClass(alwaysRun = true)
    public void clear() throws Exception {
        deleteApplication();
        removeOAuthApplicationData();
    }
}
