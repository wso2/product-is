/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.identity.scenarios.um.test.self.registration.api;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.application.common.model.idp.xsd.FederatedAuthenticatorConfig;
import org.wso2.carbon.identity.application.common.model.idp.xsd.IdentityProvider;
import org.wso2.carbon.identity.application.common.model.idp.xsd.IdentityProviderProperty;
import org.wso2.carbon.um.ws.api.stub.ClaimDTO;
import org.wso2.carbon.um.ws.api.stub.ClaimValue;
import org.wso2.identity.scenarios.commons.HTTPCommonClient;
import org.wso2.identity.scenarios.commons.ScenarioTestBase;
import org.wso2.identity.scenarios.commons.clients.Idp.IdentityProviderMgtServiceClient;
import org.wso2.identity.scenarios.commons.clients.usermgt.remote.RemoteUserStoreManagerServiceClient;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;
import static org.wso2.identity.scenarios.commons.util.Constants.IS_HTTPS_URL;
import static org.wso2.identity.scenarios.commons.util.SCIMProvisioningUtil.getCommonHeaders;
import static org.wso2.identity.scenarios.um.test.self.registration.api.util.Constants.ClaimURIs.ACCOUNT_LOCK_CLAIM;
import static org.wso2.identity.scenarios.um.test.self.registration.api.util.Constants.EndPoints.ME;
import static org.wso2.identity.scenarios.um.test.self.registration.api.util.Constants.EndPoints.RESEND_CODE;
import static org.wso2.identity.scenarios.um.test.self.registration.api.util.Constants.EndPoints.VALIDATE_CODE;
import static org.wso2.identity.scenarios.um.test.self.registration.api.util.Constants.IdPConfigParameters.SELF_REGISTRATION_CODE_EXPIRY_TIME;
import static org.wso2.identity.scenarios.um.test.self.registration.api.util.Constants.IdPConfigParameters.SELF_REGISTRATION_ENABLE;
import static org.wso2.identity.scenarios.um.test.self.registration.api.util.Constants.IdPConfigParameters.SELF_REGISTRATION_LOCK_ON_CREATION;
import static org.wso2.identity.scenarios.um.test.self.registration.api.util.Constants.IdPConfigParameters.SELF_REGISTRATION_NOTIFICATION_IM;
import static org.wso2.identity.scenarios.um.test.self.registration.api.util.Constants.IdPConfigParameters.SELF_REGISTRATION_RE_CAPTCHA;
import static org.wso2.identity.scenarios.um.test.self.registration.api.util.Constants.SelfRegistrationRequestElements.CLAIMS;
import static org.wso2.identity.scenarios.um.test.self.registration.api.util.Constants.SelfRegistrationRequestElements.CODE;
import static org.wso2.identity.scenarios.um.test.self.registration.api.util.Constants.SelfRegistrationRequestElements.REALM;
import static org.wso2.identity.scenarios.um.test.self.registration.api.util.Constants.SelfRegistrationRequestElements.URI;
import static org.wso2.identity.scenarios.um.test.self.registration.api.util.Constants.SelfRegistrationRequestElements.USER;
import static org.wso2.identity.scenarios.um.test.self.registration.api.util.Constants.SelfRegistrationRequestElements.USERNAME;
import static org.wso2.identity.scenarios.um.test.self.registration.api.util.Constants.SelfRegistrationRequestElements.VALUE;

public class SelfRegistrationTestCase extends ScenarioTestBase {

    private static final Log log = LogFactory.getLog(SelfRegistrationTestCase.class);

    private static final String REGISTER_REQUESTS_LOCATION = "registration.requests.location";

    private static JSONParser parser = new JSONParser();

    private IdentityProviderMgtServiceClient identityProviderMgtServiceClient;

    private RemoteUserStoreManagerServiceClient userStoreManagerServiceClient;

    private HTTPCommonClient httpCommonClient;

    private JSONObject registerRequestJSON;

