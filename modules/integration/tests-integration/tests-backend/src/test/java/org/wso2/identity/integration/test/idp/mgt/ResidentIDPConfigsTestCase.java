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
import org.testng.annotations.Test;
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
    private AuthenticatorClient loginManger;

    @BeforeClass(alwaysRun = true)
    public void testInit() throws Exception {

        super.init();
        ConfigurationContext configContext = ConfigurationContextFactory.createConfigurationContextFromFileSystem(null, null);
        idpMgtServiceClient = new IdentityProviderMgtServiceClient(sessionCookie, backendURL, configContext);
    }

    @Test(groups = "wso2.is", description = "Test resident IdP config URLs in super tenant mode")
    public void testResidentIdPConfigs() throws Exception {

        log.info("Retrieving resident identity provide");
        IdentityProvider idProvider = idpMgtServiceClient.getResidentIdP();

        //Extract authenticator configurations.
        FederatedAuthenticatorConfig[] authConfigs = idProvider.getFederatedAuthenticatorConfigs();

        Map<String, String> fedAuthConfigMap = new HashMap<String, String>();
        for (FederatedAuthenticatorConfig config : authConfigs) {
            for (Property property: config.getProperties()) {
                fedAuthConfigMap.put(property.getName(), property.getValue());
            }
        }

        Assert.assertEquals(fedAuthConfigMap.get("OAuth1AccessTokenUrl"),
                "https://localhost:9853/oauth/access-token",
                "Expected OAuth10a Access Token URL is not found ");
        Assert.assertEquals(fedAuthConfigMap.get("OAuth1AuthorizeUrl"),
                "https://localhost:9853/oauth/authorize-url",
                "Expected OAuth10a Authorize URL is not found");
        Assert.assertEquals(fedAuthConfigMap.get("OAuth1RequestTokenUrl"),
                "https://localhost:9853/oauth/request-token",
                "Expected OAuth10a Request Token URL is not found");
        Assert.assertEquals(fedAuthConfigMap.get("IdentityProviderUrl"),
                "https://localhost:9853/passivests",
                "Expected Passive STS URL is not found");
        Assert.assertEquals(fedAuthConfigMap.get("OpenIdUrl"),
                "https://localhost:9853/openidserver",
                "Expected OpenID Server URL is not found");
        Assert.assertEquals(fedAuthConfigMap.get("ECPUrl"),
                "https://localhost:9853/samlecp",
                "Expected ECP URL is not found");
        Assert.assertEquals(fedAuthConfigMap.get("LogoutReqUrl"),
                "https://localhost:9853/samlsso",
                "Expected Logout URL is not found");
        Assert.assertEquals(fedAuthConfigMap.get("ArtifactResolveUrl"),
                "https://localhost:9853/samlartresolve",
                "Expected Artifact Resolution URL is not found");
        Assert.assertEquals(fedAuthConfigMap.get("SSOUrl"),
                "https://localhost:9853/samlsso",
                "Expected SSO URL is not found");
        Assert.assertEquals(fedAuthConfigMap.get("IDENTITY_PROVIDER_URL"),
                "https://localhost:9853/services/wso2carbon-sts",
                "Expected Security Token Service URL is not found");
        Assert.assertEquals(fedAuthConfigMap.get("OIDCWebFingerEPUrl"),
                "https://localhost:9853/.well-known/webfinger",
                "Expected Web finger Endpoint URL is not found");
        Assert.assertEquals(fedAuthConfigMap.get("IdPEntityId"),
                "https://localhost:9853/oauth2/token",
                "Expected Token Endpoint URL is not found");
        Assert.assertEquals(fedAuthConfigMap.get("OIDCCheckSessionEPUrl"),
                "https://localhost:9853/oidc/checksession",
                "Expected Session IFrame Endpoint URL is not found");
        Assert.assertEquals(fedAuthConfigMap.get("OAuth2IntrospectEPUrl"),
                "https://localhost:9853/oauth2/introspect",
                "Expected Token Introspection Endpoint URL is not found");
        Assert.assertEquals(fedAuthConfigMap.get("OAuth2RevokeEPUrl"),
                "https://localhost:9853/oauth2/revoke",
                "Expected Token Revocation Endpoint URL is not found");
        Assert.assertEquals(fedAuthConfigMap.get("OIDCLogoutEPUrl"),
                "https://localhost:9853/oidc/logout",
                "Expected Logout Endpoint URL not found");
        Assert.assertEquals(fedAuthConfigMap.get("OAuth2AuthzEPUrl"),
                "https://localhost:9853/oauth2/authorize",
                "Expected Authorization Endpoint URL is not found");
        Assert.assertEquals(fedAuthConfigMap.get("OAuth2DCREPUrl"),
                "https://localhost:9853/api/identity/oauth2/dcr/v1.1/register",
                "Expected Dynamic Client Registration Endpoint URL is not found");
        Assert.assertEquals(fedAuthConfigMap.get("OIDCDiscoveryEPUrl"),
                "https://localhost:9853/oauth2/oidcdiscovery",
                "Expected Dynamic Client Registration Endpoint URL is not found");
        Assert.assertEquals(fedAuthConfigMap.get("OAuth2JWKSPage"),
                "https://localhost:9853/oauth2/jwks", "Expected is not found");
        Assert.assertEquals(fedAuthConfigMap.get("OAuth2TokenEPUrl"),
                "https://localhost:9853/oauth2/token",
                "Expected Token Endpoint URL is not found");
        Assert.assertEquals(fedAuthConfigMap.get("OAuth2UserInfoEPUrl"),
                "https://localhost:9853/oauth2/userinfo",
                "Expected User Info Endpoint URL is not found");

        //Extract provisioning configurations.
        ProvisioningConnectorConfig[] provisioningConfigs = idProvider.getProvisioningConnectorConfigs();

        Map<String, String> provisioningConfigMap = new HashMap<String, String>();
        for (ProvisioningConnectorConfig config : provisioningConfigs) {
            for (Property property: config.getProvisioningProperties()) {
                provisioningConfigMap.put(property.getName(), property.getValue());
            }
        }

        Assert.assertEquals(provisioningConfigMap.get("scimGroupEndpoint"),
                "https://localhost:9853/wso2/scim/Groups",
                "Expected SCIM Group Endpoint is not found");
        Assert.assertEquals(provisioningConfigMap.get("scimUserEndpoint"),
                "https://localhost:9853/wso2/scim/Users",
                "Expected SCIM User Endpoint is not found");
        Assert.assertEquals(provisioningConfigMap.get("scim2UserEndpoint"),
                "https://localhost:9853/scim2/Users",
                "Expected SCIM 2.0 User Endpoint is not found");
        Assert.assertEquals(provisioningConfigMap.get("scim2GroupEndpoint"),
                "https://localhost:9853/scim2/Groups",
                "Expected SCIM 2.0 Group Endpoint is not found");
    }

    @Test(groups = "wso2.is", description = "Test resident IDP config URLs in tenant mode")
    public void testResidentIdPConfigsTenantMode() throws Exception {

        ConfigurationContext configContext = ConfigurationContextFactory.createConfigurationContextFromFileSystem
                (null, null);
        loginManger = new AuthenticatorClient(isServer.getContextUrls().getBackEndUrl());
        String cookie = loginManger.login("admin@wso2.com", "admin",
                isServer.getInstance().getHosts().get("default"));
        idpMgtServiceClient = new IdentityProviderMgtServiceClient(cookie, isServer.getContextUrls().getBackEndUrl(),
                configContext);

        IdentityProvider residentIdP = idpMgtServiceClient.getResidentIdP();

        //Extract authenticator configurations for tenant.
        FederatedAuthenticatorConfig[] fedAuthConfigsTenant = residentIdP.getFederatedAuthenticatorConfigs();

        Map<String, String> fedAuthConfigTenantMap = new HashMap<String, String>();
        for (FederatedAuthenticatorConfig config : fedAuthConfigsTenant) {
            for (Property property: config.getProperties()) {
                fedAuthConfigTenantMap.put(property.getName(), property.getValue());
            }
        }

        Assert.assertEquals(fedAuthConfigTenantMap.get("OAuth1AccessTokenUrl"),
                "https://localhost:9853/oauth/access-token",
                "Expected OAuth10a Access Token URL is not found in the tenant mode");
        Assert.assertEquals(fedAuthConfigTenantMap.get("OAuth1AuthorizeUrl"),
                "https://localhost:9853/oauth/authorize-url",
                "Expected OAuth10a Authorize URL is not found in the tenant mode");
        Assert.assertEquals(fedAuthConfigTenantMap.get("OAuth1RequestTokenUrl"),
                "https://localhost:9853/oauth/request-token",
                "Expected OAuth10a Request Token URL is not found in the tenant mode");
        Assert.assertEquals(fedAuthConfigTenantMap.get("IdentityProviderUrl"),
                "https://localhost:9853/passivests",
                "Expected Passive STS URL is not found in the tenant mode");
        Assert.assertEquals(fedAuthConfigTenantMap.get("OpenIdUrl"),
                "https://localhost:9853/openidserver",
                "Expected OpenID Server URL is not found in the tenant mode");
        Assert.assertEquals(fedAuthConfigTenantMap.get("ECPUrl"),
                "https://localhost:9853/samlecp?tenantDomain=wso2.com",
                "Expected ECP URL is not found in the tenant mode");
        Assert.assertEquals(fedAuthConfigTenantMap.get("LogoutReqUrl"),
                "https://localhost:9853/samlsso?tenantDomain=wso2.com",
                "Expected Logout URL is not found in the tenant mode");
        Assert.assertEquals(fedAuthConfigTenantMap.get("ArtifactResolveUrl"),
                "https://localhost:9853/samlartresolve",
                "Expected Artifact Resolution URL is not found in the tenant mode");
        Assert.assertEquals(fedAuthConfigTenantMap.get("SSOUrl"),
                "https://localhost:9853/samlsso?tenantDomain=wso2.com",
                "Expected SSO URL is not found in the tenant mode");
        Assert.assertEquals(fedAuthConfigTenantMap.get("IDENTITY_PROVIDER_URL"),
                "https://localhost:9853/services/t/wso2.com/wso2carbon-sts",
                "Expected Security Token Service URL is not found in the tenant mode");
        Assert.assertEquals(fedAuthConfigTenantMap.get("OIDCWebFingerEPUrl"),
                "https://localhost:9853/.well-known/webfinger",
                "Expected Web finger Endpoint URL is not found in the tenant mode");
        Assert.assertEquals(fedAuthConfigTenantMap.get("OIDCCheckSessionEPUrl"),
                "https://localhost:9853/oidc/checksession",
                "Expected Session IFrame Endpoint URL is not found in the tenant mode");
        Assert.assertEquals(fedAuthConfigTenantMap.get("OAuth2IntrospectEPUrl"),
                "https://localhost:9853/t/wso2.com/oauth2/introspect",
                "Expected Token Introspection Endpoint URL is not found in the tenant mode");
        Assert.assertEquals(fedAuthConfigTenantMap.get("OAuth2RevokeEPUrl"),
                "https://localhost:9853/oauth2/revoke",
                "Expected Token Revocation Endpoint URL is not found in the tenant mode");
        Assert.assertEquals(fedAuthConfigTenantMap.get("OIDCLogoutEPUrl"),
                "https://localhost:9853/oidc/logout",
                "Expected Logout Endpoint URL not found in the tenant mode");
        Assert.assertEquals(fedAuthConfigTenantMap.get("OAuth2AuthzEPUrl"),
                "https://localhost:9853/oauth2/authorize",
                "Expected Authorization Endpoint URL is not found in the tenant mode");
        Assert.assertEquals(fedAuthConfigTenantMap.get("OAuth2DCREPUrl"),
                "https://localhost:9853/t/wso2.com/api/identity/oauth2/dcr/v1.1/register",
                "Expected Dynamic Client Registration Endpoint URL is not found in the tenant mode");
        Assert.assertEquals(fedAuthConfigTenantMap.get("OIDCDiscoveryEPUrl"),
                "https://localhost:9853/t/wso2.com/oauth2/oidcdiscovery",
                "Expected Dynamic Client Registration Endpoint URL is not found in the tenant mode");
        Assert.assertEquals(fedAuthConfigTenantMap.get("OAuth2JWKSPage"),
                "https://localhost:9853/t/wso2.com/oauth2/jwks",
                "Expected is not found in the tenant mode");
        Assert.assertEquals(fedAuthConfigTenantMap.get("OAuth2TokenEPUrl"),
                "https://localhost:9853/oauth2/token",
                "Expected Token Endpoint URL is not found in the tenant mode");
        Assert.assertEquals(fedAuthConfigTenantMap.get("OAuth2UserInfoEPUrl"),
                "https://localhost:9853/oauth2/userinfo",
                "Expected User Info Endpoint URL is not found in the tenant mode");

        //Extract provisioning configurations for tenant.
        ProvisioningConnectorConfig[] provisioningConfigsTenant = residentIdP.getProvisioningConnectorConfigs();

        Map<String, String> provisioningConfigTenantMap = new HashMap<String, String>();
        for (ProvisioningConnectorConfig config : provisioningConfigsTenant) {
            for (Property property: config.getProvisioningProperties()) {
                provisioningConfigTenantMap.put(property.getName(), property.getValue());
            }
        }

        Assert.assertEquals(provisioningConfigTenantMap.get("scimGroupEndpoint"),
                "https://localhost:9853/wso2/scim/Groups",
                "Expected SCIM Group Endpoint is not found in the tenant mode");
        Assert.assertEquals(provisioningConfigTenantMap.get("scimUserEndpoint"),
                "https://localhost:9853/wso2/scim/Users",
                "Expected SCIM User Endpoint is not found in the tenant mode");
        Assert.assertEquals(provisioningConfigTenantMap.get("scim2UserEndpoint"),
                "https://localhost:9853/t/wso2.com/scim2/Users",
                "Expected SCIM 2.0 User Endpoint is not found in the tenant mode");
        Assert.assertEquals(provisioningConfigTenantMap.get("scim2GroupEndpoint"),
                "https://localhost:9853/t/wso2.com/scim2/Groups",
                "Expected SCIM 2.0 Group Endpoint is not found in the tenant mode");
    }
}
