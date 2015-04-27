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
import org.apache.catalina.LifecycleException;
import org.apache.catalina.core.StandardHost;
import org.apache.catalina.startup.Tomcat;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.wso2.carbon.automation.engine.context.AutomationContext;
import org.wso2.carbon.automation.extensions.servers.carbonserver.MultipleServersManager;
import org.wso2.carbon.identity.application.common.model.idp.xsd.IdentityProvider;
import org.wso2.carbon.identity.application.common.model.xsd.ServiceProvider;
import org.wso2.carbon.identity.sso.saml.stub.types.SAMLSSOServiceProviderDTO;
import org.wso2.carbon.identity.sso.saml.stub.types.SAMLSSOServiceProviderInfoDTO;
import org.wso2.carbon.integration.common.admin.client.AuthenticatorClient;
import org.wso2.identity.integration.common.clients.Idp.IdentityProviderMgtServiceClient;
import org.wso2.identity.integration.common.clients.application.mgt.ApplicationManagementServiceClient;
import org.wso2.identity.integration.common.clients.sso.saml.SAMLSSOConfigServiceClient;
import org.wso2.identity.integration.common.utils.CarbonTestServerManager;
import org.wso2.identity.integration.common.utils.ISIntegrationTest;
import org.wso2.identity.integration.test.utils.IdentityConstants;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public abstract class AbstractIdentityFederationTestCase extends ISIntegrationTest {

    private Map<Integer, ApplicationManagementServiceClient> applicationManagementServiceClients;
    private Map<Integer, IdentityProviderMgtServiceClient> identityProviderMgtServiceClients;
    private Map<Integer, SAMLSSOConfigServiceClient> samlSSOConfigServiceClients;
    protected Map<Integer, AutomationContext> automationContextMap;
    private Map<Integer, Tomcat> tomcatServers;
    private HttpClient httpClient;
    private MultipleServersManager manager;
    protected static final int DEFAULT_PORT = 9443;

    public void initTest() throws Exception {
        super.init();

        applicationManagementServiceClients = new HashMap<Integer, ApplicationManagementServiceClient>();
        identityProviderMgtServiceClients = new HashMap<Integer, IdentityProviderMgtServiceClient>();
        samlSSOConfigServiceClients = new HashMap<Integer, SAMLSSOConfigServiceClient>();
        automationContextMap = new HashMap<Integer, AutomationContext>();
        httpClient = new DefaultHttpClient();
        tomcatServers = new HashMap<Integer, Tomcat>();
        manager = new MultipleServersManager();

        automationContextMap.put(0, isServer);
    }

    public void startCarbonServer(int portOffset, AutomationContext context, Map<String, String> startupParameters)
            throws Exception {

        automationContextMap.put(portOffset, context);
        CarbonTestServerManager server = new CarbonTestServerManager(context, System.getProperty("carbon.zip"),
                                                                      startupParameters);
        manager.startServers(server);
    }

    public void stopCarbonServer(int portOffset) throws Exception {
        manager.stopAllServers();
    }

    public HttpClient getHttpClient() {
        return httpClient;
    }

    public void stopHttpClient() {
        httpClient.getConnectionManager().shutdown();
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
                }
            }
        }
    }

    public void addServiceProvider(int portOffset, String applicationName) throws Exception {
        ServiceProvider serviceProvider = new ServiceProvider();
        serviceProvider.setApplicationName(applicationName);
        serviceProvider.setDescription("This is a test Service Provider");
        applicationManagementServiceClients.get(portOffset).createApplication(serviceProvider);
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

    public void deleteSAML2WebSSOConfiguration(int portOffset, String issuer) throws Exception {
        samlSSOConfigServiceClients.get(portOffset).removeServiceProvider(issuer);
    }

    public void startTomcat(int port) throws LifecycleException {
        Tomcat tomcat = new Tomcat();
        tomcat.getService().setContainer(tomcat.getEngine());
        tomcat.setPort(port);
        tomcat.setBaseDir("");

        StandardHost stdHost = (StandardHost) tomcat.getHost();
        stdHost.setAppBase("");
        stdHost.setAutoDeploy(true);
        stdHost.setDeployOnStartup(true);
        stdHost.setUnpackWARs(true);
        tomcat.setHost(stdHost);

        setSystemProperties();
        tomcatServers.put(port, tomcat);
        tomcat.start();
    }

    public void stopTomcat(int port) throws LifecycleException, InterruptedException {
        tomcatServers.get(port).stop();
        tomcatServers.get(port).destroy();
        tomcatServers.remove(port);
        Thread.sleep(10000);
    }

    public void addWebAppToTomcat(int port, String webAppUrl, String webAppPath)
            throws LifecycleException {
        tomcatServers.get(port).addWebapp(tomcatServers.get(port).getHost(), webAppUrl, webAppPath);
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
        Map<String, String> values = new HashMap<String, String>();
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

    public boolean validateSAMLResponse(HttpResponse response) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
        StringBuffer buffer = new StringBuffer();
        String line = "";
        while ((line = bufferedReader.readLine()) != null) {
            buffer.append(line);
        }
        bufferedReader.close();
        return buffer.toString().contains("You are logged in as admin");
    }

    public void closeHttpConnection(HttpResponse response) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
        bufferedReader.close();
    }

    private void setSystemProperties() {
        URL resourceUrl = getClass().getResource(File.separator + "keystores" + File.separator + "products" + File.separator + "wso2carbon.jks");
        System.setProperty("javax.net.ssl.trustStore", resourceUrl.getPath());
        System.setProperty("javax.net.ssl.trustStorePassword", "wso2carbon");
        System.setProperty("javax.net.ssl.trustStoreType", "JKS");
    }

    private String getSecureServiceUrl(int portOffset, String baseUrl) {
        return baseUrl.replace("9443", String.valueOf(DEFAULT_PORT + portOffset)) + "/";
    }

}
