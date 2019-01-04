/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.identity.scenarios.sso.test.saml;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.wso2.carbon.identity.application.common.model.xsd.InboundAuthenticationConfig;
import org.wso2.carbon.identity.application.common.model.xsd.InboundAuthenticationRequestConfig;
import org.wso2.carbon.identity.application.common.model.xsd.Property;
import org.wso2.carbon.identity.application.common.model.xsd.ServiceProvider;
import org.wso2.carbon.identity.sso.saml.stub.types.SAMLSSOServiceProviderDTO;
import org.wso2.identity.scenarios.commons.ScenarioTestBase;
import org.wso2.identity.scenarios.commons.TestConfig;
import org.wso2.identity.scenarios.commons.TestUserMode;
import org.wso2.identity.scenarios.commons.clients.application.mgt.ApplicationManagementServiceClient;
import org.wso2.identity.scenarios.commons.clients.sso.saml.SAMLSSOConfigServiceClient;
import org.wso2.identity.scenarios.commons.clients.usermgt.remote.RemoteUserStoreManagerServiceClient;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.wso2.identity.scenarios.commons.util.SSOUtil.getClaimMappings;

public abstract class AbstractSAMLSSOTestCase extends ScenarioTestBase {

    private static final Log log = LogFactory.getLog(AbstractSAMLSSOTestCase.class);

    protected static final String USER_AGENT = "Apache-HttpClient/4.2.5 (java 1.5)";
    private static final String INBOUND_AUTH_TYPE = "samlsso";
    private static final String ATTRIBUTE_CS_INDEX_VALUE = "1239245949";
    private static final String ATTRIBUTE_CS_INDEX_NAME = "attrConsumServiceIndex";
    public static final String TENANT_DOMAIN_PARAM = "tenantDomain";

    protected static final String SAML_SSO_URL = "%s/samlsso";
    protected static final String SAML_IDP_SLO_URL = SAML_SSO_URL + "?slo=true";
    protected static final String SAML_SSO_LOGIN_URL = "/%s/samlsso?SAML2.HTTPBinding=%s";
    protected static final String COMMON_AUTH_URL = "%s/commonauth";
    protected static final String ACS_URL = "/%s/home.jsp";

    private static final String NAMEID_FORMAT = "urn:oasis:names:tc:SAML:1.1:nameid-format:emailAddress";
    private static final String LOGIN_URL = "/carbon/admin/login.jsp";

    //Claim Uris
    private static final String profileName = "default";

    private ApplicationManagementServiceClient applicationManagementServiceClient;
    protected SAMLSSOConfigServiceClient ssoConfigServiceClient;
    private RemoteUserStoreManagerServiceClient remoteUSMServiceClient;
    protected CloseableHttpClient httpClient;

    protected String samlSSOIDPUrl;
    protected String samlIdpSloUrl;
    protected String commonAuthUrl;

    protected enum HttpBinding {

        HTTP_REDIRECT("HTTP-Redirect"),
        HTTP_POST("HTTP-POST");

        String binding;

        HttpBinding(String binding) {

            this.binding = binding;
        }
    }

    protected enum App {

        SUPER_TENANT_APP_WITH_SIGNING("travelocity.com", true),
        TENANT_APP_WITHOUT_SIGNING("travelocity.com-saml-tenantwithoutsigning", false);

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

    protected static class SAMLConfig extends TestConfig {

        private TestUserMode userMode;
        private HttpBinding httpBinding;
        private App app;

        protected SAMLConfig(TestUserMode userMode, User user, HttpBinding httpBinding, ClaimType claimType, App app) {
            super(userMode, user, claimType);

            this.userMode = userMode;
            this.httpBinding = httpBinding;
            this.app = app;
        }

        public TestUserMode getUserMode() {

            return userMode;
        }

        public App getApp() {

            return app;
        }

        public HttpBinding getHttpBinding() {

            return httpBinding;
        }

        @Override
        public String toString() {

            return "SAMLConfig[" +
                    "  userMode=" + userMode.name() +
                    ", user=" + super.getUser().getUsername() +
                    ", httpBinding=" + httpBinding +
                    ", claimType=" + super.getClaimType() +
                    ", app=" + app.getArtifact() +
                    ']';
        }
    }

