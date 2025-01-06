/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.identity.integration.test.consent;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.wink.client.Resource;
import org.apache.wink.client.RestClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.beans.User;
import org.wso2.carbon.identity.application.common.model.idp.xsd.FederatedAuthenticatorConfig;
import org.wso2.carbon.identity.application.common.model.idp.xsd.IdentityProvider;
import org.wso2.carbon.identity.application.common.model.idp.xsd.IdentityProviderProperty;
import org.wso2.carbon.integration.common.admin.client.AuthenticatorClient;
import org.wso2.carbon.user.mgt.stub.UserAdminUserAdminException;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;
import org.wso2.identity.integration.common.clients.Idp.IdentityProviderMgtServiceClient;
import org.wso2.identity.integration.common.clients.UserManagementClient;
import org.wso2.identity.integration.common.utils.ISIntegrationTest;
import org.wso2.identity.integration.test.utils.DataExtractUtil;
import org.wso2.identity.integration.test.utils.OAuth2Constant;

import java.io.IOException;
import java.rmi.RemoteException;
import javax.ws.rs.core.MediaType;
import javax.xml.xpath.XPathExpressionException;

import static org.wso2.identity.integration.test.util.Utils.getBasicAuthHeader;

/**
 * Self sign up tests - with consents.
 */
public class SelfSignUpConsentTest extends ISIntegrationTest {

    protected Log log = LogFactory.getLog(getClass());

    public static final String CONSNT_ENDPOINT_SUFFIX = "/api/identity/consent-mgt/v1.0/consents";
    public static final String USER_RECOVERY_ME_ENDPOINT = "/api/identity/user/v1.0/me";
    private static final String COUNTRY_WSO2_CLAIM = "http://wso2.org/claims/country";
    private static final String CALLBACK_QUERY_PARAM = "callback";
    private static final String CALLBACK_PATH = "/carbon/callback";
    private static final String USERNAME_QUERY_PARAM = "username";
    private static final String ADMIN = "admin";
    private static final String EBONY = "ebony";
    private static final String PASSWORD = "UsEr@123";
    private static final String ENABLE_SELF_REGISTRATION_PROP_KEY = "SelfRegistration.Enable";
    private static final String DISABLE_ACC_LOCK_ON_SELF_REG_PROP_KEY = "SelfRegistration.LockOnCreation";
    private static final String FINANCIAL_PURPOSE_NAME = "Financial Purpose";
    private static final String FINANCIAL = "Financial";
    private static final String ERROR_MESSAGE_SELF_REGISTRATION_DISABLED = "Self registration is disabled for tenant" +
            " - %s";
    private static final String ERROR_MESSAGE_INVALID_TENANT = "%s is an invalid tenant domain";
    private static final String ERROR_MESSAGE_USERNAME_TAKEN = "Username &#39;%s&#39; is already taken. Please pick a " +
            "different username";

    private String tenantAdminUserName;
    private String isServerBackendUrl;
    private String selfRegisterDoEndpoint;
    private String signupDoEndpoint;
    private String consentEndpoint;
    private String selfRegistrationMeEndpoint;
    private IdentityProvider superTenantResidentIDP;
    private IdentityProvider tenantResidentIDP;
    private IdentityProviderMgtServiceClient superTenantIDPMgtClient;
    private IdentityProviderMgtServiceClient tenantIDPMgtClient;
    private AuthenticatorClient logManager;
    private UserManagementClient tenantUserMgtClient;
    private String secondaryTenantDomain;
    private String financialPurposeId;

    @BeforeClass(alwaysRun = true)
    public void testInit() throws Exception {

        super.init();
        secondaryTenantDomain = isServer.getTenantList().get(1);
        tenantAdminUserName = ADMIN + "@" + secondaryTenantDomain;
        this.logManager = new AuthenticatorClient(backendURL);
        String tenantCookie = this.logManager.login(ADMIN + "@" + secondaryTenantDomain,
                ADMIN, isServer.getInstance().getHosts().get("default"));

        superTenantIDPMgtClient = new IdentityProviderMgtServiceClient(sessionCookie, backendURL);
        tenantIDPMgtClient = new IdentityProviderMgtServiceClient(tenantCookie, backendURL);
        tenantUserMgtClient = new UserManagementClient(backendURL, tenantCookie);
        isServerBackendUrl = isServer.getContextUrls().getWebAppURLHttps();
        consentEndpoint = isServerBackendUrl + "/t/" + secondaryTenantDomain + CONSNT_ENDPOINT_SUFFIX;
        selfRegisterDoEndpoint = isServerBackendUrl + "/accountrecoveryendpoint/register.do";
        signupDoEndpoint = isServerBackendUrl + "/accountrecoveryendpoint/signup.do";
        selfRegistrationMeEndpoint = isServerBackendUrl + "/t/" + secondaryTenantDomain + USER_RECOVERY_ME_ENDPOINT;
        superTenantResidentIDP = superTenantIDPMgtClient.getResidentIdP();
        tenantResidentIDP = tenantIDPMgtClient.getResidentIdP();

    }

