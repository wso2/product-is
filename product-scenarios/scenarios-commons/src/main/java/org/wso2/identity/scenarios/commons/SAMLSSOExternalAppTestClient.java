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

package org.wso2.identity.scenarios.commons;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;
import org.wso2.carbon.identity.application.common.model.xsd.InboundAuthenticationConfig;
import org.wso2.carbon.identity.application.common.model.xsd.InboundAuthenticationRequestConfig;
import org.wso2.carbon.identity.application.common.model.xsd.Property;
import org.wso2.carbon.identity.application.common.model.xsd.ServiceProvider;
import org.wso2.carbon.identity.sso.saml.stub.IdentitySAMLSSOConfigServiceIdentityException;
import org.wso2.carbon.identity.sso.saml.stub.types.SAMLSSOServiceProviderDTO;
import org.wso2.identity.scenarios.commons.SAML2SSOTestBase;
import org.wso2.identity.scenarios.commons.SAMLConfig;
import org.wso2.identity.scenarios.commons.TestConfig;
import org.wso2.identity.scenarios.commons.TestUserMode;
import org.wso2.identity.scenarios.commons.clients.application.mgt.ApplicationManagementServiceClient;

import java.io.IOException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import static org.wso2.identity.scenarios.commons.util.SSOUtil.getClaimMappings;

public class SAMLSSOExternalAppTestClient extends SAML2SSOTestBase {

    private static final Log log = LogFactory.getLog(SAMLSSOExternalAppTestClient.class);

    protected static final String USER_AGENT = "Apache-HttpClient/4.2.5 (java 1.5)";
    private static final String INBOUND_AUTH_TYPE = "samlsso";
    private static final String ATTRIBUTE_CS_INDEX_VALUE = "1239245949";
    private static final String ATTRIBUTE_CS_INDEX_NAME = "attrConsumServiceIndex";
    public static final String TENANT_DOMAIN_PARAM = "tenantDomain";

    private static final String SAML_SSO_INDEX_URL = "/%s/";
    protected static final String SAML_SSO_URL = "%s/samlsso";
    protected static final String SAML_IDP_SLO_URL = SAML_SSO_URL + "?slo=true";
    protected static final String SAML_SSO_LOGIN_URL = "/%s/samlsso?SAML2.HTTPBinding=%s";
    protected static final String COMMON_AUTH_URL = "%s/commonauth";
    protected static final String ACS_URL = "/%s/home.jsp";

    private static final String NAMEID_FORMAT = "urn:oasis:names:tc:SAML:1.1:nameid-format:emailAddress";
    private static final String LOGIN_URL = "/carbon/admin/login.jsp";

    private ApplicationManagementServiceClient applicationManagementServiceClient;
    private SAMLSSOServiceProviderDTO samlssoServiceProviderDTO;
    private String commonAuthUrl;
    private String webAppHostUrl;
    private String acsUrl;
    private String samlSSOLoginUrl;
    private String samlAppIndexUrl;

    public SAMLSSOExternalAppTestClient(String backendURL, String sessionCookie, String backendServiceURL, String webAppHost,
                                 ConfigurationContext configContext, SAMLConfig config) throws
            Exception {

        super(backendURL, backendServiceURL, sessionCookie, configContext);
        this.commonAuthUrl = String.format(COMMON_AUTH_URL, backendURL);
        this.applicationManagementServiceClient = new ApplicationManagementServiceClient(sessionCookie,
                backendServiceURL, configContext);
        this.webAppHostUrl = webAppHost;
        this.acsUrl = String.format(webAppHost + ACS_URL, config.getArtifact());
        this.samlSSOLoginUrl = String.format(webAppHost + SAML_SSO_LOGIN_URL, config.getArtifact()
                , config.getHttpBinding());
        this.samlAppIndexUrl = String.format(webAppHost + SAML_SSO_INDEX_URL, config.getArtifact(),
                config.getHttpBinding());
        this.samlssoServiceProviderDTO = createSsoServiceProviderDTO(config);
    }

    public String getSamlSSOIDPUrl() {
        return samlSSOIDPUrl;
    }

    public String getSamlIdpSloUrl() {
        return samlIdpSloUrl;
    }

    public String getCommonAuthUrl() {
        return commonAuthUrl;
    }

    public String getWebAppHostUrl() {
        return webAppHostUrl;
    }

    public String getAcsUrl() {
        return acsUrl;
    }

    public String getSamlSSOLoginUrl() {
        return samlSSOLoginUrl;
    }

    public String getSamlAppIndexUrl() {
        return samlAppIndexUrl;
    }

    public void clear() {

        ssoConfigServiceClient = null;
        applicationManagementServiceClient = null;
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
        requestConfig.setInboundAuthKey(config.getArtifact());

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

    public void deleteApplication(String appName) throws Exception {

        applicationManagementServiceClient.deleteApplication(appName);
    }


    public SAMLSSOServiceProviderDTO createSsoServiceProviderDTO(SAMLConfig config) {

        SAMLSSOServiceProviderDTO samlssoServiceProviderDTO = new SAMLSSOServiceProviderDTO();
        samlssoServiceProviderDTO.setIssuer(config.getArtifact());
        samlssoServiceProviderDTO.setAssertionConsumerUrls(new String[]{String.format(webAppHostUrl + ACS_URL,
                config.getArtifact())});
        samlssoServiceProviderDTO.setDefaultAssertionConsumerUrl(String.format(webAppHostUrl + ACS_URL, config
                .getArtifact()));
        samlssoServiceProviderDTO.setAttributeConsumingServiceIndex(ATTRIBUTE_CS_INDEX_VALUE);
        samlssoServiceProviderDTO.setNameIDFormat(NAMEID_FORMAT);
        samlssoServiceProviderDTO.setDoSignAssertions(config.isSigningEnabled());
        samlssoServiceProviderDTO.setDoSignResponse(config.isSigningEnabled());
        samlssoServiceProviderDTO.setDoSingleLogout(true);
        samlssoServiceProviderDTO.setLoginPageURL(LOGIN_URL);
        if (config.getClaimType() != TestConfig.ClaimType.NONE) {
            samlssoServiceProviderDTO.setEnableAttributeProfile(true);
            samlssoServiceProviderDTO.setEnableAttributesByDefault(true);
        }

        return samlssoServiceProviderDTO;
    }

    public boolean createSAMLconfigForServiceProvider() throws RemoteException, IdentitySAMLSSOConfigServiceIdentityException {
        return ssoConfigServiceClient.addServiceProvider(this.samlssoServiceProviderDTO);
    }

    public boolean removeServiceProvider(SAMLConfig config) throws RemoteException, IdentitySAMLSSOConfigServiceIdentityException {
        return ssoConfigServiceClient.removeServiceProvider(config.getArtifact());
    }


    public HttpResponse sendSAMLMessage(String url, String samlMsgKey, String samlMsgValue, SAMLConfig config,
                                        HttpClient httpClient) throws IOException {

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