    private String username;

    private String password;

    private String tenantDomain;

    private String confirmationCode;

    private String newConfirmationCode;

    private boolean resendCode;

    @Factory(dataProvider = "selfRegistrationConfigProvider")
    public SelfRegistrationTestCase(JSONObject registerRequestJSON, String username, String password,
            String tenantDomain, boolean resendCode) {

        this.registerRequestJSON = registerRequestJSON;
        this.username = username;
        this.password = password;
        this.tenantDomain = tenantDomain;
        this.resendCode = resendCode;
    }

    @DataProvider(name = "selfRegistrationConfigProvider")
    private static Object[][] selfRegistrationConfigProvider() throws Exception {

        return new Object[][] {
                {
                        getRegisterRequestJSON("request1.json"), ADMIN_USERNAME, ADMIN_PASSWORD, SUPER_TENANT_DOMAIN,
                        false
                }, {
                        getRegisterRequestJSON("request2.json"), ADMIN_USERNAME, ADMIN_PASSWORD, SUPER_TENANT_DOMAIN,
                        true
                }
        };
    }

    /**
     * Get register request JSON object.
     *
     * @param fileName File name.
     * @return Register request JSON object.
     * @throws Exception Exception.
     */
    private static JSONObject getRegisterRequestJSON(String fileName) throws Exception {

        Path path = Paths.get(System.getProperty(SelfRegistrationTestCase.REGISTER_REQUESTS_LOCATION) + fileName);
        if (!Files.exists(path)) {
            throw new FileNotFoundException("Failed to find file: " + path.toString());
        }
        return (JSONObject) parser.parse(new FileReader(path.toString()));
    }

    @BeforeClass(alwaysRun = true)
    public void init() throws Exception {

        super.init();
        super.loginAndObtainSessionCookie(username, password);
        identityProviderMgtServiceClient = new IdentityProviderMgtServiceClient(super.sessionCookie,
                super.backendServiceURL, super.configContext);
        userStoreManagerServiceClient = new RemoteUserStoreManagerServiceClient(super.backendServiceURL,
                super.sessionCookie);
        httpCommonClient = new HTTPCommonClient();
    }

    @AfterClass(alwaysRun = true)
    public void clear() throws Exception {

        userStoreManagerServiceClient.deleteUser(((JSONObject) registerRequestJSON.get(USER)).get(USERNAME).toString());

        httpCommonClient.closeHttpClient();
    }

    @Test(description = "2.1.1")
    public void updateResidentIdP() throws Exception {

        IdentityProvider identityProvider = identityProviderMgtServiceClient.getResidentIdP();

        for (IdentityProviderProperty property : identityProvider.getIdpProperties()) {

            if (SELF_REGISTRATION_ENABLE.equals(property.getName())) {
                property.setValue("true");
            } else if (SELF_REGISTRATION_LOCK_ON_CREATION.equals(property.getName())) {
                property.setValue("true");
            } else if (SELF_REGISTRATION_NOTIFICATION_IM.equals(property.getName())) {
                property.setValue("false");
            } else if (SELF_REGISTRATION_RE_CAPTCHA.equals(property.getName())) {
                property.setValue("false");
            } else if (SELF_REGISTRATION_CODE_EXPIRY_TIME.equals(property.getName())) {
                property.setValue("1440");
            }
        }
        // This is to remove invalid authenticators
        updateFederatedAuthenticators(identityProvider);

        identityProviderMgtServiceClient.updateResidentIdP(identityProvider);

        IdentityProvider updatedIdentityProvider = identityProviderMgtServiceClient.getResidentIdP();

        for (IdentityProviderProperty property : updatedIdentityProvider.getIdpProperties()) {

            if (SELF_REGISTRATION_ENABLE.equals(property.getName())) {
                assertEquals(property.getValue(), "true");
            } else if (SELF_REGISTRATION_LOCK_ON_CREATION.equals(property.getName())) {
                assertEquals(property.getValue(), "true");
            } else if (SELF_REGISTRATION_NOTIFICATION_IM.equals(property.getName())) {
                assertEquals(property.getValue(), "false");
            } else if (SELF_REGISTRATION_RE_CAPTCHA.equals(property.getName())) {
                assertEquals(property.getValue(), "false");
            } else if (SELF_REGISTRATION_CODE_EXPIRY_TIME.equals(property.getName())) {
                assertEquals(property.getValue(), "1440");
            }
        }
        // To sync the local caches.
        Thread.sleep(5000);
    }