    @AfterClass(alwaysRun = true)
    public void atEnd() throws RemoteException, UserAdminUserAdminException {

        tenantUserMgtClient.deleteUser(EBONY);
    }

    @AfterMethod
    public void resetResidentIDP() throws Exception {

        updateResidentIDPProperty(superTenantResidentIDP, ENABLE_SELF_REGISTRATION_PROP_KEY, "false", true);
        updateResidentIDPProperty(tenantResidentIDP, ENABLE_SELF_REGISTRATION_PROP_KEY, "false", false);
        updateResidentIDPProperty(tenantResidentIDP, DISABLE_ACC_LOCK_ON_SELF_REG_PROP_KEY, "true", false);
    }

    @Test(alwaysRun = true, groups = "wso2.is", description = "Testing self sign-up page without purposes enter " +
            "username")

    public void testInitialSelfSignUpPage() throws IOException, XPathExpressionException {

        String CallbackEndpoint = getBaseURL() + CALLBACK_PATH;
        HttpClient client = HttpClientBuilder.create().build();
        String selfRegisterEndpoint = selfRegisterDoEndpoint + "?" + CALLBACK_QUERY_PARAM + "=" + CallbackEndpoint;
        selfRegisterEndpoint = getTenantQualifiedURL(selfRegisterEndpoint, secondaryTenantDomain);
        HttpResponse httpResponse = sendGetRequest(client, selfRegisterEndpoint);
        String content = DataExtractUtil.getContentData(httpResponse);
        Assert.assertNotNull(content);
        Assert.assertTrue(content.contains("Enter your username"), "Page for entering username is not prompted while" +
                " self registering");
        Assert.assertTrue(content.contains(getTenantQualifiedURL(CallbackEndpoint, secondaryTenantDomain)),
                "Callback endpoint is not available in self " +
                "registration username input page.");
    }

    @Test(alwaysRun = true, groups = "wso2.is", description = "Test without enabling self registration")
    public void testWithoutEnablingSelfRegistration() throws Exception {

        String content = doCallSignUpDo(ADMIN);
        Assert.assertNotNull(content);
        Assert.assertTrue(content.contains("Self registration is disabled for tenant - " +
                isServer.getSuperTenant().getDomain()));
    }

    @Test(alwaysRun = true, groups = "wso2.is", description = "Test for an invalid tenant")
    public void testForAnInvalidTenant() throws Exception {

        String content = doCallSignUpDo("john@googleinvalid.com");
        Assert.assertNotNull(content);
        Assert.assertTrue(content.contains(String.format(ERROR_MESSAGE_INVALID_TENANT, "googleinvalid.com")));
    }

    @Test(alwaysRun = true, groups = "wso2.is", description = "Test after enabling self registration but for an " +
            "already taken username")
    public void testAfterEnablingSelfRegistrationInvalidUser() throws Exception {

        updateResidentIDPProperty(superTenantResidentIDP, ENABLE_SELF_REGISTRATION_PROP_KEY, "true", true);

        String content = doCallSignUpDo(ADMIN);
        Assert.assertTrue(content.contains(String.format(ERROR_MESSAGE_USERNAME_TAKEN, ADMIN)));
    }

    @Test(alwaysRun = true, groups = "wso2.is", description = "Test after enabling self registration, but for a " +
            "different tenant user")
    public void testForCrossTenantWithoutEnabling() throws Exception {

        updateResidentIDPProperty(superTenantResidentIDP, ENABLE_SELF_REGISTRATION_PROP_KEY, "true", true);

        String content = doCallSignUpDo("john@" + secondaryTenantDomain);
        Assert.assertTrue(content.contains(String.format(ERROR_MESSAGE_SELF_REGISTRATION_DISABLED, secondaryTenantDomain)));
    }

