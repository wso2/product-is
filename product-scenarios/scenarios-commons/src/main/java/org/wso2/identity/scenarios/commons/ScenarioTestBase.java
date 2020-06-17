/*
*  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.identity.scenarios.commons;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.commons.collections.MapUtils;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.authenticator.stub.LoginAuthenticationExceptionException;
import org.wso2.carbon.um.ws.api.stub.ClaimValue;
import org.wso2.identity.scenarios.commons.clients.login.AuthenticatorClient;
import org.wso2.identity.scenarios.commons.clients.usermgt.remote.RemoteUserStoreManagerServiceClient;
import org.wso2.identity.scenarios.commons.data.DeploymentDataHolder;

import java.rmi.RemoteException;
import java.util.Base64;
import java.util.Map;

import static org.wso2.identity.scenarios.commons.util.Constants.DEFAULT_SOCKET_TIMEOUT_IN_SECONDS;
import static org.wso2.identity.scenarios.commons.util.Constants.IS_HTTPS_URL;
import static org.wso2.identity.scenarios.commons.util.Constants.IS_SAMPLES_HTTP_URL;

/**
 * Base test case for IS scenario tests.
 */
public class ScenarioTestBase {

    private static final Logger LOG = LoggerFactory.getLogger(ScenarioTestBase.class);

    public static final String ADMIN_USERNAME = "admin";
    public static final String ADMIN_PASSWORD = "admin";
    public static final String SUPER_TENANT_DOMAIN = "carbon.super";

    protected String backendURL;
    protected String backendServiceURL;
    protected String webAppHost;
    protected AuthenticatorClient loginClient;
    protected String sessionCookie;
    protected static final String SERVICES = "/services/";
    protected ConfigurationContext configContext;

    /**
     * This is a utility method to get a deployment detail.
     *
     * @return String value of the property identified by the key.
     */
    public static String getDeploymentProperty(String key) {

        return DeploymentDataHolder.getInstance().getProperties().getProperty(key);
    }

    public String getAuthzHeader() {

        Base64.Encoder encoder = java.util.Base64.getEncoder();
        String encodedHeader = encoder.encodeToString(String.join(":", ADMIN_USERNAME, ADMIN_PASSWORD).getBytes());
        return String.join(" ", "Basic", encodedHeader);
    }

    protected void loginAndObtainSessionCookie() throws LoginAuthenticationExceptionException, RemoteException {
        loginClient = new AuthenticatorClient(backendServiceURL);
        sessionCookie = loginClient.login(ADMIN_USERNAME, ADMIN_PASSWORD, null);
    }

    protected void loginAndObtainSessionCookie(String username, String password)
            throws LoginAuthenticationExceptionException, RemoteException {

        loginClient = new AuthenticatorClient(backendServiceURL);
        sessionCookie = loginClient.login(username, password, null);
    }

    public void init() throws Exception {

        backendURL = getDeploymentProperty(IS_HTTPS_URL);
        webAppHost = getDeploymentProperty(IS_SAMPLES_HTTP_URL);
        backendServiceURL = backendURL + SERVICES;
        configContext = ConfigurationContextFactory.createConfigurationContextFromFileSystem(null, null);
    }

    public boolean createUser(TestConfig config, RemoteUserStoreManagerServiceClient remoteUSMServiceClient, String
            profileName) {

        LOG.info("Creating User " + config.getUser().getUsername());
        try {
            // creating the user
            remoteUSMServiceClient.addUser(config.getUser().getTenantAwareUsername(), config.getUser().getPassword(),
                    null, getUserClaims(config),
                    profileName, true);
        } catch (Exception e) {
            LOG.error("Error while creating the user", e);
            return false;
        }
        return true;
    }

    public boolean deleteUser(TestConfig config, RemoteUserStoreManagerServiceClient remoteUSMServiceClient) {

        LOG.info("Deleting User " + config.getUser().getUsername());
        try {
            remoteUSMServiceClient.deleteUser(config.getUser().getTenantAwareUsername());
        } catch (Exception e) {
            LOG.error("Error while deleting the user", e);
            return false;
        }
        return true;
    }

    public CloseableHttpClient createHttpClient(int timeOutInSeconds) {
        RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(timeOutInSeconds * 1000).build();
        return HttpClientBuilder.create().setDefaultCookieStore(new BasicCookieStore())
                .setDefaultRequestConfig(requestConfig).build();
    }

    public CloseableHttpClient createHttpClient() {
        return createHttpClient(DEFAULT_SOCKET_TIMEOUT_IN_SECONDS);
    }

    public ClaimValue[] getUserClaims(TestConfig config) {

        ClaimValue[] claimValues = null;

        if (MapUtils.isNotEmpty(config.getUser().getUserClaims())) {

            Map<String, String> userClaims = config.getUser().getUserClaims();
            claimValues = new ClaimValue[userClaims.size()];
            int claimNo = 0;
            for (Map.Entry entry: userClaims.entrySet()) {
                ClaimValue claimValue = new ClaimValue();
                claimValue.setClaimURI(entry.getKey().toString());
                claimValue.setValue(entry.getValue().toString());
                claimValues[claimNo] = claimValue;
                claimNo++;
            }

        }
        return claimValues;
    }
}