    @Test(description = "2.1.2",
          dependsOnMethods = { "updateResidentIdP" })
    public void selfRegisterUser() throws Exception {

        HttpResponse response = httpCommonClient
                .sendPostRequestWithJSON(getEndPoint(ME), registerRequestJSON, getCommonHeaders(username, password));

        confirmationCode = httpCommonClient.getStringFromResponse(response);
        assertEquals(response.getStatusLine().getStatusCode(), HttpStatus.SC_CREATED,
                "Self registration failed. Request Object: " + registerRequestJSON.toJSONString() + " Response body: "
                        + confirmationCode);

        assertNotNull(confirmationCode, "Failed to receive the confirmation code.");
    }

    @Test(description = "2.1.3",
          dependsOnMethods = { "selfRegisterUser" })
    public void validateUserRegistration() throws Exception {

        ClaimDTO[] userClaimValues = userStoreManagerServiceClient
                .getUserClaimValues(((JSONObject) registerRequestJSON.get(USER)).get(USERNAME).toString(), "default");

        assertNotNull(userClaimValues,
                "Failed to retrieve the user claim values for the user. Request Object: " + registerRequestJSON
                        .toJSONString());

        validateUserClaims(userClaimValues);

        assertEquals(getClaimValue(userClaimValues, ACCOUNT_LOCK_CLAIM), "true",
                "Failed to lock the user during user" + " creation.");
    }

    @Test(description = "2.1.4",
          dependsOnMethods = { "validateUserRegistration" })
    public void resendCodeForUser() throws Exception {

        if (!resendCode) {
            return;
        }

        JSONObject resendCodeRequestJSON = new JSONObject();
        JSONObject userJSON = new JSONObject();
        userJSON.put(USERNAME, ((JSONObject) registerRequestJSON.get(USER)).get(USERNAME).toString());
        userJSON.put(REALM, ((JSONObject) registerRequestJSON.get(USER)).get(REALM).toString());
        resendCodeRequestJSON.put(USER, userJSON);

        HttpResponse response = httpCommonClient
                .sendPostRequestWithJSON(getEndPoint(RESEND_CODE), resendCodeRequestJSON,
                        getCommonHeaders(username, password));

        assertEquals(response.getStatusLine().getStatusCode(), HttpStatus.SC_CREATED,
                "Resend confirmation code failed. Confirmation code: " + confirmationCode + " Request Object: "
                        + registerRequestJSON.toJSONString());

        newConfirmationCode = httpCommonClient.getStringFromResponse(response);
        assertNotNull(newConfirmationCode, "Failed to receive the new confirmation code.");
    }

    @Test(description = "2.1.5",
          dependsOnMethods = { "resendCodeForUser" })
    public void verifyPreviousConfirmationCode() throws Exception {

        if (!resendCode) {
            return;
        }

        JSONObject confirmRequestJSON = new JSONObject();
        confirmRequestJSON.put(CODE, confirmationCode);
        HttpResponse response = httpCommonClient.sendPostRequestWithJSON(getEndPoint(VALIDATE_CODE), confirmRequestJSON,
                getCommonHeaders(username, password));

        assertNotEquals(response.getStatusLine().getStatusCode(), HttpStatus.SC_ACCEPTED,
                "Previous confirmation code: " + confirmationCode + " is still valid. New confirmation code: "
                        + newConfirmationCode + ", Request Object: " + registerRequestJSON.toJSONString());
    }

