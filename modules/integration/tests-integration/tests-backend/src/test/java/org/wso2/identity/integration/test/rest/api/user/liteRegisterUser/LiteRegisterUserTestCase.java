/*
 * Copyright (c) 2022, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.identity.integration.test.rest.api.user.liteRegisterUser;

import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.apache.commons.lang.ArrayUtils;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.identity.application.common.model.idp.xsd.FederatedAuthenticatorConfig;
import org.wso2.carbon.identity.application.common.model.idp.xsd.IdentityProvider;
import org.wso2.carbon.identity.application.common.model.idp.xsd.IdentityProviderProperty;
import org.wso2.carbon.integration.common.admin.client.AuthenticatorClient;
import org.wso2.carbon.integration.common.utils.mgt.ServerConfigurationManager;
import org.wso2.identity.integration.common.clients.ClaimManagementServiceClient;
import org.wso2.identity.integration.common.clients.Idp.IdentityProviderMgtServiceClient;
import org.wso2.identity.integration.test.rest.api.common.RESTTestBase;
import org.wso2.identity.integration.test.util.Utils;
import org.wso2.identity.integration.test.utils.OAuth2Constant;

import java.io.File;
import java.io.IOException;

import static io.restassured.RestAssured.given;

public class LiteRegisterUserTestCase extends RESTTestBase {
    public static final String LITE_USER_REGISTRATION_ENDPOINT = "/api/identity/user/v1.0/lite";
    private static final String ADMIN = "admin";
    public static final String ENABLE_LITE_SIGN_UP = "LiteRegistration.Enable";
    private String authenticatingUserName = "admin";
    private String authenticatingCredential = "admin";
    private static String LITE_USER_CLAIM_ID = "aHR0cDovL3dzbzIub3JnL2NsYWltcy9pZGVudGl0eS9pc0xpdGVVc2Vy";
    private static String USERNAME_CLAIM_ID = "aHR0cDovL3dzbzIub3JnL2NsYWltcy91c2VybmFtZQ";
    public static final String UPDATE_CLAIM_URI = "/api/server/v1/claim-dialects/local/claims";
    private IdentityProviderMgtServiceClient superTenantIDPMgtClient;
    private IdentityProviderMgtServiceClient tenantIDPMgtClient;
    private AuthenticatorClient logManager;
    private IdentityProvider superTenantResidentIDP;
    private ServerConfigurationManager serverConfigurationManager;
    private ClaimManagementServiceClient adminClient;
    private String secondaryTenantDomain;
    private String selfRegisterLiteURL;
    private String isServerBackendUrl;


    @BeforeClass(alwaysRun = true)
    public void testInit() throws Exception {

        super.init(TestUserMode.SUPER_TENANT_ADMIN);
        String carbonHome = Utils.getResidentCarbonHome();
        File defaultTomlFile = getDeploymentTomlFile(carbonHome);
        File emailLoginConfigFile = new File (getISResourceLocation() + File.separator +  "user"
                + File.separator + "enable_email_username_deployment.toml");

        serverConfigurationManager = new ServerConfigurationManager(isServer);
        serverConfigurationManager.applyConfigurationWithoutRestart(emailLoginConfigFile, defaultTomlFile, true);
        serverConfigurationManager.restartGracefully();

        super.init(TestUserMode.SUPER_TENANT_ADMIN);
        isServerBackendUrl = isServer.getContextUrls().getWebAppURLHttps();
        selfRegisterLiteURL = isServerBackendUrl + LITE_USER_REGISTRATION_ENDPOINT;
        secondaryTenantDomain = isServer.getTenantList().get(1);
        this.logManager = new AuthenticatorClient(backendURL);
        String tenantCookie = this.logManager.login(ADMIN, ADMIN, isServer.getInstance().getHosts().get("default"));
        superTenantIDPMgtClient = new IdentityProviderMgtServiceClient(sessionCookie, backendURL);
        adminClient = new ClaimManagementServiceClient(backendURL, sessionCookie);
        tenantIDPMgtClient = new IdentityProviderMgtServiceClient(tenantCookie, backendURL);
        superTenantResidentIDP = superTenantIDPMgtClient.getResidentIdP();
    }

    @AfterClass(alwaysRun = true)
    public void endTest() throws Exception {

        updateResidentIDPProperty(superTenantResidentIDP, ENABLE_LITE_SIGN_UP, "false", true);
        serverConfigurationManager.restoreToLastConfiguration();
    }

    @Test(alwaysRun = true, groups = "wso2.is", description = "Lite user registration endpoint before enabling without authorization")
    public void liteUserRegistrationWithoutAuth() throws IOException {

        HttpClient client = HttpClientBuilder.create().build();
        String data = "{\"email\": \"lanka@wso2.com\",\"realm\": \"PRIMARY\",\"preferredChannel\":\"Email\",\"claims\":[], \"properties\": []}";
        HttpResponse httpResponse = sendPostRequest(client, selfRegisterLiteURL, data);
        Assert.assertEquals(httpResponse.getStatusLine().getStatusCode(), HttpStatus.SC_UNAUTHORIZED, "Error while testing request without authorization");
    }

    @Test(alwaysRun = true, groups = "wso2.is", description = "Lite user registration endpoint after enabling")
    public void liteUserRegistrationAfterEnabling() throws Exception {

        updateResidentIDPProperty(superTenantResidentIDP, ENABLE_LITE_SIGN_UP, "true", true);

        String updateLiteUserRegistrationClaimRequestBody = readResource("lite-register-user-claim.json");
        getResponseOfPut(isServerBackendUrl + UPDATE_CLAIM_URI + "/" + LITE_USER_CLAIM_ID, updateLiteUserRegistrationClaimRequestBody);

        String updateEmailAsUsernameClaimRequestBody = readResource("lite-register-user-claim-email-as-username.json");
        getResponseOfPut(isServerBackendUrl + UPDATE_CLAIM_URI + "/" + USERNAME_CLAIM_ID, updateEmailAsUsernameClaimRequestBody);

        String data = "{\"email\": \"testlitteuser@wso2.com\",\"realm\": \"PRIMARY\",\"preferredChannel\":\"Email\",\"claims\":[], \"properties\": []}";
        Response responseOfPost = getResponseOfPost( selfRegisterLiteURL, data);
        Assert.assertEquals(responseOfPost.statusCode(), HttpStatus.SC_CREATED, "Lite user registration unsuccessful");
    }

    @Test(alwaysRun = true, groups = "wso2.is", description = "Lite user registration with existing username")
    public void liteUserRegistrationExistingUser(){

        String data = "{\"email\": \"testlitteuser@wso2.com\",\"realm\": \"PRIMARY\",\"preferredChannel\":\"Email\",\"claims\":[], \"properties\": []}";
        Response responseOfPost = getResponseOfPost( selfRegisterLiteURL, data);
        Assert.assertEquals(responseOfPost.statusCode(), HttpStatus.SC_CONFLICT, "Username already exist");
    }

    private HttpResponse sendPostRequest(HttpClient client, String locationURL, String body) throws IOException {

        HttpPost postRequest = new HttpPost(locationURL);
        postRequest.setHeader("User-Agent", OAuth2Constant.USER_AGENT);
        StringEntity stringEntity = new StringEntity(body);
        postRequest.setEntity(stringEntity);
        HttpResponse response = client.execute(postRequest);

        return response;
    }

    private void updateResidentIDPProperty(IdentityProvider residentIdp, String propertyKey, String value, boolean
            isSuperTenant)
            throws Exception {

        IdentityProviderProperty[] idpProperties = residentIdp.getIdpProperties();
        for (IdentityProviderProperty providerProperty : idpProperties) {
            if (propertyKey.equalsIgnoreCase(providerProperty.getName())) {
                providerProperty.setValue(value);
            }
        }
        updateResidentIDP(residentIdp, isSuperTenant);
    }

    private void updateResidentIDP(IdentityProvider residentIdentityProvider, boolean isSuperTenant) throws Exception {

        FederatedAuthenticatorConfig[] federatedAuthenticatorConfigs =
                residentIdentityProvider.getFederatedAuthenticatorConfigs();
        for (FederatedAuthenticatorConfig authenticatorConfig : federatedAuthenticatorConfigs) {
            if (!authenticatorConfig.getName().equalsIgnoreCase("samlsso")) {
                federatedAuthenticatorConfigs = (FederatedAuthenticatorConfig[])
                        ArrayUtils.removeElement(federatedAuthenticatorConfigs,
                                authenticatorConfig);
            }
        }
        residentIdentityProvider.setFederatedAuthenticatorConfigs(federatedAuthenticatorConfigs);
        if (isSuperTenant) {
            superTenantIDPMgtClient.updateResidentIdP(residentIdentityProvider);
        } else {
            tenantIDPMgtClient.updateResidentIdP(residentIdentityProvider);
        }
    }

    @Override
    protected Response getResponseOfPost(String endpointUri, String body) {

        return given().auth().preemptive().basic(authenticatingUserName, authenticatingCredential)
                .contentType(ContentType.JSON)
                .header(HttpHeaders.ACCEPT, ContentType.JSON)
                .body(body)
                .post(endpointUri);
    }

    @Override
    protected Response getResponseOfPut(String endpointUri, String body) {

        return given().auth().preemptive().basic(authenticatingUserName, authenticatingCredential)
                .contentType(ContentType.JSON)
                .header(HttpHeaders.ACCEPT, ContentType.JSON)
                .body(body)
                .put(endpointUri);
    }
}