    public void testInit() throws Exception {
        super.init();
        loginAndObtainSessionCookie();
        commonAuthUrl = String.format(COMMON_AUTH_URL, backendURL);
        samlSSOIDPUrl = String.format(SAML_SSO_URL, backendURL);
        samlIdpSloUrl = String.format(SAML_IDP_SLO_URL, backendURL);
        applicationManagementServiceClient = new ApplicationManagementServiceClient(sessionCookie, backendServiceURL,
                configContext);
        ssoConfigServiceClient = new SAMLSSOConfigServiceClient(backendServiceURL, sessionCookie);
        remoteUSMServiceClient = new RemoteUserStoreManagerServiceClient(backendServiceURL, sessionCookie);
        httpClient = HttpClients.createDefault();
    }

    public void testClear() throws Exception {

        ssoConfigServiceClient = null;
        applicationManagementServiceClient = null;
        remoteUSMServiceClient = null;
        httpClient = null;
    }

    protected void createUser(SAMLConfig config) {
        super.createUser(config, remoteUSMServiceClient, "default");
    }

    public void createApplication(SAMLConfig config, String appName) throws Exception {

        ServiceProvider serviceProvider = new ServiceProvider();
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

        serviceProvider.setInboundAuthenticationConfig(inboundAuthenticationConfig);
        applicationManagementServiceClient.updateApplicationData(serviceProvider);
    }

    protected void deleteUser(SAMLConfig config) {
        super.deleteUser(config, remoteUSMServiceClient);
    }

    public void deleteApplication(String appName) throws Exception {

        applicationManagementServiceClient.deleteApplication(appName);
    }


    public SAMLSSOServiceProviderDTO createSsoServiceProviderDTO(SAMLConfig config) {

        SAMLSSOServiceProviderDTO samlssoServiceProviderDTO = new SAMLSSOServiceProviderDTO();
        samlssoServiceProviderDTO.setIssuer(config.getApp().getArtifact());
        samlssoServiceProviderDTO.setAssertionConsumerUrls(new String[]{String.format(webAppHost + ACS_URL,
                config.getApp().getArtifact())});
        samlssoServiceProviderDTO.setDefaultAssertionConsumerUrl(String.format(webAppHost + ACS_URL, config.getApp()
                .getArtifact()));
        samlssoServiceProviderDTO.setAttributeConsumingServiceIndex(ATTRIBUTE_CS_INDEX_VALUE);
        samlssoServiceProviderDTO.setNameIDFormat(NAMEID_FORMAT);
        samlssoServiceProviderDTO.setDoSignAssertions(config.getApp().isSigningEnabled());
        samlssoServiceProviderDTO.setDoSignResponse(config.getApp().isSigningEnabled());
        samlssoServiceProviderDTO.setDoSingleLogout(true);
        samlssoServiceProviderDTO.setLoginPageURL(LOGIN_URL);
        if (config.getClaimType() != TestConfig.ClaimType.NONE) {
            samlssoServiceProviderDTO.setEnableAttributeProfile(true);
            samlssoServiceProviderDTO.setEnableAttributesByDefault(true);
        }

        return samlssoServiceProviderDTO;
    }

    public HttpResponse sendSAMLMessage(String url, String samlMsgKey, String samlMsgValue, SAMLConfig config)
            throws IOException {

        List<NameValuePair> urlParameters = new ArrayList<NameValuePair>();
        HttpPost post = new HttpPost(url);
        post.setHeader("User-Agent", USER_AGENT);
        urlParameters.add(new BasicNameValuePair(samlMsgKey, samlMsgValue));
        if (config.getUserMode() == TestUserMode.TENANT_ADMIN || config.getUserMode() == TestUserMode.TENANT_USER) {
            urlParameters.add(new BasicNameValuePair(TENANT_DOMAIN_PARAM, config.getUser().getTenantDomain()));
        }
        post.setEntity(new UrlEncodedFormEntity(urlParameters));

        return httpClient.execute(post);
    }
}
