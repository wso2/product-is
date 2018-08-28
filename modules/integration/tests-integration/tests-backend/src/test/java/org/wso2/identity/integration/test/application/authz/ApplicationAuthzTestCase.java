/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */

package org.wso2.identity.integration.test.application.authz;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpResponse;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.identity.entitlement.stub.EntitlementPolicyAdminServiceEntitlementException;
import org.wso2.carbon.identity.entitlement.stub.dto.PolicyDTO;
import org.wso2.identity.integration.common.clients.application.mgt.ApplicationManagementServiceClient;
import org.wso2.identity.integration.common.clients.entitlement.EntitlementPolicyServiceClient;
import org.wso2.identity.integration.common.clients.sso.saml.SAMLSSOConfigServiceClient;
import org.wso2.identity.integration.common.clients.usermgt.remote.RemoteUserStoreManagerServiceClient;
import org.wso2.identity.integration.common.utils.ISIntegrationTest;
import org.wso2.identity.integration.test.util.Utils;
import org.wso2.identity.integration.test.utils.CommonConstants;

import java.io.File;
import java.net.URL;
import java.rmi.RemoteException;

public class ApplicationAuthzTestCase extends AbstractApplicationAuthzTestCase {

    private static final String AZ_TEST_ROLE = "azTestRole";
    private static final String HTTP_REDIRECT = "HTTP-Redirect";
    private static final String AZ_TEST_USER = "azTestUser";
    private static final String AZ_TEST_USER_PW = "azTest123";
    private static final String NON_AZ_TEST_USER = "nonAzTestUser";
    private static final String NON_AZ_TEST_USER_PW = "nonAzTest123";
    private static final Log log = LogFactory.getLog(ApplicationAuthzTestCase.class);
    private static final String APPLICATION_NAME = "travelocity.com";
    private static final String POLICY_ID = "spAuthPolicy";
    private static final String POLICY =
            "<Policy xmlns=\"urn:oasis:names:tc:xacml:3.0:core:schema:wd-17\" PolicyId=\"spAuthPolicy\" RuleCombiningAlgId=\"urn:oasis:names:tc:xacml:1.0:rule-combining-algorithm:first-applicable\" Version=\"1.0\">\n" +
                    "    <Target>\n" +
                    "        <AnyOf>\n" +
                    "            <AllOf>\n" +
                    "                <Match MatchId=\"urn:oasis:names:tc:xacml:1.0:function:string-equal\">\n" +
                    "                    <AttributeValue DataType=\"http://www.w3.org/2001/XMLSchema#string\">" +
                    APPLICATION_NAME + "</AttributeValue>\n" +
                    "                    <AttributeDesignator AttributeId=\"http://wso2" +
                    ".org/identity/sp/sp-name\" Category=\"http://wso2.org/identity/sp\" " +
                    "DataType=\"http://www.w3.org/2001/XMLSchema#string\" MustBePresent=\"true\"/>\n" +
                    "                </Match>\n" +
                    "            </AllOf>\n" +
                    "        </AnyOf>\n" +
                    "    </Target>\n" +
                    "    <Rule Effect=\"Permit\" RuleId=\"permitRole\">\n" +
                    "        <Condition>\n" +
                    "            <Apply FunctionId=\"urn:oasis:names:tc:xacml:1.0:function:string-is-in\">\n" +
                    "                <AttributeValue DataType=\"http://www.w3.org/2001/XMLSchema#string\">" +
                    AZ_TEST_ROLE + "</AttributeValue>\n" +
                    "                <AttributeDesignator AttributeId=\"http://wso2.org/claims/role\" Category=\"urn:oasis:names:tc:xacml:1.0:subject-category:access-subject\" DataType=\"http://www.w3.org/2001/XMLSchema#string\" MustBePresent=\"true\"/>\n" +
                    "            </Apply>\n" +
                    "        </Condition>\n" +
                    "    </Rule>\n" +
                    "    <Rule Effect=\"Deny\" RuleId=\"denyall\"/>\n" +
                    "</Policy>";

    @BeforeClass(alwaysRun = true)
    public void testInit() throws Exception {

        super.init(TestUserMode.SUPER_TENANT_ADMIN);

        ConfigurationContext configContext = ConfigurationContextFactory
                .createConfigurationContextFromFileSystem(null, null);
        applicationManagementServiceClient =
                new ApplicationManagementServiceClient(sessionCookie, backendURL, configContext);
        ssoConfigServiceClient =
                new SAMLSSOConfigServiceClient(backendURL, sessionCookie);
        remoteUSMServiceClient = new RemoteUserStoreManagerServiceClient(backendURL, sessionCookie);
        entitlementPolicyClient = new EntitlementPolicyServiceClient(backendURL, sessionCookie);

        httpClientAzUser = new DefaultHttpClient();
        httpClientNonAzUser = new DefaultHttpClient();

        createRole(AZ_TEST_ROLE);
        createUser(AZ_TEST_USER, AZ_TEST_USER_PW, new String[]{AZ_TEST_ROLE});
        createUser(NON_AZ_TEST_USER, NON_AZ_TEST_USER_PW, new String[0]);
        createApplication(APPLICATION_NAME);
        createSAMLApp(APPLICATION_NAME, true, true, true);
        setupXACMLPolicy(POLICY_ID, POLICY);

        //Starting tomcat
        log.info("Starting Tomcat");
        tomcatServer = Utils.getTomcat(getClass());

        URL resourceUrl = getClass()
                .getResource(ISIntegrationTest.URL_SEPARATOR + "samples" + ISIntegrationTest.URL_SEPARATOR + "travelocity.com.war");
        Utils.startTomcat(tomcatServer, "/" + APPLICATION_NAME, resourceUrl.getPath());

    }

