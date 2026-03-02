/*
 *  Copyright (c) 2014 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.identity.integration.test.application.mgt;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.config.Lookup;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.cookie.CookieSpecProvider;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.cookie.RFC6265CookieSpecProvider;
import org.json.JSONException;
import org.wso2.carbon.automation.engine.context.AutomationContext;
import org.wso2.carbon.automation.extensions.servers.carbonserver.MultipleServersManager;
import org.wso2.carbon.identity.application.common.model.idp.xsd.IdentityProvider;
import org.wso2.carbon.identity.application.common.model.xsd.ServiceProvider;
import org.wso2.carbon.identity.oauth.stub.dto.OAuthConsumerAppDTO;
import org.wso2.carbon.identity.sso.saml.stub.types.SAMLSSOServiceProviderDTO;
import org.wso2.carbon.identity.sso.saml.stub.types.SAMLSSOServiceProviderInfoDTO;
import org.wso2.carbon.integration.common.admin.client.AuthenticatorClient;
import org.wso2.carbon.user.mgt.stub.types.carbon.ClaimValue;
import org.wso2.identity.integration.common.clients.Idp.IdentityProviderMgtServiceClient;
import org.wso2.identity.integration.common.clients.UserManagementClient;
import org.wso2.identity.integration.common.clients.application.mgt.ApplicationManagementServiceClient;
import org.wso2.identity.integration.common.clients.oauth.OauthAdminClient;
import org.wso2.identity.integration.common.clients.sso.saml.SAMLSSOConfigServiceClient;
import org.wso2.identity.integration.common.utils.CarbonTestServerManager;
import org.wso2.identity.integration.common.utils.ISIntegrationTest;
import org.wso2.identity.integration.test.base.TestDataHolder;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.ApplicationModel;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.ApplicationResponseModel;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.OpenIDConnectConfiguration;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.SAML2ServiceProvider;
import org.wso2.identity.integration.test.rest.api.server.idp.v1.model.IdentityProviderPOSTRequest;
import org.wso2.identity.integration.test.restclients.IdpMgtRestClient;
import org.wso2.identity.integration.test.restclients.OAuth2RestClient;
import org.wso2.identity.integration.test.utils.CommonConstants;
import org.wso2.identity.integration.test.utils.IdentityConstants;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

public abstract class AbstractIdentityFederationTestCase extends ISIntegrationTest {

    private Map<Integer, ApplicationManagementServiceClient> applicationManagementServiceClients;
    private Map<Integer, IdentityProviderMgtServiceClient> identityProviderMgtServiceClients;
    private Map<Integer, SAMLSSOConfigServiceClient> samlSSOConfigServiceClients;
    private Map<Integer, OauthAdminClient> oauthAdminClients;
    private Map<Integer, OAuth2RestClient> applicationManagementRestClients;
    private Map<Integer, IdpMgtRestClient> identityProviderMgtRestClients;
    private Map<Integer, UserManagementClient> userManagementClients;
    protected Map<Integer, AutomationContext> automationContextMap;
    private MultipleServersManager manager;
    protected static final int DEFAULT_PORT = CommonConstants.IS_DEFAULT_HTTPS_PORT;

    public void initTest() throws Exception {

        super.init();
        TestDataHolder testDataHolder = TestDataHolder.getInstance();
        applicationManagementRestClients = new HashMap<>();
        identityProviderMgtRestClients = new HashMap<>();
        applicationManagementServiceClients = new HashMap<>();
        identityProviderMgtServiceClients = new HashMap<>();
        samlSSOConfigServiceClients = new HashMap<>();
        oauthAdminClients = new HashMap<>();
        userManagementClients = new HashMap<>();
        automationContextMap = testDataHolder.getAutomationContextMap();
        manager = testDataHolder.getManager();

        automationContextMap.put(0, isServer);
    }

    public void startCarbonServer(int portOffset, AutomationContext context, Map<String, String> startupParameters)
            throws Exception {

        startupParameters.put("-Doptimize", String.valueOf(false));
        automationContextMap.put(portOffset, context);
        CarbonTestServerManager server = new CarbonTestServerManager(context, System.getProperty("carbon.zip"),
                startupParameters);
        manager.startServers(server);
    }

    public void stopCarbonServer(int portOffset) throws Exception {

        manager.stopAllServers();
    }

    public void createServiceClients(int portOffset, String sessionCookie,
                                     IdentityConstants.ServiceClientType[] adminClients)
            throws Exception {

        if (adminClients == null) {
            return;
        }

        String serviceUrl = getSecureServiceUrl(portOffset,
                automationContextMap.get(portOffset).getContextUrls()
                        .getSecureServiceUrl());

        if (sessionCookie == null) {
            AuthenticatorClient authenticatorClient = new AuthenticatorClient(serviceUrl);

            sessionCookie = authenticatorClient.login(automationContextMap.get(portOffset).getSuperTenant().getTenantAdmin()
                            .getUserName(),
                    automationContextMap.get(portOffset).getSuperTenant()
                            .getTenantAdmin().getPassword(),
                    automationContextMap.get(portOffset).getDefaultInstance()
                            .getHosts().get("default"));
        }

        if (sessionCookie != null) {
            ConfigurationContext configContext = ConfigurationContextFactory.createConfigurationContextFromFileSystem
                    (null, null);
            for (IdentityConstants.ServiceClientType clientType : adminClients) {
                if (IdentityConstants.ServiceClientType.APPLICATION_MANAGEMENT.equals(clientType)) {
                    applicationManagementServiceClients.put(portOffset, new ApplicationManagementServiceClient
                            (sessionCookie, serviceUrl, configContext));
                } else if (IdentityConstants.ServiceClientType.IDENTITY_PROVIDER_MGT.equals(clientType)) {
                    identityProviderMgtServiceClients.put(portOffset, new IdentityProviderMgtServiceClient(sessionCookie,
                            serviceUrl));
                } else if (IdentityConstants.ServiceClientType.SAML_SSO_CONFIG.equals(clientType)) {
                    samlSSOConfigServiceClients.put(portOffset, new SAMLSSOConfigServiceClient(serviceUrl, sessionCookie));
                } else if (IdentityConstants.ServiceClientType.OAUTH_ADMIN.equals(clientType)) {
                    oauthAdminClients.put(portOffset, new OauthAdminClient(serviceUrl, sessionCookie));
                } else if (IdentityConstants.ServiceClientType.USER_MGT.equals(clientType)) {
                    userManagementClients.put(portOffset, new UserManagementClient(serviceUrl, sessionCookie));
                }
            }
        }
    }

    public void createServiceClients(int portOffset, IdentityConstants.ServiceClientType[] adminClients)
            throws Exception {

        if (adminClients == null) {
            return;
        }

        serverURL = automationContextMap.get(portOffset).getContextUrls().getSecureServiceUrl()
                .replace("/services", "");
        String serviceUrl = getSecureServiceUrl(portOffset, serverURL);

        for (IdentityConstants.ServiceClientType clientType : adminClients) {
            if (IdentityConstants.ServiceClientType.APPLICATION_MANAGEMENT.equals(clientType)) {
                applicationManagementRestClients.put(portOffset, new OAuth2RestClient(serviceUrl, tenantInfo));
            } else if (IdentityConstants.ServiceClientType.IDENTITY_PROVIDER_MGT.equals(clientType)) {
                identityProviderMgtRestClients.put(portOffset, new IdpMgtRestClient(serviceUrl, tenantInfo));
            }
        }
    }

    public void addUser(int portOffset, String username, String password, String[] roles,
                        String profileName, ClaimValue[] claims) throws Exception {

        userManagementClients.get(portOffset).addUser(username, password, roles, profileName, claims);
    }

    public void deleteUser(int portOffset, String username) throws Exception {

        userManagementClients.get(portOffset).deleteUser(username);
    }

    public HashSet<String> getUserList(int portOffset) throws Exception {

        return userManagementClients.get(portOffset).getUserList();
    }

    public void addServiceProvider(int portOffset, String applicationName) throws Exception {

        ServiceProvider serviceProvider = new ServiceProvider();
        serviceProvider.setApplicationName(applicationName);
        serviceProvider.setManagementApp(true);
        serviceProvider.setDescription("This is a test Service Provider");
        applicationManagementServiceClients.get(portOffset).createApplication(serviceProvider);
    }

    public String addApplication(int portOffset, ApplicationModel applicationModel) throws JSONException, IOException {

        return applicationManagementRestClients.get(portOffset).createApplication(applicationModel);
    }

    public ApplicationResponseModel getApplication(int portOffset, String appId) throws Exception {

        return applicationManagementRestClients.get(portOffset).getApplication(appId);
    }

    public OpenIDConnectConfiguration getOIDCInboundDetailsOfApplication(int portOffset, String appId) throws Exception {

        return applicationManagementRestClients.get(portOffset).getOIDCInboundDetails(appId);
    }

    public SAML2ServiceProvider getSAMLInboundDetailsOfApplication(int portOffset, String appId) throws Exception {

        return applicationManagementRestClients.get(portOffset).getSAMLInboundDetails(appId);
    }

    public void deleteApplication(int portOffset, String appId) throws Exception {

        applicationManagementRestClients.get(portOffset).deleteApplication(appId);
    }

    public ServiceProvider getServiceProvider(int portOffset, String applicationName)
            throws Exception {

        return applicationManagementServiceClients.get(portOffset).getApplication(applicationName);
    }

    public void updateServiceProvider(int portOffset, ServiceProvider serviceProvider)
            throws Exception {

        applicationManagementServiceClients.get(portOffset).updateApplicationData(serviceProvider);
    }

    public void deleteServiceProvider(int portOffset, String applicationName) throws Exception {

        applicationManagementServiceClients.get(portOffset).deleteApplication(applicationName);
    }

    public void addIdentityProvider(int portOffset, IdentityProvider identityProvider)
            throws Exception {

        identityProviderMgtServiceClients.get(portOffset).addIdP(identityProvider);
    }

    public String addIdentityProvider(int portOffset, IdentityProviderPOSTRequest idp) throws Exception {

        return identityProviderMgtRestClients.get(portOffset).createIdentityProvider(idp);
    }

    public void deleteIdp(int portOffset, String idpId) throws Exception {

        identityProviderMgtRestClients.get(portOffset).deleteIdp(idpId);
    }

    public IdentityProvider getIdentityProvider(int portOffset, String idPName) throws Exception {

        return identityProviderMgtServiceClients.get(portOffset).getIdPByName(idPName);
    }

    public void updateIdentityProvider(int portOffset, String oldIdPName,
                                       IdentityProvider identityProvider) throws Exception {

        identityProviderMgtServiceClients.get(portOffset).updateIdP(oldIdPName, identityProvider);
    }

    public void deleteIdentityProvider(int portOffset, String idPName) throws Exception {

        identityProviderMgtServiceClients.get(portOffset).deleteIdP(idPName);
    }

    public String createSAML2WebSSOConfiguration(int portOffset,
                                                 SAMLSSOServiceProviderDTO samlssoServiceProviderDTO)
            throws Exception {

        samlSSOConfigServiceClients.get(portOffset).addServiceProvider(samlssoServiceProviderDTO);
        SAMLSSOServiceProviderInfoDTO serviceProviders = samlSSOConfigServiceClients.get(portOffset).getServiceProviders();
        if (serviceProviders != null && serviceProviders.getServiceProviders() != null) {
            for (SAMLSSOServiceProviderDTO serviceProvider : serviceProviders.getServiceProviders()) {
                if (samlssoServiceProviderDTO.getIssuer().equals(serviceProvider.getIssuer())) {
                    return serviceProvider.getAttributeConsumingServiceIndex();
                }
            }
        }
        return null;
    }

     public OAuthConsumerAppDTO[] createOIDCConfiguration(int portOffset, OAuthConsumerAppDTO oAuthConsumerAppDTO)
            throws Exception {

        oauthAdminClients.get(portOffset).registerOAuthApplicationData(oAuthConsumerAppDTO);
        return oauthAdminClients.get(portOffset).getAllOAuthApplicationData();
    }

    public void deleteSAML2WebSSOConfiguration(int portOffset, String issuer) throws Exception {

        samlSSOConfigServiceClients.get(portOffset).removeServiceProvider(issuer);
    }

    public void deleteOIDCConfiguration(int portOffset, String consumerKey) throws Exception {

        oauthAdminClients.get(portOffset).removeOAuthApplicationData(consumerKey);
    }

    public String extractValueFromResponse(HttpResponse response, String key, int token)
            throws IOException {

        String value = null;
        String line = null;
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
        while ((line = bufferedReader.readLine()) != null) {
            if (line.contains(key)) {
                String[] tokens = line.split("'");
                value = tokens[token];
                break;
            }
        }
        bufferedReader.close();
        return value;
    }

    public Map<String, String> extractValuesFromResponse(HttpResponse response,
                                                         Map<String, Integer> keyMap)
            throws IOException {

        Map<String, String> values = new HashMap<>();
        String line = null;
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
        while ((line = bufferedReader.readLine()) != null && keyMap.size() > 0) {
            Iterator iterator = keyMap.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<String, Integer> entry = (Map.Entry<String, Integer>) iterator.next();
                if (line.contains(entry.getKey())) {
                    String[] tokens = line.split("'");
                    values.put(entry.getKey(), tokens[entry.getValue()]);
                    iterator.remove();
                }
            }
        }
        bufferedReader.close();
        return values;
    }

    public String getHeaderValue(HttpResponse response, String headerName) {

        Header[] headers = response.getAllHeaders();
        String headerValue = null;
        for (Header header : headers) {
            if (headerName.equals(header.getName())) {
                headerValue = header.getValue();
                break;
            }
        }
        return headerValue;
    }

    public boolean validateSAMLResponse(HttpResponse response, String userName) throws IOException {

        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
        StringBuffer buffer = new StringBuffer();
        String line = "";
        while ((line = bufferedReader.readLine()) != null) {
            buffer.append(line);
        }
        bufferedReader.close();
        return buffer.toString().contains("You are logged in as " + userName);
    }

    public void closeHttpConnection(HttpResponse response) throws IOException {

        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
        bufferedReader.close();
    }

    public HttpClient getNewHttpClientWithCookieStore() {

        Lookup<CookieSpecProvider> cookieSpecRegistry = RegistryBuilder.<CookieSpecProvider>create()
                .register(CookieSpecs.DEFAULT, new RFC6265CookieSpecProvider())
                .build();
        RequestConfig requestConfig = RequestConfig.custom()
                .setCookieSpec(CookieSpecs.DEFAULT)
                .build();
        return HttpClientBuilder.create()
                .setDefaultCookieSpecRegistry(cookieSpecRegistry)
                .setDefaultRequestConfig(requestConfig)
                .setDefaultCookieStore(new BasicCookieStore())
                .build();
    }

    private String getSecureServiceUrl(int portOffset, String baseUrl) {

        return baseUrl.replace("9853", String.valueOf(DEFAULT_PORT + portOffset)) + "/";
    }

}
