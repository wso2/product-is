/*
*  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.authenticator.stub.LoginAuthenticationExceptionException;
import org.wso2.carbon.um.ws.api.stub.ClaimValue;
import org.wso2.identity.scenarios.commons.clients.login.AuthenticatorClient;
import org.wso2.identity.scenarios.commons.clients.usermgt.remote.RemoteUserStoreManagerServiceClient;
import org.wso2.identity.scenarios.commons.data.DeploymentDataHolder;

import java.rmi.RemoteException;
import java.util.Base64;

import static org.wso2.identity.scenarios.commons.util.Constants.ClaimURIs.EMAIL_CLAIM_URI;
import static org.wso2.identity.scenarios.commons.util.Constants.ClaimURIs.FIRST_NAME_CLAIM_URI;
import static org.wso2.identity.scenarios.commons.util.Constants.ClaimURIs.LAST_NAME_CLAIM_URI;
import static org.wso2.identity.scenarios.commons.util.Constants.IS_HTTPS_URL;
import static org.wso2.identity.scenarios.commons.util.Constants.IS_SAMPLES_HTTP_URL;

/**
 * Base test case for IS scenario tests.
 */
public class ScenarioTestBase {

    private static final Logger LOG = LoggerFactory.getLogger(ScenarioTestBase.class);

    public static final String ADMIN_USERNAME = "admin";
    public static final String ADMIN_PASSWORD = "admin";

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

    public void init() throws Exception {

        backendURL = getDeploymentProperty(IS_HTTPS_URL);
        webAppHost = getDeploymentProperty(IS_SAMPLES_HTTP_URL);
        backendServiceURL = backendURL + SERVICES;
        configContext = ConfigurationContextFactory.createConfigurationContextFromFileSystem(null, null);
    }

    public void createUser(TestConfig config, RemoteUserStoreManagerServiceClient remoteUSMServiceClient, String profileName) {

        LOG.info("Creating User " + config.getUser().getUsername());
        try {
            // creating the user
            remoteUSMServiceClient.addUser(config.getUser().getTenantAwareUsername(), config.getUser().getPassword(),
                    null, getUserClaims(config.getUser().getSetUserClaims(), config),
                    profileName, true);
        } catch (Exception e) {
            LOG.error("Error while creating the user", e);
        }
    }

    public void deleteUser(TestConfig config, RemoteUserStoreManagerServiceClient remoteUSMServiceClient) {

        LOG.info("Deleting User " + config.getUser().getUsername());
        try {
            remoteUSMServiceClient.deleteUser(config.getUser().getTenantAwareUsername());
        } catch (Exception e) {
            LOG.error("Error while deleting the user", e);
        }
    }

    public ClaimValue[] getUserClaims(boolean setClaims, TestConfig config) {

        ClaimValue[] claimValues;

        if (setClaims) {
            claimValues = new ClaimValue[3];

            ClaimValue firstName = new ClaimValue();
            firstName.setClaimURI(FIRST_NAME_CLAIM_URI);
            firstName.setValue(config.getUser().getNickname());
            claimValues[0] = firstName;

            ClaimValue lastName = new ClaimValue();
            lastName.setClaimURI(LAST_NAME_CLAIM_URI);
            lastName.setValue(config.getUser().getUsername());
            claimValues[1] = lastName;

            ClaimValue email = new ClaimValue();
            email.setClaimURI(EMAIL_CLAIM_URI);
            email.setValue(config.getUser().getEmail());
            claimValues[2] = email;
        } else {
            claimValues = new ClaimValue[1];

            ClaimValue lastName = new ClaimValue();
            lastName.setClaimURI(LAST_NAME_CLAIM_URI);
            lastName.setValue(config.getUser().getUsername());
            claimValues[0] = lastName;
        }

        return claimValues;
    }
}