    @Test(alwaysRun = true, groups = "wso2.is", description = "Test for a valid user")
    public void testForValidUsername() throws Exception {

        updateResidentIDPProperty(superTenantResidentIDP, ENABLE_SELF_REGISTRATION_PROP_KEY, "true", true);

        String content = doCallSignUpDo("smith");
        Assert.assertTrue(content.contains("Password"));
        Assert.assertTrue(content.contains("Confirm password"));

    }

    @Test(alwaysRun = true, groups = "wso2.is", description = "Test whether purposes are shown in self registration " +
            "page, without configuring purposes")
    public void testPurposesWithoutConfiguredPurposes() throws Exception {

        updateResidentIDPProperty(tenantResidentIDP, ENABLE_SELF_REGISTRATION_PROP_KEY, "true", false);

        String content = doCallSignUpDo("smith@" + secondaryTenantDomain);
        Assert.assertTrue(content.contains("Password"));
        Assert.assertTrue(content.contains("Confirm password"));
        Assert.assertTrue(!content.contains(FINANCIAL_PURPOSE_NAME));
    }

    @Test(alwaysRun = true, groups = "wso2.is", description = "Test whether purposes are shown on self registration " +
            "page", dependsOnMethods = "testPurposesWithoutConfiguredPurposes")
    public void testWithPurposes() throws Exception {

        updateResidentIDPProperty(tenantResidentIDP, ENABLE_SELF_REGISTRATION_PROP_KEY, "true", false);

        addCategoryCountry();
        addFinancialPurpose();
        addFinancialPurposeCategory();
        String content = doCallSignUpDo("smith@" + secondaryTenantDomain);
        Assert.assertTrue(content.contains("Password"));
        Assert.assertTrue(content.contains("Confirm password"));
        Assert.assertTrue(content.contains(FINANCIAL_PURPOSE_NAME));
    }

    @Test(alwaysRun = true, groups = "wso2.is", description = "Self sign up with a valid user with proper consents",
            dependsOnMethods = "testWithPurposes")
    public void selfSignUpWithConsents() throws Exception {

        updateResidentIDPProperty(tenantResidentIDP, ENABLE_SELF_REGISTRATION_PROP_KEY, "true", false);
        updateResidentIDPProperty(tenantResidentIDP, DISABLE_ACC_LOCK_ON_SELF_REG_PROP_KEY, "false", false);
        selfRegister(EBONY, PASSWORD, EBONY, EBONY + "@gmail.com", "Jackson", "+9433909388");
        String content = doCallSignUpDo(EBONY + "@" + secondaryTenantDomain);
        Assert.assertTrue(content.contains(String.format(ERROR_MESSAGE_USERNAME_TAKEN, EBONY)));

    }

