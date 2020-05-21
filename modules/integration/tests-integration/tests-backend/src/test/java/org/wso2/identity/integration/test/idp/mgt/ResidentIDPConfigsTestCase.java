/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
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

package org.wso2.identity.integration.test.idp.mgt;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.AutomationContext;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.identity.application.common.model.idp.xsd.FederatedAuthenticatorConfig;
import org.wso2.carbon.identity.application.common.model.idp.xsd.IdentityProvider;
import org.wso2.carbon.identity.application.common.model.idp.xsd.Property;
import org.wso2.carbon.identity.application.common.model.idp.xsd.ProvisioningConnectorConfig;
import org.wso2.carbon.integration.common.admin.client.AuthenticatorClient;
import org.wso2.identity.integration.common.clients.Idp.IdentityProviderMgtServiceClient;
import org.wso2.identity.integration.common.utils.ISIntegrationTest;

import java.util.HashMap;
import java.util.Map;

/**
 * This test class is to test the resident IDP config URLs in super tenant legacy URL mode and tenant legacy URL mode.
 */
public class ResidentIDPConfigsTestCase extends ISIntegrationTest {

    private IdentityProviderMgtServiceClient idpMgtServiceClient;

    private final String username;
    private final String userPassword;
    private final String activeTenant;
    private Map<String, String> fedAuthConfigMap;
    private Map<String, String> provisioningConfigMap;
    private final String SUPER_TENANT = "carbon.super";
    private final String TENANT_DOMAIN = "wso2.com";

    @DataProvider(name = "configProvider")
    public static Object[][] configProvider() {
        return new Object[][]{
                {TestUserMode.SUPER_TENANT_ADMIN},
                {TestUserMode.TENANT_ADMIN}
        };
    }

    @Factory(dataProvider = "configProvider")
    public ResidentIDPConfigsTestCase(TestUserMode userMode) throws Exception {

        super.init(userMode);
        AutomationContext context = new AutomationContext("IDENTITY", userMode);
        this.username = context.getContextTenant().getTenantAdmin().getUserName();
        this.userPassword = context.getContextTenant().getTenantAdmin().getPassword();
        this.activeTenant = context.getContextTenant().getDomain();
    }

    @BeforeClass(alwaysRun = true)
    public void testInit() throws Exception {

        AuthenticatorClient loginManger = new AuthenticatorClient(backendURL);
        ConfigurationContext configContext = ConfigurationContextFactory.createConfigurationContextFromFileSystem
                (null, null);
        String cookie = loginManger.login(username, userPassword, isServer.getInstance().getHosts().get("default"));
        idpMgtServiceClient = new IdentityProviderMgtServiceClient(cookie, isServer.getContextUrls().getBackEndUrl(),
                configContext);
        IdentityProvider idProvider = idpMgtServiceClient.getResidentIdP();
        fedAuthConfigMap = getFedAuthConfigMap(idProvider);
        provisioningConfigMap = getProvisioningConfigMap(idProvider);
    }

