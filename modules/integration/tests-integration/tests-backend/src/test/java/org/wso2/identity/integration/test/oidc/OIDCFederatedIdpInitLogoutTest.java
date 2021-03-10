package org.wso2.identity.integration.test.oidc;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.client.CookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import org.wso2.carbon.authenticator.stub.LoginAuthenticationExceptionException;
import org.wso2.carbon.automation.engine.context.AutomationContext;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.identity.application.common.model.idp.xsd.FederatedAuthenticatorConfig;
import org.wso2.carbon.identity.application.common.model.idp.xsd.IdentityProvider;
import org.wso2.carbon.identity.application.common.model.idp.xsd.JustInTimeProvisioningConfig;
import org.wso2.carbon.identity.application.common.model.idp.xsd.Property;
import org.wso2.carbon.identity.application.common.model.xsd.InboundAuthenticationRequestConfig;
import org.wso2.carbon.identity.application.common.model.xsd.ServiceProvider;
import org.wso2.carbon.identity.oauth.stub.dto.OAuthConsumerAppDTO;
import org.wso2.carbon.integration.common.admin.client.AuthenticatorClient;
import org.wso2.identity.integration.common.clients.Idp.IdentityProviderMgtServiceClient;
import org.wso2.identity.integration.common.clients.TenantManagementServiceClient;
import org.wso2.identity.integration.common.clients.application.mgt.ApplicationManagementServiceClient;
import org.wso2.identity.integration.common.clients.oauth.OauthAdminClient;
import org.wso2.identity.integration.common.utils.ISIntegrationTest;
import org.wso2.identity.integration.test.utils.IdentityConstants;
import org.wso2.identity.integration.test.utils.OAuth2Constant;

import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Map;

import javax.xml.xpath.XPathExpressionException;

public class OIDCFederatedIdpInitLogoutTest extends ISIntegrationTest {

    private Map<Integer, ApplicationManagementServiceClient> applicationManagementServiceClients;
    private Map<Integer, OauthAdminClient> oAuthAdminClients;
    private IdentityProviderMgtServiceClient identityProviderMgtServiceClient;

    private static final String FEDERATED_IS_PRIMARY_SP_NAME = "PrimaryIS";

    private static final String PRIMARY_IS_IDP_NAME = "PrimaryIS";
    private static final String PRIMARY_IS_IDP_AUTHENTICATOR_NAME_OIDC = "OpenIDConnectAuthenticator";
    private static final String PRIMARY_IS_IDP_CALLBACK_URL = "https://localhost:9853/t/primary.com/commonauth";
    //TODO: Enable endpoint in framework.
    private static final String PRIMARY_IS_BACK_CHANNEL_LOGOUT_ENDPOINT = "https://localhost:9853/t/primary" +
            ".com/identity/oidc/slo";

    private static final String SECONDARY_IS_AUTHORIZE_ENDPOINT = "https://localhost:9854/t/federated" +
            ".com/oauth2/authorize";
    private static final String SECONDARY_IS_TOKEN_ENDPOINT = "https://localhost:9854/t/federated.com/oauth2/token";
    private static final String SECONDARY_IS_LOGOUT_ENDPOINT = "https://localhost:9854/t/federated.com/oidc/logout";

    private static final String OIDC_APP_CLIENT_ID = "ClientID";
    private static final String OIDC_APP_CLIENT_SECRET = "ClientSecret";

    protected Log log = LogFactory.getLog(OIDCFederatedIdpInitLogoutTest.class);

    private TenantManagementServiceClient tenantServiceClient;
    private final AutomationContext context;

    private String fedISPrimSPClientID;
    private String fedISPrimSPClientSecret;

    private static final int PRIMARY_TENANT = 0;
    private static final int FEDERATED_TENANT = 1;

    @DataProvider(name = "configProvider")
    public static Object[][] configProvider() {

        return new Object[][]{{TestUserMode.SUPER_TENANT_ADMIN}};
    }

    @Factory(dataProvider = "configProvider")
    public OIDCFederatedIdpInitLogoutTest(TestUserMode userMode) throws Exception {

        context = new AutomationContext("IDENTITY", userMode);
    }