    @Test(alwaysRun = true, groups = "wso2.is", description = "Test for a valid user",
            dependsOnMethods = "selfSignUpWithConsents")
    public void getPurposes() throws Exception {

        updateResidentIDPProperty(tenantResidentIDP, ENABLE_SELF_REGISTRATION_PROP_KEY, "true", false);
        updateResidentIDPProperty(tenantResidentIDP, DISABLE_ACC_LOCK_ON_SELF_REG_PROP_KEY, "false", false);
        String consents = getConsents(EBONY + "@" + secondaryTenantDomain, PASSWORD);
        log.info("Consents for user " + EBONY + " :" + consents);
        Assert.assertNotNull(consents);
        JSONArray purposesJson = new JSONArray(consents);
        String receiptID = ((JSONObject) purposesJson.get(0)).getString("consentReceiptID");
        String receipt = getConsent(EBONY + "@" + secondaryTenantDomain, PASSWORD, receiptID);
        JSONObject receiptJson = new JSONObject(receipt);
        Assert.assertEquals(receiptJson.getString("collectionMethod"), "Web Form - Self Registration");

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

    private String doCallSignUpDo(String username) throws IOException {

        HttpClient client = HttpClientBuilder.create().build();
        String tenantDomain = MultitenantConstants.SUPER_TENANT_DOMAIN_NAME;
        if (username.contains("@")) {
            tenantDomain = MultitenantUtils.getTenantDomain(username);
            username = MultitenantUtils.getTenantAwareUsername(username);
        }
        String selfRegisterEndpoint =
                signupDoEndpoint + "?" + USERNAME_QUERY_PARAM + "=" + username;
        selfRegisterEndpoint = getTenantQualifiedURL(selfRegisterEndpoint, tenantDomain);
        HttpResponse httpResponse = sendGetRequest(client, selfRegisterEndpoint);
        return DataExtractUtil.getContentData(httpResponse);
    }

    public void addCategoryCountry() {

        String name = COUNTRY_WSO2_CLAIM;
        String description = "Country";
        addPIICategory(name, description);

    }

    public void addFinancialPurposeCategory() throws JSONException {

        String name = FINANCIAL;
        String description = FINANCIAL_PURPOSE_NAME;
        addPurposeCategory(name, description);
    }

    private void addPIICategory(String name, String description) {

        RestClient restClient = new RestClient();
        Resource piiCatResource = restClient.resource(consentEndpoint + "/" + "pii-categories");

        String addPIICatString = "{\"piiCategory\": " + "\"" + name + "\"" + ", \"description\": " + "\"" +
                description + "\" , \"sensitive\": \"" + true + "\"}";

        User user = new User();
        user.setUserName(tenantAdminUserName);
        user.setPassword(ADMIN);

        piiCatResource.
                contentType(MediaType.APPLICATION_JSON_TYPE).
                accept(MediaType.APPLICATION_JSON).
                header(HttpHeaders.AUTHORIZATION, getBasicAuthHeader(user)).
                post(String.class, addPIICatString);
    }

    private String addPurposeCategory(String name, String description) throws JSONException {

        RestClient restClient = new RestClient();
        Resource piiCatResource = restClient.resource(consentEndpoint + "/" + "purpose-categories");

        String addPurposeCatString = "{\"purposeCategory\": " + "\"" + name + "\"" + ", \"description\": " + "\"" +
                description + "\"}";

        User user = new User();
        user.setUserName(tenantAdminUserName);
        user.setPassword(ADMIN);

        String content = piiCatResource.
                contentType(MediaType.APPLICATION_JSON_TYPE).
                accept(MediaType.APPLICATION_JSON).
                header(HttpHeaders.AUTHORIZATION, getBasicAuthHeader(user)).
                post(String.class, addPurposeCatString);
        JSONObject purpose = new JSONObject(content);
        return purpose.getString("purposeCategoryId");
    }

    public void addFinancialPurpose() throws JSONException {

        String name = FINANCIAL_PURPOSE_NAME;
        String description = FINANCIAL_PURPOSE_NAME;
        String group = "SELF-SIGNUP";
        String groupType = "SYSTEM";
        financialPurposeId  = addPurpose(name, description, group, groupType);

    }

    private String addPurpose(String name, String description, String group, String groupType)
            throws JSONException {

        RestClient restClient = new RestClient();
        Resource piiCatResource = restClient.resource(consentEndpoint + "/" + "purposes");

        String addPurposeString = "{" +
                                  "  \"purpose\": \"" + name + "\"," +
                                  "  \"description\": \"" + description + "\"," +
                                  "  \"group\": \"" + group + "\"," +
                                  "  \"groupType\": \"" + groupType + "\"," +
                                  "  \"piiCategories\": [" +
                                  "    {" +
                                  "      \"piiCategoryId\": 1," +
                                  "      \"mandatory\": true" +
                                  "    }" +
                                  "  ]" +
                                  "}";

        User user = new User();
        user.setUserName(tenantAdminUserName);
        user.setPassword(ADMIN);

        String response = piiCatResource.
                contentType(MediaType.APPLICATION_JSON_TYPE).
                accept(MediaType.APPLICATION_JSON).
                header(HttpHeaders.AUTHORIZATION, getBasicAuthHeader(user)).
                post(String.class, addPurposeString);
        JSONObject purpose = new JSONObject(response);
        return purpose.getString("purposeId");
    }

    private HttpResponse sendGetRequest(HttpClient client, String locationURL) throws IOException {

        HttpGet getRequest = new HttpGet(locationURL);
        getRequest.setHeader("User-Agent", OAuth2Constant.USER_AGENT);
        HttpResponse response = client.execute(getRequest);

        return response;
    }

    private void selfRegister(String username, String password, String givenName, String emailAddress, String
            lastName, String mobile) {

        String consent = getConsentReqBody(financialPurposeId, 1, username);

        String selfRegisterReqBody = "{\"user\": {\"username\": \"" + username + "\",\"realm\": \"\", " +
                "\"password\": \"" + password + "\",\"claims\": " +
                "[{\"uri\": \"http://wso2.org/claims/givenname\",\"value\": \"" + givenName + "\" }," +
                "{\"uri\": \"http://wso2.org/claims/emailaddress\",\"value\": \"" + emailAddress + "\"}," +
                "{\"uri\": \"http://wso2.org/claims/lastname\",\"value\": \"" + lastName + "\"}," +
                "{\"uri\": \"http://wso2.org/claims/mobile\",\"value\": \"" + mobile + "\"} ] }," +
                "\"properties\": [{\"key\": \"consent\", \"value\": \"" + consent + "\"}]}";

        RestClient restClient = new RestClient();
        Resource selfRegistrationResource = restClient.resource(selfRegistrationMeEndpoint);

        User user = new User();
        user.setUserName(tenantAdminUserName);
        user.setPassword(ADMIN);

        selfRegistrationResource.contentType(MediaType.APPLICATION_JSON_TYPE).
                accept(MediaType.APPLICATION_JSON).
                header(HttpHeaders.AUTHORIZATION, getBasicAuthHeader(user)).
                post(String.class, selfRegisterReqBody);
    }

    private String getConsentReqBody(String purposeId, int piiCategoryId, String username) {

        return  "{\\\"jurisdiction\\\":\\\"someJurisdiction\\\",\\\"collectionMethod\\\":\\\"Web Form - Self " +
                "Registration\\\"," +
                "\\\"language\\\":\\\"en\\\",\\\"piiPrincipalId\\\":\\\""+username+"\\\",\\\"services\\\":" +
                "[{\\\"tenantDomain\\\":\\\"wso2.com\\\",\\\"serviceDisplayName\\\":\\\"Resident IDP\\\"," +
                "\\\"serviceDescription\\\":\\\"Resident IDP\\\",\\\"purposes\\\":[{\\\"purposeId\\\":"+purposeId+"," +
                "\\\"purposeCategoryId\\\":[1]," +
                "\\\"consentType\\\":\\\"EXPLICIT\\\",\\\"piiCategory\\\":[{\\\"piiCategoryId\\\":"+piiCategoryId+"," +
                "\\\"validity\\\":\\\"DATE_UNTIL:INDEFINITE\\\"}],\\\"primaryPurpose\\\":true," +
                "\\\"termination\\\":\\\"DATE_UNTIL:INDEFINITE\\\",\\\"thirdPartyDisclosure\\\":false}],\\\"tenantId\\\":1}]," +
                "\\\"policyURL\\\":\\\"somePolicyUrl\\\",\\\"tenantId\\\":1,\\\"properties\\\":{}}";
    }

    private String getConsents(String username, String password) {

        RestClient restClient = new RestClient();
        Resource resource = restClient.resource(consentEndpoint + "?piiPrincipalId=" + MultitenantUtils
                .getTenantAwareUsername(username));

        User user = new User();
        user.setUserName(username);
        user.setPassword(password);

        String response = resource.
                contentType(MediaType.APPLICATION_JSON_TYPE).
                accept(MediaType.APPLICATION_JSON).
                header(HttpHeaders.AUTHORIZATION, getBasicAuthHeader(user)).
                get(String.class);
        return response;
    }

    private String getConsent(String username, String password, String receiptId) {

        log.info("Retrieving consent for username " + username + ". reciptId : " + receiptId);

        RestClient restClient = new RestClient();
        Resource resource = restClient.resource(consentEndpoint + "/receipts/" + receiptId);
        log.info("Calling to receipt endpoint :" + consentEndpoint + "/receipts/" + receiptId);

        User user = new User();
        user.setUserName(username);
        user.setPassword(password);

        String response = resource.
                contentType(MediaType.APPLICATION_JSON_TYPE).
                accept(MediaType.APPLICATION_JSON).
                header(HttpHeaders.AUTHORIZATION, getBasicAuthHeader(user)).
                get(String.class);
        return response;
    }
}