    protected void setupXACMLPolicy(String policyId, String xacmlPolicy)
            throws InterruptedException, RemoteException, EntitlementPolicyAdminServiceEntitlementException {

        PolicyDTO policy = new PolicyDTO();
        policy.setPolicy(xacmlPolicy);
        policy.setPolicy(policy.getPolicy().replaceAll(">\\s+<", "><").trim());
        policy.setVersion("3.0");
        policy.setPolicyId(policyId);
        entitlementPolicyClient.addPolicy(policy);
        Thread.sleep(5000); // waiting for the policy to deploy
        entitlementPolicyClient
                .publishPolicies(new String[]{policyId}, new String[]{"PDP Subscriber"}, "CREATE", true, null, 1);

    }

    @AfterClass(alwaysRun = true)
    public void testClear() throws Exception {

        deleteUser(AZ_TEST_USER);
        deleteUser(NON_AZ_TEST_USER);
        deleteRole(AZ_TEST_ROLE);
        deleteApplication(APPLICATION_NAME);
        entitlementPolicyClient.removePolicy(POLICY_ID);

        ssoConfigServiceClient = null;
        applicationManagementServiceClient = null;
        remoteUSMServiceClient = null;
        httpClientAzUser = null;
        //Stopping tomcat
        tomcatServer.stop();
        tomcatServer.destroy();
        Thread.sleep(10000);
    }


    @Test(alwaysRun = true, description = "Testing authorized user login", groups = "wso2.is")
    public void testAuthorizedSAMLSSOLogin() throws Exception {

        HttpResponse response;
        response =
                Utils.sendGetRequest(String.format(SAML_SSO_LOGIN_URL, APPLICATION_NAME, HTTP_REDIRECT), USER_AGENT,
                        httpClientAzUser);

        String sessionKey = Utils.extractDataFromResponse(response, CommonConstants.SESSION_DATA_KEY, 1);
        response = Utils.sendPOSTMessage(sessionKey, COMMON_AUTH_URL, USER_AGENT, ACS_URL, APPLICATION_NAME,
                AZ_TEST_USER, AZ_TEST_USER_PW, httpClientAzUser);

        String locationHeader = Utils.getRedirectUrl(response);
        if (Utils.requestMissingClaims(response)) {
            String pastrCookie = Utils.getPastreCookie(response);
            Assert.assertNotNull(pastrCookie, "pastr cookie not found in response.");
            EntityUtils.consume(response.getEntity());

            response = Utils.sendPOSTConsentMessage(response, COMMON_AUTH_URL, USER_AGENT, locationHeader,
                    httpClientAzUser, pastrCookie);
        }
        EntityUtils.consume(response.getEntity());

        if (Utils.requestMissingClaims(response)) {
            String pastrCookie = Utils.getPastreCookie(response);
            Assert.assertNotNull(pastrCookie, "pastr cookie not found in response.");
            EntityUtils.consume(response.getEntity());

            response = Utils.sendPOSTConsentMessage(response, COMMON_AUTH_URL, USER_AGENT,
                                                    String.format(ACS_URL, APPLICATION_NAME),
                                                    httpClientAzUser, pastrCookie);
            EntityUtils.consume(response.getEntity());
        }

        response = Utils.sendRedirectRequest(response, USER_AGENT, ACS_URL, APPLICATION_NAME, httpClientAzUser);
        String samlResponse = Utils.extractDataFromResponse(response, CommonConstants.SAML_RESPONSE_PARAM, 5);

        response = sendSAMLMessage(String.format(ACS_URL, APPLICATION_NAME), CommonConstants
                .SAML_RESPONSE_PARAM, samlResponse);
        String resultPage = extractDataFromResponse(response);

        Assert.assertTrue(resultPage.contains("You are logged in as " + AZ_TEST_USER),
                "SAML SSO Login should be successful and page should have a message \"You are logged in as\" " + AZ_TEST_USER);

    }

    @Test(alwaysRun = true, description = "Testing unauthorized user login", groups = "wso2.is")
    public void testUnauthorizedSAMLSSOLogin() throws Exception {

        HttpResponse response = Utils.sendGetRequest(String.format(SAML_SSO_LOGIN_URL, APPLICATION_NAME,
                HTTP_REDIRECT), USER_AGENT, httpClientNonAzUser);
        String sessionKey = Utils.extractDataFromResponse(response, CommonConstants.SESSION_DATA_KEY, 1);
        response = Utils.sendPOSTMessage(sessionKey, COMMON_AUTH_URL, USER_AGENT, ACS_URL, APPLICATION_NAME,
                NON_AZ_TEST_USER, NON_AZ_TEST_USER_PW, httpClientNonAzUser);

        String locationHeader = Utils.getRedirectUrl(response);
        if (Utils.requestMissingClaims(response)) {
            String pastrCookie = Utils.getPastreCookie(response);
            Assert.assertNotNull(pastrCookie, "pastr cookie not found in response.");
            EntityUtils.consume(response.getEntity());

            response = Utils.sendPOSTConsentMessage(response, COMMON_AUTH_URL, USER_AGENT, locationHeader,
                    httpClientNonAzUser, pastrCookie);
        }
        String redirectUrl = Utils.getRedirectUrl(response);
        EntityUtils.consume(response.getEntity());
        response = Utils.sendGetRequest(redirectUrl, USER_AGENT, httpClientNonAzUser);
        String responseString = extractDataFromResponse(response);
        Assert.assertTrue(responseString.contains("Authorization Failed"),
                "SAML SSO Login should be unsuccessful and page should have a message \"Authorization failed for\" " + NON_AZ_TEST_USER);

    }
}