    @Test(description = "2.1.6",
          dependsOnMethods = { "verifyPreviousConfirmationCode" })
    public void confirmUserRegistration() throws Exception {

        JSONObject confirmRequestJSON = new JSONObject();
        if (resendCode) {
            confirmRequestJSON.put(CODE, newConfirmationCode);
        } else {
            confirmRequestJSON.put(CODE, confirmationCode);
        }
        HttpResponse response = httpCommonClient.sendPostRequestWithJSON(getEndPoint(VALIDATE_CODE), confirmRequestJSON,
                getCommonHeaders(username, password));

        assertEquals(response.getStatusLine().getStatusCode(), HttpStatus.SC_ACCEPTED,
                "User confirmation failed. Confirmation code: " + confirmationCode + " Request Object: "
                        + registerRequestJSON.toJSONString());

        ClaimValue[] claimValues = userStoreManagerServiceClient
                .getUserClaimValuesForClaims(((JSONObject) registerRequestJSON.get(USER)).get(USERNAME).toString(),
                        new String[] { ACCOUNT_LOCK_CLAIM }, "default");
        log.info("Claim values received: " + Arrays.toString(claimValues));
        assertNotNull(claimValues, "Failed to get the value for Claim URI: " + ACCOUNT_LOCK_CLAIM);

        assertEquals(claimValues[0].getValue(), "false",
                "Failed to unlock the user account upon confirmation. Confirmation code: " + confirmationCode
                        + " Request Object: " + registerRequestJSON.toJSONString());
    }

    private String getEndPoint(String path) {

        if (tenantDomain == null || SUPER_TENANT_DOMAIN.equals(tenantDomain)) {
            return getDeploymentProperty(IS_HTTPS_URL) + "/api/identity/user/v1.0/" + path;
        }
        return getDeploymentProperty(IS_HTTPS_URL) + "/t/" + tenantDomain + "/api/identity/user/v1.0/" + path;
    }

    private void validateUserClaims(ClaimDTO[] claimDTOs) {

        if (((JSONObject) registerRequestJSON.get(USER)).get(CLAIMS) == null) {
            return;
        }

        JSONArray claims = (JSONArray) ((JSONObject) registerRequestJSON.get(USER)).get(CLAIMS);
        for (Object claim : claims) {
            JSONObject claimJSON = (JSONObject) claim;
            String claimUri = claimJSON.get(URI).toString();
            boolean found = false;
            for (ClaimDTO claimDTO : claimDTOs) {
                if (claimUri.equals(claimDTO.getClaimUri())) {
                    assertEquals(claimDTO.getValue(), claimJSON.get(VALUE).toString(),
                            "Invalid claim value for the Claim URI: " + claimUri);
                    found = true;
                    break;
                }
            }
            assertTrue(found, "ClaimDTO not found for the Claim URI: " + claimUri);
        }
    }

    private String getClaimValue(ClaimDTO[] claimDTOs, String claimUri) {

        for (ClaimDTO claimDTO : claimDTOs) {
            if (claimUri.equals(claimDTO.getClaimUri())) {
                return claimDTO.getValue();
            }
        }
        return null;
    }

    private void updateFederatedAuthenticators(IdentityProvider identityProvider) {

        List<FederatedAuthenticatorConfig> updatedConfigs = new ArrayList<>();
        for (FederatedAuthenticatorConfig config : identityProvider.getFederatedAuthenticatorConfigs()) {
            if ("samlsso".equals(config.getName())) {
                updatedConfigs.add(config);
            } else if ("openidconnect".equals(config.getName())) {
                updatedConfigs.add(config);
            } else if ("passivests".equals(config.getName())) {
                updatedConfigs.add(config);
            }
        }
        identityProvider.setFederatedAuthenticatorConfigs(updatedConfigs.toArray(new FederatedAuthenticatorConfig[0]));
    }
}