    @DataProvider(name = "federatedAuthConfigURLProvider")
    public static Object[][] federatedAuthConfigURLProvider() {
        return new Object[][]{
                {"carbon.super", "OAuth1AccessTokenUrl", "https://localhost:9853/oauth/access-token",
                        "Expected OAuth10a Access Token URL is not found "},
                {"carbon.super", "OAuth1AuthorizeUrl", "https://localhost:9853/oauth/authorize-url",
                        "Expected OAuth10a Authorize URL is not found"},
                {"carbon.super", "OAuth1RequestTokenUrl", "https://localhost:9853/oauth/request-token",
                        "Expected OAuth10a Request Token URL is not found"},
                {"carbon.super", "IdentityProviderUrl", "https://localhost:9853/passivests",
                        "Expected Passive STS URL is not found"},
                {"carbon.super", "OpenIdUrl", "https://localhost:9853/openidserver",
                        "Expected OpenID Server URL is not found"},
                {"carbon.super", "ECPUrl", "https://localhost:9853/samlecp",
                        "Expected ECP URL is not found"},
                {"carbon.super", "LogoutReqUrl", "https://localhost:9853/samlsso",
                        "Expected Logout URL is not found"},
                {"carbon.super", "ArtifactResolveUrl", "https://localhost:9853/samlartresolve",
                        "Expected Artifact Resolution URL is not found"},
                {"carbon.super", "SSOUrl", "https://localhost:9853/samlsso",
                        "Expected SSO URL is not found"},
                {"carbon.super", "IDENTITY_PROVIDER_URL", "https://localhost:9853/services/wso2carbon-sts",
                        "Expected Security Token Service URL is not found"},
                {"carbon.super", "OIDCWebFingerEPUrl", "https://localhost:9853/.well-known/webfinger",
                        "Expected Web finger Endpoint URL is not found"},
                {"carbon.super", "IdPEntityId", "https://localhost:9853/oauth2/token",
                        "Expected Token Endpoint URL is not found"},
                {"carbon.super", "OIDCCheckSessionEPUrl", "https://localhost:9853/oidc/checksession",
                        "Expected Session IFrame Endpoint URL is not found"},
                {"carbon.super", "OAuth2IntrospectEPUrl", "https://localhost:9853/oauth2/introspect",
                        "Expected Token Introspection Endpoint URL is not found"},
                {"carbon.super", "OAuth2RevokeEPUrl", "https://localhost:9853/oauth2/revoke",
                        "Expected Token Revocation Endpoint URL is not found"},
                {"carbon.super", "OIDCLogoutEPUrl", "https://localhost:9853/oidc/logout",
                        "Expected Logout Endpoint URL not found"},
                {"carbon.super", "OAuth2AuthzEPUrl", "https://localhost:9853/oauth2/authorize",
                        "Expected Authorization Endpoint URL is not found"},
                {"carbon.super", "OAuth2DCREPUrl", "https://localhost:9853/api/identity/oauth2/dcr/v1.1/register",
                        "Expected Dynamic Client Registration Endpoint URL is not found"},
                {"carbon.super", "OIDCDiscoveryEPUrl", "https://localhost:9853/oauth2/oidcdiscovery",
                        "Expected Dynamic Client Registration Endpoint URL is not found"},
                {"carbon.super", "OAuth2JWKSPage", "https://localhost:9853/oauth2/jwks", "Expected is not found"},
                {"carbon.super", "OAuth2TokenEPUrl", "https://localhost:9853/oauth2/token",
                        "Expected Token Endpoint URL is not found"},
                {"carbon.super", "OAuth2UserInfoEPUrl", "https://localhost:9853/oauth2/userinfo",
                        "Expected User Info Endpoint URL is not found"},
                {"wso2.com", "OAuth1AccessTokenUrl", "https://localhost:9853/oauth/access-token",
                        "Expected OAuth10a Access Token URL is not found in the tenant mode"},
                {"wso2.com", "OAuth1AuthorizeUrl", "https://localhost:9853/oauth/authorize-url",
                        "Expected OAuth10a Authorize URL is not found in the tenant mode"},
                {"wso2.com", "OAuth1RequestTokenUrl", "https://localhost:9853/oauth/request-token",
                        "Expected OAuth10a Request Token URL is not found in the tenant mode"},
                {"wso2.com", "IdentityProviderUrl", "https://localhost:9853/passivests",
                        "Expected Passive STS URL is not found in the tenant mode"},
                {"wso2.com", "OpenIdUrl", "https://localhost:9853/openidserver",
                        "Expected OpenID Server URL is not found in the tenant mode"},
                {"wso2.com", "ECPUrl", "https://localhost:9853/samlecp?tenantDomain=wso2.com",
                        "Expected ECP URL is not found in the tenant mode"},
                {"wso2.com", "LogoutReqUrl", "https://localhost:9853/samlsso?tenantDomain=wso2.com",
                        "Expected Logout URL is not found in the tenant mode"},
                {"wso2.com", "ArtifactResolveUrl", "https://localhost:9853/samlartresolve",
                        "Expected Artifact Resolution URL is not found in the tenant mode"},
                {"wso2.com", "SSOUrl", "https://localhost:9853/samlsso?tenantDomain=wso2.com",
                        "Expected SSO URL is not found in the tenant mode"},
                {"wso2.com", "IDENTITY_PROVIDER_URL", "https://localhost:9853/services/t/wso2.com/wso2carbon-sts",
                        "Expected Security Token Service URL is not found in the tenant mode"},
                {"wso2.com", "OIDCWebFingerEPUrl", "https://localhost:9853/.well-known/webfinger",
                        "Expected Web finger Endpoint URL is not found in the tenant mode"},
                {"wso2.com", "OIDCCheckSessionEPUrl", "https://localhost:9853/oidc/checksession",
                        "Expected Session IFrame Endpoint URL is not found in the tenant mode"},
                {"wso2.com", "OAuth2IntrospectEPUrl", "https://localhost:9853/t/wso2.com/oauth2/introspect",
                        "Expected Token Introspection Endpoint URL is not found in the tenant mode"},
                {"wso2.com", "OAuth2RevokeEPUrl", "https://localhost:9853/oauth2/revoke",
                        "Expected Token Revocation Endpoint URL is not found in the tenant mode"},
                {"wso2.com", "OIDCLogoutEPUrl", "https://localhost:9853/oidc/logout",
                        "Expected Logout Endpoint URL not found in the tenant mode"},
                {"wso2.com", "OAuth2AuthzEPUrl", "https://localhost:9853/oauth2/authorize",
                        "Expected Authorization Endpoint URL is not found in the tenant mode"},
                {"wso2.com", "OAuth2DCREPUrl", "https://localhost:9853/t/wso2.com/api/identity/oauth2/dcr/v1.1/register",
                        "Expected Dynamic Client Registration Endpoint URL is not found in the tenant mode"},
                {"wso2.com", "OIDCDiscoveryEPUrl", "https://localhost:9853/t/wso2.com/oauth2/oidcdiscovery",
                        "Expected Dynamic Client Registration Endpoint URL is not found in the tenant mode"},
                {"wso2.com", "OAuth2JWKSPage", "https://localhost:9853/t/wso2.com/oauth2/jwks",
                        "Expected is not found in the tenant mode"},
                {"wso2.com", "OAuth2TokenEPUrl", "https://localhost:9853/oauth2/token",
                        "Expected Token Endpoint URL is not found in the tenant mode"},
                {"wso2.com", "OAuth2UserInfoEPUrl", "https://localhost:9853/oauth2/userinfo",
                        "Expected User Info Endpoint URL is not found in the tenant mode"}
        };
    }

