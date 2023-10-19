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

import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.config.Lookup;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.cookie.CookieSpecProvider;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.cookie.RFC6265CookieSpecProvider;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.identity.application.common.model.xsd.Claim;
import org.wso2.carbon.identity.application.common.model.xsd.ClaimMapping;
import org.wso2.carbon.identity.application.common.model.xsd.InboundAuthenticationConfig;
import org.wso2.carbon.identity.application.common.model.xsd.InboundAuthenticationRequestConfig;
import org.wso2.carbon.identity.application.common.model.xsd.LocalAndOutboundAuthenticationConfig;
import org.wso2.carbon.identity.application.common.model.xsd.Property;
import org.wso2.carbon.identity.application.common.model.xsd.RequestPathAuthenticatorConfig;
import org.wso2.carbon.identity.application.common.model.xsd.ServiceProvider;
import org.wso2.carbon.identity.sso.saml.stub.types.SAMLSSOServiceProviderDTO;
import org.wso2.carbon.um.ws.api.stub.ClaimValue;
import org.wso2.identity.integration.common.clients.application.mgt.ApplicationManagementServiceClient;
import org.wso2.identity.integration.common.clients.sso.saml.SAMLSSOConfigServiceClient;
import org.wso2.identity.integration.common.clients.usermgt.remote.RemoteUserStoreManagerServiceClient;
import org.wso2.identity.integration.common.utils.ISIntegrationTest;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.AdvancedApplicationConfiguration;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.ApplicationModel;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.AuthenticationSequence;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.ClaimConfiguration;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.ClaimConfiguration.DialectEnum;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.ClaimMappings;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.RequestedClaimConfiguration;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.SAML2Configuration;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.SAML2ServiceProvider;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.SAMLAssertionConfiguration;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.SAMLAttributeProfile;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.SAMLResponseSigning;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.SingleLogoutProfile;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.SingleSignOnProfile;
import org.wso2.identity.integration.test.rest.api.user.common.model.Email;
import org.wso2.identity.integration.test.rest.api.user.common.model.Name;
import org.wso2.identity.integration.test.rest.api.user.common.model.UserObject;
import org.wso2.identity.integration.test.restclients.OAuth2RestClient;
import org.wso2.identity.integration.test.restclients.SCIM2RestClient;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public abstract class AbstractSAMLSSOTestCase extends ISIntegrationTest {

    private static final Log log = LogFactory.getLog(AbstractSAMLSSOTestCase.class);

    protected static final String USER_AGENT = "Apache-HttpClient/4.2.5 (java 1.5)";
    private static final String INBOUND_AUTH_TYPE = "samlsso";
    private static final String ATTRIBUTE_CS_INDEX_VALUE = "1239245949";
    private static final String ATTRIBUTE_CS_INDEX_NAME = "attrConsumServiceIndex";
    protected static final String SAML = "saml";
    protected static final String SAML_SSO_URL = "https://localhost:9853/samlsso";
    protected static final String SAML_IDP_SLO_URL = SAML_SSO_URL + "?slo=true";
    protected static final String SAML_SSO_LOGIN_URL = "http://localhost:8490/%s/samlsso?SAML2.HTTPBinding=%s";
    protected static final String COMMON_AUTH_URL = "https://localhost:9853/commonauth";
    protected static final String ACS_URL = "http://localhost:8490/%s/home.jsp";

    private static final String NAMEID_FORMAT = "urn:oasis:names:tc:SAML:1.1:nameid-format:emailAddress";
    private static final String LOGIN_URL = "/carbon/admin/login.jsp";

    protected static final String SAML_ECP_SSO_URL = "https://localhost:9853/samlecp";
    protected static final String SAML_ECP_ACS_URL = "https://localhost/ECP-SP/SAML2/ECP";
    //Claim Uris
    private static final String firstNameClaimURI = "http://wso2.org/claims/givenname";
    private static final String lastNameClaimURI = "http://wso2.org/claims/lastname";
    private static final String emailClaimURI = "http://wso2.org/claims/emailaddress";

    private static final String profileName = "default";
    private static Boolean samlArtResolve = false;

    private ApplicationManagementServiceClient applicationManagementServiceClient;
    protected SAMLSSOConfigServiceClient ssoConfigServiceClient;
    protected SCIM2RestClient scim2RestClient;
    protected OAuth2RestClient applicationMgtRestClient;
    private RemoteUserStoreManagerServiceClient remoteUSMServiceClient;
    protected HttpClient httpClient;
    protected Lookup<CookieSpecProvider> cookieSpecRegistry;
    protected RequestConfig requestConfig;


    protected enum HttpBinding {

        HTTP_REDIRECT("HTTP-Redirect"),
        HTTP_POST("HTTP-POST"),
        HTTP_SOAP("SOAP");

        String binding;

        HttpBinding(String binding) {

            this.binding = binding;
        }
    }

    protected enum ClaimType {

        LOCAL, CUSTOM, NONE
    }

    protected enum User {

        SUPER_TENANT_USER("samluser1", "samluser1", "carbon.super", "samluser1", "samluser1@abc.com", "samlnickuser1",
                true),
        TENANT_USER("samluser2@wso2.com", "samluser2", "wso2.com", "samluser2", "samluser2@abc.com", "samlnickuser2",
                true),
        SUPER_TENANT_USER_WITHOUT_MANDATORY_CLAIMS("samluser3", "samluser3", "carbon.super", "samluser3",
                "providedClaimValue", "providedClaimValue", false),
        TENANT_USER_WITHOUT_MANDATORY_CLAIMS("samluser4@wso2.com", "samluser4", "wso2.com", "samluser4",
                "providedClaimValue", "providedClaimValue", false);

        private String username;
        private String password;
        private String tenantDomain;
        private String tenantAwareUsername;
        private String email;
        private String nickname;
        private boolean setUserClaims;

        User(String username, String password, String tenantDomain, String tenantAwareUsername, String email,
             String nickname, boolean setUserClaims) {

            this.username = username;
            this.password = password;
            this.tenantDomain = tenantDomain;
            this.tenantAwareUsername = tenantAwareUsername;
            this.email = email;
            this.nickname = nickname;
            this.setUserClaims = setUserClaims;
        }

        public String getUsername() {

            return username;
        }

        public String getPassword() {

            return password;
        }

        public String getTenantDomain() {

            return tenantDomain;
        }

        public String getTenantAwareUsername() {

            return tenantAwareUsername;
        }

        public String getEmail() {

            return email;
        }

        public String getNickname() {

            return nickname;
        }

        public boolean getSetUserClaims() {

            return setUserClaims;
        }
    }

    protected enum App {

        SUPER_TENANT_APP_WITH_SIGNING("travelocity.com", true),
        TENANT_APP_WITHOUT_SIGNING("travelocity.com-saml-tenantwithoutsigning", false),
        SUPER_TENANT_APP_WITHOUT_SIGNING("travelocity.com-saml-supertenantwithoutsigning", false),
        SUPER_TENANT_APP_WITH_SAMLARTIFACT_CONFIG("travelocity.com-saml-artifactresolving", false),
        TENANT_APP_WITH_SAMLARTIFACT_CONFIG("travelocity.com-saml-tenant-artifactresolving", false),
        ECP_APP("https://localhost/ecp-sp", false);

        private String artifact;
        private boolean signingEnabled;

        App(String artifact, boolean signingEnabled) {

            this.artifact = artifact;
            this.signingEnabled = signingEnabled;
        }

        public String getArtifact() {

            return artifact;
        }

        public boolean isSigningEnabled() {

            return signingEnabled;
        }
    }

    protected static class SAMLConfig {

        private TestUserMode userMode;
        private User user;
        private HttpBinding httpBinding;
        private ClaimType claimType;
        private App app;

        protected SAMLConfig(TestUserMode userMode, User user, HttpBinding httpBinding, ClaimType claimType, App app) {

            this.userMode = userMode;
            this.user = user;
            this.httpBinding = httpBinding;
            this.claimType = claimType;
            this.app = app;
        }

        public TestUserMode getUserMode() {

            return userMode;
        }

        public App getApp() {

            return app;
        }

        public User getUser() {

            return user;
        }

        public ClaimType getClaimType() {

            return claimType;
        }

        public HttpBinding getHttpBinding() {

            return httpBinding;
        }

        @Override
        public String toString() {

            return "SAMLConfig[" +
                    ", userMode=" + userMode.name() +
                    ", user=" + user.getUsername() +
                    ", httpBinding=" + httpBinding +
                    ", claimType=" + claimType +
                    ", app=" + app.getArtifact() +
                    ']';
        }
    }

    public void testInit() throws Exception {

        ConfigurationContext configContext = ConfigurationContextFactory
                .createConfigurationContextFromFileSystem(null, null);
        applicationManagementServiceClient = new ApplicationManagementServiceClient(sessionCookie, backendURL,
                configContext);
        ssoConfigServiceClient = new SAMLSSOConfigServiceClient(backendURL, sessionCookie);
        remoteUSMServiceClient = new RemoteUserStoreManagerServiceClient(backendURL, sessionCookie);
        scim2RestClient = new SCIM2RestClient(serverURL, tenantInfo);
        applicationMgtRestClient = new OAuth2RestClient(serverURL, tenantInfo);

        cookieSpecRegistry = RegistryBuilder.<CookieSpecProvider>create()
                .register(CookieSpecs.DEFAULT, new RFC6265CookieSpecProvider())
                .build();
        requestConfig = RequestConfig.custom()
                .setCookieSpec(CookieSpecs.DEFAULT)
                .build();
        httpClient = HttpClientBuilder.create().setDefaultCookieStore(new BasicCookieStore())
                .setDefaultCookieSpecRegistry(cookieSpecRegistry)
                .setDefaultRequestConfig(requestConfig)
                .build();
    }

    public void testClear() throws Exception {

        ssoConfigServiceClient = null;
        applicationManagementServiceClient = null;
        remoteUSMServiceClient = null;
        applicationMgtRestClient = null;
        scim2RestClient = null;
        httpClient = null;
    }

    public void createUser(SAMLConfig config) {

        log.info("Creating User " + config.getUser().getUsername());
        try {
            // creating the user
            remoteUSMServiceClient.addUser(config.getUser().getTenantAwareUsername(), config.getUser().getPassword(),
                    null, getUserClaims(config.getUser().getSetUserClaims(), config),
                    profileName, true);
        } catch (Exception e) {
            log.error("Error while creating the user", e);
        }
    }

    public String addUser(SAMLConfig config) throws Exception {

        log.info("Creating User " + config.getUser().getUsername());
        UserObject user = new UserObject()
                .userName(config.getUser().getTenantAwareUsername())
                .password(config.getUser().getPassword());

        if (config.getUser().getSetUserClaims()) {
            user.setName(new Name()
                    .givenName(config.getUser().getNickname())
                    .familyName(config.getUser().getUsername()));
            user.addEmail(new Email().value(config.getUser().getEmail()));
        } else {
            user.setName(new Name().familyName(config.getUser().getUsername()));
        }
        return scim2RestClient.createUser(user);
    }

    /**
     * Create application for SAML artifact resolve.
     *
     * @param config  SAML config.
     * @param appName Application name.
     * @throws Exception If error occurs while creating the application.
     */
    public void createApplicationForSAMLArtResolve(SAMLConfig config, String appName) throws Exception {

        samlArtResolve = true;
        createApplication(config, appName);
    }

    public String addApplication(SAMLConfig config, String appName) throws JSONException, IOException {

        ApplicationModel applicationModel = new ApplicationModel()
                .name(appName)
                .description("This is a test Service Provider")
                .claimConfiguration(getClaimConfigurations());

        if (config.httpBinding.equals(HttpBinding.HTTP_SOAP)) {
            applicationModel.setAuthenticationSequence(new AuthenticationSequence()
                    .addRequestPathAuthenticatorsItem("BasicAuthRequestPathAuthenticator"));
        }

        if (samlArtResolve) {
            applicationModel.setAdvancedConfigurations(new AdvancedApplicationConfiguration()
                    .skipLoginConsent(true));
        }

        return applicationMgtRestClient.createApplication(applicationModel);
    }

    public void createApplication(SAMLConfig config, String appName) throws Exception {

        ServiceProvider serviceProvider = new ServiceProvider();
        createApplication(serviceProvider, config, appName);
    }

    public ServiceProvider createApplication(ServiceProvider serviceProvider,
                                             SAMLConfig config, String appName) throws Exception {

        serviceProvider.setApplicationName(appName);
        serviceProvider.setDescription("This is a test Service Provider");
        applicationManagementServiceClient.createApplication(serviceProvider);

        serviceProvider = applicationManagementServiceClient.getApplication(appName);

        serviceProvider.getClaimConfig().setClaimMappings(getClaimMappings());

        InboundAuthenticationRequestConfig requestConfig = new InboundAuthenticationRequestConfig();
        requestConfig.setInboundAuthType(INBOUND_AUTH_TYPE);
        requestConfig.setInboundAuthKey(config.getApp().getArtifact());

        Property attributeConsumerServiceIndexProp = new Property();
        attributeConsumerServiceIndexProp.setName(ATTRIBUTE_CS_INDEX_NAME);
        attributeConsumerServiceIndexProp.setValue(ATTRIBUTE_CS_INDEX_VALUE);
        requestConfig.setProperties(new Property[]{attributeConsumerServiceIndexProp});

        InboundAuthenticationConfig inboundAuthenticationConfig = new InboundAuthenticationConfig();
        inboundAuthenticationConfig.setInboundAuthenticationRequestConfigs(
                new InboundAuthenticationRequestConfig[]{requestConfig});

        if (config.httpBinding.equals(HttpBinding.HTTP_SOAP)) {
            RequestPathAuthenticatorConfig requestPathAuthenticatorConfig = new RequestPathAuthenticatorConfig();
            requestPathAuthenticatorConfig.setName("BasicAuthRequestPathAuthenticator");
            serviceProvider.setRequestPathAuthenticatorConfigs(
                    new RequestPathAuthenticatorConfig[]{requestPathAuthenticatorConfig});
        }

        if (samlArtResolve) {
        LocalAndOutboundAuthenticationConfig localAndOutboundAuthenticationConfig =
                new LocalAndOutboundAuthenticationConfig();
        localAndOutboundAuthenticationConfig.setSkipConsent(true);
        serviceProvider.setLocalAndOutBoundAuthenticationConfig(localAndOutboundAuthenticationConfig);
        }
        serviceProvider.setInboundAuthenticationConfig(inboundAuthenticationConfig);
        applicationManagementServiceClient.updateApplicationData(serviceProvider);
        return serviceProvider;
    }

    public void updateServiceProvider(ServiceProvider serviceProvider, ClaimMapping[] claimMappings) throws Exception {

        serviceProvider.getClaimConfig().setClaimMappings(claimMappings);
        applicationManagementServiceClient.updateApplicationData(serviceProvider);
    }

    public void deleteUser(String userId) throws IOException {

        scim2RestClient.deleteUser(userId);
    }

    public void deleteUser(SAMLConfig config) {

        log.info("Deleting User " + config.getUser().getUsername());
        try {
            remoteUSMServiceClient.deleteUser(config.getUser().getTenantAwareUsername());
        } catch (Exception e) {
            log.error("Error while deleting the user", e);
        }
    }

    public void deleteApp(String appId) throws Exception {

        applicationMgtRestClient.deleteApplication(appId);
    }

    public void deleteApplication(String appName) throws Exception {

        applicationManagementServiceClient.deleteApplication(appName);
    }

    public ClaimValue[] getUserClaims(boolean setClaims, SAMLConfig config) {

        ClaimValue[] claimValues;

        if (setClaims) {
            claimValues = new ClaimValue[3];

            ClaimValue firstName = new ClaimValue();
            firstName.setClaimURI(firstNameClaimURI);
            firstName.setValue(config.getUser().getNickname());
            claimValues[0] = firstName;

            ClaimValue lastName = new ClaimValue();
            lastName.setClaimURI(lastNameClaimURI);
            lastName.setValue(config.getUser().getUsername());
            claimValues[1] = lastName;

            ClaimValue email = new ClaimValue();
            email.setClaimURI(emailClaimURI);
            email.setValue(config.getUser().getEmail());
            claimValues[2] = email;
        } else {
            claimValues = new ClaimValue[1];

            ClaimValue lastName = new ClaimValue();
            lastName.setClaimURI(lastNameClaimURI);
            lastName.setValue(config.getUser().getUsername());
            claimValues[0] = lastName;
        }

        return claimValues;
    }

    public ClaimMapping[] getClaimMappings() {

        List<ClaimMapping> claimMappingList = new ArrayList<ClaimMapping>();

        Claim firstNameClaim = new Claim();
        firstNameClaim.setClaimUri(firstNameClaimURI);
        ClaimMapping firstNameClaimMapping = new ClaimMapping();
        firstNameClaimMapping.setRequested(true);
        firstNameClaimMapping.setLocalClaim(firstNameClaim);
        firstNameClaimMapping.setRemoteClaim(firstNameClaim);
        claimMappingList.add(firstNameClaimMapping);

        Claim lastNameClaim = new Claim();
        lastNameClaim.setClaimUri(lastNameClaimURI);
        ClaimMapping lastNameClaimMapping = new ClaimMapping();
        lastNameClaimMapping.setRequested(true);
        lastNameClaimMapping.setLocalClaim(lastNameClaim);
        lastNameClaimMapping.setRemoteClaim(lastNameClaim);
        claimMappingList.add(lastNameClaimMapping);

        Claim emailClaim = new Claim();
        emailClaim.setClaimUri(emailClaimURI);
        ClaimMapping emailClaimMapping = new ClaimMapping();
        emailClaimMapping.setRequested(true);
        emailClaimMapping.setLocalClaim(emailClaim);
        emailClaimMapping.setRemoteClaim(emailClaim);
        claimMappingList.add(emailClaimMapping);

        return claimMappingList.toArray(new ClaimMapping[claimMappingList.size()]);
    }

    public ClaimConfiguration getClaimConfigurations() {

        return new ClaimConfiguration()
                .dialect(DialectEnum.LOCAL)
                .addClaimMappingsItem(new ClaimMappings()
                        .applicationClaim(firstNameClaimURI)
                        .localClaim(new org.wso2.identity.integration.test.rest.api.server.application.management
                                .v1.model.Claim().uri(firstNameClaimURI)))
                .addClaimMappingsItem(new ClaimMappings()
                        .applicationClaim(lastNameClaimURI)
                        .localClaim(new org.wso2.identity.integration.test.rest.api.server.application.management
                                .v1.model.Claim().uri(lastNameClaimURI)))
                .addClaimMappingsItem(new ClaimMappings()
                        .applicationClaim(emailClaimURI)
                        .localClaim(new org.wso2.identity.integration.test.rest.api.server.application.management.
                                v1.model.Claim().uri(emailClaimURI)))
                .addRequestedClaimsItem(new RequestedClaimConfiguration()
                        .claim(new org.wso2.identity.integration.test.rest.api.server.application.management
                                .v1.model.Claim().uri(firstNameClaimURI)))
                .addRequestedClaimsItem(new RequestedClaimConfiguration()
                        .claim(new org.wso2.identity.integration.test.rest.api.server.application.management
                                .v1.model.Claim().uri(lastNameClaimURI)))
                .addRequestedClaimsItem(new RequestedClaimConfiguration()
                        .claim(new org.wso2.identity.integration.test.rest.api.server.application.management
                                .v1.model.Claim().uri(emailClaimURI)));
    }

    /**
     * @param config contains the details of the IDP initiated SSO enabled service provider application.
     * @return the created SAMLSSOServiceProviderDTO.
     */
    public SAML2Configuration getSAMLConfigurationsForIdPInit(SAMLConfig config){
        SAML2Configuration idpInitSamlConfig = getSAMLConfigurations(config);
        idpInitSamlConfig.getManualConfiguration().getSingleSignOnProfile().setEnableIdpInitiatedSingleSignOn(true);
        return idpInitSamlConfig;
    }

    /**
     * Create SAML SSO Service Provider DTO For SAML artifact binding.
     *
     * @param config contains the details of the SAML artifact binding enabled service provider application.
     * @return the created SAMLSSOServiceProviderDTO.
     */
    public SAMLSSOServiceProviderDTO createSsoSPDTOForSAMLartifactBinding(SAMLConfig config){
        SAMLSSOServiceProviderDTO idpInitSpDTO = createSsoSPDTO(config);
        idpInitSpDTO.setEnableSAML2ArtifactBinding(true);
        return idpInitSpDTO;
    }

    public SAML2Configuration getSAMLConfigurations(SAMLConfig config) {

        SAML2ServiceProvider serviceProvider = new SAML2ServiceProvider()
                .issuer(config.getApp().getArtifact())
                .addAssertionConsumerUrl(String.format(ACS_URL, config.getApp().getArtifact()))
                .defaultAssertionConsumerUrl(String.format(ACS_URL, config.getApp().getArtifact()))
                .singleLogoutProfile(new SingleLogoutProfile()
                        .enabled(true))
                .responseSigning(new SAMLResponseSigning()
                        .enabled(config.getApp().isSigningEnabled()))
                .singleSignOnProfile(new SingleSignOnProfile()
                        .assertion(new SAMLAssertionConfiguration().nameIdFormat(NAMEID_FORMAT))
                        .attributeConsumingServiceIndex(ATTRIBUTE_CS_INDEX_VALUE));

        if (config.getClaimType() != AbstractSAMLSSOTestCase.ClaimType.NONE) {
            serviceProvider.setAttributeProfile(new SAMLAttributeProfile()
                            .enabled(true)
                            .alwaysIncludeAttributesInResponse(true));
        }
        return new SAML2Configuration().manualConfiguration(serviceProvider);
    }

    public SAMLSSOServiceProviderDTO createSsoServiceProviderDTO(SAMLConfig config){
        return createSsoSPDTO(config);
    }

    private SAMLSSOServiceProviderDTO createSsoSPDTO(SAMLConfig config) {

        SAMLSSOServiceProviderDTO samlssoServiceProviderDTO = new SAMLSSOServiceProviderDTO();
        samlssoServiceProviderDTO.setIssuer(config.getApp().getArtifact());
        samlssoServiceProviderDTO.setAssertionConsumerUrls(new String[]{String.format(ACS_URL,
                config.getApp().getArtifact())});
        samlssoServiceProviderDTO.setDefaultAssertionConsumerUrl(String.format(ACS_URL, config.getApp().getArtifact()));
        samlssoServiceProviderDTO.setAttributeConsumingServiceIndex(ATTRIBUTE_CS_INDEX_VALUE);
        samlssoServiceProviderDTO.setNameIDFormat(NAMEID_FORMAT);
        samlssoServiceProviderDTO.setDoSignAssertions(config.getApp().isSigningEnabled());
        samlssoServiceProviderDTO.setDoSignResponse(config.getApp().isSigningEnabled());
        samlssoServiceProviderDTO.setDoSingleLogout(true);
        samlssoServiceProviderDTO.setLoginPageURL(LOGIN_URL);
        if (config.getClaimType() != AbstractSAMLSSOTestCase.ClaimType.NONE) {
            samlssoServiceProviderDTO.setEnableAttributeProfile(true);
            samlssoServiceProviderDTO.setEnableAttributesByDefault(true);
        }
        return samlssoServiceProviderDTO;
    }

    public SAMLSSOServiceProviderDTO createECPServiceProviderDTO(SAMLConfig config) {

        SAMLSSOServiceProviderDTO samlssoServiceProviderDTO = new SAMLSSOServiceProviderDTO();
        samlssoServiceProviderDTO.setIssuer(config.getApp().getArtifact());
        samlssoServiceProviderDTO.setAssertionConsumerUrls(new String[]{SAML_ECP_ACS_URL});
        samlssoServiceProviderDTO.setDefaultAssertionConsumerUrl(SAML_ECP_ACS_URL);
        samlssoServiceProviderDTO.setAttributeConsumingServiceIndex(ATTRIBUTE_CS_INDEX_VALUE);
        samlssoServiceProviderDTO.setNameIDFormat(NAMEID_FORMAT);
        samlssoServiceProviderDTO.setDoSignAssertions(config.getApp().isSigningEnabled());
        samlssoServiceProviderDTO.setDoSignResponse(config.getApp().isSigningEnabled());
        samlssoServiceProviderDTO.setDoSingleLogout(true);
        samlssoServiceProviderDTO.setLoginPageURL(LOGIN_URL);

        if (config.getClaimType() != AbstractSAMLSSOTestCase.ClaimType.NONE) {
            samlssoServiceProviderDTO.setEnableAttributeProfile(true);
            samlssoServiceProviderDTO.setEnableAttributesByDefault(true);
        }

        return samlssoServiceProviderDTO;
    }

    public HttpResponse sendSAMLMessage(String url, String samlMsgKey, String samlMsgValue, SAMLConfig config)
            throws IOException {

        List<NameValuePair> urlParameters = new ArrayList<NameValuePair>();
        HttpPost post = new HttpPost(getTenantQualifiedURL(url, tenantInfo.getDomain()));
        post.setHeader("User-Agent", USER_AGENT);
        urlParameters.add(new BasicNameValuePair(samlMsgKey, samlMsgValue));
        post.setEntity(new UrlEncodedFormEntity(urlParameters));

        return httpClient.execute(post);
    }
}