    @BeforeClass(alwaysRun = true)
    public void initTest() throws Exception {

        super.init();
        applicationManagementServiceClients = new HashMap<>();
        oAuthAdminClients = new HashMap<>();
        tenantServiceClient = new TenantManagementServiceClient(isServer.getContextUrls().getBackEndUrl(),
                sessionCookie);
        createTenants();

        backendURL = context.getContextUrls().getBackEndUrl();
        AuthenticatorClient logManger = new AuthenticatorClient(backendURL);
        String primaryTenantCookie = logManger
                .login("primaryAdmin@primary.com", "password", isServer.getInstance().getHosts().get("default"));
        String federatedTenantCookie = logManger
                .login("primaryAdmin@primary.com", "password", isServer.getInstance().getHosts().get("default"));

        createServiceClient(primaryTenantCookie, federatedTenantCookie);

        createPrimaryServiceProviderInSecondaryTenant();
        createIdentityProviderInPrimaryTenant();
        //TODO: createServiceProviderInPrimaryTenant()
        //TODO: createServiceProviderInSecondaryTenant()

    }

    /**
     * Creates two tenants, primary.com and federated.com.
     *
     * @throws Exception
     */
    private void createTenants() throws Exception {

        tenantServiceClient.addTenant("primary.com", "primaryAdmin", "password", "primaryAdmin@primary.com", "Luke",
                "Skywalker");
        tenantServiceClient.addTenant("federated.com", "federatedAdmin", "password", "federatedAdmin@federated.com",
                "Leia", "Organa");
        log.info(" ##### Tenant Created: " + tenantServiceClient.getTenant("primary.com").getTenantDomain());
        log.info(" ##### Tenant Created: " + tenantServiceClient.getTenant("federated.com").getTenantDomain());
    }

    /**
     * Create service clients for the tenants.
     *
     * @throws XPathExpressionException
     * @throws RemoteException
     * @throws LoginAuthenticationExceptionException
     */
    private void createServiceClient(String primaryTenantCookie, String federatedTenantCookie)
            throws XPathExpressionException, RemoteException {

        ConfigurationContext configContext = ConfigurationContextFactory.createConfigurationContextFromFileSystem
                (null, null);
        applicationManagementServiceClients
                .put(PRIMARY_TENANT, new ApplicationManagementServiceClient(primaryTenantCookie,
                        isServer.getContextUrls().getBackEndUrl(), configContext));
        applicationManagementServiceClients
                .put(FEDERATED_TENANT, new ApplicationManagementServiceClient(federatedTenantCookie,
                        isServer.getContextUrls().getBackEndUrl(), configContext));
        identityProviderMgtServiceClient =
                new IdentityProviderMgtServiceClient(primaryTenantCookie, isServer.getContextUrls().getBackEndUrl());
        oAuthAdminClients.put(PRIMARY_TENANT, new OauthAdminClient(isServer.getContextUrls().getBackEndUrl(),
                primaryTenantCookie));
        oAuthAdminClients.put(FEDERATED_TENANT, new OauthAdminClient(isServer.getContextUrls().getBackEndUrl(),
                federatedTenantCookie));
    }

    /**
     * Creates a service provider for primary tenant IS in the federated tenant IS.
     *
     * @throws Exception
     */
    private void createPrimaryServiceProviderInSecondaryTenant()
            throws Exception {

        addServiceProvider(FEDERATED_TENANT, FEDERATED_IS_PRIMARY_SP_NAME);
        ServiceProvider serviceProvider = getServiceProvider(FEDERATED_TENANT, FEDERATED_IS_PRIMARY_SP_NAME);

        HashMap<String, String> credentials =
                updateServiceProviderWithOIDCConfigs(FEDERATED_TENANT, FEDERATED_IS_PRIMARY_SP_NAME,
                        PRIMARY_IS_IDP_CALLBACK_URL, PRIMARY_IS_BACK_CHANNEL_LOGOUT_ENDPOINT, serviceProvider);

        fedISPrimSPClientID = credentials.get(OIDC_APP_CLIENT_ID);
        fedISPrimSPClientSecret = credentials.get(OIDC_APP_CLIENT_SECRET);

        updateServiceProvider(FEDERATED_TENANT, serviceProvider);

        serviceProvider = getServiceProvider(FEDERATED_TENANT, FEDERATED_IS_PRIMARY_SP_NAME);

        InboundAuthenticationRequestConfig[] configs = serviceProvider.getInboundAuthenticationConfig().
                getInboundAuthenticationRequestConfigs();
        boolean success = false;
        if (configs != null) {
            for (InboundAuthenticationRequestConfig config : configs) {
                if (fedISPrimSPClientID.equals(config.getInboundAuthKey()) && OAuth2Constant.OAUTH_2.equals(
                        config.getInboundAuthType())) {
                    success = true;
                    break;
                }
            }
        }

        Assert.assertTrue(success, "Failed to update PrimaryIS service provider with inbound OIDC configs in " +
                "secondary IS");

    }