    @DataProvider(name = "provisioningConfigURLProvider")
    public static Object[][] provisioningConfigURLProvider() {
        return new Object[][]{
                {"carbon.super", "scimGroupEndpoint", "https://localhost:9853/wso2/scim/Groups",
                        "Expected SCIM Group Endpoint is not found"},
                {"carbon.super", "scimUserEndpoint", "https://localhost:9853/wso2/scim/Users",
                        "Expected SCIM User Endpoint is not found"},
                {"carbon.super", "scim2UserEndpoint", "https://localhost:9853/scim2/Users",
                        "Expected SCIM 2.0 User Endpoint is not found"},
                {"carbon.super", "scim2GroupEndpoint", "https://localhost:9853/scim2/Groups",
                        "Expected SCIM 2.0 Group Endpoint is not found"},
                {"wso2.com", "scimGroupEndpoint", "https://localhost:9853/wso2/scim/Groups",
                        "Expected SCIM Group Endpoint is not found in the tenant mode"},
                {"wso2.com", "scimUserEndpoint", "https://localhost:9853/wso2/scim/Users",
                        "Expected SCIM User Endpoint is not found in the tenant mode"},
                {"wso2.com", "scim2UserEndpoint", "https://localhost:9853/t/wso2.com/scim2/Users",
                        "Expected SCIM 2.0 User Endpoint is not found in the tenant mode"},
                {"wso2.com", "scim2GroupEndpoint", "https://localhost:9853/t/wso2.com/scim2/Groups",
                        "Expected SCIM 2.0 Group Endpoint is not found in the tenant mode"}
        };
    }

    @Test(groups = "wso2.is", dataProvider = "federatedAuthConfigURLProvider",
            description = "Test resident IdP authentication configs URLs in tenant and super tenant mode")
    public void testResidentIdPAuthenticationConfigs(String configBindTenant, String endpoint,
                                                     String expectedURL, String errorMessage) {

        if (SUPER_TENANT.equals(activeTenant) && SUPER_TENANT.equals(configBindTenant)) {
            Assert.assertEquals(fedAuthConfigMap.get(endpoint), expectedURL, errorMessage);
        }

        if (TENANT_DOMAIN.equals(activeTenant) && TENANT_DOMAIN.equals(configBindTenant)) {
            Assert.assertEquals(fedAuthConfigMap.get(endpoint), expectedURL, errorMessage);
        }
    }

    @Test(groups = "wso2.is", dataProvider = "provisioningConfigURLProvider",
            description = "Test resident IdP provisioning config URLs in super tenant and tenant mode")
    public void testResidentIdPProvisioningConfigs(String configBindTenant, String endpoint, String expectedURL,
                                                   String errorMessage) {

        if (SUPER_TENANT.equals(activeTenant) && SUPER_TENANT.equals(configBindTenant)) {
            Assert.assertEquals(provisioningConfigMap.get(endpoint), expectedURL, errorMessage);
        }

        if (TENANT_DOMAIN.equals(activeTenant) && TENANT_DOMAIN.equals(configBindTenant)) {
            Assert.assertEquals(provisioningConfigMap.get(endpoint), expectedURL, errorMessage);
        }
    }

    private Map<String, String> getFedAuthConfigMap(IdentityProvider idProvider) {

        FederatedAuthenticatorConfig[] authConfigs = idProvider.getFederatedAuthenticatorConfigs();
        Map<String, String> fedAuthConfigMap = new HashMap<String, String>();
        for (FederatedAuthenticatorConfig config : authConfigs) {
            for (Property property : config.getProperties()) {
                fedAuthConfigMap.put(property.getName(), property.getValue());
            }
        }
        return fedAuthConfigMap;
    }

    private Map<String, String> getProvisioningConfigMap(IdentityProvider idProvider) {

        ProvisioningConnectorConfig[] provisioningConfigs = idProvider.getProvisioningConnectorConfigs();
        Map<String, String> provisioningConfigMap = new HashMap<String, String>();
        for (ProvisioningConnectorConfig config : provisioningConfigs) {
            for (Property property : config.getProvisioningProperties()) {
                provisioningConfigMap.put(property.getName(), property.getValue());
            }
        }
        return provisioningConfigMap;
    }
}