    /**
     * Creates a identity provider in primary tenant IS.
     *
     * @throws Exception
     */
    private void createIdentityProviderInPrimaryTenant() throws Exception {

        IdentityProvider identityProvider = new IdentityProvider();
        identityProvider.setIdentityProviderName(PRIMARY_IS_IDP_NAME);

        FederatedAuthenticatorConfig oidcAuthnConfig = new FederatedAuthenticatorConfig();
        oidcAuthnConfig.setName(PRIMARY_IS_IDP_AUTHENTICATOR_NAME_OIDC);
        oidcAuthnConfig.setDisplayName("openidconnect");
        oidcAuthnConfig.setEnabled(true);
        oidcAuthnConfig.setProperties(getOIDCAuthnConfigProperties());
        identityProvider.setDefaultAuthenticatorConfig(oidcAuthnConfig);
        identityProvider.setFederatedAuthenticatorConfigs(new FederatedAuthenticatorConfig[]{oidcAuthnConfig});

        JustInTimeProvisioningConfig jitConfig = new JustInTimeProvisioningConfig();
        jitConfig.setProvisioningEnabled(true);
        jitConfig.setProvisioningUserStore("PRIMARY");
        identityProvider.setJustInTimeProvisioningConfig(jitConfig);

        identityProviderMgtServiceClient.addIdP(identityProvider);

        Assert.assertNotNull(identityProviderMgtServiceClient.getIdPByName(PRIMARY_IS_IDP_NAME), "Failed to " +
                "create " +
                "Identity Provider 'trustedIdP' in primary IS");
    }

    /**
     * Create a service provider in ApplicationManagementServiceClient for the given tenant.
     *
     * @param tenant          - Tenant to which the service provider needs to be added.
     * @param applicationName - Name of the application.
     * @throws Exception
     */
    public void addServiceProvider(int tenant, String applicationName) throws Exception {

        ServiceProvider serviceProvider = new ServiceProvider();
        serviceProvider.setApplicationName(applicationName);
        serviceProvider.setDescription("This is a test Service Provider");
        applicationManagementServiceClients.get(tenant).createApplication(serviceProvider);
    }

    /**
     * Retrieve the a service provider from ApplicationManagementServiceClient for the given tenant.
     *
     * @param tenant          - Tenant from where the service provider needs to be fetched.
     * @param applicationName - Name of the application.
     * @return
     * @throws Exception
     */
    public ServiceProvider getServiceProvider(int tenant, String applicationName)
            throws Exception {

        return applicationManagementServiceClients.get(tenant).getApplication(applicationName);
    }

    /**
     * Update a service provider in ApplicationManagementServiceClient for the given tenant.
     *
     * @param tenant          - Tenant where the service provider needs to be updated.
     * @param serviceProvider - Service provider which needs to be updated.
     * @throws Exception
     */
    public void updateServiceProvider(int tenant, ServiceProvider serviceProvider)
            throws Exception {

        applicationManagementServiceClients.get(tenant).updateApplicationData(serviceProvider);
    }

    /**
     * Update the given service provider with OIDC configurations.
     *
     * @param tenant               - Tenant where the service provider needs to be updated.
     * @param applicationName      - Name of the application.
     * @param callbackUrl          - Call back url.
     * @param backChannelLogoutUrl - Back channel logout url.
     * @param serviceProvider      - Service provider.
     * @return
     * @throws Exception
     */
    private HashMap<String, String> updateServiceProviderWithOIDCConfigs(int tenant,
                                                                         String applicationName,
                                                                         String callbackUrl,
                                                                         String backChannelLogoutUrl,
                                                                         ServiceProvider serviceProvider)
            throws Exception {

        String oidcAppClientId = null;
        String oidcAppClientSecret = null;
        OAuthConsumerAppDTO appDTO = new OAuthConsumerAppDTO();
        appDTO.setApplicationName(applicationName);
        appDTO.setCallbackUrl(callbackUrl);
        appDTO.setOAuthVersion(OAuth2Constant.OAUTH_VERSION_2);
        appDTO.setGrantTypes(OAuth2Constant.OAUTH2_GRANT_TYPE_AUTHORIZATION_CODE);
        appDTO.setBackChannelLogoutUrl(backChannelLogoutUrl);

        oAuthAdminClients.get(tenant).registerOAuthApplicationData(appDTO);
        OAuthConsumerAppDTO createdApp = oAuthAdminClients.get(tenant).getOAuthAppByName(applicationName);
        Assert.assertNotNull(createdApp, "Adding OIDC app failed.");

        InboundAuthenticationRequestConfig inboundAuthenticationRequestConfig = new
                InboundAuthenticationRequestConfig();
        inboundAuthenticationRequestConfig.setInboundAuthType(OAuth2Constant.OAUTH_2);

        if (StringUtils.isNotBlank(createdApp.getOauthConsumerKey())) {
            inboundAuthenticationRequestConfig.setInboundAuthKey(createdApp.getOauthConsumerKey());
            oidcAppClientId = createdApp.getOauthConsumerKey();
        }

        if (StringUtils.isNotBlank(createdApp.getOauthConsumerSecret())) {
            org.wso2.carbon.identity.application.common.model.xsd.Property property = new org.wso2.carbon.identity.
                    application.common.model.xsd.Property();
            property.setName(OAuth2Constant.OAUTH_CONSUMER_SECRET);
            property.setValue(createdApp.getOauthConsumerSecret());
            oidcAppClientSecret = createdApp.getOauthConsumerSecret();
            org.wso2.carbon.identity.application.common.model.xsd.Property[] properties = {property};
            inboundAuthenticationRequestConfig.setProperties(properties);
        }
        serviceProvider.getInboundAuthenticationConfig().setInboundAuthenticationRequestConfigs(new
                InboundAuthenticationRequestConfig[]{inboundAuthenticationRequestConfig});

        HashMap<String, String> credentials = new HashMap<>();
        credentials.put(OIDC_APP_CLIENT_ID, oidcAppClientId);
        credentials.put(OIDC_APP_CLIENT_SECRET, oidcAppClientSecret);
        return credentials;
    }

    /**
     * Get the OIDC authentication configuration properties for the Idp.
     *
     * @return
     */
    private Property[] getOIDCAuthnConfigProperties() {

        Property[] properties = new Property[8];
        Property property = new Property();
        property.setName(IdentityConstants.Authenticator.OIDC.IDP_NAME);
        property.setValue("oidcFedIdP");
        properties[0] = property;

        property = new Property();
        property.setName(IdentityConstants.Authenticator.OIDC.CLIENT_ID);
        property.setValue(fedISPrimSPClientID);
        properties[1] = property;

        property = new Property();
        property.setName(IdentityConstants.Authenticator.OIDC.CLIENT_SECRET);
        property.setValue(fedISPrimSPClientSecret);
        properties[2] = property;

        property = new Property();
        property.setName(IdentityConstants.Authenticator.OIDC.OAUTH2_AUTHZ_URL);
        property.setValue(SECONDARY_IS_AUTHORIZE_ENDPOINT);
        properties[3] = property;

        property = new Property();
        property.setName(IdentityConstants.Authenticator.OIDC.OAUTH2_TOKEN_URL);
        property.setValue(SECONDARY_IS_TOKEN_ENDPOINT);
        properties[4] = property;

        property = new Property();
        property.setName(IdentityConstants.Authenticator.OIDC.CALLBACK_URL);
        property.setValue(PRIMARY_IS_IDP_CALLBACK_URL);
        properties[5] = property;

        property = new Property();
        property.setName(IdentityConstants.Authenticator.OIDC.OIDC_LOGOUT_URL);
        property.setValue(SECONDARY_IS_LOGOUT_ENDPOINT);
        properties[6] = property;

        property = new Property();
        property.setName("commonAuthQueryParams");
        property.setValue("scope=" + OAuth2Constant.OAUTH2_SCOPE_OPENID_WITH_INTERNAL_LOGIN);
        properties[7] = property;
        return properties;
    }

    @Test(alwaysRun = true, groups = "wso2.is", description = "Testing create new tenant")
    public void testStart() {

        Assert.assertTrue(true);
        log.info("######################################### OIDC TEST ##############################");
    }

}
